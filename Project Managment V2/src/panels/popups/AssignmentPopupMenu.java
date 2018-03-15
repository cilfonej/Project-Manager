package panels.popups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import managment.Assignment;
import managment.Priority;
import managment.icons.IconLoader;

public class AssignmentPopupMenu extends JPopupMenu implements ActionListener {
	private static final long serialVersionUID = -5007789003276238103L;
	
	private JMenuItem newButton;
	private JMenuItem copyButton;
	private JMenuItem pauseButton;
	private JMenuItem deleteButton;
	
	private Assignment assignment;
	private Consumer<Assignment> assignmentUpdate;
	
	public AssignmentPopupMenu(Assignment assignment, Consumer<Assignment> assignmentUpdate) {
		this.assignment = assignment;
		this.assignmentUpdate = assignmentUpdate;
		
		newButton = new JMenuItem("New Task...");
		newButton.setIcon(PopupIcons.CREATE_ICON);
		add(newButton);
		
		copyButton = new JMenuItem("Copy");
		copyButton.setIcon(IconLoader.loadIcon("Copy.png", 16));
		add(copyButton);
		
		if(assignment.getPriority() != Priority.Pause) {
			pauseButton = new JMenuItem("Pause");
			pauseButton.setIcon(PopupIcons.PAUSE_ICON);
			add(pauseButton);
		} else {
			pauseButton = new JMenuItem("Un-Pause");
			pauseButton.setIcon(PopupIcons.RESUME_ICON);
			add(pauseButton);
		}
		
		JSeparator separator = new JSeparator();
		add(separator);
		
		deleteButton = new JMenuItem("Delete");
		deleteButton.setIcon(PopupIcons.DELETE_ICON);
		add(deleteButton);
		
		newButton.addActionListener(this);
		copyButton.addActionListener(this);
		pauseButton.addActionListener(this);
		deleteButton.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == newButton) {
			assignment.addTask("<New Task>", "");
			if(assignmentUpdate != null) assignmentUpdate.accept(assignment);
			return;
		}
		
		if(e.getSource() == copyButton) {
			assignment.makeCopy();
			return;
		}
		
		if(e.getSource() == pauseButton) {
			if(assignment.getPriority() != Priority.Pause) {
				assignment.setPassive(true);
				assignment.setPriority(Priority.Pause);
			} else {
				assignment.setPassive(false);
				assignment.setPriority(Priority.Low);
			}
			
			assignment.update();
			if(assignmentUpdate != null) assignmentUpdate.accept(assignment);
			return;
		}
		
		if(e.getSource() == deleteButton) {
			assignment.delete();
//			assignment.getGrouping().removeAssignment(assignment);
			if(assignmentUpdate != null) assignmentUpdate.accept(null);
			return;
		}
	}

}
