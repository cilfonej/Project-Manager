package components.color.custom;

import java.awt.Color;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxUI;

public class ColoredCheckBoxUI extends BasicCheckBoxUI {
	public static ComponentUI createUI(JComponent c) { return new ColoredCheckBoxUI(); }

	private static final Icon NORMAL_ICON = new ImageIcon(ColoredCheckBoxUI.class.getResource("CheckBlank.png"));
	private static final Icon CHECKED_ICON = new ImageIcon(ColoredCheckBoxUI.class.getResource("CheckChecked.png"));
	
	protected void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		
		JCheckBox box = (JCheckBox) b;
		box.setIcon(NORMAL_ICON);
		box.setSelectedIcon(CHECKED_ICON);
		
		b.setContentAreaFilled(false);
		b.setBackground(new Color(255, 255, 255, 0));
		b.setOpaque(false);
	}
}
