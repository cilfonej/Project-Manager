package managment;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import managment.storage.IOUtil;
import managment.storage.JsonSerializationContext;
import managment.storage.serialization.json.IJsonManualSerializable;
import managment.storage.serialization.json.JsonStringGenerator;

public class AssignmentGrouping implements IJsonManualSerializable {
	private ArrayList<Assignment> assignments;
	private String name;
	
	private Root root;
	
	private JsonSerializationContext context;
	private boolean deleted;
	
	public AssignmentGrouping(String name, Root root, JsonSerializationContext context) {
		this(context, root);
		this.name = name;
		
		IOUtil.update(this);
	}
	
	private AssignmentGrouping(JsonSerializationContext context, Root root) { 
		this.context = context;
		this.root = root;
		assignments = new ArrayList<>();
	}

	public void display_updated(Assignment assignment) { root.display_update(assignment); }

	public Assignment createAssignment(LocalDateTime deadline, Priority priority, float impotrance, String name, String description) { 
		Assignment assignment = new Assignment(this, deadline, priority, impotrance, name, description, context);
		addAssignment(assignment);
		return assignment;
	}
	
	public void addAssignment(Assignment assignment) { 
		if(deleted) return;
		root.display_addAssignment(assignment);
		assignments.add(assignment);
		IOUtil.update(this);
	}
	
	public void removeAssignment(Assignment assignment) { 
		if(deleted) return;
		if(!assignments.contains(assignment)) throw new IllegalArgumentException("Group does not contain Assignment: " + assignment);
		root.display_removeAssignment(assignment);
		assignments.remove(assignment);
		IOUtil.update(this);
	}
	
	public void delete() { 
		root.removeAssignmentGroup(this);
		IOUtil.delete(this); 
		deleted = true;
		
		for(Assignment assignment : assignments)
			assignment.delete();
	}
	
	protected void display_addTask(Task task) 	 { root.display_addTask(task); }
	protected void display_removeTask(Task task) { root.display_removeTask(task); }
	
	public ArrayList<Assignment> getAssignments() { return assignments; }

	public String getName() { return name; }
	public String toString() { return name; }

	public String serialize() {
		JsonStringGenerator gen = JsonStringGenerator.getInstance();
		
		gen.writeStringField("name", name);
		
		gen.writeFieldName("assignments"); gen.writeStartArray();
		for(Assignment assignment : assignments)
			gen.writeNumber(context.getID(assignment));
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
						if(!currentName.equals("assignments")) continue;
						assignments.add(context.load(Assignment.class, parser.getIntValue(), this));
					break;
					
					case VALUE_STRING:
						if(parser.getCurrentName().equals("name")) 
							name = parser.getText();
					break;
					
					default: break;
				}
			}
			
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public JsonSerializationContext getContext() { return context; }
}
