package managment;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;

import managment.storage.IOUtil;
import managment.storage.SerializationContext;
import managment.storage.serialization.ISerializable;
import managment.storage.serialization.Offset;
import managment.storage.serialization.SerializationUtil;

public class Assignment implements ISerializable {
	private static final TemporalUnit UNITS = ChronoUnit.MINUTES;
	
	private LocalDateTime creationDate;
	private LocalDateTime deadline;
	
	private boolean completeOverride;
	private boolean completed;
	private Priority priority;
	
	private boolean passive;
	private float impotrance;
	
	private String name;
	private String description;
	
	private ArrayList<Task> tasks;
	private float percentDone;
	
	private AssignmentGrouping grouping;
	private SerializationContext context;
	private boolean deleted;
	
	protected Assignment(AssignmentGrouping grouping, LocalDateTime deadline, Priority priority, float impotrance, String name, String description, SerializationContext context) {
		this(context, grouping);
		
		this.creationDate = LocalDateTime.now();
		this.deadline = deadline;
		
		this.completed = false;
		this.priority = priority;
		this.impotrance = impotrance;
		
		this.name = name;
		this.description = description;
		
		IOUtil.update(this);
	}

	private Assignment(SerializationContext context, AssignmentGrouping grouping) { 
		this.context = context; 
		this.grouping = grouping;
		this.tasks = new ArrayList<>();
	}
	
	public Task addTask(String name, String description) {
		Task task = new Task(this, name, description, context);
		grouping.display_addTask(task);
		tasks.add(task);
		update();
		
		IOUtil.update(this); 
		return task;
	}
	
	public Task addTask(Task task) {
		Task newTask = new Task(this, task.getName(), task.getDescription(), context);
		newTask.setDone(task.isDone());
		tasks.add(task);
		update();
		
		IOUtil.update(this); 
		return newTask;
	}
	
	public void removeTask(Task task) {
		if(deleted) return;
		grouping.display_removeTask(task);
		tasks.remove(task);
		update();
		
		IOUtil.update(this); 
	}
	
	public void update() {
		boolean startComplete = completed;
		Priority startPriority = priority;
		
		if(!completeOverride) {
			boolean completedStart = completed;
			this.completed = true;
			this.percentDone = 0;
			
			for(Task task : tasks) {
				if(!task.isDone()) {
					completed = false;
				} else {
					percentDone ++;
				}
			}
			
			if(tasks.size() > 0)
				percentDone /= tasks.size();
			else 
				completed = completedStart;
		}
		
		if(!completed && !passive) {
			LocalDateTime now = LocalDateTime.now();
			long expressedTime = getExpressedTime(now);
			long remaindingTime = getRemaindingTime(now);
			long totalTime = expressedTime + remaindingTime;
			
			Priority[] priorities = Priority.values();
			int stage = stage(expressedTime, totalTime, impotrance);
//			priority = stage > priority.ordinal() ? priorities[stage] : priority;
			priority = priorities[stage];
		}
		
		display_update();
		
		if(startComplete != completed || startPriority != priority)
			IOUtil.update(this); 

		if(completed && LocalDateTime.now().isAfter(deadline))
			delete();
	}
	
	protected void display_update() { grouping.display_updated(this); }
	
	public void setCompleted(boolean completed) {
		this.completed = completed;
		this.completeOverride = completed;
		for(Task task : tasks) task.setOverride(completed);
		
		update();
		IOUtil.update(this);
	}
	
	public void changeGrouping(AssignmentGrouping grouping) { 
		if(this.grouping != null) 
			this.grouping.removeAssignment(this);
		
		this.grouping = grouping; 
		grouping.addAssignment(this);
	}
	
	public void delete() {
		grouping.removeAssignment(this);
		IOUtil.delete(this);
		deleted = true;
		
		for(Task task : tasks)
			task.delete();
	}

	public void setPassive(boolean passive) { this.passive = passive; update(); IOUtil.update(this); }
	public void setImportance(float importance) { this.impotrance = importance; update(); IOUtil.update(this); }
	public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; update(); IOUtil.update(this); }
	public void setCreationDate(LocalDateTime creation) { this.creationDate = creation; update(); IOUtil.update(this); }
	
	public void setDescription(String descript) { this.description = descript; display_update(); IOUtil.update(this); }
	public void setPriority(Priority priority) { this.priority = priority; display_update(); IOUtil.update(this); }
	public void setName(String name) { this.name = name; display_update(); IOUtil.update(this); }

	public AssignmentGrouping getGrouping() { return grouping; }
	public LocalDateTime getCreationDate() { return creationDate; }
	public LocalDateTime getDeadline() { return deadline; }
	
	public boolean isPassive() { return passive; }
	public float getImportance() { return impotrance; }
	
	public Priority getPriority() { return priority; }
	public boolean isCompleted() { return completed; }
	public boolean isOverride() { return completeOverride; }

	public String getName() { return name; }
	public String getDescription() { return description; }
	
	public long getExpressedTime() { return getExpressedTime(LocalDateTime.now()); }
	public long getRemaindingTime() { return getRemaindingTime(LocalDateTime.now()); }
	
	public long getExpressedTime(LocalDateTime now) { return creationDate.until(now, UNITS); }
	public long getRemaindingTime(LocalDateTime now) { return now.until(deadline, UNITS); }
	public long getTotalTime() { return creationDate.until(deadline, UNITS); }
	
	public ArrayList<Task> getTasks() { return tasks; }
	public float getPercentDone() { return percentDone; }

	public String toString() { return name; }
	
	public Assignment makeCopy() {
		Assignment assignment = grouping.createAssignment(deadline, priority, impotrance, name, description);
		
		assignment.completed = this.completed;
		assignment.creationDate = this.creationDate;
		assignment.completeOverride = this.completeOverride;
		
		for(Task task : tasks) {
			task.copyTo(assignment.addTask(task.getName(), task.getDescription()));
		}
		
		assignment.update();
		IOUtil.update(assignment);
		return assignment;
	}
	
	public byte[] serialize() {
		byte[] data = new byte[8 + 8 + 1 + 4 + name.length() + 4 + description.length() + 4 + tasks.size() * 4 + 4];
		Offset offset = new Offset();
		
		SerializationUtil.serialize(creationDate, offset, data);
		SerializationUtil.serialize(	deadline, offset, data);
		
		byte pack = 0;
		pack |= (byte) priority.ordinal();						   // 3-bits
		pack |= (byte) (completeOverride ? 0xFF & 0b0_001000 : 0); // 1-bit
		pack |= (byte) (	   completed ? 0xFF & 0b0_010000 : 0); // 1-bit
		pack |= (byte) (	   	 passive ? 0xFF & 0b0_100000 : 0); // 1-bit
		data[offset.get()] = pack; offset.add(1);

		SerializationUtil.serialize(impotrance, offset, data);

		SerializationUtil.serialize(name, offset, data);
		SerializationUtil.serialize(description, offset, data);
		
		SerializationUtil.serialize(tasks.size(), offset, data);
		for(Task task : tasks) SerializationUtil.serialize(context.getID(task), offset, data);
		
		return data;
	}
	
	public void deserialize(byte[] data) {
		Offset offset = new Offset();
		
		creationDate = SerializationUtil.deserializeDateTime(offset, data);
			deadline = SerializationUtil.deserializeDateTime(offset, data);
				
		byte pack = data[offset.get()];
		priority = Priority.values()[pack & 0b0_000111];
		completeOverride = (pack & 0b0_001000) != 0; 
		completed 		 = (pack & 0b0_010000) != 0;
		passive 		 = (pack & 0b0_100000) != 0;
		offset.add(1);
		
		impotrance = SerializationUtil.deserializeFloat(offset, data);
		
		name = SerializationUtil.deserializeString(offset, data);
		description = SerializationUtil.deserializeString(offset, data);
		
		for(int i = 0, n = SerializationUtil.deserializeInt(offset, data); i < n; i ++) {
//			Task task = context.load(Task.class, SerializationUtil.deserializeInt(offset, data), this);
//			tasks.add(task);

			IOUtil.load(context, SerializationUtil.deserializeInt(offset, data), Task.class, tasks::add, this);
		}
	}

	public SerializationContext getContext() { return context; }
	
	public static int stage(long expressed, long total, float importance) {
		if(expressed > total) return 4; if(expressed < 0) return 0;
		
		return (int) Math.max(1, Math.min(4, 
				-Math.floor(
					Math.log(-expressed * 1.26 / total + 1.375) / 
					Math.log(1.75 + (4.5 - 1.75) * (1 - 
								(
									Math.log(importance * (1 - .1) + .1) / 
									-Math.log(.1) + 1
								)
							)
					)
				) + 1
			));
	}
}
