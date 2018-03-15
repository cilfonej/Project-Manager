package panels;

import static components.color.ColorLookupTable.FOREGROUND_COLOR;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;

import components.color.ColorLookupTable;
import components.color.ColoredIconUtil;
import components.datetime.AssignmentDateTimeDisplay;
import components.datetime.CalenderTimeLine;
import components.datetime.TimeLine;
import managment.Assignment;
import managment.Priority;
import managment.Task;
import managment.icons.IconLoader;

public class EditAssignmentPanel extends JPanel implements ActionListener, TableModelListener, ListSelectionListener {
	private static final long serialVersionUID = 5628713176917379223L;
	
	private static final int MIN_CALENDER_SIZE = 250;
	private static final int MIN_TIMELINE_SIZE = 300;
	
	private JTextField nameTextField;
	private JComboBox<Priority> priorityCombBox;
	private JButton closeButton;

	private AssignmentDateTimeDisplay dueDisplay;
	private CalenderTimeLine calenderTimeline;
	private TimeLine timeLine;
	
	private JButton removeButton;
	private JButton addButton;
	private JTable table;
	private DefaultTableModel tableModel;

	private Assignment assignment;
	
	public EditAssignmentPanel() {
		setBorder(new CompoundBorder(ColorLookupTable.TABLE_BORDER, new EmptyBorder(3, 3, 3, 3)));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{125, 0, MIN_CALENDER_SIZE, 100, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, MIN_CALENDER_SIZE * 5 / 6 + 1, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		priorityCombBox = new JComboBox<>(Priority.values());
		GridBagConstraints gbc_priorityCombBox = new GridBagConstraints();
		gbc_priorityCombBox.insets = new Insets(0, 0, 5, 5);
		gbc_priorityCombBox.fill = GridBagConstraints.BOTH;
		gbc_priorityCombBox.gridx = 0;
		gbc_priorityCombBox.gridy = 0;
		add(priorityCombBox, gbc_priorityCombBox);
		priorityCombBox.addActionListener(this);
		
		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 1;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 5);
		gbc_nameTextField.fill = GridBagConstraints.BOTH;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 0;
		add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
		dueDisplay = new AssignmentDateTimeDisplay();
		GridBagConstraints gbc_dueDatePanel = new GridBagConstraints();
		gbc_dueDatePanel.insets = new Insets(0, 0, 5, 5);
		gbc_dueDatePanel.fill = GridBagConstraints.BOTH;
		gbc_dueDatePanel.gridx = 3;
		gbc_dueDatePanel.gridy = 0;
		add(dueDisplay, gbc_dueDatePanel);
		
		closeButton = new JButton();
		Image exitIcon = IconLoader.loadImage("Exit.png");
		closeButton.setBackground(ColorLookupTable.BACKGROUND_COLOR);
		closeButton.setUI(new BasicButtonUI());
		closeButton.setBorder(null);
		closeButton.setRolloverIcon(ColoredIconUtil.prepImage(exitIcon, new Color(250, 100, 100)));
		closeButton.setPressedIcon(ColoredIconUtil.prepImage(exitIcon, new Color(250, 100, 100).darker()));
		closeButton.setIcon(ColoredIconUtil.prepImage(exitIcon, ColorLookupTable.darker(FOREGROUND_COLOR, .75f)));
		GridBagConstraints gbc_closeButton = new GridBagConstraints();
		gbc_closeButton.anchor = GridBagConstraints.EAST;
		gbc_closeButton.fill = GridBagConstraints.VERTICAL;
		gbc_closeButton.insets = new Insets(0, 0, 5, 0);
		gbc_closeButton.gridx = 4;
		gbc_closeButton.gridy = 0;
		add(closeButton, gbc_closeButton);
		
		timeLine = new TimeLine();
		GridBagConstraints gbc_duePanel = new GridBagConstraints();
		gbc_duePanel.gridwidth = 2;
		gbc_duePanel.insets = new Insets(0, 0, 0, 5);
		gbc_duePanel.fill = GridBagConstraints.BOTH;
		gbc_duePanel.gridx = 0;
		gbc_duePanel.gridy = 1;
		add(timeLine, gbc_duePanel);
		
		calenderTimeline = new CalenderTimeLine();
		GridBagConstraints gbc_calenderTimeline = new GridBagConstraints();
		gbc_calenderTimeline.insets = new Insets(0, 0, 0, 5);
		gbc_calenderTimeline.fill = GridBagConstraints.BOTH;
		gbc_calenderTimeline.gridx = 2;
		gbc_calenderTimeline.gridy = 0;
		gbc_calenderTimeline.gridheight = 2;
		add(calenderTimeline, gbc_calenderTimeline);
		
		JPanel listWrapperPanel = new JPanel();
		listWrapperPanel.setPreferredSize(new Dimension(200, 10));
		GridBagConstraints gbc_listWrapperPanel = new GridBagConstraints();
		gbc_listWrapperPanel.gridwidth = 2;
		gbc_listWrapperPanel.fill = GridBagConstraints.BOTH;
		gbc_listWrapperPanel.gridx = 3;
		gbc_listWrapperPanel.gridy = 1;
		add(listWrapperPanel, gbc_listWrapperPanel);
		listWrapperPanel.setLayout(new BorderLayout(0, 3));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(ColorLookupTable.PLAIN_BORDER);
		listWrapperPanel.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();
		table.setModel(tableModel = new DefaultTableModel(new Object[][] { }, new String[] { "Tasks" }));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFillsViewportHeight(true);
		table.setRowHeight(25);
		scrollPane.setViewportView(table);
		
		JPanel buttonsPanel = new JPanel();
		listWrapperPanel.add(buttonsPanel, BorderLayout.SOUTH);
		buttonsPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel addPanel = new JPanel();
		buttonsPanel.add(addPanel);
		addPanel.setLayout(new BorderLayout(0, 0));
		
		addButton = new JButton("Add New");
		addPanel.add(addButton);
		addPanel.add(Box.createHorizontalStrut(2), BorderLayout.EAST);
		
		JPanel removePanel = new JPanel();
		buttonsPanel.add(removePanel);
		removePanel.setLayout(new BorderLayout(0, 0));
		
		removeButton = new JButton("Remove");
		removePanel.add(removeButton);
		removePanel.add(Box.createHorizontalStrut(2), BorderLayout.WEST);
		
		nameTextField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) { actionPerformed(new ActionEvent(nameTextField, 0, "")); }
		});

		addButton.addActionListener(this);
		removeButton.addActionListener(this);
		closeButton.addActionListener(this);
		nameTextField.addActionListener(this);
		
		tableModel.addTableModelListener(this);
		table.getSelectionModel().addListSelectionListener(this);
		
		dueDisplay.addChangeListener(e -> {
			timeLine.updateInflectionPoints();
			reloadPriotity();
		});
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				int availableWidth = getWidth() - table.getWidth() - 10;
				int availableHeight = getHeight() - 6;
				
				if(availableWidth < MIN_CALENDER_SIZE + MIN_TIMELINE_SIZE) {
					gridBagLayout.columnWidths[2] = 0;
					calenderTimeline.setVisible(false);
					
				} else {
					int min = Math.min(Math.min(availableWidth, availableHeight), availableWidth - MIN_TIMELINE_SIZE);
					gridBagLayout.columnWidths[2] = min;
					calenderTimeline.setVisible(true);
				}
				
				revalidate();
			}
		});

		setVisible(false);
	}
	
	private boolean updatingTable;
	public void setAssignment(Assignment assignment) {
		if(assignment == null) { setVisible(false); return; }
		setVisible(true);
		
		this.assignment = assignment;
		
		updatingTable = true; {
			removeButton.setEnabled(false);
			dueDisplay.setAssignment(assignment);
			nameTextField.setText(assignment.getName());
			priorityCombBox.setSelectedItem(assignment.getPriority());
			priorityCombBox.setEnabled(assignment.isPassive());
			
			calenderTimeline.setAssignment(assignment);
			timeLine.setAssignment(assignment); updatingTable = true;
			
			for(int i = tableModel.getRowCount() - 1; i >= 0; i --)
				tableModel.removeRow(i);
			for(Task task : assignment.getTasks())
				tableModel.addRow(new Object[] { task });
		
		} updatingTable = false;
	}
	
	public void setTask(Task task) {
		setAssignment(task.getAssignment());
		int index = assignment.getTasks().indexOf(task);
		table.setRowSelectionInterval(index, index);
	}
	
	public void reloadPriotity() {
		updatingTable = true; {
			calenderTimeline.setAssignment(assignment);
			priorityCombBox.setSelectedItem(assignment.getPriority());
			priorityCombBox.setEnabled(assignment.isPassive());
		} updatingTable = false;
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == addButton) {
			updatingTable = true;
				tableModel.addRow(new Object[] { assignment.addTask("<New Task>", "") });
			updatingTable = false;
			
			return;
		}
		
		if(e.getSource() == removeButton) {
			int selected = table.getSelectedRow();
			table.getCellEditor(selected, 0).stopCellEditing();
			
			updatingTable = true;
				tableModel.removeRow(selected);
			updatingTable = false;
			
			assignment.getTasks().get(selected).delete();
//			assignment.removeTask(assignment.getTasks().get(selected));
			return;
		}
		
		if(e.getSource() == closeButton) { setVisible(false); return; }
		
		if(e.getSource() == nameTextField) {
			assignment.setName(nameTextField.getText());
			return;
		}
		
		if(e.getSource() == priorityCombBox) {
			if(updatingTable) return;
			
			assignment.setPriority((Priority) priorityCombBox.getSelectedItem());
			assignment.setPassive(true);
			timeLine.updateInflectionPoints();
			calenderTimeline.setAssignment(assignment);
			
//			if(assignment.getImportance() < 0) {
//				timeLine.updateInflectionPoints();
//			}
			
			return;
		}
	}

	public void tableChanged(TableModelEvent e) {
		if(updatingTable) return;
		
		int row = e.getFirstRow();
		String value = (String) tableModel.getValueAt(row, e.getColumn());
		
		if(value.trim().isEmpty()) tableModel.setValueAt(assignment.getTasks().get(row).toString(), row, e.getColumn());
		else assignment.getTasks().get(row).setName(value);
	}

	public void valueChanged(ListSelectionEvent e) {
		int column = table.getSelectedColumn();
		int row = table.getSelectedRow();
		
		removeButton.setEnabled(!(column < 0 || row < 0));
	}
}
