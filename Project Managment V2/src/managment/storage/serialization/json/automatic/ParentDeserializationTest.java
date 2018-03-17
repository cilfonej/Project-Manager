package managment.storage.serialization.json.automatic;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class ParentDeserializationTest {

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
//		try {
//			ReflectionFactory fact = AccessController.doPrivileged(new ReflectionFactory.GetReflectionFactoryAction());
//			Constructor<?> constructor = Parent.class.getDeclaredConstructors()[0];
//			Child child = (Child) fact.newConstructorForSerialization((Class<?>) Child.class, constructor).newInstance();
//			
//			System.out.println(child.i);
//		} catch(InstantiationException | IllegalAccessException | IllegalArgumentException
//				| InvocationTargetException | SecurityException e) {
//			e.printStackTrace();
//		}
		
//		try {
//			Class<?> clazz = Child.class;
//			Field filed = clazz.getDeclaredField("a");
//			filed.setAccessible(true);
//			
//			Child c = new Child(6);
//			Object array = Array.newInstance(float.class, 5);
//			Array.set(array, 0, 5f);
//			Array.set(array, 1, (Object) new Float(4));
////			Array.set(array, 2, new Double(3));
//			
//			filed.set(c, array);
//			System.out.println(Arrays.toString(c.a));
//		} catch(IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException e) {
//			e.printStackTrace();
//		}
//		
//		System.exit(0);
		
		try {
			StringWriter write = new StringWriter();
			JsonGenerator gen = new JsonFactory().createGenerator(write).useDefaultPrettyPrinter();
			JsonSerializer json = new JsonSerializer();
			
			Child child = new Child(100);
			
			Child friend = new Child(500);
			child.friends.add(friend);
			friend.friends.add(child);
			
//			gen.writeStartObject();
//			json.appendObject(gen, "child", child);
//			gen.writeEndObject();
			
			int id = json.lookupId(child);
			gen.writeStartObject();
			json.appendJSON(gen);
			gen.writeEndObject();
			
			gen.flush();
			System.out.println(write.toString());
//			Map<String, Object> data = json.parseJSON(new JsonFactory().createParser(write.toString()));
			
			JsonSerializer deserializer = new JsonSerializer();
			deserializer.loadJSON(new JsonFactory().createParser(write.toString()));
			Child c = (Child) deserializer.get(id);
			
//			System.out.println(write.toString());
//			System.out.println(data);
//			Child c = (Child) json.parseObject(((Map) data.get(null)).get("child"), Child.class);
			System.out.println(c);
			
		} catch(IOException e) { e.printStackTrace(); }
		
	}

	public static class Parent implements IJsonSerializable {
		protected int field;
		private ArrayList<String> memos;
		protected Child child;
		
		protected Parent() {
			field = 5;
			System.out.println("Parent::Constructor");
			
			memos = new ArrayList<>();
			memos.add("Hello");
			memos.add("World");
			memos.add("Meeeeeeemeeeess");
			memos.add("Fudge");
			memos.add("Your");
			memos.add("Rules");
		}

		public int getField() {
			return field;
		}
	}

	public static class Child extends Parent implements IJsonSerializable {
		protected int i;
		private float[] a;
		private HashMap<Integer, String> map;
		private long time;
		
		public ArrayList<Child> friends;
		
		private transient String name;
		private transient Parent parent;
		private int parentID;

		public Child(int i) {
			this.i = i;
			a = new float[]{ 0.1f, 10, 100, -.1f };
			System.out.println("Child::Constructor");
			
			map = new HashMap<>();
			map.put(5, "Five");
			map.put(10, "Ten");
			map.put(3, "Three");
			map.put(1, "One");
			
			time = System.nanoTime();
			name = time % 2 == 0 ? "Bob" : "Steve";
			
			parent = new Parent();
			friends = new ArrayList<>();
			
			parent.child = this;
		}
		
		public void deserializeObject(JsonSerializer json, byte[] data) {
			name = new String(data);
			parent = (Parent) json.get(parentID);
//			parent = new Parent();
		}
		
		public byte[] serializeObject(JsonSerializer json) {
			parentID = json.lookupId(parent);
			return name.getBytes();
		}

		public int getI() {
			return i;
		}
	}
}