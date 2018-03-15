package components.tree.table;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JPanel;

import components.tree.ComponentTree;

public class TableLevelController<T> extends JPanel {
	private static final long serialVersionUID = 1045440028430963075L;
	private TableController<T> controller; 
	
	private ArrayList<Column<T>> columns;
	private ArrayList<TableRow<T>> rows;
	private int level;
	
	private Column<T> sorter;
	private boolean accending;
	
	private int currentX;
	private int lastWidth;
	
	public TableLevelController(TableController<T> controller, int level) {
		this.controller = controller;
		this.level = level;
		
		columns = new ArrayList<>();
		rows = new ArrayList<>();
		
		setLayout(null);
		setPreferredSize(new Dimension(10000, ComponentTree.ROW_HEIGHT));
	}
	
	public void updateWidth(int width) {
		this.lastWidth = width;
		
		float spreadCount = 0; int unusedWidth = width;
		for(Column<T> column : columns) {
			if(column.getTargetWidth() < 0) spreadCount ++; 
			else unusedWidth -= column.getTargetWidth();
		}
		
		currentX = 0; boolean first = true;
		width += TableRow.BORDER_THICKNESS * columns.size();
		
		for(Column<T> column : columns) {
			column.resize(column.getTargetWidth() < 0 ? (int)(unusedWidth / spreadCount) : column.getTargetWidth(), first);
			first = false;
			
			column.setLocation(currentX, 0);
			column.setSize(column.getSize().width, column.getSize().height);
			
			currentX += column.getWidth();
		}
		
		for(TableRow<T> row : rows) row.updateLayout();
	}
	
	public void add(Column<T> column) {
		this.columns.add(column);
		super.add(column);
		column.setLocation(currentX, 0);
		currentX += column.getWidth();
	}
	
	public void add(TableRow<T> row) {
		this.rows.add(row);
	}
	
	public void sortBy(Column<T> column) {
		if(sorter != null) sorter.clearSort();
		
		if(sorter == column) accending = !accending;
		else accending = true;
		sorter = column;
		
		// Sort ???
		if(level == 0) return;
		for(TableRow<T> row : controller.getLevelController(level - 1).rows) {
			row.getNode().sort(column);
		}
		
		controller.rebuild();
	}
	
	protected Column<T> getSorter() { return sorter; }
	protected boolean isAccending() { return accending; }
	
	protected int lastWidth() { return lastWidth; }
	
	protected ArrayList<Column<T>> getColumns() { return columns; }
}
