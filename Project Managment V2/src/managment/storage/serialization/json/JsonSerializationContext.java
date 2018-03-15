package managment.storage.serialization.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import managment.storage.serialization.ISerializable;

public class JsonSerializationContext {
	private ArrayList<String> objects;
	private HashMap<IJsonSerializable, Integer> ids;
	
	private File saveLocation;
	
	public JsonSerializationContext(File saveLocation) {
		this.objects = new ArrayList<>();
		this.ids = new HashMap<>();
		
		this.saveLocation = saveLocation;
//		load();
	}
	
	public int getID(IJsonSerializable serializable) {
		Integer index = ids.get(serializable);
		
		if(index == null) {
			index = objects.indexOf(null);
			if(index < 0) index = objects.size();
			
			ids.put(serializable, index);
			
			if(index == objects.size())
				objects.add("{}");
			else objects.set(index, "{}");
		}
		
		return index;
	}
	
	protected void update(IJsonSerializable serializable) {
		objects.set(getID(serializable), serializable.serialize());
	}
	
	protected void delete(IJsonSerializable serializable) {
		if(!ids.containsKey(serializable)) return;
		objects.set(getID(serializable), null);
		ids.remove(serializable);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends IJsonSerializable> T load(Class<T> clazz, int index, Object... args) {
		if(index >= objects.size()) return null;
		
		for(Map.Entry<IJsonSerializable, Integer> entry : ids.entrySet()) {
			if(entry.getValue() == index) return (T) entry.getKey();
		}
		
		Constructor<? extends IJsonSerializable> constructor; 
		Class<?>[] argClasses = null;
		
		try {
			argClasses = new Class[args.length + 1];
			argClasses[0] = JsonSerializationContext.class;

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
		try(JsonGenerator gen = JsonStringGenerator.FACTORY.createGenerator(new FileOutputStream(saveLocation))) {
			gen.writeStartArray();
			
			for(String data : objects) {
				if(data == null) { gen.writeNull(); continue; }
				gen.writeRaw(data); gen.writeRaw(',');
			}
			
			gen.writeEndArray();
		} catch(IOException e) { e.printStackTrace(); }
	
		System.out.println("Saved!");
	}
	
	protected void load() {
		try(JsonParser parser = JsonStringGenerator.FACTORY.createParser(new FileInputStream(saveLocation))) {
			JsonToken token;
			while((token = parser.nextToken()) != null) {
				switch(token) {
					case END_ARRAY: break;
					case END_OBJECT: break;
					case FIELD_NAME: break;
					case NOT_AVAILABLE: break;
					case START_ARRAY: break;
					case START_OBJECT: break;
					case VALUE_EMBEDDED_OBJECT: break;
					case VALUE_FALSE: break;
					case VALUE_NULL: break;
					case VALUE_NUMBER_FLOAT: break;
					case VALUE_NUMBER_INT: break;
					case VALUE_STRING: break;
					case VALUE_TRUE: break;
					default: break;
				}
				
				Modifier.isTransient(getClass().getDeclaredField("").getModifiers())
			}
		} catch(FileNotFoundException e) {
		} catch(IOException e) { e.printStackTrace(); }
	}
	
	public static class MutedSerializationContext extends JsonSerializationContext {
		public MutedSerializationContext(File saveLocation) { super(saveLocation); }
		public void update(ISerializable serializable) { }
		public void delete(ISerializable serializable) { }
		public void save() { }

		public int getCount() { return super.objects.size(); }
		public String getRaw(int index) { return super.objects.get(index); }
		
		public Object loadRaw(int index) {
			for(Map.Entry<IJsonSerializable, Integer> entry : super.ids.entrySet())
				if(entry.getValue() == index) return entry.getKey();
			return null;
		}
	}
}
