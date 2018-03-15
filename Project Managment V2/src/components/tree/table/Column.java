package components.tree.table;

import static components.color.ColorLookupTable.BACKGROUND_COLOR;
import static components.color.ColorLookupTable.FOREGROUND_COLOR;
import static components.color.ColorLookupTable.SELECT_BG_COLOUR;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;

import components.color.ColorLookupTable;
import components.color.ColoredIconUtil;
import components.tree.ComponentTree;
import components.tree.TreeNode;
import components.tree.table.TableRow.TableBorder;

public class Column<T> extends JComponent implements Comparator<TreeNode<T>>, MouseInputListener {
	private static final long serialVersionUID = 6505333788725055524L;
	private static final Image ARROW_IMAGE = new ImageIcon(ComponentTree.class.getResource("Arrow.png")).getImage();
	
	private static final Icon NONE = ColoredIconUtil.emptyImage();
	private static final Icon ACCENDING_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, FOREGROUND_COLOR.darker(), BACKGROUND_COLOR);
	private static final Icon DECENDING_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, BACKGROUND_COLOR.brighter(), -Math.PI, BACKGROUND_COLOR);
	private static final Icon ACCENDING_ROLLOVER_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, SELECT_BG_COLOUR.brighter(), BACKGROUND_COLOR);
	private static final Icon DECENDING_ROLLOVER_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, SELECT_BG_COLOUR.brighter(), -Math.PI, BACKGROUND_COLOR);
	
	public static interface CompCreator<T> { public JComponent createComponent(Column<T> column, T obj, JComponent comp); }

	private int width, targetWidth;
	
	private Method varAccess;
	private Method varWrite;
	
	private CompCreator<T> creator;
	private Comparator<TreeNode<T>> sorter;
	
	private TableLevelController<T> controller;
	private JLabel arrow;
	
//	private boolean resizeDrag;
	
	public Column(String name, Method varAccess, Method varWrite, CompCreator<T> creator, Comparator<TreeNode<T>> sorter, TableLevelController<T> controller) {
		this.varAccess = varAccess;
		this.varWrite = varWrite;
		
		this.creator = creator;
		this.sorter = sorter != null ? sorter : this::compareRows;

		this.targetWidth = 100;
		this.controller = controller;
		
		{
			JPanel panel = createLabel(name, targetWidth, true);
			arrow = (JLabel) ((BorderLayout) panel.getLayout())
					.getLayoutComponent(name == null ? BorderLayout.CENTER : BorderLayout.EAST);
			panel.addMouseListener(this); panel.addMouseMotionListener(this);
			
			setLayout(new BorderLayout());
			add(panel);
		}
	}
	
	public JComponent createNew(T obj) { return creator == null ? new JLabel() : creator.createComponent(this, obj, null); }
	public JComponent update(JComponent comp, T obj) { return creator == null ? comp : creator.createComponent(this, obj, comp); }
	
	public int compare(TreeNode<T> o1, TreeNode<T> o2) {
		return controller.isAccending() ? sorter.compare(o1, o2) : sorter.compare(o2, o1); 
	}

	@SuppressWarnings("unchecked")
	private <R extends Comparable<R>> int compareRows(TreeNode<T> node_1, TreeNode<T> node_2) {
		R v1 = (R) get(node_1.get()), v2 = (R) get(node_2.get());
		return v1 == null ? -1 : v1.compareTo(v2);
	}

	public Object get(T obj) { 
		if(varAccess == null) return null;
		try { return varAccess.invoke(obj); } 
		catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { 
			throw new IllegalArgumentException("Failed to get value from " + obj, e);
		}
	}
	
	public void set(T obj, Object... arg) { 
		if(varWrite == null) return;
		try { varWrite.invoke(obj, arg); } 
		catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { 
			throw new IllegalArgumentException("Failed to set value of " + obj, e);
		}
	}
	
	protected void resize(int width, boolean first) {
		this.width = width;// - TableRow.BORDER_THICKNESS;
		resizeLabel(getComponent(0), width, first);
		Dimension size = new Dimension(width, ComponentTree.ROW_HEIGHT);
		setMinimumSize(size); setSize(size);
	}
	
	public void setTargetWidth(int width) { this.targetWidth = width; }
	
	public int getWidth() { return width; }
	public int getTargetWidth() { return targetWidth; }
	public void clearSort() { arrow.setIcon(NONE); }

	public void mouseClicked(MouseEvent e) {
		controller.sortBy(this);
		arrow.setIcon(controller.isAccending() ? ACCENDING_ROLLOVER_ICON : DECENDING_ROLLOVER_ICON);
	}

	public void mouseEntered(MouseEvent e) {
		if(controller.getSorter() != this) return;
		arrow.setIcon(controller.isAccending() ? ACCENDING_ROLLOVER_ICON : DECENDING_ROLLOVER_ICON);
	}

	public void mouseExited(MouseEvent e) {
		if(controller.getSorter() != this) return;
		arrow.setIcon(controller.isAccending() ? ACCENDING_ICON : DECENDING_ICON);
	}
	
//	private static final int RESIZE_GAP = 5;
//	
//	private int lastX;
	public void mouseDragged(MouseEvent e) {
//		if(resizeDrag) {
//			int dx = e.getX() - lastX;
//			targetWidth += dx * (float) targetWidth / width;
//			if(targetWidth < 16) targetWidth = 16;
//			controller.updateWidth(controller.lastWidth());
//			lastX = e.getX();
//			
//			System.out.println(targetWidth + " -> " + dx + " Ratio: " +  (float) targetWidth / width);
//			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
//		}
	}

	public void mouseMoved(MouseEvent e) { 
//		setCursor(Cursor.getPredefinedCursor(
//				e.getX() > super.getWidth() - RESIZE_GAP ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR
//			));
	}

	public void mousePressed(MouseEvent e) { 
//		if(e.getX() > super.getWidth() - RESIZE_GAP) { 
//			resizeDrag = true; 
//			lastX = e.getX(); 
//		}
	}
	
	public void mouseReleased(MouseEvent e) { 
//		resizeDrag = false; 
//		mouseMoved(e);
	}
	
	public static JPanel createLabel(String text, int width, boolean addArrow) {
		JPanel panel = new JPanel(); 
		
		panel.setBorder(new CompoundBorder(new TableBorder(false), new EmptyBorder(0, 3, 0, 3)));
		panel.setLayout(new BorderLayout(3, 3));
		
		if(text != null) {
			JLabel label = new JLabel(text);
			label.setFont(ColorLookupTable.HEADER_FONT);
			panel.add(label, BorderLayout.WEST);
		}
		
		if(addArrow) {
			JLabel arrow = new JLabel(NONE);
			panel.add(arrow, text == null ? BorderLayout.CENTER : BorderLayout.EAST);
			panel.setComponentZOrder(arrow, 0);
		}
		
		resizeLabel(panel, width, false);
		return panel;
	}
	
	public static final Graphics GRAPHICS = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
	public static void resizeLabel(Component label, int width, boolean first) {
//		width = width - TableRow.BORDER_THICKNESS;
		Dimension size = new Dimension(width, GRAPHICS.getFontMetrics(ColorLookupTable.HEADER_FONT).getHeight());
		label.setMinimumSize(size); label.setPreferredSize(size); label.setSize(size); label.setMaximumSize(size);
		
		((TableBorder) ((CompoundBorder) ((JComponent) label).getBorder()).getOutsideBorder()).setLeft(first);
	}
}
