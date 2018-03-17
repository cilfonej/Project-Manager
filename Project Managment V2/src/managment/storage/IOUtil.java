package managment.storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import managment.Assignment;
import managment.AssignmentGrouping;
import managment.Root;
import managment.storage.serialization.json.IJsonManualSerializable;
import panels.MainAssignmentFrame;

public class IOUtil extends Thread {
	private static final long  SAVE_STABLE_TIME =  500;
	private static final long  SYNC_STABLE_TIME = 2500;
	private static final long UPDATE_CYCLE_TIME = 10 * 60 * 1000;
	
	private static final HashMap<JsonSerializationContext, IOUtil> UTIL = new HashMap<>();
	private static IOUtil get(JsonSerializationContext context) {
		IOUtil util = UTIL.get(context);
		if(util == null) UTIL.put(context, util = new IOUtil(context));
		return util;
	}

	public static void updateAll(JsonSerializationContext context) { get(context)._updateAll(); }
	
	public static void update(IJsonManualSerializable serializable) { get(serializable.getContext())._update(serializable); }
	public static void delete(IJsonManualSerializable serializable) { get(serializable.getContext())._delete(serializable); }

	public static void driveCheck(IJsonManualSerializable serializable) { get(serializable.getContext())._driveCheck(); }
	        
	public static <T extends IJsonManualSerializable> void load(
			JsonSerializationContext context, int index, Class<T> clazz, Consumer<T> callback, Object... args) {
		get(context)._load(index, clazz, callback, args);
	}
	
	private DriveInterface driveInterface;
	private JsonSerializationContext context;
	
	private LinkedBlockingQueue<Token> actions; 
	private HashMap<IJsonManualSerializable, UpdateToken> invoke;
	
	private Root root;
	
	private long saveTimeout, syncTimeout, pokeTimeout;
	private boolean resetSaveTimeout, resetSyncTimeout, resetPokeTimeout;
	
	private IOUtil(JsonSerializationContext context) {
		actions = new LinkedBlockingQueue<>();
		invoke = new HashMap<>();
		
		driveInterface = DriveInterface.getInstance();
		this.context = context;
		
		actions.offer(new SyncFromDriveToken(null));
		actions.offer(new LoadContextToken(null));
//		actions.offer(new SyncToDriveToken(null));
		
		saveTimeout = Long.MAX_VALUE;
		syncTimeout = Long.MAX_VALUE;
		pokeTimeout = Long.MAX_VALUE;
		
		setDaemon(true);
		setName("Project Managment - I/O Thread");
		
		Runtime.getRuntime().addShutdownHook(new Thread(this::close, "Project Manager - Save Thread"));
		start();
	}
	
	public void run() {
		while(isAlive()) { 
			long startTime = System.currentTimeMillis();
			
			try {
				if(saveTimeout <= 0) { actions.offer(new SaveContextToken(null)); saveTimeout = Long.MAX_VALUE; }
				if(syncTimeout <= 0) { actions.offer(new SyncToDriveToken(null)); syncTimeout = Long.MAX_VALUE; }
				if(pokeTimeout <= 0) { actions.offer(new UpdateAllToken()); 	  pokeTimeout = Long.MAX_VALUE; }

				long timeout = Math.min(Math.min(saveTimeout, syncTimeout), pokeTimeout);
				Token action = actions.poll(timeout, TimeUnit.MILLISECONDS);
				if(action == null) continue; // ----------------------- Non-Timeout Events --------------------
				
				action.invoke();
				
			} catch(InterruptedException e) { 
			} catch(Exception e) { e.printStackTrace();
			} finally {
				long duration = System.currentTimeMillis() - startTime;
				saveTimeout -= duration; syncTimeout -= duration; pokeTimeout -= duration;
			}
			
			if(resetSaveTimeout) saveTimeout = SAVE_STABLE_TIME;
			if(resetSyncTimeout) syncTimeout = SYNC_STABLE_TIME;
			if(resetPokeTimeout) pokeTimeout = UPDATE_CYCLE_TIME;
			
			resetSaveTimeout = resetSyncTimeout = resetPokeTimeout = false;
		}
	}
	
	private void close() {
		context.saveFile();
		driveInterface.syncToDrive(); 
	}

//	private void _syncToDrive(IJsonManualSerializable serializable)   { actions.add(new SyncToDriveToken(null));   }
//	private void _loadFromDrive(IJsonManualSerializable serializable) { actions.add(new SyncFromDriveToken(null)); }
	
	private void _updateAll() { offerToken(new UpdateAllToken()); }
	
	private void _update(IJsonManualSerializable serializable) { offerToken(new UpdateToken(serializable, false)); }
	private void _delete(IJsonManualSerializable serializable) { offerToken(new UpdateToken(serializable,  true)); }
	
	private void _driveCheck() { try { driveInterface.checkAndCorrectVersion(); } catch(IOException e) { e.printStackTrace(); }}
	
	private <T extends IJsonManualSerializable> void _load(int index, Class<T> clazz, Consumer<T> callback, Object... args) {
		offerToken(new LoadToken<>(index, clazz, args, callback));
	}
	
	private void offerToken(Token token) {
		if(Thread.currentThread() != this) {
			actions.offer(token);
			return;
		}
		
		token.invoke();
	}
	
	private static abstract class Token { public abstract void invoke(); }
	private static abstract class ReturningToken<T> extends Token { 
		Consumer<T> callback; 
		ReturningToken(Consumer<T> c) { callback = c; }
		
		public abstract T invokeCallback();
		public void invoke() { 
			T t = invokeCallback();
			if(callback != null)
				callback.accept(t);
		}
	}
	
	private class SaveContextToken extends ReturningToken<Void> {
		SaveContextToken(Consumer<Void> c) { super(c); }
		
		public Void invokeCallback() {
			MainAssignmentFrame.setStatusText("Saving...");
				for(Map.Entry<IJsonManualSerializable, UpdateToken> entry : invoke.entrySet()) {
					UpdateToken token = entry.getValue();
					if(token.delete) context.delete(token.serializable);
					else context.update(token.serializable);
				}
			
				invoke.clear();
				context.saveFile();
				resetSyncTimeout = true;
			MainAssignmentFrame.setStatusText("");
			
			return null;
		}
	}
	
	private class LoadContextToken extends ReturningToken<Root> { 
		LoadContextToken(Consumer<Root> c) { super(c); }
		
		public Root invokeCallback() {
			MainAssignmentFrame.setStatusText("Loading...");
				context.loadFile(FileStorage.SAVE_FILE);
				
				root = context.load(Root.class, 0);
				if(root == null) {
					root = new Root(context);
					context.update(root);
				}
			MainAssignmentFrame.setStatusText("");
			
			return root;
		}
	}

	private class SyncToDriveToken extends ReturningToken<Void> { 
		SyncToDriveToken(Consumer<Void> c) { super(c); }
		
		public Void invokeCallback() {
			MainAssignmentFrame.setStatusText("Uploading...");
				driveInterface.syncToDrive();
			MainAssignmentFrame.setStatusText("");
			
			return null;
		}
	}
	
	private class SyncFromDriveToken extends ReturningToken<Void> { 
		SyncFromDriveToken(Consumer<Void> c) { super(c); }
		
		public Void invokeCallback() {
			MainAssignmentFrame.setStatusText("Downloading...");
				driveInterface.syncToLocal();
			MainAssignmentFrame.setStatusText("");
			
			return null;
		}
	}
	
	private class UpdateAllToken extends Token {
		public void invoke() {
			LinkedList<Assignment> assignments = new LinkedList<>();
			
			for(AssignmentGrouping grouping : root.getGroupings()) {
			for(Assignment assignment : grouping.getAssignments()) {
				assignments.add(assignment);
			}}

			for(Assignment assignment : assignments)
				assignment.update();
			
			resetPokeTimeout = true;
		} 
	}
	
	private class UpdateToken extends Token { 
		IJsonManualSerializable serializable;  boolean delete;
		
		UpdateToken(IJsonManualSerializable serializable, boolean delete) { 
			this.serializable = serializable; 
			this.delete = delete;
		}

		public void invoke() {
			UpdateToken current = invoke.get(serializable);
			if(current == null || Boolean.compare(current.delete, delete) > 0)
				invoke.put(serializable, this);
			
			resetSaveTimeout = true;
		}
	}
	
	private class LoadToken<T extends IJsonManualSerializable> extends ReturningToken<T> { 
		int index; Class<T> clazz; Object[] args;
		
		LoadToken(int index, Class<T> clazz, Object[] args, Consumer<T> callback) {
			super(callback);
			
			this.index = index;
			this.clazz = clazz;
			this.args = args;
		}

		public T invokeCallback() { return context.load(clazz, index, args); } 
	}
}
