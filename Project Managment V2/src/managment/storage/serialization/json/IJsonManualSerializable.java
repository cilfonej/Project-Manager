package managment.storage.serialization.json;

import managment.storage.JsonSerializationContext;

public interface IJsonManualSerializable {
	public String serialize();
	public void deserialize(String data);
	
	public JsonSerializationContext getContext();
}
