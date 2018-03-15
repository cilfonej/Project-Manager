package managment.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import managment.storage.serialization.ISerializable;
import managment.storage.serialization.Offset;
import managment.storage.serialization.SerializationUtil;

public class SerializationContext {
	private ArrayList<byte[]> objects;
	private HashMap<ISerializable, Integer> ids;
	
	private File saveLocation;
	
	public SerializationContext(File saveLocation) {
		this.objects = new ArrayList<>();
		this.ids = new HashMap<>();
		
		this.saveLocation = saveLocation;
//		load();
	}
	
	public int getID(ISerializable serializable) {
		Integer index = ids.get(serializable);
		
		if(index == null) {
			index = objects.indexOf(null);
			if(index < 0) index = objects.size();
			
			ids.put(serializable, index);
			objects.add(new byte[0]);
		}
		
		return index;
	}
	
	protected void update(ISerializable serializable) {
		objects.set(getID(serializable), serializable.serialize());
	}
	
	protected void delete(ISerializable serializable) {
		objects.set(getID(serializable), null);
		ids.remove(serializable);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends ISerializable> T load(Class<T> clazz, int index, Object... args) {
		if(index >= objects.size()) return null;
		
		for(Map.Entry<ISerializable, Integer> entry : ids.entrySet()) {
			if(entry.getValue() == index) return (T) entry.getKey();
		}
		
		Constructor<? extends ISerializable> constructor; 
		Class<?>[] argClasses = null;
		
		try {
			argClasses = new Class[args.length + 1];
			argClasses[0] = SerializationContext.class;

			for(int i = 0; i < args.length; i ++)
				argClasses[i + 1] = args[i].getClass();
			
			constructor = clazz.getDeclaredConstructor(argClasses);
			constructor.setAccessible(true);
			
		} catch(NoSuchMethodException | SecurityException e) {
			StringBuilder errMessage = new StringBuilder();
			for(Class<?> arg : argClasses) { errMessage.append(arg.getSimpleName()); errMessage.append(", "); }
			errMessage.setLength(errMessage.length() - 2);
			
			throw new IllegalArgumentException("No Constructor found for " + clazz.getSimpleName() + "(" + errMessage + ")", e);
		}
		
		try {
			Object[] fullArgs = new Object[args.length + 1];
			System.arraycopy(args, 0, fullArgs, 1, args.length);
			fullArgs[0] = this;
			
			T obj = (T) constructor.newInstance(fullArgs);
			obj.deserialize(objects.get(index));
			ids.put(obj, index);
			return obj;
			
		} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void save() {
		byte[] lengthWrite = new byte[4]; Offset offset = new Offset();
		
		try(FileOutputStream out = new FileOutputStream(saveLocation)) {
			for(byte[] data : objects) {
				SerializationUtil.serialize(data == null ? -1 : data.length, offset, lengthWrite);
				out.write(lengthWrite, 0, 4); offset.set(0);
				if(data != null) out.write(data);
			}
		} catch(IOException e) { e.printStackTrace(); }
	
		System.out.println("Saved!");
	}
	
	protected void load() {
		byte[] lengthRead = new byte[4]; Offset offset = new Offset();
		
		try(FileInputStream in = new FileInputStream(saveLocation)) {
			while(in.read(lengthRead) > 0) {
				int length = SerializationUtil.deserializeInt(offset, lengthRead); 
				if(length == -1) { objects.add(null); offset.set(0); continue; }
				byte[] data = new byte[length]; offset.set(0);
				in.read(data, 0, length);
				objects.add(data);
			}
		} catch(FileNotFoundException e) {
		} catch(IOException e) { e.printStackTrace(); }
	}
	
	public static class MutedSerializationContext extends SerializationContext {
		public MutedSerializationContext(File saveLocation) { super(saveLocation); }
		public void update(ISerializable serializable) { }
		public void delete(ISerializable serializable) { }
		public void save() { }

		public int getCount() { return super.objects.size(); }
		public byte[] getRaw(int index) { return super.objects.get(index); }
		
		public Object loadRaw(int index) {
			for(Map.Entry<ISerializable, Integer> entry : super.ids.entrySet())
				if(entry.getValue() == index) return entry.getKey();
			return null;
		}
	}
}
