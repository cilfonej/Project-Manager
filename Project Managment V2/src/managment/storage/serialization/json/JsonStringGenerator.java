package managment.storage.serialization.json;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Queue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;

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
		this.generator = FACTORY.createGenerator(writer).useDefaultPrettyPrinter();

		WRITER_POOL.offer(this);
		enabled = false;
	}

	private JsonStringGenerator prep() {
		writer.getBuffer().setLength(0);
		enabled = true;
		
		writeStartObject();
		return this;
	}
	
	public String generate() {
		writeEndObject();
		
		enabled = false;
		
		try { generator.flush(); } 
		catch (IOException e) { }
		
		WRITER_POOL.offer(this);
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

	public JsonStringGenerator writeStartObject() { try { getGenerator().writeStartObject(); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeEndObject() { try { getGenerator().writeEndObject(); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	
	public JsonStringGenerator writeStartArray() { try { getGenerator().writeStartArray(); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeEndArray() { try { getGenerator().writeEndArray(); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	
	public JsonStringGenerator writeFieldName(String name) { try { getGenerator().writeFieldName(name); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeFieldName(SerializableString name) { try { getGenerator().writeFieldName(name); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	
	public JsonStringGenerator writeString(String text) { try { getGenerator().writeString(text); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeString(char[] text, int offset, int len) { try { getGenerator().writeString(text, offset, len); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeString(SerializableString text) { try { getGenerator().writeString(text); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeUTF8String(byte[] text, int offset, int length) { try { getGenerator().writeUTF8String(text, offset, length); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	
	public JsonStringGenerator writeStringField(String fieldName, String value) { try { getGenerator().writeStringField(fieldName, value); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	
	public JsonStringGenerator writeRawUTF8String(byte[] text, int offset, int length) { try { getGenerator().writeRawUTF8String(text, offset, length); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeRaw(String text) { try { getGenerator().writeRaw(text); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeRaw(String text, int offset, int len) { try { getGenerator().writeRaw(text, offset, len); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeRaw(char[] text, int offset, int len) { try { getGenerator().writeRaw(text, offset, len); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeRaw(char c) { try { getGenerator().writeRaw(c); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeRaw(SerializableString raw) { try { getGenerator().writeRaw(raw); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	
	public JsonStringGenerator writeRawValue(String text) { try { getGenerator().writeRawValue(text); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeRawValue(String text, int offset, int len) { try { getGenerator().writeRawValue(text, offset, len); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeRawValue(char[] text, int offset, int len) { try { getGenerator().writeRawValue(text, offset, len); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	
	public JsonStringGenerator writeNumber(int v) { try { getGenerator().writeNumber(v); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNumber(long v) { try { getGenerator().writeNumber(v); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNumber(BigInteger v) { try { getGenerator().writeNumber(v); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNumber(double d) { try { getGenerator().writeNumber(d); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNumber(float f) { try { getGenerator().writeNumber(f); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNumber(BigDecimal dec) { try { getGenerator().writeNumber(dec); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNumber(String encodedValue)throws IOException, UnsupportedOperationException {try { getGenerator().writeNumber(encodedValue); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }

	public JsonStringGenerator writeNumberField(String fieldName, int value) { try { getGenerator().writeNumberField(fieldName, value); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNumberField(String fieldName, long value) { try { getGenerator().writeNumberField(fieldName, value); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNumberField(String fieldName, double value) { try { getGenerator().writeNumberField(fieldName, value); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNumberField(String fieldName, float value) { try { getGenerator().writeNumberField(fieldName, value); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNumberField(String fieldName, BigDecimal value) { try { getGenerator().writeNumberField(fieldName, value); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	
	public JsonStringGenerator writeBoolean(boolean state) { try { getGenerator().writeBoolean(state); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeBooleanField(String fieldName, boolean value) { try { getGenerator().writeBooleanField(fieldName, value); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	
	public JsonStringGenerator writeNull() { try { getGenerator().writeNull(); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	public JsonStringGenerator writeNullField(String fieldName) { try { getGenerator().writeNullField(fieldName); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
	
	//public JsonStringGenerator writeTree(TreeNode rootNode) throws IOException, JsonProcessingException { try { getGenerator().writeTree(rootNode); } catch(IOException e) { throw new IllegalArgumentException(e); } return this; }
}
