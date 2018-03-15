package managment.storage.serialization.json;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Queue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;

public class JsonStringGenerator {
	public  static final JsonFactory FACTORY = new JsonFactory();
	private static final Queue<JsonStringGenerator> WRITER_POOL = new LinkedList<>();
	
	public static JsonStringGenerator getInstance() {
		if(WRITER_POOL.isEmpty()) 
			try { new JsonStringGenerator(); } 
			catch(IOException e) { }
		
		return WRITER_POOL.poll().prep();
	}
	
	private boolean enabled;
	private StringWriter writer;
	private JsonGenerator generator;
	
	private JsonStringGenerator() throws IOException { 
		this.writer = new StringWriter();
		this.generator = FACTORY.createGenerator(writer);

		WRITER_POOL.offer(this);
		enabled = false;
	}

	private JsonStringGenerator prep() {
		writer.getBuffer().setLength(0);
		enabled = true;
		return this;
	}
	
	public String generate() {
		enabled = false;
		return writer.toString();
	}

//	-------------------------------------- ------------------ ----------------------------------------- \\
//	-------------------------------------- Delegation Methods ----------------------------------------- \\
//	-------------------------------------- ------------------ ----------------------------------------- \\
	
	private JsonGenerator getGenerator() {
		if(!enabled) throw new IllegalStateException("Generator is not in use or illegally obtained!"
				+ " Discard Instance and call JsonStringGenerator.getInstance() for usable instance");
		return generator;
	}

	public JsonStringGenerator writeStartObject() throws IOException, JsonGenerationException { getGenerator().writeStartObject(); return this; }
	public JsonStringGenerator writeEndObject() throws IOException, JsonGenerationException { getGenerator().writeEndObject(); return this; }
	
	public JsonStringGenerator writeStartArray() throws IOException, JsonGenerationException { getGenerator().writeStartArray(); return this; }
	public JsonStringGenerator writeEndArray() throws IOException, JsonGenerationException { getGenerator().writeEndArray(); return this; }
	
	public JsonStringGenerator writeFieldName(String name) throws IOException, JsonGenerationException { getGenerator().writeFieldName(name); return this; }
	public JsonStringGenerator writeFieldName(SerializableString name) throws IOException, JsonGenerationException { getGenerator().writeFieldName(name); return this; }
	
	public JsonStringGenerator writeString(String text) throws IOException, JsonGenerationException { getGenerator().writeString(text); return this; }
	public JsonStringGenerator writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException { getGenerator().writeString(text, offset, len); return this; }
	public JsonStringGenerator writeString(SerializableString text) throws IOException, JsonGenerationException { getGenerator().writeString(text); return this; }
	public JsonStringGenerator writeUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException { getGenerator().writeUTF8String(text, offset, length); return this; }
	
	public JsonStringGenerator writeStringField(String fieldName, String value) throws IOException, JsonGenerationException { getGenerator().writeStringField(fieldName, value); return this; }
	
	public JsonStringGenerator writeRawUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException { getGenerator().writeRawUTF8String(text, offset, length); return this; }
	public JsonStringGenerator writeRaw(String text) throws IOException, JsonGenerationException { getGenerator().writeRaw(text); return this; }
	public JsonStringGenerator writeRaw(String text, int offset, int len) throws IOException, JsonGenerationException { getGenerator().writeRaw(text, offset, len); return this; }
	public JsonStringGenerator writeRaw(char[] text, int offset, int len) throws IOException, JsonGenerationException { getGenerator().writeRaw(text, offset, len); return this; }
	public JsonStringGenerator writeRaw(char c) throws IOException, JsonGenerationException { getGenerator().writeRaw(c); return this; }
	public JsonStringGenerator writeRaw(SerializableString raw) throws IOException, JsonGenerationException { getGenerator().writeRaw(raw); return this; }
	
	public JsonStringGenerator writeRawValue(String text) throws IOException, JsonGenerationException { getGenerator().writeRawValue(text); return this; }
	public JsonStringGenerator writeRawValue(String text, int offset, int len) throws IOException, JsonGenerationException { getGenerator().writeRawValue(text, offset, len); return this; }
	public JsonStringGenerator writeRawValue(char[] text, int offset, int len) throws IOException, JsonGenerationException { getGenerator().writeRawValue(text, offset, len); return this; }
	
	public JsonStringGenerator writeNumber(int v) throws IOException, JsonGenerationException { getGenerator().writeNumber(v); return this; }
	public JsonStringGenerator writeNumber(long v) throws IOException, JsonGenerationException { getGenerator().writeNumber(v); return this; }
	public JsonStringGenerator writeNumber(BigInteger v) throws IOException, JsonGenerationException { getGenerator().writeNumber(v); return this; }
	public JsonStringGenerator writeNumber(double d) throws IOException, JsonGenerationException { getGenerator().writeNumber(d); return this; }
	public JsonStringGenerator writeNumber(float f) throws IOException, JsonGenerationException { getGenerator().writeNumber(f); return this; }
	public JsonStringGenerator writeNumber(BigDecimal dec) throws IOException, JsonGenerationException { getGenerator().writeNumber(dec); return this; }
	public JsonStringGenerator writeNumber(String encodedValue)throws IOException, JsonGenerationException, UnsupportedOperationException {getGenerator().writeNumber(encodedValue); return this; }

	public JsonStringGenerator writeNumberField(String fieldName, int value) throws IOException, JsonGenerationException { getGenerator().writeNumberField(fieldName, value); return this; }
	public JsonStringGenerator writeNumberField(String fieldName, long value) throws IOException, JsonGenerationException { getGenerator().writeNumberField(fieldName, value); return this; }
	public JsonStringGenerator writeNumberField(String fieldName, double value) throws IOException, JsonGenerationException { getGenerator().writeNumberField(fieldName, value); return this; }
	public JsonStringGenerator writeNumberField(String fieldName, float value) throws IOException, JsonGenerationException { getGenerator().writeNumberField(fieldName, value); return this; }
	public JsonStringGenerator writeNumberField(String fieldName, BigDecimal value) throws IOException, JsonGenerationException { getGenerator().writeNumberField(fieldName, value); return this; }
	
	public JsonStringGenerator writeBoolean(boolean state) throws IOException, JsonGenerationException { getGenerator().writeBoolean(state); return this; }
	public JsonStringGenerator writeBooleanField(String fieldName, boolean value) throws IOException, JsonGenerationException { getGenerator().writeBooleanField(fieldName, value); return this; }
	
	public JsonStringGenerator writeNull() throws IOException, JsonGenerationException { getGenerator().writeNull(); return this; }
	public JsonStringGenerator writeNullField(String fieldName) throws IOException, JsonGenerationException { getGenerator().writeNullField(fieldName); return this; }
	
	public JsonStringGenerator writeTree(TreeNode rootNode) throws IOException, JsonProcessingException { getGenerator().writeTree(rootNode); return this; }
}
