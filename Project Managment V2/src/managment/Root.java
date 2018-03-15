package managment;

import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import components.tree.TreeNode;
import components.tree.TreeNodeMetadata;
import managment.storage.IOUtil;
import managment.storage.SerializationContext;
import managment.storage.serialization.ISerializable;
import managment.storage.serialization.Offset;
import managment.storage.serialization.SerializationUtil;
import managment.storage.serialization.json.IJsonSerializable;

public class Root extends TreeNode<Object> implements IJsonSerializable {
	private transient Consumer<Assignment> updater;
	
	private transient Consumer<AssignmentGrouping> addGrouping, removeGrouping;
	private transient Consumer<Assignment> addAssignment, removeAssignment;
	private transient Consumer<Task> addTask, removeTask;
	
	private ArrayList<AssignmentGrouping> groupings;
	private SerializationContext context;
	
	public Root(SerializationContext context) {
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
	
	public byte[] serialize() {
		byte[] data = new byte[groupings.size() * 4 + 4];
		Offset offset = new Offset();
		
		SerializationUtil.serialize(groupings.size(), offset, data);
		for(AssignmentGrouping grouping : groupings)
			SerializationUtil.serialize(context.getID(grouping), offset, data);
		
		return data;
	}
	
	public void deserialize(byte[] data) {
		Offset offset = new Offset();
		
		for(int i = 0, n = SerializationUtil.deserializeInt(offset, data); i < n; i ++) {
//			AssignmentGrouping grouping = context.load(AssignmentGrouping.class, SerializationUtil.deserializeInt(offset, data), this);
//			groupings.add(grouping);

			IOUtil.load(context, SerializationUtil.deserializeInt(offset, data), AssignmentGrouping.class, groupings::add, this);
		}
	}

	public SerializationContext getContext() { return context; }
}
