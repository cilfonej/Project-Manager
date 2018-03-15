package panels.popups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import managment.Assignment;
import managment.Task;

public class TaskPopupMenu extends JPopupMenu implements ActionListener {
	private static final long serialVersionUID = -5007789003276238103L;
	
	private JMenuItem deleteButton;
	private Task task;
	
	private Consumer<Assignment> assignmentUpdate;
	
	public TaskPopupMenu(Task task, Consumer<Assignment> assignmentUpdate) {
		this.task = task;
		this.assignmentUpdate = assignmentUpdate;
		
		deleteButton = new JMenuItem("Delete");
		deleteButton.setIcon(PopupIcons.DELETE_ICON);
		add(deleteButton);
		
		deleteButton.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == deleteButton) {
			task.delete();
//			task.getAssignment().removeTask(task);
			if(assignmentUpdate != null) assignmentUpdate.accept(task.getAssignment());
			return;
		}
	}
}
