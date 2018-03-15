package managment.storage.serialization;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SerializationUtil {
	public static void serialize(String value, Offset offset, byte[] data) {
		serialize(value.length(), offset, data);
		System.arraycopy(value.getBytes(), 0, data, offset.get(), value.length()); 
		offset.add(value.length());
	}
	
	public static void serialize(long value, Offset offset, byte[] data) {
		for(int i = 7; i >= 0; i --) {
			data[offset.get() + (7 - i)] = (byte) (value >> i * 8 & 0xFF);
		}
		
		offset.add(8);
	}
	
	public static void serialize(int value, Offset offset, byte[] data) {
		for(int i = 3; i >= 0; i --) {
			data[offset.get() + (3 - i)] = (byte) (value >> i * 8 & 0xFF);
		}

		offset.add(4);
	}
	
	public static void serialize(float value, Offset offset, byte[] data) {
		serialize(Float.floatToIntBits(value), offset, data);
	}
	
	public static void serialize(LocalDateTime value, Offset offset, byte[] data) {
		serialize(value.atZone(ZoneId.systemDefault()).toEpochSecond(), offset, data);
	}
	
	public static String deserializeString(Offset offset, byte[] data) { 
		int length = deserializeInt(offset, data); 
		String s = new String(data, offset.get(), length);
		offset.add(length);
		return s;
	}
	
	public static long deserialize(Offset offset, byte[] data, int strid) {
		if(strid > 8) throw new IllegalArgumentException("Strid must be <= 8, not " + strid);
		long value = 0;
		
		for(int i = 0; i < strid; i ++) {
			value <<= 8;
			value |= data[offset.get() + i] & 0xFF;
		}
		
		offset.add(strid);
		return value;
	}
	
	public static long deserializeLong(Offset offset, byte[] data) { return deserialize(offset, data, 8); }
	public static int deserializeInt(Offset offset, byte[] data) { return (int) deserialize(offset, data, 4); }
	public static float deserializeFloat(Offset offset, byte[] data) { return Float.intBitsToFloat(deserializeInt(offset, data)); }
	
	public static LocalDateTime deserializeDateTime(Offset offset, byte[] data) { 
		return Instant.ofEpochSecond(SerializationUtil.deserialize(offset, data, 8))
				.atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
}
