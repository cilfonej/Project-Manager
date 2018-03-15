package managment;

import managment.storage.IOUtil;
import managment.storage.SerializationContext;
import managment.storage.serialization.ISerializable;
import managment.storage.serialization.Offset;
import managment.storage.serialization.SerializationUtil;

public class Task implements ISerializable {
	private boolean done;
	private boolean doneOverride;
	
	private String name;
	private String description;
	
	private Assignment assignment;
	private SerializationContext context;

	protected Task(Assignment assignment, String name, String description, SerializationContext context) {
		this(context, assignment);
		
		this.name = name;
		this.description = description;
		
		IOUtil.update(this);
	}

	private Task(SerializationContext context, Assignment assignment) { 
		this.context = context; 
		this.assignment = assignment;
	}
	
//	public void setAssignment(Assignment assignment) { 
//		if(this.assignment != null) {
//			this.assignment.removeTask(this);
//		}
//		
//		this.assignment = assignment; 
//		assignment.getGrouping().addTask(this);
//		assignment.getTasks().add(this);
//		assignment.update(); 
//	}
	
	public void delete() { assignment.removeTask(this); IOUtil.delete(this); }
	
	public Assignment getAssignment() { return assignment; }
	
	public void setDone(boolean done) { this.done = done; assignment.update(); IOUtil.update(this); }
	void setOverride(boolean done) { this.doneOverride = done; assignment.display_update(); IOUtil.update(this); }
	
	public boolean isDone() { return doneOverride || done; }
	public boolean isOverride() { return doneOverride; }
	
	public void setDescription(String descript) { this.description = descript; assignment.display_update(); IOUtil.update(this); }
	public String getDescription() { return description; }
	
	public void setName(String name) { this.name = name; assignment.display_update(); IOUtil.update(this); }
	public String getName() { return name; }
	public String toString() { return name; }
	
	protected void copyTo(Task task) {
		task.done = this.done;
		task.doneOverride = this.doneOverride;
		
		task.name = this.name;
		task.description = this.description;
	}

	public byte[] serialize() {
		byte[] data = new byte[1 + name.length() + 4 + description.length() + 4];
		Offset offset = new Offset();
		
		byte pack = 0;
		pack |= (byte) (		done ? 1 << 0 : 0);	// 1-bit
		pack |= (byte) (doneOverride ? 1 << 1 : 0); // 1-bit
		data[offset.get()] = pack; offset.add(1);

		SerializationUtil.serialize(name, offset, data);
		SerializationUtil.serialize(description, offset, data);
		
		return data;
	}
	
	public void deserialize(byte[] data) {
		Offset offset = new Offset();
		
		byte pack = data[offset.get()];
				done = (pack & 0b0_01) != 0; 
		doneOverride = (pack & 0b0_10) != 0;
		offset.add(1);
		
		name = SerializationUtil.deserializeString(offset, data);
		description = SerializationUtil.deserializeString(offset, data);
	}
	
	public SerializationContext getContext() { return context; }
}
