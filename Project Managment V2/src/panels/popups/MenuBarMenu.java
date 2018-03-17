package panels.popups;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import components.color.ColorLookupTable;
import components.color.picker.ColorPicker;
import managment.icons.IconLoader;
import managment.storage.JsonSerializationContext;
import managment.storage.SerializationContext;

public class MenuBarMenu extends JMenu implements ActionListener {
	private static final long serialVersionUID = 849662231510333356L;
	private static final ColorPicker COLOR_PICKER = new ColorPicker();
	
	private JMenuItem syncButton;
	private JMenuItem reloadButton;
	private JMenuItem colorButton;
	private JCheckBoxMenuItem darkCkeckBox;
	
	private JsonSerializationContext context;
	private Color newColor;
	
	private boolean doneRestartAlert;
	
	public MenuBarMenu(JsonSerializationContext context) {
		super("Settings");
		this.context = context;
		
		syncButton = new JMenuItem("Sync to Drive...");
		syncButton.setIcon(IconLoader.loadIcon("SyncBlank.png", 16, new Color(125, 175, 225)));
		add(syncButton);
		
		reloadButton = new JMenuItem("Reload From Drive...");
		reloadButton.setIcon(IconLoader.loadIcon("DownBlank.png", 16, new Color(225, 200, 125)));
		add(reloadButton);
		
		JSeparator menuSeparator = new JSeparator();
		add(menuSeparator);
		
		colorButton = new JMenuItem("Change Active Color");
		newColor = ColorLookupTable.SELECT_BG_COLOUR;
		updateColorIcon();
		add(colorButton);
		
		darkCkeckBox = new JCheckBoxMenuItem("Dark Theme");
		darkCkeckBox.setSelected(ColorLookupTable.DARK);
		add(darkCkeckBox);
		
		syncButton.setEnabled(false);
		reloadButton.setEnabled(false);
		
		syncButton.addActionListener(this);
		reloadButton.addActionListener(this);
		colorButton.addActionListener(this);
		darkCkeckBox.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
//		if(e.getSource() == syncButton) {
//			IOUtil.
//			return;
//		}
//		
//		if(e.getSource() == reloadButton) {
//			int result = JOptionPane.showConfirmDialog(null, "<HTML>This will overwrite any Un-Sycned Changes."
//					+ "<BR>Do you want to Continue?</HTML>", "Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION,
//					JOptionPane.WARNING_MESSAGE);
//			
//			if(result == JOptionPane.CANCEL_OPTION) return;
//			driveInterface.syncToLocal();
//			return;
//		}
		
		if(e.getSource() == colorButton) {
			Color startColor = newColor;
			
			COLOR_PICKER.setColor(newColor);
			COLOR_PICKER.setVisible(true);
			newColor = COLOR_PICKER.getColor();
			
			if(!newColor.equals(startColor)) {
				updateSaveValue();
				updateColorIcon();
				showRestartAlert();
			}
			
			return;
		}
		
		if(e.getSource() == darkCkeckBox) {
			updateSaveValue();
			showRestartAlert();
			return;
		}
	}
	
	private void updateSaveValue() {
		ColorLookupTable.DATA_VALUE = 
				(darkCkeckBox.isSelected() ? 0xFF << 24 : 0) | 
				newColor.getRGB() & ColorLookupTable.COLOR_MASK;
	}
	
	private void updateColorIcon() { 
		colorButton.setIcon(IconLoader.loadIcon("ColorSample.png", 16, newColor)); 
	}
	
	private void showRestartAlert() {
		if(doneRestartAlert) return;
		
		JOptionPane.showMessageDialog(null, "You must Restart Application to see these Changes",
				"Restart Required", JOptionPane.WARNING_MESSAGE);
		doneRestartAlert = true;
	}
}
