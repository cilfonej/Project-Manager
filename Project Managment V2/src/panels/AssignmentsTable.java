package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import components.color.ColorLookupTable;
import components.datetime.AssignmentDateTimeDisplay;
import components.tree.ComponentTree;
import components.tree.EmptyNode;
import components.tree.TreeNode;
import components.tree.TreeNode.TreeListener;
import components.tree.table.Column;
import components.tree.table.TableController;
import components.tree.table.TableRow;
import managment.Assignment;
import managment.AssignmentGrouping;
import managment.Priority;
import managment.Root;
import managment.Task;
import managment.icons.IconLoader;
import managment.storage.IOUtil;
import panels.popups.AssignmentPopupMenu;
import panels.popups.GroupPopupMenu;
import panels.popups.RootPopupMenu;
import panels.popups.TaskPopupMenu;

public class AssignmentsTable extends JPanel implements TreeListener<Object> {
	private static final long serialVersionUID = 5137874673819831503L;

	private TableController<Object> controller;
	private ComponentTree<Object> tree;
	private Root root;
	
	private HashMap<AssignmentGrouping, TreeNode<Object>> groupingNodes;
	private HashMap<Assignment, TreeNode<Object>> assignmentNodes;
	private HashMap<Task, TreeNode<Object>> taskNodes;
	
	private Consumer<Assignment> rightClickUpdate;
	
	public AssignmentsTable(Root root) {
		setLayout(null);
		
		groupingNodes = new HashMap<>();
		assignmentNodes = new HashMap<>();
		taskNodes = new HashMap<>();
		
		this.root = root;
		
		tree = new ComponentTree<>(root);
		tree.setSize(1000, 1000);//tree.getPreferredSize().height);
		add(tree);
		
		tree.assignIcon(0, IconLoader.loadIcon("Grouped.png", ComponentTree.ICON_SIZE));
		tree.assignIcon(1, IconLoader.loadIcon("TaskList.png", ComponentTree.ICON_SIZE));
		tree.assignIcon(2, IconLoader.loadIcon("Task.png", ComponentTree.ICON_SIZE));
		tree.assignIcon(3, IconLoader.loadIcon("SDU.png", ComponentTree.ICON_SIZE, ColorLookupTable.INV_SELECT_BG_COLOUR));

		controller = new TableController<>(tree::fullRebuild);
		
		try {
			controller.addColumn(1, "   ", null, null, null, null);

			Class<Assignment> a = Assignment.class; // -------------------------------------------------------------------------
			controller.addColumn(2, null, a.getMethod("getPriority"), null, 
					(c, v, comp) -> {
						Assignment assign = (Assignment) v;
						Priority priority = (Priority) c.get(v);
						Icon icon = assign.isCompleted() ? priority.getDesaturatedIcon() : priority.getLargeIcon();
						
						if(comp == null) comp = new JLabel(icon);
						else ((JLabel) comp).setIcon(icon);
						return comp;
					}, (node0, node1) -> {
						Assignment a0 = (Assignment) node0.get();
						Assignment a1 = (Assignment) node1.get();
						
						int doneCompare = Boolean.compare(a0.isCompleted(), a1.isCompleted());
						int priorityCompare = a0.getPriority().compareTo(a1.getPriority());
						
						return doneCompare == 0 ? 
								priorityCompare == 0 ?
										Float.compare(a0.getImportance(), a1.getImportance())
										: priorityCompare
										: doneCompare; 
					}, ComponentTree.ROW_HEIGHT + TableRow.BORDER_THICKNESS * 2
				);
			
			controller.addColumn(2, "Done", a.getMethod("isCompleted"), a.getMethod("setCompleted", boolean.class),
				AssignmentsTable::createCheckBox, null, 72);
			
			controller.addColumn(2, "Deadline", a.getMethod("getDeadline"), null,
				(col, v, comp) -> wrap(new JLabel(((LocalDateTime) col.get(v)).format(AssignmentDateTimeDisplay.DATE_FORMATTER))),
				 null, Column.GRAPHICS.getFontMetrics(ColorLookupTable.NORMAL_FONT)
				.stringWidth(" 00 MMM, 0000 @ 00:00 am") + TableRow.BORDER_THICKNESS * 3);
			
			controller.addColumn(2, "Description", a.getMethod("getDescription"), a.getMethod("setDescription", String.class),
				AssignmentsTable::createTextField, null);
			

			Class<Task> t = Task.class; // -------------------------------------------------------------------------
			controller.addColumn(3, null, null, null, null, null, (int) ((ComponentTree.ROW_HEIGHT + TableRow.BORDER_THICKNESS * 2) * 1.5));
			
			controller.addColumn(3, "Done", t.getMethod("isDone"), t.getMethod("setDone", boolean.class),
				AssignmentsTable::createCheckBox, null, 72);
			
			controller.addColumn(3, "Description", t.getMethod("getDescription"), t.getMethod("setDescription", String.class),
				AssignmentsTable::createTextField, null);
			
		} catch(NoSuchMethodException | SecurityException e) { e.printStackTrace(); }
		
		
		controller.getHeader().setSize(controller.getHeader().getPreferredSize());
		controller.getHeader().setLocation(ComponentTree.ROW_LABEL_WIDTH + 3, 0);
		add(controller.getHeader());
		
		setComponentZOrder(controller.getHeader(), 0);
		
		tree.setPopupAction(this::triggerPopup);
		tree.addChangeListener(e -> controller.displayLevelHeader(getNodeLevel(tree.getSelectedNode(), -1)));
		controller.fillWidth(getWidth() - ComponentTree.ROW_LABEL_WIDTH);
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				controller.fillWidth(getWidth() - ComponentTree.ROW_LABEL_WIDTH);
				controller.getHeader().setSize(controller.getHeader().getPreferredSize());
				tree.fullResize();
			}
		});
		
		root.addAll();
		root.addTreeListener(this);
		
		root.setMethods(this::updateAssignment, 
				this::addAssignmentGroup, this::removeAssignmentGroup, 
				this::addAssignment, this::removeAssignment, 
				this::addTask, this::removeTask
			);
		
		IOUtil.updateAll(root.getContext());
//		setPreferredSize(tree.getSize());
		
//		context = new SerializationContext(new File("C:/Users/Joshua/Desktop/Assign.dat"));
//		AssignmentGrouping grouping = context.load(AssignmentGrouping.class, 0);
//		grouping.setMethods(this::updateAssignment, this::addAssignment,this::addTask, this::removeAssignment, this::removeTask);
//		for(Assignment assignment : grouping.getAssignments()) {
//			for(Task task : assignment.getTasks()) addTask(task);
//			addAssignment(assignment);
//		}
	}
	
	public Dimension getPreferredSize() { 
		int height = tree.getPreferredSize().height;
		tree.setSize(getWidth(), height);
		return new Dimension(getWidth(), height); 
	}
	
	public void setAssignmentDisplayUpdate(Consumer<Assignment> updater) { this.rightClickUpdate = updater; }
	
	private void triggerPopup(MouseEvent e) {
		if(!(e.getSource() instanceof TreeNode)) return;
		
		@SuppressWarnings("unchecked")
		TreeNode<Object> node = (TreeNode<Object>) e.getSource();
		if(node instanceof EmptyNode) return;
		
		if(node.get() == null) {
			new RootPopupMenu(root::createAssignmentGroup).show(this, e.getX(), e.getY());
			return;
		}
		
		if(node.get() instanceof AssignmentGrouping) {
			new GroupPopupMenu((AssignmentGrouping) node.get()).show(this, e.getX(), e.getY());
			return;
		}
		
		if(node.get() instanceof Assignment) {
			new AssignmentPopupMenu((Assignment) node.get(), rightClickUpdate).show(this, e.getX(), e.getY());
			return;
		}
		
		if(node.get() instanceof Task) {
			new TaskPopupMenu((Task) node.get(), rightClickUpdate).show(this, e.getX(), e.getY());
			return;
		}
	}
	
	public void addChangeListener(ChangeListener listener) { tree.addChangeListener(listener); }
	public TreeNode<Object> getSelectedNode() { return tree.getSelectedNode(); }
	
	private static <T> JComponent createCheckBox(Column<T> col, T val, JComponent panel) {
		JCheckBox checkbox;
		
		if(panel == null) {
			checkbox = new JCheckBox("  ");
			checkbox.addActionListener(e -> col.set(val, checkbox.isSelected()));
			panel = wrap(checkbox);
		} else {
			checkbox = (JCheckBox) panel.getComponent(1);
		}
		
		if(val instanceof Task) {
			checkbox.setEnabled(!((Task) val).isOverride());
			if(checkbox.isEnabled()) checkbox.setSelected((boolean) col.get(val));
			
		} else if(val instanceof Assignment) {
			checkbox.setSelected((boolean) col.get(val));
			
			if(checkbox.isSelected()) checkbox.setEnabled(((Assignment) val).isOverride());
			else checkbox.setEnabled(true);
			
		} else {
			checkbox.setSelected((boolean) col.get(val));
		}
		
		return panel;
	}
	
	private static <T> JComponent createTextField(Column<T> col, T val, JComponent raw) {
		JTextField textField;
		
		if(raw == null) {
			textField = new JTextField();
			textField.addActionListener(e -> col.set(val, textField.getText()));
			textField.addFocusListener(new FocusAdapter() { 
				public void focusLost(FocusEvent e) { col.set(val, textField.getText()); }
			});
		} else {
			textField = (JTextField) raw;
		}
		
		textField.setText((String) col.get(val));
		return textField;
	}
	
	private static JPanel wrap(JComponent component) {
		JPanel panel = new JPanel();
		panel.add(Box.createHorizontalGlue());
		panel.add(component);
		panel.add(Box.createHorizontalGlue());
		panel.setOpaque(false);
		panel.setBackground(new Color(255, 255, 255, 0));
		return panel;
	}
	
	private int getNodeLevel(TreeNode<?> node, int level) { 
		return node == null ? level : getNodeLevel(node.getRoot(), level + 1); 
	}
	
//	public void updateAll() {
//		for(Assignment assignment : assignmentNodes.keySet()) {
//			assignment.update();
//			updateAssignment(assignment);
//		}
//	}
	
	public void updateAssignment(Assignment assignment) {
		if(!assignmentNodes.containsKey(assignment)) throw new IllegalArgumentException("Assignment not found!");
		
		TreeNode<Object> node = assignmentNodes.get(assignment);
		node.getMetadata().setName(assignment.getName());
		
		tree.updateNode(node);
		node.getAttachedRow().update(assignment);
		
		for(Task task : assignment.getTasks()) {
			node = taskNodes.get(task);
			node.getMetadata().setName(task.getName());
			
			tree.updateNode(node);
			node.getAttachedRow().update(task);
		}
	}
	
	public void addAssignmentGroup(AssignmentGrouping grouping) {
		if(groupingNodes.containsKey(grouping)) return;
		
		TreeNode<Object> node = controller.addRow(1, root, grouping);
		groupingNodes.put(grouping, node);
		node.setNeverEmpty(true);
	} 
	
	public void addAssignment(Assignment assignment) {
		if(assignmentNodes.containsKey(assignment)) return;
		
		AssignmentGrouping grouping = assignment.getGrouping();
		if(!groupingNodes.containsKey(grouping)) addAssignmentGroup(grouping);
		
		TreeNode<Object> node = controller.addRow(2, groupingNodes.get(grouping), assignment);
		assignmentNodes.put(assignment, node);
	} 
	
	public void addTask(Task task) {
		Assignment assignment = task.getAssignment();
		if(!assignmentNodes.containsKey(assignment)) addAssignment(assignment);
		taskNodes.put(task, controller.addRow(3, assignmentNodes.get(assignment), task));
	}
	
	public void removeAssignmentGroup(AssignmentGrouping grouping) {
		TreeNode<Object> node = groupingNodes.get(grouping);
		if(node == null) throw new IllegalArgumentException("No such grouping: " + grouping);
		node.getRoot().remove(node);
	}
	
	public void removeAssignment(Assignment assignment) {
		TreeNode<Object> node = assignmentNodes.get(assignment);
		if(node == null) throw new IllegalArgumentException("No such Assignment: " + assignment);
		node.getRoot().remove(node);
	}
	
	public void removeTask(Task task) {
		TreeNode<Object> node = taskNodes.get(task);
		if(node == null) throw new IllegalArgumentException("No such Task: " + task);
		node.getRoot().remove(node);
	}

	public void nodeChanged(TreeNode<Object> source, TreeNode<Object> root, Object oldValue) { }
	public void structureChanged(TreeNode<Object> source, TreeNode<Object> root, boolean added) {
		if(!added) return;
		Object value = source.get();
		
		if(value instanceof Task) {
			Task task = (Task) value;
			Assignment assignment = (Assignment) source.getRoot().get();
			
			if(assignment != task.getAssignment()) {
				assignment.addTask(task);
				task.delete();
			}
			
		} else if(value instanceof Assignment) {
			Assignment assignment = (Assignment) value;
			AssignmentGrouping grouping = (AssignmentGrouping) source.getRoot().get();
			
			if(grouping != assignment.getGrouping()) 
				assignment.changeGrouping(grouping);
		}
	} 
}
