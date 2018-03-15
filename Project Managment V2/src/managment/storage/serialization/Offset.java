package managment.storage.serialization;

public class Offset {
	private int offset;
	
	public Offset() { }
	public Offset(int offset) { set(offset); }
	
	public int get() { return offset; }
	public void set(int offset) { this.offset = offset; }
	public void add(int offset) { this.offset += offset; }
}
