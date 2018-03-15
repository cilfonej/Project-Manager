package components.tree.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import components.color.ColorLookupTable;
import components.tree.ComponentTree;
import components.tree.TreeNode;

public class TableRow<T> extends JComponent {
	private static final long serialVersionUID = -4161078147037643593L;
	
	public static final int BORDER_THICKNESS = 2;
//	public  static final Border GRID_BORDER = new TableBorder();
	
	private TableLevelController<T> controller;
	private TreeNode<T> node;
	private int xPadding;
	
	public TableRow(TableLevelController<T> controller, TreeNode<T> node, T value) {
		this.controller = controller;
		this.node = node;
		
		node.attachTableRow(this);
		
		setLayout(null); boolean first = true;
		for(Column<T> col : controller.getColumns()) {
			JComponent comp = col.createNew(value);
//			comp.setBorder(null);
			
			JPanel wrapper = new JPanel(new BorderLayout());
			wrapper.setBorder(new TableBorder(first));
//			wrapper.add(Box.createHorizontalStrut(3), BorderLayout.WEST);
			wrapper.add(comp);
			add(wrapper);
			
			first = false;
		}
		
		updateLayout();
	}
	
	public void updateLayout() {
		int index = 0, x = xPadding;
		for(Column<T> col : controller.getColumns()) {
			Component component = getComponent(index ++);
			component.setSize(col.getWidth(), ComponentTree.ROW_HEIGHT);
			component.setLocation(x, 0); x += col.getWidth();
		}
		
		Dimension size = new Dimension(x, ComponentTree.ROW_HEIGHT);
		setSize(size); setPreferredSize(size); setMinimumSize(size);
	}
	
	public void update(T value) {
		int index = 0;
		for(Column<T> col : controller.getColumns()) {
			JPanel wrapper = (JPanel) getComponent(index ++);
			JComponent component = wrapper.getComponentCount() > 0 ? (JComponent) wrapper.getComponent(0) : null;
			JComponent comp = col.update(component, value);
			
			if(component != comp) {
				wrapper.remove(0);
				wrapper.add(comp);
			}
		}
	}
	
	public void setXPadding(int padding) { this.xPadding = padding; }
	public TreeNode<T> getNode() { return node; }
	
	public static class TableBorder implements Border {
		private boolean left;
		public TableBorder(boolean left) { this.left = left; }
		public void setLeft(boolean left) { this.left = left; }
		
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			g.setColor(ColorLookupTable.BORDER_PRIMARY);
			for(int i = 0; i < BORDER_THICKNESS; i ++) {
				if(left) g.drawLine(i, 0, i, height);
//				if(top)  g.drawLine(0, i, width, i);
				
				g.drawLine(width - i - 1, 0, width - i - 1, height);
				g.drawLine(0, height - i - 1, width, height - i - 1);
			}
		}

		public Insets getBorderInsets(Component c) { 
			return new Insets(-1, left ? BORDER_THICKNESS - 1 : -1, BORDER_THICKNESS - 1, BORDER_THICKNESS - 1); }

		public boolean isBorderOpaque() { return true; }
	}
}
