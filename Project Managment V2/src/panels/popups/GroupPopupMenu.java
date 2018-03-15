package panels.popups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import components.datetime.DateTimePopout;
import managment.AssignmentGrouping;
import managment.Priority;

public class GroupPopupMenu extends JPopupMenu implements ActionListener {
	private static final long serialVersionUID = -5007789003276238103L;
	
	private JMenuItem newButton;
	private JMenuItem deleteButton;
	
	private AssignmentGrouping grouping;
	
	public GroupPopupMenu(AssignmentGrouping grouping) {
		this.grouping = grouping;
		
		newButton = new JMenuItem("New Assignment...");
		newButton.setIcon(PopupIcons.CREATE_ICON);
		add(newButton);
		
		JSeparator separator = new JSeparator();
		add(separator);
		
		deleteButton = new JMenuItem("Delete");
		deleteButton.setIcon(PopupIcons.DELETE_ICON);
		add(deleteButton);
		
		newButton.addActionListener(this);
		deleteButton.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == newButton) {
			String name = JOptionPane.showInputDialog(null, "Create new Assingment:", "<New Assignment>");
			if(name == null) return;
			
			LocalDateTime due = LocalDateTime.now();
			DateTimePopout datePicker = new DateTimePopout();
			datePicker.set(due); datePicker.setVisible(true); due = datePicker.get();
			
			grouping.createAssignment(due, Priority.Low, .5f, name, "");
			return;
		}
		
		if(e.getSource() == deleteButton) {
			grouping.delete();
			return;
		}
	}
}
