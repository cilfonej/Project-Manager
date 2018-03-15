package panels.popups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

public class RootPopupMenu extends JPopupMenu implements ActionListener {
	private static final long serialVersionUID = -5007789003276238103L;
	
	private JMenuItem newButton;
	private Consumer<String> grouping;
	
	public RootPopupMenu(Consumer<String> grouping) {
		this.grouping = grouping;
		
		newButton = new JMenuItem("New Group...");
		newButton.setIcon(PopupIcons.CREATE_ICON);
		add(newButton);
		
		newButton.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == newButton) {
			String name = JOptionPane.showInputDialog(null, "Create new Assingment Grouping:", "New Grouping");
			if(name == null) return;
			grouping.accept(name);
			return;
		}
	}
}
