package components.color.custom;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import components.color.ColorLookupTable;

public class ColoredSplitPaneUI extends BasicSplitPaneUI {
	public static ComponentUI createUI(JComponent c) { return new ColoredSplitPaneUI(); }
	
	public BasicSplitPaneDivider createDefaultDivider() {
		return new Divider(this);
	}
	
	private static class Divider extends BasicSplitPaneDivider {
		private static final long serialVersionUID = 8826859395080877376L;
		
		public Divider(BasicSplitPaneUI ui) { super(ui); 
//			Color highlight = ColorLookupTable.brighter(BACKGROUND_COLOR, .25f);
		
			super.setBorder(new EmptyBorder(1, 1, 1, 1));
			super.setBackground(ColorLookupTable.BACKGROUND_COLOR); //highlight);
//			super.setBorder(new CaplessBorder(new SoftBevelBorder(SoftBevelBorder.RAISED,
//					highlight, ColorLookupTable.darker(BACKGROUND_COLOR, .75f)
//				), c -> ui.getOrientation() != JSplitPane.VERTICAL_SPLIT));
		}
		
		public void setBorder(Border border) { }
		
		public void paint(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			
//			super.paint(g);
		}
	}
	
	public static class CaplessBorder implements Border {
		private Border border;
		private Predicate<Component> top;
		
		public CaplessBorder(Border border, Predicate<Component> top) {
			this.border = border;
			this.top = top;
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			if(top.test(c)) border.paintBorder(c, g, x, y - 50, width, height + 100);
			else	border.paintBorder(c, g, x - 50, y, width + 100, height);
		}

		public Insets getBorderInsets(Component c) { return border.getBorderInsets(c); }
		public boolean isBorderOpaque() { return border.isBorderOpaque(); }
	}
}