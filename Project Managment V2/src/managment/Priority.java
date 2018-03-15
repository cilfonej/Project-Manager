package managment;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import components.tree.ComponentTree;
import managment.icons.IconLoader;

public enum Priority {
	Pause, Low, Normal, High, Urgent;
	
	private Icon icon, large, desaturated;
	private Color primaryColor;
	
	private Priority() {
		this.icon = IconLoader.loadIcon(toString() + " Priority Icon.png", 16);
		this.large = IconLoader.loadIcon(toString() + " Priority Icon.png", ComponentTree.ROW_HEIGHT);
		this.desaturated = IconLoader.loadIcon(toString() + " Priority Icon Desaturated.png", ComponentTree.ROW_HEIGHT);
		
		this.primaryColor = new Color(((BufferedImage) ((ImageIcon) icon).getImage()).getRGB(4, 4));
	}
	
	public Icon getIcon() { return icon; }
	public Icon getLargeIcon() { return large; }
	public Icon getDesaturatedIcon() { return desaturated; }
	
	public Color getPirmaryColor() { return primaryColor; }
}
