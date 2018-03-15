package components.color.picker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class ColorSliderUI extends BasicSliderUI {
	public static interface ColoringFunction { public Color color(float percent); }
	
	private static final int EDGE = 1;
	
	private ColoringFunction function;
	private BufferedImage image;
	private TexturePaint paint;
	
	public ColorSliderUI(JSlider slider, ColoringFunction function, int res) {
		super(slider);
		
		this.function = function;
		image = new BufferedImage(res, 1, BufferedImage.TYPE_INT_ARGB);
	}

	public Dimension getMaximumSize(JComponent c) { return getPreferredSize(c); }
	public Dimension getMinimumSize(JComponent c) { return getPreferredSize(c); }
	
	public Dimension getPreferredSize(JComponent c) {
		return super.getPreferredHorizontalSize();
	}

	protected void calculateThumbSize() {
		thumbRect.setSize(getThumbSize().width, trackRect.height);
	}
	
	protected void calculateTrackRect() {
		trackRect.setLocation(EDGE, EDGE);
		trackRect.setSize(slider.getWidth() - EDGE*2, slider.getHeight() - EDGE*2);
	}
	
	public void paint(Graphics g, JComponent c) {
		updatePaint();
		Graphics2D g2d = (Graphics2D) g;
		
		int width = c.getWidth();
		int height = c.getHeight();

		g2d.setColor(Color.BLACK);
		g2d.fill(new Rectangle2D.Float(0, 0, width, height));
		
		g2d.setPaint(paint);
		g2d.fill(trackRect);
	
		g2d.setColor(Color.BLACK);
		g2d.fillPolygon(new int[] { thumbRect.x, thumbRect.x + thumbRect.width, thumbRect.x + thumbRect.width / 2}, 
						new int[] { thumbRect.y, thumbRect.y, 					thumbRect.y + thumbRect.height / 3}, 3);
		
		g2d.setColor(Color.WHITE);
		int lowerHeight = thumbRect.y + thumbRect.height;
		g2d.fillPolygon(new int[] { thumbRect.x, thumbRect.x + thumbRect.width, thumbRect.x + thumbRect.width / 2}, 
						new int[] { lowerHeight, lowerHeight, 					lowerHeight - thumbRect.height / 3}, 3);
	
	}
	
	private void updatePaint() {
		Graphics2D g2d = image.createGraphics();
		int res = image.getWidth();
		
		for(int i = 0; i < res; i ++) {
			float p = (float) i / res;
			g2d.setColor(function.color(p));
			g2d.fillRect(i, 0, 1, 1);
		}
		
		g2d.dispose();
		this.paint = new TexturePaint(image, new Rectangle(0, 0, slider.getWidth(), slider.getHeight()));
	}
}
