package components.color.custom;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;

import components.color.ColorLookupTable;

public class ColoredComboBoxUI extends BasicComboBoxUI {
	public static ComponentUI createUI(JComponent c) { return new ColoredComboBoxUI(); }
	
	@SuppressWarnings("unchecked")
	protected void installDefaults() {
		super.installDefaults();
		comboBox.setRenderer(new IconComboBoxRender());
	}

	protected ComboPopup createPopup() {
		JPopupMenu popup = (JPopupMenu) super.createPopup();
		return (ComboPopup) popup;
	}

	protected JButton createArrowButton() {
		JButton button = ColorLookupTable.createArrowButton(BasicArrowButton.SOUTH);
		button.setName("ComboBox.arrowButton");
		return button;
	}
	
	private static class IconComboBoxRender extends DefaultListCellRenderer {
		private static final long serialVersionUID = 6074617725057260519L;
		
		private static final Object NO_ICON_PROVIDER = new Object();
		private static final HashMap<Class<?>, Object> LOOK_UP = new HashMap<>();
		
		private static Icon lookUp(Object val) {
			if(val == null) return null;
			
			Class<?> clazz = val.getClass();
			Object method = LOOK_UP.get(clazz);
			if(method == NO_ICON_PROVIDER) return null;
			
			try { if(method != null) return (Icon)((Method) method).invoke(val); } 
			catch(IllegalAccessException | InvocationTargetException | SecurityException e) { e.printStackTrace(); return null; }
		
			try { LOOK_UP.put(clazz, clazz.getMethod("getIcon")); } 
			catch(SecurityException | NoSuchMethodException e) { e.printStackTrace(); LOOK_UP.put(clazz, NO_ICON_PROVIDER); }
			return lookUp(val);
		}
		
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
			label.setBorder(new EmptyBorder(2, 3, 2, 5));//ColorLookupTable.LEFT_PADDING_BORDER);
			label.setIcon(lookUp(value));
			
			if(!selected) label.setBackground(ColorLookupTable.TEXT_BG_COLOR);
			label.setForeground(ColorLookupTable.FOREGROUND_COLOR);
			
			return label;
		}
	}
}