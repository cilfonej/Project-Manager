package managment.storage.serialization;

import managment.storage.SerializationContext;

public interface ISerializable {
	public byte[] serialize();
	public void deserialize(byte[] data);
	
	public SerializationContext getContext();
}
