package components.datetime;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;

import components.color.ColorLookupTable;
import managment.Assignment;
import managment.icons.IconLoader;

public class AssignmentDateTimeDisplay extends JComponent implements ActionListener, MouseListener {
	private static final long serialVersionUID = 3408058921985852764L;
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(" d MMM, yyyy @ h:mm a");

	private Assignment assignment;
	
	private JLabel dueLabel;
	private JButton changeDueButton;
	
	private JPopupMenu menu;
	private JMenuItem editDue;
	private JMenuItem editStart;
	
	private ArrayList<ChangeListener> listeners;
	
	public AssignmentDateTimeDisplay() {
		listeners = new ArrayList<>();
		setLayout(new BorderLayout());
		
		JPanel dueDatePanel = new JPanel();
		dueDatePanel.setLayout(new BorderLayout(3, 0));
		dueDatePanel.setBackground(ColorLookupTable.TEXT_BG_COLOR);
		dueDatePanel.setBorder(new CompoundBorder(ColorLookupTable.PLAIN_BORDER, new EmptyBorder(0, 3, 0, 5)));
		add(dueDatePanel);
		
		dueLabel = new JLabel();
		dueDatePanel.add(dueLabel);
		
		changeDueButton = new JButton();
		changeDueButton.setPressedIcon(IconLoader.loadIcon("Calender.png", 24, ColorLookupTable.ARROW_BUTTON_PRESSED_BG_COLOUR));
		changeDueButton.setIcon(IconLoader.loadIcon("Calender.png", 24));
		changeDueButton.setUI(new BasicButtonUI());
		changeDueButton.setContentAreaFilled(false);
		changeDueButton.setBorder(null);
		dueDatePanel.add(changeDueButton, BorderLayout.WEST);
		
		menu = new JPopupMenu();
		editDue = new JMenuItem("Edit Due Date...");
		editStart = new JMenuItem("Edit Creation Date...");
		menu.add(editDue); menu.add(editStart);
		
		editDue.addActionListener(this);
		editStart.addActionListener(this);
		
		changeDueButton.addActionListener(this);
		addMouseListener(this);
	}
	
	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
		dueLabel.setText(DATE_FORMATTER.format(assignment.getDeadline()));
	}
	
	public void addChangeListener(ChangeListener listener) { this.listeners.add(listener); }
	public void removeChangeListener(ChangeListener listener) { this.listeners.remove(listener); }

	public void actionPerformed(ActionEvent e) {
		if(assignment == null) return;
		
		if(e.getSource() == changeDueButton || e.getSource() == editDue) {
			DateTimePopout popout = new DateTimePopout();
			popout.set(assignment.getDeadline());
			popout.setVisible(true);
			
			assignment.setDeadline(popout.get());
			dueLabel.setText(DATE_FORMATTER.format(assignment.getDeadline()));
			listeners.forEach(l -> l.stateChanged(new ChangeEvent(assignment.getDeadline())));
			
			return;
		}
		
		if(e.getSource() == editStart) {
			DateTimePopout popout = new DateTimePopout();
			popout.set(assignment.getCreationDate());
			popout.setVisible(true);
			
			assignment.setCreationDate(popout.get());
			dueLabel.setText(DATE_FORMATTER.format(assignment.getDeadline()));
			listeners.forEach(l -> l.stateChanged(new ChangeEvent(assignment.getCreationDate())));
			
			return;
		}
	}
	
	private void checkPopup(MouseEvent e) {
		if(!e.isPopupTrigger()) return;
		menu.show(this, e.getX(), e.getY());
	}

	public void mouseClicked(MouseEvent e) 	{ checkPopup(e); }
	public void mousePressed(MouseEvent e) 	{ checkPopup(e); }
	public void mouseReleased(MouseEvent e) { checkPopup(e); }
	public void mouseEntered(MouseEvent e) 	{ checkPopup(e); }
	public void mouseExited(MouseEvent e) 	{ checkPopup(e); }
}
