package managment.storage.serialization.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

import sun.reflect.ReflectionFactory;

public class JsonSerializer {
	private HashMap<IJsonSerializable, Integer> objectLookup, newLookups;
	private ArrayList<IJsonSerializable> objects;
	
	public JsonSerializer() {
		objectLookup = new HashMap<>();
		newLookups = new HashMap<>();
		objects = new ArrayList<>();
	}
	
	public IJsonSerializable get(int id) { return objects.get(id); }
	
	public int lookupId(IJsonSerializable serializable) {
		int index = objectLookup.getOrDefault(serializable, -1);
		
		if(index < 0) {
			index = objects.indexOf(EMPTY);
			
			if(index < 0) {
				index = objects.size();
				objectLookup.put(serializable, index);
				newLookups.put(serializable, index);
				objects.add(serializable);
			} else {
				objectLookup.put(serializable, index);
				newLookups.put(serializable, index);
				objects.set(index, serializable);
			}
		}
		
		return index;
	}
	
	public void remove(IJsonSerializable serializable) {
		int index = objectLookup.getOrDefault(serializable, -1);
		if(index < 0) return;
		objects.remove(index);
	}
	
	public void appendJSON(JsonGenerator gen) {
		newLookups.putAll(objectLookup);
		
		while(!newLookups.isEmpty()) {
			HashMap<IJsonSerializable, Integer> copyMap = new HashMap<>(newLookups);
			newLookups.clear();
			
			for(Entry<IJsonSerializable, Integer> entry : copyMap.entrySet()) {
				try { 
					gen.writeFieldName(entry.getValue() + "");
					new ObjectDescriptor(entry.getKey()).appendJSON(gen);
				} catch(IOException e) { e.printStackTrace(); }
			}
		}
	}
	
	private static final IJsonSerializable EMPTY = new IJsonSerializable() {};
	
	@SuppressWarnings("unchecked")
	public void loadJSON(JsonParser parser) throws JsonParseException, IOException {
		Map<String, Object> map = (Map<String, Object>) parseJSON(parser).get(null);
		int maxIndex = -1;
		
		ArrayList<ObjectDescriptor> descriptors = new ArrayList<>();
		
		objectLookup.clear();
		for(Entry<String, Object> entry : map.entrySet()) {
			ObjectDescriptor descriptor = new ObjectDescriptor((Map<String, Object>) entry.getValue());
			descriptors.add(descriptor);
			
			int index = Integer.parseInt(entry.getKey());
			objectLookup.put(descriptor.createInstance(), index);
			if(index > maxIndex) maxIndex = index;
		}
		
		objects.clear();
		objects.ensureCapacity(maxIndex);
		for(int i = 0; i < maxIndex + 1; i ++) objects.add(EMPTY);
		for(Entry<IJsonSerializable, Integer> entry : objectLookup.entrySet())
			objects.set(entry.getValue(), entry.getKey());
		
		for(ObjectDescriptor descriptor : descriptors) descriptor.parseInstance();
		for(ObjectDescriptor descriptor : descriptors) descriptor.postLoad();
	}
	
	private Map<String, Object> parseJSON(JsonParser parser) throws JsonParseException, IOException {
		Map<String, Object> data = new HashMap<>();
		
		JsonToken token;
		String currentName = null;
		ArrayList<Object> array = null;
		Stack<ArrayList<Object>> arrayStack = new Stack<>();

		while((token = parser.nextToken()) != null) {
			switch(token) {
				case START_OBJECT: 
					if(array != null) array.add(parseJSON(parser)); 
					else data.put(currentName, parseJSON(parser)); break;
				case END_OBJECT: return data;

				case START_ARRAY: 
					ArrayList<Object> newArray = new ArrayList<>();
					if(array != null) array.add(newArray); 
					else data.put(currentName, newArray); 
					arrayStack.push(array = newArray); break;
				case END_ARRAY: 
					if(!arrayStack.isEmpty()) arrayStack.pop(); 
					if(!arrayStack.isEmpty()) array = arrayStack.peek();
					else array = null;
				break;

				case FIELD_NAME: currentName = parser.getText(); break;
				
				case VALUE_STRING: 
					if(array != null) array.add(parser.getText()); 
					else data.put(currentName, parser.getText()); break;
					
				case VALUE_NULL: 
					if(array != null) array.add(null); 
					else data.put(currentName, null); break;
				
				case VALUE_NUMBER_FLOAT: 
					if(array != null) array.add(parser.getValueAsDouble()); 
					else data.put(currentName, parser.getValueAsDouble()); break;
				case VALUE_NUMBER_INT: 
					if(array != null) array.add(parser.getValueAsLong()); 
					else data.put(currentName, parser.getValueAsLong()); break;
				
				case VALUE_FALSE: 
					if(array != null) array.add(false); 
					else data.put(currentName, false); break;
				case VALUE_TRUE: 
					if(array != null) array.add(true); 
					else data.put(currentName, true); break;

				case VALUE_EMBEDDED_OBJECT: 
					if(array != null) array.add(parser.getEmbeddedObject()); 
					else data.put(currentName, parser.getEmbeddedObject()); break;
				
				case NOT_AVAILABLE: break;
				default: break;
			}
		}
		
		return data;
	}
	
	private Object parseObject(Object data, Class<?> type) {
		if(data == null) return null;

		if(IJsonSerializable.class.isAssignableFrom(type)) return objects.get(((Long) data).intValue());
		if(isClassifyablePrimitive(type)) return specifyPrimitive(data, type);
		if(List.class.isAssignableFrom(type)) return parseList(data, type);
		if(Map.class.isAssignableFrom(type)) return parseMap(data, type);
		if(type.isArray()) return parseArray(data, type);
		if(type == String.class) return (String) data;
		
		throw new IllegalArgumentException("Cannot parse Object into " + type.getSimpleName());
//		return new ObjectDescriptor((Map<String, Object>) data);
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Object> classifyArray(Object data) {
		HashMap<String, Object> dataDetails = (HashMap<String, Object>) data;
		
		ArrayList<Object> raw = (ArrayList<Object>) dataDetails.get("data");
		ArrayList<String> classNames = (ArrayList<String>) dataDetails.get("classes");
		
		ArrayList<Class<?>> classes = new ArrayList<>();
		for(String className : classNames) {
			try { 
				if(className.isEmpty()) {
					classes.add(null); 
					continue;
				}
				
				classes.add(Class.forName(className)); 
			} catch(ClassNotFoundException e) { 
				e.printStackTrace(); 
				classes.add(null);
			}
		}
		
		ArrayList<Object> classified = new ArrayList<>(raw.size());
		
		for(int i = 0; i < raw.size(); i ++) {
			Class<?> clazz = classes.get(i);
			if(clazz == null) classified.add(null);
			else classified.add(parseObject(raw.get(i), clazz));
		}
		
		return classified;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List parseList(Object data, Class<?> type) {
		Constructor<?> defaultConst = null, capacityConst = null, copyConst = null;

		try { defaultConst  = type.getDeclaredConstructor();           } catch(Exception e) { }
		try { capacityConst = type.getDeclaredConstructor(int.class);  } catch(Exception e) { }
		try { copyConst 	= type.getDeclaredConstructor(List.class); } catch(Exception e) { }

		try { if(capacityConst == null) capacityConst = type.getDeclaredConstructor(Integer.class);  } catch(Exception e) {}
		try { if(copyConst 	 == null)   copyConst 	= type.getDeclaredConstructor(Collection.class); } catch(Exception e) {}

		List list = null;
		ArrayList<Object> raw = classifyArray(data);
		
		try {
			if(copyConst != null) {
				copyConst.setAccessible(true);
				list = (List) copyConst.newInstance(raw);
				
			} else if(defaultConst != null) {
				defaultConst.setAccessible(true);
				list = (List) defaultConst.newInstance();
				list.addAll(raw);
			
			} else if(capacityConst != null) {
				capacityConst.setAccessible(true);
				list = (List) capacityConst.newInstance(raw.size());
				list.addAll(raw);
			
			} else throw new NoSuchMethodError("Counld not construct List of type " + type.getSimpleName()); 
		} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	private Object parseArray(Object data, Class<?> type) {
		type = type.getComponentType();
		ArrayList<Object> raw = classifyArray(data);
		Object array = Array.newInstance(type, raw.size());
		
		for(int i = 0; i < raw.size(); i ++)
			Array.set(array, i, raw.get(i));
		return array;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map parseMap(Object data, Class<?> type) {
		try { 
			Constructor<?> constructor = type.getDeclaredConstructor();       
			constructor.setAccessible(true);
			Map map = (Map) constructor.newInstance();
			
			ArrayList<Object> raw = (ArrayList<Object>) data;
			for(Object entry : raw) {
				Map<String, Object> entryMap = (Map<String, Object>) entry;
				
				String keyClassName = (String) entryMap.get("key.class");
				String valueClassName = (String) entryMap.get("value.class");
				
				Class<?> keyClass = keyClassName.isEmpty() ? null : Class.forName(keyClassName);
				Class<?> valueClass = valueClassName.isEmpty() ? null : Class.forName(valueClassName);
				
				Object key = keyClass == null ? null : parseObject(entryMap.get("key"), keyClass);
				Object value = valueClass == null ? null : parseObject(entryMap.get("value"), valueClass);
				
				map.put(key, value);
			}
			
			return map;
		} catch(NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) { 
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void appendObject(JsonGenerator gen, String entryName, Object obj) throws JsonProcessingException, IOException {
		if(entryName != null) gen.writeFieldName(entryName);
		
		if(obj == null) gen.writeNull();
		
		else if(isClassifyablePrimitive(obj)) gen.writeObject(classifyPrimitive(obj));
		else if(obj.getClass().isArray()) appendArray(gen, (Object[]) obj);
		else if(obj instanceof String) gen.writeString((String) obj);
		else if(obj instanceof Map) appendMap(gen, (Map) obj);
		
		else if(obj instanceof IJsonSerializable) gen.writeNumber(lookupId((IJsonSerializable) obj));
		else throw new IllegalArgumentException("Cannont Serialize type " + obj.getClass().getSimpleName());
	}
	
	private void appendArray(JsonGenerator gen, Object[] array) throws JsonGenerationException, IOException {
		gen.writeStartObject();
		
			gen.writeArrayFieldStart("data");
				for(Object obj : array) 
					appendObject(gen, null, obj);
			gen.writeEndArray();

			gen.writeArrayFieldStart("classes");
				for(Object obj : array) 
					gen.writeString(obj == null ? "" : obj.getClass().getName());
			gen.writeEndArray();
		
		gen.writeEndObject();
	}
	
	private void appendMap(JsonGenerator gen, Map<?, ?> map) throws JsonGenerationException, IOException {
		gen.writeStartArray();
		
		for(Entry<?, ?> entry : map.entrySet()) {
			gen.writeStartObject();
				appendObject(gen, "key", entry.getKey());
				appendObject(gen, "value", entry.getValue());

				appendObject(gen, "key.class", entry.getKey() == null ? "" : entry.getKey().getClass().getName());
				appendObject(gen, "value.class", entry.getValue() == null ? "" : entry.getValue().getClass().getName());
			gen.writeEndObject();
		}
		
		gen.writeEndArray();
	}

	private static final ReflectionFactory FACTORTY = 
			AccessController.doPrivileged(new ReflectionFactory.GetReflectionFactoryAction());
	
	private class ObjectDescriptor {
		private Map<String, Object> fieldValues;
		private IJsonSerializable instance;
		private ObjectDescriptor parent;
		private byte[] transientData;
		private Class<?> clazz;
		
		public ObjectDescriptor(IJsonSerializable instance) { this(instance.getClass(), instance); }
		@SuppressWarnings("rawtypes")
		public ObjectDescriptor(Class<?> clazz, IJsonSerializable instance) {
			this.clazz = clazz;
			this.instance = instance;
			this.fieldValues = new HashMap<>();
			
			try {
				transientData = instance.serializeObject(JsonSerializer.this);
				
				for(Field field : clazz.getDeclaredFields()) {
					field.setAccessible(true);
					if(Modifier.isTransient(field.getModifiers())) continue;
					
					Object data = field.get(instance);
					if(data != null) {
						Class<?> dataClass = data.getClass();
						
						if(dataClass.isPrimitive()) {
							data = classifyPrimitive(data);
						
						} else if(dataClass.isArray() || data instanceof List<?>) {
							Object[] array;
							
							if(data instanceof List<?>) {
								array = ((List) data).toArray();
							
							} else {
								array = new Object[Array.getLength(data)];
								for(int i = 0; i < array.length; i ++)
									array[i] = Array.get(data, i);
							}
							
							data = array;
							
						} else if(data instanceof IJsonSerializable) {
							data = lookupId((IJsonSerializable) data);
						}
					}
					
					fieldValues.put(field.getName(), data);
				}
			} catch(IllegalArgumentException | IllegalAccessException e) { e.printStackTrace(); }
			
			Class<?> parentClass = clazz.getSuperclass();
			if(parentClass == null || !IJsonSerializable.class.isAssignableFrom(parentClass)) return;
			parent = new ObjectDescriptor(parentClass, instance);
		}
		
		public void appendJSON(JsonGenerator gen) throws JsonGenerationException, IOException {
			gen.writeStartObject();
				for(Entry<String, Object> entry : fieldValues.entrySet())
					appendObject(gen, entry.getKey(), entry.getValue());
				
				gen.writeStringField("class", clazz.getName());
				
				if(transientData != null) 
					gen.writeStringField("transient", Base64.getEncoder().encodeToString(transientData));
				
				gen.writeFieldName("super");
				if(parent != null) parent.appendJSON(gen);
				else gen.writeNull();
				
			gen.writeEndObject();
		}
		
		@SuppressWarnings("unchecked")
		public ObjectDescriptor(Map<String, Object> dataMap) {
			if(!dataMap.containsKey("class")) return;
			if(!dataMap.containsKey("super")) return;
			
			try {
				clazz = Class.forName((String) dataMap.get("class"));
				Map<String, Object> parentData = (Map<String, Object>) dataMap.get("super");
				if(parentData != null) parent = new ObjectDescriptor(parentData);
				
				fieldValues = new HashMap<>();
				for(Entry<String, Object> entry : dataMap.entrySet()) 
					fieldValues.put(entry.getKey(), entry.getValue());
				
				fieldValues.remove("class");
				fieldValues.remove("super");

				if(dataMap.containsKey("transient"))
					transientData = Base64.getDecoder().decode((String) dataMap.get("transient"));
				fieldValues.remove("transient");
				
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		public IJsonSerializable createInstance() {
			Class<?> parentClass = clazz.getSuperclass();
			ObjectDescriptor rootParent = parent;
			
			while(rootParent != null) {
				parentClass = rootParent.clazz.getSuperclass();
				rootParent = rootParent.parent;
			}
			
			try {
				Constructor<?> constructor = parentClass.getDeclaredConstructor();
				constructor = FACTORTY.newConstructorForSerialization(clazz, constructor);
				constructor.setAccessible(true);
				
				return this.instance = (IJsonSerializable) constructor.newInstance();
				
			} catch(IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) { 
				e.printStackTrace(); 
				return null; 
			}
		}
		
		public void parseInstance() {
			try {
				ObjectDescriptor descriptor = this; do {
					for(Field field : descriptor.clazz.getDeclaredFields()) {
						if(!descriptor.fieldValues.containsKey(field.getName())) continue;
						
						Object value = descriptor.fieldValues.get(field.getName());
						Class<?> type = field.getType();
						field.setAccessible(true);
						
						field.set(instance, parseObject(value, type));
					}
				} while((descriptor = descriptor.parent) != null);
			} catch(IllegalArgumentException | IllegalAccessException e) { 
				e.printStackTrace(); 
			}
		}
		
		public void postLoad() {
//			if(loadDependencies == null) throw new IllegalStateException("Instance was not loaded by Deserializer");
			((IJsonSerializable) instance).deserializeObject(JsonSerializer.this, transientData);
		}
	}
	
	private static Object classifyPrimitive(Object primitive) {
		if(primitive instanceof Byte) return new Long((Byte) primitive);
		if(primitive instanceof Short) return new Long((Short) primitive);
		if(primitive instanceof Integer) return new Long((Integer) primitive);
		if(primitive instanceof Long) return new Long((Long) primitive);

		if(primitive instanceof Float) return new Double((Float) primitive);
		if(primitive instanceof Double) return new Double((Double) primitive);

		if(primitive instanceof Boolean) return new Boolean((Boolean) primitive);
		if(primitive instanceof Character) return new Character((Character) primitive);
		
		throw new IllegalArgumentException(primitive.getClass().getSimpleName() + " is not recognized");
	}
	
	private static Object specifyPrimitive(Object obj, Class<?> type) {
		if(type == byte.class 	|| type == Byte.class) 		return ((Long) obj).byteValue();
		if(type == short.class 	|| type == Short.class) 	return ((Long) obj).shortValue();
		if(type == int.class 	|| type == Integer.class) 	return ((Long) obj).intValue();
		if(type == long.class 	|| type == Long.class) 		return ((Long) obj).longValue();
                                     
		if(type == float.class 	|| type == Float.class) 	return ((Double) obj).floatValue();
		if(type == double.class || type == Double.class) 	return ((Double) obj).doubleValue();
                                     
		if(type == boolean.class || type == Boolean.class) 	 return ((Boolean) obj).booleanValue();
		if(type == char.class 	 || type == Character.class) return ((Character) obj).charValue();
		
		throw new IllegalArgumentException(type.getSimpleName() + " is not recognized");
	}
	
	private static boolean isClassifyablePrimitive(Object primitive) {
		return  primitive instanceof Byte 		|| primitive instanceof Short		|| 
				primitive instanceof Integer 	|| primitive instanceof Long 		||
				primitive instanceof Float 		|| primitive instanceof Double 		||
				primitive instanceof Boolean 	|| primitive instanceof Character	;
	}
	
	private static boolean isClassifyablePrimitive(Class<?> primitive) {
		return  primitive == byte.class 	|| primitive == short.class	 || 
				primitive == int.class 		|| primitive == long.class 	 ||
				primitive == float.class 	|| primitive == double.class ||
				primitive == boolean.class 	|| primitive == char.class	 ||
				
				primitive == Byte.class 	|| primitive == Short.class		|| 
				primitive == Integer.class 	|| primitive == Long.class 		||
				primitive == Float.class 	|| primitive == Double.class 	||
				primitive == Boolean.class 	|| primitive == Character.class	;
	}
}
