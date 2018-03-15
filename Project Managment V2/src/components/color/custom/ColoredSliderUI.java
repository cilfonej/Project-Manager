package components.color.custom;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;

import components.color.ColorLookupTable;

public class ColoredSliderUI extends BasicSliderUI {
	public static ComponentUI createUI(JComponent c) { return new ColoredSliderUI((JSlider) c); }
	public static final int THUMB_SIZE = 15;
	
	private Handler handler;
	
	private boolean tickRecalcNeeded;
	private float barHeight = 1;
	
	private Rectangle thumbRect;
	private int dispValue;
	
	public ColoredSliderUI(JSlider slider) {
		super(slider);
		this.thumbRect = new Rectangle();
//		
//		installDefaults(slider);
//		installListeners(slider);
//		
//		calculateThumbSize();
	}
	
	protected void calculateThumbSize() {
		super.calculateThumbSize();
		thumbRect.setSize(THUMB_SIZE, slider.getHeight());
	}
	
	public void paint(Graphics g, JComponent c) {
		if(handler.grabPoint == null)
			dispValue = slider.getValue();
		
		if(tickRecalcNeeded) calculateTickSpacing();
		
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(c.getBackground());
		g2d.fillRect(0, 0, c.getWidth(), c.getHeight());
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setBackground(c.getBackground().darker().darker());
			
		float width = c.getWidth() - thumbRect.width;
		float height = (int) (c.getHeight() * barHeight);
		float y = (int) (c.getHeight() * (1 - barHeight) / 2);

		Shape clip = g2d.getClip();
//		g2d.setClip(new Rectangle2D.Float(strokWidth, y + strokWidth, c.getWidth() - strokWidth * 2, height - strokWidth * 2));
		
		paintProgress(g2d, 0, y, width, height);
		paintThumb(g2d);
		paintTicks(g2d, y, height);
		
		g2d.setClip(clip);

//		paintBorder(g2d, 0, y, c.getWidth(), height);
		
		Border border = c.getBorder();
		if(border != null) border.paintBorder(c, g2d, 0, 0, c.getWidth(), c.getHeight());
		g2d.dispose();
	}
	
	public void paintThumb(Graphics2D g2d) {
		ColoredSliderUI.this.calculateThumbSize();
		
		int value = (dispValue - slider.getMinimum());
		float useSliderWidth = slider.getWidth() - thumbRect.width;
		double range = slider.getMaximum() - slider.getMinimum();
		double pixlePerUnit = range / useSliderWidth;
		double x = value / pixlePerUnit;
		thumbRect.x = (int) x;
		
		g2d.setColor(ColorLookupTable.BORDER_PRIMARY); 
		g2d.fill(thumbRect);
	}
	
	public void paintProgress(Graphics2D g2d, float x, float y, float width, float height) {
		float progress = (float) (dispValue - slider.getMinimum()) / (slider.getMaximum() - slider.getMinimum());
		Rectangle2D progressRect = new Rectangle2D.Float(x, y, width * progress, height);
		
		g2d.setColor(slider.getForeground().darker());
		g2d.fill(progressRect);
	}
	
//	public void paintBorder(Graphics2D g2d, float x, float y, float width, float height) {
//		g2d.setColor(Color.DARK_GRAY);
//		g2d.setStroke(new BasicStroke(strokWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
//		g2d.draw(new Rectangle2D.Float(x + strokWidth, y + strokWidth, width - strokWidth * 2, height - strokWidth * 2));
//	}
	
	public void paintTicks(Graphics2D g2d, float offsetY, float limitY) {
		float x = 0;
		float baseY = limitY + offsetY;

		float useSliderWidth = slider.getWidth() - thumbRect.width;
		float duration = slider.getMaximum() - slider.getMinimum();
		float xShift = useSliderWidth / duration * slider.getMinorTickSpacing();
		int majorTickDivisor = (int) ((float) slider.getMajorTickSpacing() / slider.getMinorTickSpacing());
		
		float minorHight = baseY - 3/10f * limitY;
		float majorHight = baseY - 6/10f * limitY;
		
		float cutOff = (float) thumbRect.getMaxX();
		g2d.setStroke(new BasicStroke(1));
		
		x += thumbRect.getWidth() / 2;
		
		Color dark = ColorLookupTable.mix(Color.BLACK, ColorLookupTable.BACKGROUND_COLOR, .25f);
		Color light = ColorLookupTable.mix(Color.WHITE, ColorLookupTable.FOREGROUND_COLOR, .25f);
		
		for(int i = 0; x < useSliderWidth + xShift; i ++, x += xShift) {
			Line2D mark = new Line2D.Float(x, baseY, x, i % majorTickDivisor == 0 ? majorHight : minorHight);
			g2d.setColor(x > cutOff ? dark : light);
			g2d.draw(mark);
		}
	}
	
	private void calculateTickSpacing() {
		int width = slider.getWidth();
		int count = slider.getMaximum() - slider.getMinimum();
	
		slider.setMajorTickSpacing(findSpacer(THUMB_SIZE * 4, width, count, 5, 10, 25, 50));
		slider.setMinorTickSpacing(findSpacer(THUMB_SIZE / 2, width, count, 1, 2, 5, 10));
	}
	
	private int findSpacer(float minBound, float width, int count, int... trials) {
		for(int div : trials) {
			if(width / (count / div) > minBound)
				return div;
		}
		
		
		return trials[trials.length - 1];
	}
	
	protected void installDefaults(JSlider slider) {
		super.installDefaults(slider);
		
		calculateTickSpacing();
		slider.setBorder(ColorLookupTable.PLAIN_BORDER);
	}
	
	protected void installListeners(JSlider slider) {
		slider.addMouseListener(getHandler());
		slider.addMouseMotionListener(getHandler());
		slider.addComponentListener(getHandler());
	}
	
	protected void uninstallListeners(JSlider slider) {
		slider.removeMouseListener(getHandler());
		slider.removeMouseMotionListener(getHandler());
		slider.removeComponentListener(getHandler());
	}
		
	protected Handler getHandler() {
		if(handler == null)
			handler = new Handler();
		return handler;
	}
		
	private class Handler implements MouseInputListener, ComponentListener {
		private Point2D grabPoint;

		public void mousePressed(MouseEvent e) {
			if(grabPoint == null) {
				if(!thumbRect.contains(e.getPoint())) return;
				grabPoint = new Point2D.Float(e.getX() - thumbRect.x, e.getY());
				slider.repaint();
				
				slider.firePropertyChange("dragging", false, true);
			}
		}
		
		public void mouseReleased(MouseEvent e) { 
			grabPoint = null; 
			slider.setValue(dispValue);
			slider.repaint();
			
			slider.firePropertyChange("dragging", true, false);
		}
		
		public void mouseDragged(MouseEvent e) {
			if(grabPoint == null)
				return;
			
			float useSliderWidth = slider.getWidth() - thumbRect.width;
			
			double x = e.getX() - grabPoint.getX();
			if(x < 0) x = 0; else if(x > useSliderWidth) x = useSliderWidth;
			
			double range = slider.getMaximum() - slider.getMinimum();
			double pixlePerUnit = range / useSliderWidth;
			int value = slider.getMinimum() + (int)(pixlePerUnit * x);
			
			thumbRect.x = (int) x;

			slider.setValue(dispValue);
			slider.firePropertyChange("displayValue", dispValue, value);
			dispValue = value;
			
			slider.repaint();
		}
		
		public void componentResized(ComponentEvent e) {
			calculateTickSpacing();
		}

		public void mouseClicked(MouseEvent e) { }
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }
		public void mouseMoved(MouseEvent e) { }

		public void componentMoved(ComponentEvent e) { }
		public void componentShown(ComponentEvent e) { }
		public void componentHidden(ComponentEvent e) { }
	}
}
