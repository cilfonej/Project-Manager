package managment;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import managment.storage.IOUtil;
import managment.storage.JsonSerializationContext;
import managment.storage.serialization.json.IJsonManualSerializable;
import managment.storage.serialization.json.JsonStringGenerator;

public class Task implements IJsonManualSerializable {
	private boolean done;
	private boolean doneOverride;
	
	private String name;
	private String description;
	
	private Assignment assignment;
	private JsonSerializationContext context;

	protected Task(Assignment assignment, String name, String description, JsonSerializationContext context) {
		this(context, assignment);
		
		this.name = name;
		this.description = description;
		
		IOUtil.update(this);
	}

	private Task(JsonSerializationContext context, Assignment assignment) { 
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

	public String serialize() {
		JsonStringGenerator gen = JsonStringGenerator.getInstance();
		
		gen.writeStringField("name", name);
		gen.writeStringField("description", description);
		
		gen.writeBooleanField("doneOverride", doneOverride);
		gen.writeBooleanField("done", done);
		
		return gen.generate();
	}
	
	public void deserialize(String data) {
		try(JsonParser parser = JsonStringGenerator.FACTORY.createParser(data)) {

			JsonToken token;
			while((token = parser.nextToken()) != null) {
				switch(token) {
					case VALUE_STRING:
						if(parser.getCurrentName().equals("name")) 
							name = parser.getText();
						
						else if(parser.getCurrentName().equals("description")) 
							description = parser.getText();
					break;
					
					case VALUE_TRUE:
					case VALUE_FALSE:
						if(parser.getCurrentName().equals("doneOverride")) 
							doneOverride = parser.getBooleanValue();

						else if(parser.getCurrentName().equals("done")) 
							done = parser.getBooleanValue();
					break;
					
					default: break;
				}
			}
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public JsonSerializationContext getContext() { return context; }
}
