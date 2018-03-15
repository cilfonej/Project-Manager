package panels;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import components.SplashScreen;
import components.color.ColorLookupTable;
import components.tree.ComponentTree;
import managment.Assignment;
import managment.Root;
import managment.Task;
import managment.icons.IconLoader;
import managment.storage.FileStorage;
import managment.storage.IOUtil;
import managment.storage.SerializationContext;
import panels.popups.MenuBarMenu;

public class MainAssignmentFrame extends JFrame {
	private static final long serialVersionUID = -2612023745336791820L;
	
	private static SplashScreen splashScreen;
	private static JLabel statusLabel;
	
	public static void setStatusText(String text) { 
		SwingUtilities.invokeLater(() -> {
			if(statusLabel != null) statusLabel.setText(text);
		});
	}
	
	private AssignmentsTable table;
	private EditAssignmentPanel editPanel;
	
	
	public static void main(String[] args) {
		splashScreen = new SplashScreen();
		IOUtil.load(new SerializationContext(FileStorage.SAVE_FILE), 0, Root.class, 
				root -> EventQueue.invokeLater(() -> new MainAssignmentFrame(root))
			);
	}

	public MainAssignmentFrame(Root root) {
		ColorLookupTable.loadUI();
		
		setTitle("Assignment Manager");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setIconImage(IconLoader.loadImage("TaskList.png"));
		setSize(600, 450);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		menuBar.add(new MenuBarMenu(root.getContext()));
		menuBar.add(Box.createHorizontalGlue());
		
		statusLabel = new JLabel("Saving...");
		statusLabel.setFont(ColorLookupTable.NORMAL_FONT);
		statusLabel.setBorder(new EmptyBorder(1, 1, 1, 3));
		menuBar.add(statusLabel);
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(1.0);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		table = new AssignmentsTable(root);
		JScrollPane scrollPane = new JScrollPane(table);
		splitPane.setLeftComponent(scrollPane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		table.setAssignmentDisplayUpdate(this::updateAssignmentDisplay);
		
		editPanel = new EditAssignmentPanel();
		splitPane.setRightComponent(editPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(ComponentTree.LEVEL_SHIFT_Y / 2);
		
//		Assignment a = null;
//		
//		for(int i = 0; i < 4; i ++) {
//			AssignmentGrouping grouping = new AssignmentGrouping("Test Group #" + i, table::updateAssignment,
//					table::addAssignment, table::addTask, table::removeAssignment, table::removeTask);
//			
//			for(int j = 0, n = (int) (Math.random() * 5) + 1; j < n; j ++) {
//				int plus = (int) (Math.random() * 31) + 1;
//				Assignment assignment = grouping.createAssignment(LocalDateTime.now().plusDays(plus), 
//						Priority.Low, (float) Math.random(), 
//						"Assignment #" + j, 
//						"This Test Assigmenet is due in " + plus + " days");
//				
//				if(a == null) a = assignment;
//				for(int k = 0, m = (int) (Math.random() * 8) + 1; k < m; k ++) {
//					assignment.addTask("Thing #" + k, "Some Assignemnet numberrator");
////					table.addTask(assignment.getTasks().get(k));
//				}
//				
//				assignment.update();
//			}
//		}
		
//		editPanel.setAssignment(a);
//		editPanel.setVisible(true);
				
		table.addChangeListener(e -> {
			if(table.getSelectedNode() == null) return;
			Object value = table.getSelectedNode().get();
			
			if(value instanceof Assignment) editPanel.setAssignment((Assignment) value);
			else if(value instanceof Task) editPanel.setTask((Task) value);
			else return;
			
			editPanel.setVisible(true);
			splitPane.setDividerLocation(splitPane.getMaximumDividerLocation());
		});
		
//		Assignment assignment = new Assignment(grouping, LocalDateTime.now().plusMinutes(30), Priority.Low, .5f, "Assignment", 
//				"This Test Assigmenet is due in 70 seconds");
//		assignment.addTask("Thing #1", "Do the thing");
//		assignment.addTask("Stuffs #2", "Do the Other thing");
//		
//		table.addTask(assignment.getTasks().get(0));
//		table.addTask(assignment.getTasks().get(1));
		
		Timer timer = new Timer(10 * 60 * 1000, e -> {
			if(MainAssignmentFrame.this.getState() != JFrame.ICONIFIED) {
//				table.updateAll();
			}
		});
		
		timer.setRepeats(true);
		timer.setCoalesce(false);
		timer.start();
		
		setLocationRelativeTo(null);
		splashScreen.setVisible(false);
		splashScreen.dispose();
		setVisible(true);
	}
	
	public void updateAssignmentDisplay(Assignment assignment) { editPanel.setAssignment(assignment); }
}
