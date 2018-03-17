package managment.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import managment.storage.serialization.ISerializable;
import managment.storage.serialization.json.IJsonManualSerializable;
import managment.storage.serialization.json.JsonStringGenerator;

public class JsonSerializationContext {
	private ArrayList<String> objects;
	private HashMap<IJsonManualSerializable, Integer> ids;
	
	private File saveLocation;
	
	public JsonSerializationContext(File saveLocation) {
		this.objects = new ArrayList<>();
		this.ids = new HashMap<>();
		
		this.saveLocation = saveLocation;
	}
	
	public int getID(IJsonManualSerializable serializable) {
		Integer index = ids.get(serializable);
		
		if(index == null) {
			index = objects.indexOf(null);
			
			if(index < 0) {
				index = objects.size();
				objects.add("{}");
				
			} else {
				objects.set(index, "{}");
			}
			
			ids.put(serializable, index);
		}
		
		return index;
	}
	
	protected void update(IJsonManualSerializable serializable) {
		objects.set(getID(serializable), serializable.serialize());
	}
	
	protected void delete(IJsonManualSerializable serializable) {
		if(!ids.containsKey(serializable)) return;
		objects.set(getID(serializable), null);
		ids.remove(serializable);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IJsonManualSerializable> T load(Class<T> clazz, int index, Object... args) {
		if(index >= objects.size()) return null;
		
		for(Map.Entry<IJsonManualSerializable, Integer> entry : ids.entrySet()) {
			if(entry.getValue() == index) return (T) entry.getKey();
		}
		
		Constructor<? extends IJsonManualSerializable> constructor; 
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
	
	protected void saveFile() {		
		try(JsonGenerator gen = JsonStringGenerator.FACTORY.createGenerator(new FileOutputStream(saveLocation)).useDefaultPrettyPrinter()){
			gen.writeStartObject();
			
			for(Entry<IJsonManualSerializable, Integer> entry : ids.entrySet()) {
				gen.writeFieldName(entry.getValue() + "");
				gen.writeRawValue(objects.get(entry.getValue())); // gen.writeRaw(',');
			}
			
			gen.writeEndObject();
		} catch(IOException e) { e.printStackTrace(); }
	
		System.out.println("Saved!");
	}
	
	protected void loadFile(File file) {
		objects.clear();
		ids.clear();
		
		HashMap<Integer, String> objectDataMap = new HashMap<>();
		int maxID = -1;
		
		try(MarkedReader reader = new MarkedReader(file); JsonParser parser = JsonStringGenerator.FACTORY.createParser(reader)) {
			JsonToken token;
			int currentId = -1;
			
			while((token = parser.nextToken()) != null) {
				switch(token) {
					case FIELD_NAME: 
						currentId = Integer.parseInt(parser.getCurrentName()); 
						if(currentId > maxID) maxID = currentId;
					break;
					
					case START_OBJECT:
						if(currentId < 0) break;
					case START_ARRAY:
						reader.mark();
						parser.skipChildren();
						objectDataMap.put(currentId, "{ " + new String(reader.fromMark()));
					break;	
					
					default: break;
				}
			}
		} catch(IOException e) { e.printStackTrace(); }
		
		for(int i = 0; i < maxID + 1; i ++) 
			objects.add(objectDataMap.getOrDefault(i, null));
	}
	
	private static class MarkedReader extends InputStream {
		private FileInputStream source;
		private byte[] record;
		private int index;
		
		private int mark;
		
		public MarkedReader(File file) throws IOException {
			source = new FileInputStream(file);
			record = new byte[Math.max(source.available(), 16)];
		}
			
		public int read() throws IOException {
			int read = source.read();
			
			if(read > 0) {
				if(index > record.length) expand();
				record[index ++] = (byte) read;
			}
			
			return read;
		}
		
		public int read(byte[] b, int off, int len) throws IOException {
			len = 1;
			int read = source.read(b, off, len);
			
			if(read > 0) {
				while(record.length - index < read) expand();
				System.arraycopy(b, off, record, index, read);
				index += read;
			}
			
			return read;
		}
		
		private void expand() {
			byte[] expanded = new byte[(int) (record.length * 1.5)];
			System.arraycopy(record, 0, expanded, 0, record.length);
			record = expanded;
		}
		
		public void mark() { mark = index; }
		public byte[] fromMark() {
			byte[] data = new byte[index - mark];
			System.arraycopy(record, mark, data, 0, data.length);
			return data;
		}
	}
	
	public static class MutedSerializationContext extends JsonSerializationContext {
		public MutedSerializationContext(File saveLocation) { super(saveLocation); }
		public void update(ISerializable serializable) { }
		public void delete(ISerializable serializable) { }
		public void save() { }

		public int getCount() { return super.objects.size(); }
		public String getRaw(int index) { return super.objects.get(index); }
		
		public Object loadRaw(int index) {
			for(Map.Entry<IJsonManualSerializable, Integer> entry : super.ids.entrySet())
				if(entry.getValue() == index) return entry.getKey();
			return null;
		}
	}
}
