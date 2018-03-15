package components.tree;

import javax.swing.Icon;

public class TreeNodeMetadata {
	private String name;
	private Icon icon;
	
	private boolean isContainer;
	
	public TreeNodeMetadata(String name, boolean isContainer) { this(name, null, isContainer); }
	public TreeNodeMetadata(String name, Icon icon, boolean isContainer) {
		this.name = name;
		this.icon = icon;
		this.isContainer = isContainer;
	}
	
	public String getName() { return name; }
	public Icon getIcon() { return icon; }
	public boolean isContainer() { return isContainer; }

	public void setName(String name) { this.name = name; }
	public void setIcon(Icon icon) { this.icon = icon; }
	public void setContainer(boolean isContainer) { this.isContainer = isContainer; }
}
