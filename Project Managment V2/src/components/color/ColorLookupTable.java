package components.color;

import java.awt.Color;
import java.awt.Font;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.plaf.basic.BasicArrowButton;

import components.color.custom.ColoredButtonUI;
import components.color.custom.ColoredCheckBoxMenuItemUI;
import components.color.custom.ColoredCheckBoxUI;
import components.color.custom.ColoredComboBoxUI;
import components.color.custom.ColoredScrollBarUI;
import components.color.custom.ColoredSliderUI;
import components.color.custom.ColoredSpinnerUI;
import components.color.custom.ColoredSplitPaneUI;
import components.tree.table.TableRow;
import managment.storage.FileStorage;

public class ColorLookupTable {
	private ColorLookupTable() { }
	
	public static final int COLOR_MASK = 0x00FFFFFF;
	public static int DATA_VALUE;
	
	static {
		try(DataInputStream in = new DataInputStream(new FileInputStream(FileStorage.SETTINGS_FILE))) {
			DATA_VALUE = in.readInt();
			
		} catch(FileNotFoundException e) {
			DATA_VALUE = 255 << 24 | 200 << 16 | 100 << 8 | 100;
			
		} catch(IOException e) { e.printStackTrace(); }
	}
	
	public static final boolean DARK = DATA_VALUE < 0;//true;
	
	public static final Color BACKGROUND_COLOR = DARK ? Color.GRAY.darker() : new Color(245, 245, 245);
	public static final Color FOREGROUND_COLOR = DARK ? Color.LIGHT_GRAY.brighter() : Color.BLACK;
	
	public static final Color SELECT_BG_COLOUR = new Color(DATA_VALUE & COLOR_MASK); //new Color(200, 100, 100);
	public static final Color INV_SELECT_BG_COLOUR; static { 
		float[] hsv = Color.RGBtoHSB(SELECT_BG_COLOUR.getRed(), SELECT_BG_COLOUR.getGreen(), SELECT_BG_COLOUR.getBlue(), null);
		INV_SELECT_BG_COLOUR = new Color(Color.HSBtoRGB((hsv[0] - .25f) % 1, hsv[1] / 2, hsv[2])); }
	public static final Color TEXT_SELECT_BG_COLOR = DARK ? brighter(SELECT_BG_COLOUR, .25f) : darker(SELECT_BG_COLOUR, .75f);
	public static final Color TEXT_DISABLE_BG_COLOR = mix(BACKGROUND_COLOR, FOREGROUND_COLOR, .5f);
	public static final Color TEXT_BG_COLOR = DARK ? brighter(BACKGROUND_COLOR, .15f) : darker(BACKGROUND_COLOR, 0f);  
		// Swap .15 -> .05

	public static final Color ARROW_BUTTON_SHADOW_COLOUR = DARK ? BACKGROUND_COLOR : brighter(BACKGROUND_COLOR, .5f);
	public static final Color ARROW_BUTTON_DARK_SHADOW_COLOUR = mix(ARROW_BUTTON_SHADOW_COLOUR, Color.BLACK, .5f);
	public static final Color ARROW_BUTTON_HIGHLIGHT_COLOUR = DARK ? FOREGROUND_COLOR : darker(BACKGROUND_COLOR, .5f);

	public static final Color ARROW_BUTTON_BG_COLOUR = DARK ? darker(FOREGROUND_COLOR, .75f) : brighter(BACKGROUND_COLOR, .25f);
	public static final Color ARROW_BUTTON_PRESSED_BG_COLOUR = mix(ARROW_BUTTON_BG_COLOUR, Color.BLACK, .75f);
	public static final Color ARROW_BUTTON_ROLLOVER_BG_COLOUR = darker(ARROW_BUTTON_BG_COLOUR, .85f);
	
	public static final Color BUTTON_BG_COLOUR = brighter(BACKGROUND_COLOR, .25f);//darker(ARROW_BUTTON_BG_COLOUR, .5f);//BACKGROUND_COLOR;//brighter(BACKGROUND_COLOR, .1f);
	public static final Color BUTTON_PRESSED_BG_COLOUR = mix(BACKGROUND_COLOR, Color.BLACK, .9f);//darker(ARROW_BUTTON_PRESSED_BG_COLOUR, .6f);//mix(BACKGROUND_COLOR, Color.BLACK, .9f);
	public static final Color BUTTON_ROLLOVER_BG_COLOUR = mix(BACKGROUND_COLOR, FOREGROUND_COLOR, .6f);

	public static final Color SCROLL_BAR_TRACK_COLOUR = brighter(BACKGROUND_COLOR, .1f);
	
	public static final Color BORDER_PRIMARY = DARK ? darker(FOREGROUND_COLOR, .75f) : brighter(BACKGROUND_COLOR, .5f);
	public static final Border PLAIN_BORDER = new LineBorder(BORDER_PRIMARY);	
	public static final Border TABLE_BORDER = new LineBorder(BORDER_PRIMARY, TableRow.BORDER_THICKNESS);	
	public static final Border ETCHED_UP_BORDER = DARK ? 
				new EtchedBorder(EtchedBorder.RAISED, brighter(BACKGROUND_COLOR, .25f), darker(BACKGROUND_COLOR, .75f)) : 
				new EtchedBorder(EtchedBorder.RAISED, darker(BACKGROUND_COLOR, .75f), brighter(BACKGROUND_COLOR, .25f)) ;	
	
	public static final Border BUTTON_NORMAL_BORDER = new CompoundBorder(new SoftBevelBorder(BevelBorder.RAISED), new EmptyBorder(1, 3, 1, 3));
	public static final Border BUTTON_PRESSED_BORDER = new CompoundBorder(new SoftBevelBorder(BevelBorder.LOWERED), new EmptyBorder(1, 3, 1, 3));	
	
	public static final Border LEFT_PADDING_BORDER = new EmptyBorder(1, 5, 1, 0);
	
	public static final Font NORMAL_FONT = new Font("Tahoma", Font.PLAIN, 14);
	public static final Font NORMAL_TITLE_FONT = new Font("Tahoma", Font.BOLD, 14);
	public static final Font HEADER_FONT = new Font("Tahoma", Font.BOLD, 16);
	public static final Font HUGE_FONT = new Font("Tahoma", Font.BOLD, 24);
	
	
	private static boolean loaded = false;
	public static void loadUI() {
		if(loaded) return;
		loaded = true;
		
		UIManager.put("Panel.background", 	BACKGROUND_COLOR);
		UIManager.put("Panel.foreground", 	FOREGROUND_COLOR);

		UIManager.put("PopupMenu.background", 	BACKGROUND_COLOR);
		UIManager.put("PopupMenu.foreground", 	FOREGROUND_COLOR);
		UIManager.put("PopupMenu.border", 		PLAIN_BORDER);

		UIManager.put("ButtonUI", ColoredButtonUI.class.getName());
		UIManager.put("Button.background", 	BUTTON_BG_COLOUR);
		UIManager.put("Button.foreground", 	FOREGROUND_COLOR);
		UIManager.put("Button.font", 		NORMAL_FONT);

		UIManager.put("CheckBoxUI", ColoredCheckBoxUI.class.getName());
		UIManager.put("CheckBox.background", 	BUTTON_BG_COLOUR);
		UIManager.put("CheckBox.foreground", 	FOREGROUND_COLOR);
		UIManager.put("CheckBox.font", 			NORMAL_FONT);
		UIManager.put("CheckBox.border", 		LEFT_PADDING_BORDER);

		UIManager.put("Label.background", 	BACKGROUND_COLOR);
		UIManager.put("Label.foreground", 	FOREGROUND_COLOR);
		UIManager.put("Label.font", 		NORMAL_FONT);
		UIManager.put("Label.border", 		LEFT_PADDING_BORDER);
		
		UIManager.put("TextField.background", 			TEXT_BG_COLOR);
		UIManager.put("TextField.disabledBackground", 	TEXT_BG_COLOR);
		UIManager.put("TextField.foreground", 			FOREGROUND_COLOR);
		UIManager.put("TextField.font", 				NORMAL_FONT);
		UIManager.put("TextField.border", 				new CompoundBorder(PLAIN_BORDER, LEFT_PADDING_BORDER));
		UIManager.put("TextField.selectionBackground", 	TEXT_SELECT_BG_COLOR);
		UIManager.put("TextField.selectionForeground", 	FOREGROUND_COLOR);

		UIManager.put("FormattedTextField.background", 				TEXT_BG_COLOR);
		UIManager.put("FormattedTextField.foreground", 				FOREGROUND_COLOR);
		UIManager.put("FormattedTextField.border", 					PLAIN_BORDER);
		UIManager.put("FormattedTextField.font", 					NORMAL_FONT);
		UIManager.put("FormattedTextField.selectionBackground", 	TEXT_SELECT_BG_COLOR);
		UIManager.put("FormattedTextField.selectionForeground", 	FOREGROUND_COLOR);
		
		UIManager.put("List.background", 			TEXT_BG_COLOR);
		UIManager.put("List.foreground", 			FOREGROUND_COLOR);
		UIManager.put("List.font", 					NORMAL_FONT);
		UIManager.put("List.border", 				PLAIN_BORDER);
		UIManager.put("List.selectionBackground", 	TEXT_SELECT_BG_COLOR);
		UIManager.put("List.selectionForeground", 	FOREGROUND_COLOR);

		UIManager.put("Table.background", 						TEXT_BG_COLOR);
		UIManager.put("Table.foreground", 						FOREGROUND_COLOR);
		UIManager.put("Table.font", 							NORMAL_FONT);
//		UIManager.put("Table.border", 							PLAIN_BORDER);
		UIManager.put("Table.gridColor", 						BORDER_PRIMARY);
		UIManager.put("Table.highlight", 						TEXT_SELECT_BG_COLOR);
		UIManager.put("Table.selectionBackground", 				TEXT_SELECT_BG_COLOR);
		UIManager.put("Table.selectionForeground", 				FOREGROUND_COLOR);
		UIManager.put("Table.focusCellHighlightBorder",		 	PLAIN_BORDER);
		UIManager.put("Table.focusSelectedCellHighlightBorder", PLAIN_BORDER);
		UIManager.put("Table.focusCellBackground", 				TEXT_SELECT_BG_COLOR);
		UIManager.put("Table.focusCellForeground", 				FOREGROUND_COLOR);
		
		UIManager.put("TableHeader.background", BACKGROUND_COLOR);
		UIManager.put("TableHeader.foreground", FOREGROUND_COLOR);
		UIManager.put("TableHeader.font", 		NORMAL_TITLE_FONT);
		UIManager.put("TableHeader.cellBorder", new EmptyBorder(2, 0, 2, 0));

		UIManager.put("SliderUI", ColoredSliderUI.class.getName());
		UIManager.put("Slider.background", 	TEXT_BG_COLOR);
		UIManager.put("Slider.foreground", 	SELECT_BG_COLOUR);
		
		UIManager.put("SpinnerUI", ColoredSpinnerUI.class.getName());
		UIManager.put("Spinner.background", 	TEXT_BG_COLOR);
		UIManager.put("Spinner.foreground", 	FOREGROUND_COLOR);

		UIManager.put("SplitPaneUI", ColoredSplitPaneUI.class.getName());
		UIManager.put("SplitPaneDivider.draggingColor", FOREGROUND_COLOR);
		UIManager.put("SplitPaneDivider.dividerSize", 	1);
		UIManager.put("SplitPane.border", 				new EmptyBorder(0, 0, 0, 0));

		UIManager.put("ComboBoxUI", ColoredComboBoxUI.class.getName());
		UIManager.put("ComboBox.background", 			TEXT_BG_COLOR);
		UIManager.put("ComboBox.foreground", 			FOREGROUND_COLOR);
		UIManager.put("ComboBox.disabledBackground", 	mix(TEXT_BG_COLOR,    BACKGROUND_COLOR, .5f));
		UIManager.put("ComboBox.disabledForeground", 	mix(FOREGROUND_COLOR, BACKGROUND_COLOR, .5f));
		UIManager.put("ComboBox.selectionForeground",   FOREGROUND_COLOR);
		UIManager.put("ComboBox.selectionBackground",   TEXT_SELECT_BG_COLOR);
		UIManager.put("ComboBox.border", 				PLAIN_BORDER);
		UIManager.put("ComboBox.font", 					NORMAL_FONT);

		UIManager.put("ScrollBarUI", ColoredScrollBarUI.class.getName());
		UIManager.put("ScrollBar.background", 		BACKGROUND_COLOR);
		UIManager.put("ScrollBar.foreground", 		FOREGROUND_COLOR);
		
		UIManager.put("ScrollBar.thumbHighlight", 	ARROW_BUTTON_HIGHLIGHT_COLOUR);
		UIManager.put("ScrollBar.thumbShadow", 		ARROW_BUTTON_SHADOW_COLOUR);
		UIManager.put("ScrollBar.thumbDarkShadow", 	ARROW_BUTTON_DARK_SHADOW_COLOUR);
		UIManager.put("ScrollBar.thumb", 			ARROW_BUTTON_BG_COLOUR);
		
		UIManager.put("ScrollBar.track", 			SCROLL_BAR_TRACK_COLOUR);
		UIManager.put("ScrollBar.trackHighlight", 	BUTTON_PRESSED_BG_COLOUR);
		
//		BORDER_PRIMARY
//		BACKGROUND_COLOR
//		BUTTON_PRESSED_BG_COLOUR
//		FOREGROUND_COLOR
		
		UIManager.put("ScrollPane.background", 		BACKGROUND_COLOR);
		UIManager.put("ScrollPane.foreground", 		FOREGROUND_COLOR);
		UIManager.put("ScrollPane.border", 			TABLE_BORDER);
		
		UIManager.put("Separator.background", 	DARK ? brighter(BACKGROUND_COLOR, .25f) : darker(BACKGROUND_COLOR, .25f));
		UIManager.put("Separator.foreground", 	DARK ? darker(FOREGROUND_COLOR, .25f) : brighter(BACKGROUND_COLOR, .25f));
		
		UIManager.put("Menu.background", 			TEXT_BG_COLOR);
		UIManager.put("Menu.foreground", 			FOREGROUND_COLOR);
		UIManager.put("Menu.font", 					NORMAL_FONT);
		UIManager.put("Menu.border", 				new EmptyBorder(1, 1, 1, 1));
		UIManager.put("Menu.selectionBackground", 	TEXT_SELECT_BG_COLOR);
		UIManager.put("Menu.selectionForeground", 	FOREGROUND_COLOR);

		UIManager.put("MenuItem.background", 				TEXT_BG_COLOR);
		UIManager.put("MenuItem.foreground", 				FOREGROUND_COLOR);
		UIManager.put("MenuItem.font", 						NORMAL_FONT);
		UIManager.put("MenuItem.border", 					new EmptyBorder(1, 1, 1, 1));
		UIManager.put("MenuItem.selectionBackground", 		TEXT_SELECT_BG_COLOR);
		UIManager.put("MenuItem.selectionForeground", 		FOREGROUND_COLOR);
		UIManager.put("MenuItem.selectedBackgroundPainter", null);

		UIManager.put("CheckBoxMenuItemUI", 					ColoredCheckBoxMenuItemUI.class.getName());
		UIManager.put("CheckBoxMenuItem.background", 			TEXT_BG_COLOR);
		UIManager.put("CheckBoxMenuItem.foreground", 			FOREGROUND_COLOR);
		UIManager.put("CheckBoxMenuItem.font", 					NORMAL_FONT);
		UIManager.put("CheckBoxMenuItem.border", 				new EmptyBorder(1, 1, 1, 1));
		UIManager.put("CheckBoxMenuItem.selectionBackground", 	TEXT_SELECT_BG_COLOR);
		UIManager.put("CheckBoxMenuItem.selectionForeground", 	FOREGROUND_COLOR);

		UIManager.put("MenuBar.background", 			TEXT_BG_COLOR);
		UIManager.put("MenuBar.foreground", 			FOREGROUND_COLOR);
		UIManager.put("MenuBar.border", 				new EmptyBorder(1, 1, 1, 1));
		UIManager.put("MenuBar.font", 					NORMAL_FONT);
		UIManager.put("MenuBar.highlight", 				UIManager.get("Separator.foreground"));
		UIManager.put("MenuBar.shadow", 				UIManager.get("Separator.background"));
		UIManager.put("MenuBar.rolloverEnabled", 		true);
		
		UIManager.put("PopupMenu.background", 			TEXT_BG_COLOR);
		UIManager.put("PopupMenu.foreground", 			FOREGROUND_COLOR);
		UIManager.put("PopupMenu.font", 				NORMAL_FONT);
		UIManager.put("PopupMenu.border", 				PLAIN_BORDER);
		UIManager.put("PopupMenu.selectionBackground", 	TEXT_SELECT_BG_COLOR);
		UIManager.put("PopupMenu.selectionForeground", 	FOREGROUND_COLOR);
		
		UIManager.put("OptionPane.background", 			BACKGROUND_COLOR);
		UIManager.put("OptionPane.foreground", 			FOREGROUND_COLOR);
		UIManager.put("OptionPane.font", 				NORMAL_FONT);
		UIManager.put("OptionPane.messageForeground", 	FOREGROUND_COLOR);
		UIManager.put("OptionPane.messageFont", 		NORMAL_FONT);
	}
	
	public static Color brighter(Color c, float FACTOR) { // Swap WHITE -> BLACK
		return DARK ? mix(c, Color.WHITE, 1 - FACTOR) : mix(c, Color.BLACK, 1 - FACTOR);
	}

	public static Color darker(Color c, float FACTOR) { // Swap BLACK -> WHITE
		return DARK ? mix(c, Color.BLACK, FACTOR) : mix(c, Color.WHITE, FACTOR);
	}
	
	public static Color mix(Color c1, Color c2, float v) {
		float r = 1 - v;
		return new Color(
				(int) (c1.getRed()   * v)  + (int) (c2.getRed()   * r),
				(int) (c1.getGreen() * v)  + (int) (c2.getGreen() * r),
				(int) (c1.getBlue()  * v)  + (int) (c2.getBlue()  * r),
				(int) (c1.getAlpha() * v)  + (int) (c2.getAlpha() * r)
			);
	}
	
	public static JButton createArrowButton(int direction) { // Swap FG, BG, first Arg. -> brighter 25
//		if(DARK) {
//			Color bg = BACKGROUND_COLOR, fg = FOREGROUND_COLOR;//darker(BACKGROUND_COLOR, .75f), fg = darker(FOREGROUND_COLOR, .75f);
			JButton button = new BasicArrowButton(direction, ARROW_BUTTON_BG_COLOUR, ARROW_BUTTON_SHADOW_COLOUR, 
					ARROW_BUTTON_DARK_SHADOW_COLOUR, ARROW_BUTTON_HIGHLIGHT_COLOUR);
			return button;
			
//		} else {
//			Color fg = darker(BACKGROUND_COLOR, .75f), bg = darker(FOREGROUND_COLOR, .75f);
//			JButton button = new BasicArrowButton(direction, brighter(fg, .25f), bg, darker(bg, .75f), fg);
//			return button;
//		}
	}
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try(DataOutputStream out = new DataOutputStream(new FileOutputStream(FileStorage.SETTINGS_FILE))) {
				out.writeInt(DATA_VALUE);
				
			} catch(IOException e) { e.printStackTrace(); }
		}, "Project Managment - Settings Save Thread"));
	}
}
