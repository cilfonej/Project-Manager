package components.color.picker;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

public class ColorSamplePanelUI extends PanelUI {
	public void paint(Graphics g, JComponent c) {
		Color color = c.getBackground();
		color = new Color(color.getRGB(), false);
		
		int width = c.getWidth();
		int height = c.getHeight();
		
		g.setColor(color);
		g.fill3DRect(0, 0, width, height, true);
	}
}
