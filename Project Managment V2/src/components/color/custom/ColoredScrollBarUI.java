package components.color.custom;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

import components.color.ColorLookupTable;

public class ColoredScrollBarUI extends BasicScrollBarUI {
	public static ComponentUI createUI(JComponent c) { return new ColoredScrollBarUI(); }
	
//	protected void configureScrollBarColors() {
//        thumbHighlightColor = ColorLookupTable.FOREGROUND_COLOR;
//        thumbLightShadowColor = ColorLookupTable.BACKGROUND_COLOR;
//        thumbDarkShadowColor = ColorLookupTable.BACKGROUND_COLOR.darker();
//        thumbColor = ColorLookupTable.FOREGROUND_COLOR.darker();
//        
//        trackColor = ColorLookupTable.TEXT_BG_COLOR;
//        trackHighlightColor = ColorLookupTable.BACKGROUND_COLOR.darker();
//    }
	
	protected JButton createDecreaseButton(int orientation) { return createIncreaseButton(orientation); }
	
	protected JButton createIncreaseButton(int orientation) {
//		BasicArrowButton button = new BasicArrowButton(orientation, 
//				thumbColor, thumbLightShadowColor, thumbDarkShadowColor, thumbHighlightColor);
//		button.setForeground(ColorLookupTable.FOREGROUND_COLOR);
		
		JButton button = ColorLookupTable.createArrowButton(orientation);
		return button;
	}
}
