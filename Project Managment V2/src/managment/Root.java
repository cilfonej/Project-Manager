package managment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import components.tree.TreeNode;
import components.tree.TreeNodeMetadata;
import managment.storage.IOUtil;
import managment.storage.JsonSerializationContext;
import managment.storage.serialization.json.IJsonManualSerializable;
import managment.storage.serialization.json.JsonStringGenerator;

public class Root extends TreeNode<Object> implements IJsonManualSerializable {
	private transient Consumer<Assignment> updater;
	
	private transient Consumer<AssignmentGrouping> addGrouping, removeGrouping;
	private transient Consumer<Assignment> addAssignment, removeAssignment;
	private transient Consumer<Task> addTask, removeTask;
	
	private ArrayList<AssignmentGrouping> groupings;
	private JsonSerializationContext context;
	
	public Root(JsonSerializationContext context) {
		super(null);
		this.context = context;
		
		super.setMetadata(new TreeNodeMetadata("Root", null, true));
		super.setExpanded(true);

		groupings = new ArrayList<>();
	}
	
	public Root setMethods(Consumer<Assignment> updater, 
			Consumer<AssignmentGrouping> addGrouping, Consumer<AssignmentGrouping> removeGrouping,
			Consumer<Assignment> addAssignment, Consumer<Assignment> removeAssignment,
			Consumer<Task> addTask,  Consumer<Task> removeTask
	) {
		this.updater = updater;
		
		this.addGrouping = addGrouping;			this.addAssignment = addAssignment;			this.addTask = addTask;
		this.removeGrouping = removeGrouping;	this.removeAssignment = removeAssignment;	this.removeTask = removeTask;
		
		return this;
	}
	
	public AssignmentGrouping createAssignmentGroup(String name) {
		AssignmentGrouping grouping = new AssignmentGrouping(name, this, context);
		display_addGrouping(grouping);
		groupings.add(grouping);
		IOUtil.update(this);
		return grouping;
	}
	
	public void removeAssignmentGroup(AssignmentGrouping grouping) {
		display_removeGrouping(grouping); // Display
		groupings.remove(grouping);
		IOUtil.update(this);
	}
	
	public void addAll() { 
		for(AssignmentGrouping grouping : groupings) {
			display_addGrouping(grouping);
			
			for(Assignment assignment : grouping.getAssignments()) {
				display_addAssignment(assignment);
				
				for(Task task : assignment.getTasks()) {
					display_addTask(task);
				}
			}
		}
	}
	
	protected void display_update(Assignment assignment) { SwingUtilities.invokeLater(() -> { if(updater != null) updater.accept(assignment); }); }
	
	protected void display_addGrouping(AssignmentGrouping grouping)    { SwingUtilities.invokeLater(() -> { if(addGrouping != null) addGrouping.accept(grouping); }); }
	protected void display_removeGrouping(AssignmentGrouping grouping) { SwingUtilities.invokeLater(() -> { if(removeGrouping != null) removeGrouping.accept(grouping); }); }
	
	protected void display_addAssignment(Assignment assignment)    { SwingUtilities.invokeLater(() -> { if(addAssignment != null) addAssignment.accept(assignment); }); }
	protected void display_removeAssignment(Assignment assignment) { SwingUtilities.invokeLater(() -> { if(removeAssignment != null) removeAssignment.accept(assignment); }); }

	protected void display_addTask(Task task) 	 { SwingUtilities.invokeLater(() -> { if(addTask != null) addTask.accept(task); }); }
	protected void display_removeTask(Task task) { SwingUtilities.invokeLater(() -> { if(removeTask != null) removeTask.accept(task); }); }
	
	public ArrayList<AssignmentGrouping> getGroupings() { return groupings; }
	
	public String serialize() {
		JsonStringGenerator gen = JsonStringGenerator.getInstance();
		
		gen.writeFieldName("groups"); gen.writeStartArray();
		for(AssignmentGrouping grouping : groupings)
			gen.writeNumber(context.getID(grouping));
		gen.writeEndArray();
		
		return gen.generate();
	}
	
	public void deserialize(String data) {
		try(JsonParser parser = JsonStringGenerator.FACTORY.createParser(data)) {
			
			JsonToken token;
//			boolean startReading = false;
			String currentName = null;
			
			while((token = parser.nextToken()) != null) {
				switch(token) {
//					case START_ARRAY: 
//						if(parser.getCurrentName().equals("groups")) 
//							startReading = true; 
//					break;
//					
//					case END_ARRAY: 
//						if(parser.getCurrentName().equals("groups")) {
//							startReading = false; 
//							return;
//						}
//					break;
				
					case FIELD_NAME: currentName = parser.getCurrentName(); break;
					
					case VALUE_NUMBER_INT:
//						if(!startReading) continue;
						if(!currentName.equals("groups")) continue;
						groupings.add(context.load(AssignmentGrouping.class, parser.getIntValue(), this));
					break;
					
					default: break;
				}
			}
			
		} catch (IOException e) { e.printStackTrace(); }
	}

	public JsonSerializationContext getContext() { return context; }
}
