package components.tree.table;

import java.awt.CardLayout;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.JPanel;

import components.tree.TreeNode;
import components.tree.table.Column.CompCreator;

public class TableController<T> {
	public static interface RebuildFunction { public void rebuild(); }
	
	private ArrayList<TableLevelController<T>> columns;
	private RebuildFunction rebuild;
	private JPanel tableHeader;
	private CardLayout layout;
	
	public TableController(RebuildFunction rebuild) {
		this.rebuild = rebuild;
		columns = new ArrayList<>();
		
		tableHeader = new JPanel();
		tableHeader.setLayout(layout = new CardLayout());

		addColumn(0, "   ", null, null, null, null);
	}
	
	public JPanel getHeader() { return tableHeader; }
	public void displayLevelHeader(int level) { layout.show(tableHeader, "" + level); }
	
	public void fillWidth(int width) {
		for(TableLevelController<T> controller : columns) {
			controller.updateWidth(width);
		}
	}
	
	public void addColumn(int level, String name, Method acc, Method mod, CompCreator<T> create, Comparator<TreeNode<T>> sorter) {
		addColumn(level, name, acc, mod, create, sorter, -1);
	}
	
	public void addColumn(int level, String name, Method acc, Method mod, CompCreator<T> create, Comparator<TreeNode<T>> sorter, int width) {
		TableLevelController<T> controller = getLevelController(level, true);
		Column<T> column = new Column<>(name, acc, mod, create, sorter, controller);
		column.setTargetWidth(width);
		controller.add(column);
	}
	
	public TreeNode<T> addRow(int level, TreeNode<T> parent, T value) {
		TableLevelController<T> controller = getLevelController(level, true);
		TreeNode<T> node = new TreeNode<>(value);
		TableRow<T> row = new TableRow<>(controller, node, value);
		
		parent.append(node);
		controller.add(row);
		
		return node;
	}
	
	private TableLevelController<T> getLevelController(int level, boolean create) {
		TableLevelController<T> controller = level >= this.columns.size() ? null : this.columns.get(level);
		
		if(controller == null) {
			for(int i = columns.size(); i <= level; i ++) {
				columns.add(i, controller = new TableLevelController<>(this, i));
				tableHeader.add(controller, "" + i);
			}
		}
		
		return controller;
	}

	protected TableLevelController<T> getLevelController(int level) { return this.columns.get(level); }
	protected void rebuild() { rebuild.rebuild(); }
}
