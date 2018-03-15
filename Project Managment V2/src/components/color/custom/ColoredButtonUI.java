package components.color.custom;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicButtonUI;

import components.color.ColorLookupTable;

public class ColoredButtonUI extends BasicButtonUI {
	public static ComponentUI createUI(JComponent c) { return new ColoredButtonUI(); }
	private BorderDesignListsner listener;
	
	protected void installDefaults(AbstractButton b) {
		super.installDefaults(b);
	}
	
	protected void installListeners(AbstractButton b) {
		super.installListeners(b);
		
		listener = new BorderDesignListsner(b);
		b.addPropertyChangeListener(listener);
		b.addMouseMotionListener(listener);
		b.addMouseListener(listener);
		
		listener.mouseClicked(null);
		b.revalidate();
		b.repaint();
	}
	
	protected void uninstallListeners(AbstractButton b) {
		super.uninstallListeners(b);
		
		b.removePropertyChangeListener(listener);
		b.removeMouseMotionListener(listener);
		b.removeMouseListener(listener);
	}
	
	private static class BorderDesignListsner implements MouseListener, MouseMotionListener, PropertyChangeListener {
		private AbstractButton button;
		public BorderDesignListsner(AbstractButton button) { this.button = button; updateDesign(); }

		private void updateDesign() {
			ButtonModel model = button.getModel();
			
			if(button instanceof BasicArrowButton) {
				if(model.isArmed()) button.setBackground(ColorLookupTable.ARROW_BUTTON_PRESSED_BG_COLOUR);
				else if(model.isRollover()) button.setBackground(ColorLookupTable.ARROW_BUTTON_ROLLOVER_BG_COLOUR);
				else button.setBackground(ColorLookupTable.ARROW_BUTTON_BG_COLOUR);
			} else {
				if(model.isArmed()) button.setBackground(ColorLookupTable.BUTTON_PRESSED_BG_COLOUR);
				else if(model.isRollover()) button.setBackground(ColorLookupTable.BUTTON_ROLLOVER_BG_COLOUR);
				else button.setBackground(ColorLookupTable.BUTTON_BG_COLOUR);
			}
			
			if(button instanceof BasicArrowButton) button.setBorder(null);
			else if(!button.isEnabled()) button.setBorder(ColorLookupTable.PLAIN_BORDER);
			else if(model.isPressed()) button.setBorder(ColorLookupTable.BUTTON_PRESSED_BORDER);
			else button.setBorder(ColorLookupTable.BUTTON_NORMAL_BORDER);

			button.revalidate();
			button.repaint();
		}
		
		public void propertyChange(PropertyChangeEvent e) { 
			if(e.getPropertyName().equals("enabled")) updateDesign(); 
		}
		
		public void mouseMoved(MouseEvent e) 	{ updateDesign(); }
		public void mousePressed(MouseEvent e) 	{ updateDesign(); }
		public void mouseReleased(MouseEvent e) { updateDesign(); }
		public void mouseEntered(MouseEvent e) 	{ updateDesign(); }
		public void mouseExited(MouseEvent e) 	{ updateDesign(); }
		public void mouseDragged(MouseEvent e) 	{ updateDesign(); }
		public void mouseClicked(MouseEvent e) 	{ updateDesign(); }
	}
}
