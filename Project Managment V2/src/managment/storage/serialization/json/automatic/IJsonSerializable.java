package managment.storage.serialization.json.automatic;

public interface IJsonSerializable {
	public default void deserializeObject(JsonSerializer json, byte[] data) { }
	public default byte[] serializeObject(JsonSerializer json) { return null; }
}
