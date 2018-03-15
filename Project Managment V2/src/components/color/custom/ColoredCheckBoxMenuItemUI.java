package components.color.custom;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

public class ColoredCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {
	public static ComponentUI createUI(JComponent c) { return new ColoredCheckBoxMenuItemUI(); }

	private static final Icon NORMAL_ICON = new ImageIcon(ColoredCheckBoxMenuItemUI.class.getResource("CheckBlank.png"));
	private static final Icon CHECKED_ICON = new ImageIcon(ColoredCheckBoxMenuItemUI.class.getResource("CheckChecked.png"));
	
	protected void installDefaults() {
		super.installDefaults();
		checkIcon = null;

		JCheckBoxMenuItem box = (JCheckBoxMenuItem) menuItem;
		box.setIcon(new CheckIcon());
		
		box.setContentAreaFilled(false);
		box.setBackground(new Color(255, 255, 255, 0));
		box.setOpaque(false);
	}
	
	private static class CheckIcon implements Icon {
		public void paintIcon(Component c, Graphics g, int x, int y) {
			JCheckBoxMenuItem box = (JCheckBoxMenuItem) c;
			if(box.isSelected()) CHECKED_ICON.paintIcon(c, g, x, y);
			else NORMAL_ICON.paintIcon(c, g, x, y);
		}

		public int getIconWidth() { return NORMAL_ICON.getIconWidth(); }
		public int getIconHeight() { return NORMAL_ICON.getIconHeight(); }
	}
}
