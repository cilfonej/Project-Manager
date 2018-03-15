package managment;

import java.time.LocalDateTime;
import java.util.ArrayList;

import managment.storage.IOUtil;
import managment.storage.SerializationContext;
import managment.storage.serialization.Offset;
import managment.storage.serialization.SerializationUtil;
import managment.storage.serialization.json.IJsonSerializable;

public class AssignmentGrouping implements IJsonSerializable {
	private ArrayList<Assignment> assignments;
	private String name;
	
	private Root root;
	
	private SerializationContext context;
	private boolean deleted;
	
	public AssignmentGrouping(String name, Root root, SerializationContext context) {
		this(context, root);
		this.name = name;
		
		IOUtil.update(this);
	}
	
	private AssignmentGrouping(SerializationContext context, Root root) { 
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

	public byte[] serialize() {
		byte[] data = new byte[name.length() + 4 + assignments.size() * 4 + 4];
		Offset offset = new Offset();
		
		SerializationUtil.serialize(name, offset, data);
		
		SerializationUtil.serialize(assignments.size(), offset, data);
		for(Assignment assignment : assignments)
			SerializationUtil.serialize(context.getID(assignment), offset, data);
		
		return data;
	}
	
	public void deserialize(byte[] data) {
		Offset offset = new Offset();
		name = SerializationUtil.deserializeString(offset, data);
		
		for(int i = 0, n = SerializationUtil.deserializeInt(offset, data); i < n; i ++) {
//			Assignment assignment = IOUtil.load(Assignment.class, SerializationUtil.deserializeInt(offset, data), this);
//			assignments.add(assignment);
			
			IOUtil.load(context, SerializationUtil.deserializeInt(offset, data), Assignment.class, assignments::add, this);
		}
	}
	
	public SerializationContext getContext() { return context; }
}
