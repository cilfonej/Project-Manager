package components.color.picker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

public class SVSelectorPanel extends JPanel implements MouseInputListener {
	private static final long serialVersionUID = 7075474267892645964L;
	
	private static final int EDGE = 1;
	private static final int GAP = 2;
	
	private Color color;
	private float hue, lastHue;
	private BufferedImage image;
	
	public SVSelectorPanel() {
		addMouseListener(this);
		addMouseMotionListener(this);
		
		lastHue = -1;
		setPreferredSize(new Dimension(471, 471));
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		if(color == null) return;
		if(image == null || lastHue != hue) {
			updateHueMap();
		}
		
		g2d.drawImage(image, EDGE, EDGE, null);
		
		float[] parts = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		int sat = (int) ((1 - parts[1]) * image.getWidth());
		int val = (int) ((1 - parts[2]) * image.getHeight());

		g2d.setStroke(new BasicStroke(GAP * 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

		g2d.setColor(Color.WHITE);
		g2d.drawLine(sat, EDGE, sat, val - GAP);		
		g2d.drawLine(EDGE, val, sat - GAP, val);	
		g2d.drawLine(sat, EDGE + val + GAP, sat, image.getHeight() + EDGE);
		g2d.drawLine(EDGE + sat + GAP, val, image.getWidth() + EDGE, val);
		
		g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

		g2d.setColor(Color.BLACK);
		g2d.drawLine(sat, EDGE, sat, val - GAP);		
		g2d.drawLine(EDGE, val, sat - GAP, val);	
		g2d.drawLine(sat, EDGE + val + GAP, sat, image.getHeight() + EDGE);
		g2d.drawLine(EDGE + sat + GAP, val, image.getWidth() + EDGE, val);
	}
	
	private void updateHueMap() {
		lastHue = hue;
		
		image = new BufferedImage(getWidth() - EDGE * 2, getHeight() - EDGE * 2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		g2d.scale(image.getWidth() / 100f, image.getHeight() / 100f);
		
		for(int sat = 0; sat < 100; sat ++) {
		for(int val = 0; val < 100; val ++) {
			g2d.setColor(Color.getHSBColor(hue, (100 - sat) / 100f, (100 - val) / 100f));
			g2d.fillRect(sat, val, 1, 1);
		}}
		
		g2d.dispose();
	}
	
	public void setColor(Color color) {
		this.color = color;
		hue = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[0];
	}
	
	private Color sample(Point point) {
		point.x -= EDGE;
		point.y -= EDGE;
		
		if(point.x < 0 || point.y < 0) return null;
		if(point.x >= image.getWidth()) return null;
		if(point.y >= image.getHeight()) return null;
		
		return new Color(image.getRGB(point.x, point.y));
	}
	
	private ArrayList<ChangeListener> listeners;
	
	public void addChangeListenser(ChangeListener listener) {
		if(listeners == null) listeners = new ArrayList<>();
		listeners.add(listener);
	}
	
	public void fireChangeEvent(Color color) {
		if(listeners == null) return;
		
		ChangeEvent event = new ChangeEvent(color);
		for(ChangeListener listener : listeners)
			listener.stateChanged(event);
	}
	
	public void mousePressed(MouseEvent e) { 
		Color color = sample(e.getPoint());
		if(color == null) return;
		fireChangeEvent(color);
	}
	
	public void mouseDragged(MouseEvent e) {
		Color color = sample(e.getPoint());
		if(color == null) return;
		fireChangeEvent(color);
	}
	
	public void mouseClicked(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mouseMoved(MouseEvent e) { }
}
