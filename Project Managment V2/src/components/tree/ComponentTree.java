package components.tree;

import static components.color.ColorLookupTable.BACKGROUND_COLOR;
import static components.color.ColorLookupTable.FOREGROUND_COLOR;
import static components.color.ColorLookupTable.SELECT_BG_COLOUR;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.PanelUI;

import components.color.ColorLookupTable;
import components.color.ColoredIconUtil;
import components.tree.TreeNode.TreeListener;
import components.tree.table.TableRow;

public class ComponentTree<T> extends JComponent {
	private static final long serialVersionUID = -4036508478177633065L;
	
	private static final int TRIM = 1;
	
	public static final int ICON_SIZE = 24;
	public static final int PADDING = 3;
	
	public static final int LEVEL_SHIFT_X = ICON_SIZE + PADDING * 2;
	public static final int LEVEL_SHIFT_Y = ICON_SIZE + PADDING * 2;
	
	public static final int ROW_HEIGHT = LEVEL_SHIFT_Y;
	public static final int ROW_LABEL_WIDTH = LEVEL_SHIFT_X * 5 + 200;
	
	private static final float ALPHALPHA = .45f;
	
	private static final Image ARROW_IMAGE = new ImageIcon(ComponentTree.class.getResource("Arrow.png")).getImage();
	
	private static final Icon EXPANDED_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.darker(FOREGROUND_COLOR, .75f));
	private static final Icon COLLAPSED_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.brighter(BACKGROUND_COLOR, .25f), -Math.PI / 2);
	private static final Icon EXPANDED_ROLLOVER_ICON  = ColoredIconUtil.prepImage(ARROW_IMAGE, SELECT_BG_COLOUR.brighter());
	private static final Icon COLLAPSED_ROLLOVER_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, SELECT_BG_COLOUR.brighter(), -Math.PI / 2);
	private static final Icon HIDDEN_ICON = new ImageIcon(new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB));
	
	private TreeNode<T> root;
	private SelectionListener selectionListener;
	private NodeStructureChangeListener nodeListener;
	
	private Row movingRow, selectedRow;
	private JPanel movingElement;
	private JLabel notionTo, lastNotionTo;
	private boolean isNotionToTop;
	
	private Timer nodeOpener, rebuildStabilizer;
	private JLabel timerNotion;
	
	private ArrayList<ChangeListener> listeners;
	private ArrayList<Icon> iconLevelMap;
	
	private Consumer<MouseEvent> popupAction;
	
	@SuppressWarnings("unchecked")
	public ComponentTree(TreeNode<T> root) {
		setUI(new UI());

		setBorder(new EmptyBorder(5 - PADDING, 5, 5 - PADDING, 5));
		super.setLayout(null);
		
		rebuildStabilizer = new Timer(100, e -> fullRebuild());
		rebuildStabilizer.setRepeats(false);
		rebuildStabilizer.setCoalesce(false);
		
		nodeListener = new NodeStructureChangeListener();
		selectionListener = new SelectionListener();
		addMouseMotionListener(selectionListener);
		addMouseListener(selectionListener);
		setRoot(root);
		
		nodeOpener = new Timer(750, e -> {
			if(timerNotion == notionTo) {
				((Row) notionTo.getParent().getParent()).node.setExpanded(true);
				updateCollapse();
				
				timerNotion = null;
			}
		});

		nodeOpener.setRepeats(false);
		nodeOpener.setCoalesce(false);
		
		listeners = new ArrayList<>();
		iconLevelMap = new ArrayList<>();
	}
	
	public void addChangeListener(ChangeListener listener) { listeners.add(listener); }
	public void removeChangeListener(ChangeListener listener) { listeners.remove(listener); }
	
	public void setPopupAction(Consumer<MouseEvent> popupAction) { this.popupAction = popupAction; }
	
	public void setRoot(TreeNode<T> root) { 
		if(this.root != null) this.root.removeTreeListener(nodeListener);
		root.addTreeListener(nodeListener); 
		this.root = root; 
		
		rebuildStabilizer.restart(); 
	}
	
	public TreeNode<T> getSelectedNode() { return selectedRow == null ? null : selectedRow.node; }
	
	public void collapseAll() { root.walk((i, node) -> node.setExpanded(false)); updateCollapse(); }
	public void expandAll()   { root.walk((i, node) -> node.setExpanded(true));  updateCollapse(); }
	
	private class NodeStructureChangeListener implements TreeListener<T> {
		public void nodeChanged(TreeNode<T> source, TreeNode<T> root, T oldValue) {
			System.out.println("Node Changes");
		}

		public void structureChanged(TreeNode<T> source, TreeNode<T> root, boolean added) {
			rebuildStabilizer.restart();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void updateCollapse() {
		int hideUntil = Integer.MAX_VALUE;
		int sumShift = 0;
		
		for(Component component : getComponents()) {
			if(!(component instanceof ComponentTree.Row)) continue;
			Row row = (Row) component;

			boolean wasVisible = row.isVisible();
			row.setVisible(row.level < hideUntil);
			if(row.isVisible()) hideUntil = !row.node.isExpanded() ? row.level + 1 : Integer.MAX_VALUE;
			
			row.setLocation(0, row.getY() + sumShift);
			
			if(wasVisible != row.isVisible()) sumShift += LEVEL_SHIFT_Y * (wasVisible ? -1 : 1);
			((JToggleButton) row.getComponent(0)).setSelected(row.node.isExpanded());
		}
	}

	@SuppressWarnings("unchecked")
	public void updateNode(TreeNode<T> node) {
		for(int i = 0; i < ComponentTree.this.getComponentCount(); i ++) {
			Component comp = ComponentTree.super.getComponent(i);
			if(comp instanceof ComponentTree.Row) {
				Row row = (Row) comp;
				if(row.node != node) continue;
				row.updateLabel();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void fullResize() {
		for(int i = 0; i < ComponentTree.this.getComponentCount(); i ++) {
			Component comp = ComponentTree.super.getComponent(i);
			if(comp instanceof ComponentTree.Row) ((Row) comp).updateSize();
		}
	}
	
	public void fullRebuild() {
		for(int i = 0, count = ComponentTree.this.getComponentCount(); i < count; i ++) ComponentTree.super.remove(0);
		root.walk(false, this::createRow); updateCollapse();
		revalidate(); repaint();
	}
	
	private void createRow(int level, TreeNode<T> node) { 
		super.addImpl(new Row(node, level), null, -1); 
	}
	
	private class Row extends JPanel {
		private static final long serialVersionUID = 4878656006010370343L;
		
		private TreeNode<T> node;
		private int level;
		
		public Row(TreeNode<T> node, int level) {
			TreeNodeMetadata metadata = node.getMetadata();
			this.level = level;
			this.node = node;
			
			setLayout(new BorderLayout(PADDING, 0));
			setBorder(new CompoundBorder(new RowBorder(), new EmptyBorder(0, LEVEL_SHIFT_X * level, 0, 0)));
			setMaximumSize(new Dimension(10000, LEVEL_SHIFT_Y));
			
//			setBackground(new Color(Color.HSBtoRGB((float) Math.random(), 1, 1)));//ComponentTree.this.getBackground()); TODO
			
			add(createButton(node), BorderLayout.WEST);
			add(createLabel(level, metadata), BorderLayout.CENTER);

			int extra = (node.getAttachedRow() == null ? PADDING : 0);
			int padding = (int) (ROW_LABEL_WIDTH - getPreferredSize().getWidth());
			Component comp = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
			Dimension current = comp.getPreferredSize(), size = new Dimension(current.width + padding + extra, current.height);
			comp.setMinimumSize(size); comp.setPreferredSize(size); comp.setMaximumSize(size);
			
			if(node.getAttachedRow() != null)
				add(node.getAttachedRow(), BorderLayout.EAST);
			
			setLocation(0, ComponentTree.this.getComponentCount() * LEVEL_SHIFT_Y);
			updateSize();
		}
		
		public Dimension getPreferredSize() {
			Dimension superSize = super.getPreferredSize();
			return new Dimension(superSize.width, LEVEL_SHIFT_Y);
		}
		
		public void updateSize() { setSize(this == movingRow ? new Dimension(0, 0) : getPreferredSize()); }
		
		public void updateLabel() {
			TreeNodeMetadata metadata = node.getMetadata();
			BorderLayout layout = (BorderLayout) getLayout();
			
			String currentText = ((JLabel) ((JPanel) layout.getLayoutComponent(BorderLayout.CENTER)).getComponent(0)).getText();
			if(currentText.equals(metadata.getName())) return;
			
			remove(layout.getLayoutComponent(BorderLayout.EAST));
			remove(layout.getLayoutComponent(BorderLayout.CENTER));
			
			add(createLabel(level, metadata), BorderLayout.CENTER);
			
			int extra = (node.getAttachedRow() == null ? PADDING : 0);
			int padding = (int) (ROW_LABEL_WIDTH - getPreferredSize().getWidth());
			Component comp = layout.getLayoutComponent(BorderLayout.CENTER);
			Dimension current = comp.getPreferredSize(), size = new Dimension(current.width + padding + extra, current.height);
			comp.setMinimumSize(size); comp.setPreferredSize(size); comp.setMaximumSize(size);
			
			if(node.getAttachedRow() != null) add(node.getAttachedRow(), BorderLayout.EAST);
			
			revalidate();
		}
	}
	
	public static class RowBorder implements Border {
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			g.setColor(ColorLookupTable.BORDER_PRIMARY);
			for(int i = 0; i < TableRow.BORDER_THICKNESS; i ++) {
				g.drawLine(0, height - i - 1, c.getWidth(), height - i - 1);
			}
		}

		public Insets getBorderInsets(Component c) { return new Insets(0, 0, TableRow.BORDER_THICKNESS, 0); }
		public boolean isBorderOpaque() { return true; }
	}
	
	private JComponent createLabel(int level, TreeNodeMetadata metadata) {
		JLabel label;
		
		if(metadata == null) {
			label = new JLabel("  <Empty>", null, JLabel.LEADING);
			label.setForeground(ColorLookupTable.TEXT_DISABLE_BG_COLOR);
			
		} else {
			label = new JLabel(metadata.getName(), lookupIcon(metadata, level), JLabel.LEADING) {
				private static final long serialVersionUID = -4913469516036215706L;
				public void paint(Graphics g) {
					if(getParent() == movingElement)
						((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ALPHALPHA));
					super.paint(g);
				}
			};
			
			label.setForeground(this.getForeground());
		}

		label.setBackground(this.getBackground()); 
		label.setBorder(new EmptyBorder(0, TRIM, TRIM * 2, TRIM));
		label.setIconTextGap(PADDING * 2);
		label.setOpaque(true);
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING - TRIM, PADDING - TRIM)) {
			private static final long serialVersionUID = -7764436013610979447L;
			public void paint(Graphics g) {
				if(this == movingElement)
					((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ALPHALPHA));
				super.paint(g);
			}
		};
		
		panel.setBackground(this.getBackground()); 
		panel.add(label);
		return panel;
	}
	
	private JToggleButton createButton(TreeNode<T> node) {
		JToggleButton button = new JToggleButton();

		button.setRolloverIcon(COLLAPSED_ROLLOVER_ICON);
		button.setPressedIcon(COLLAPSED_ROLLOVER_ICON);
		button.setIcon(COLLAPSED_ICON);
		
		button.setRolloverSelectedIcon(EXPANDED_ROLLOVER_ICON);
		button.setSelectedIcon(EXPANDED_ICON);
		
		button.setDisabledIcon(HIDDEN_ICON);
		button.setDisabledSelectedIcon(HIDDEN_ICON);
		
		Dimension size = new Dimension(ICON_SIZE, ICON_SIZE);
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setMaximumSize(size);

		button.setBorder(null);
		button.setFocusable(false);
		button.setContentAreaFilled(false);
		button.addActionListener(e -> {
			node.setExpanded(button.isSelected());
			updateCollapse();
		});
		
		button.setEnabled(node instanceof EmptyNode ? false : node.getMetadata().isContainer());
		
		return button;
	}
	
	private class SelectionListener implements MouseListener, MouseMotionListener {
		private Point grabPoint;
		private JLabel highlight;
		
		@SuppressWarnings("unchecked")
		public void mouseDragged(MouseEvent e) {
			Point loc = getLocationOnScreen();
			
			Point p = new Point(
					Math.max(0, Math.min(e.getXOnScreen() - loc.x, getWidth())), 
					Math.max(0, Math.min(e.getYOnScreen() - loc.y, getHeight()))
				);
			
			if(!(e.getSource() instanceof ComponentTree || e.getSource() instanceof JLabel)) return;
			if(movingElement == null && selectedRow != null) {
				JLabel label = findLabel(e.getLocationOnScreen(), ComponentTree.this, false);
				
				if(label == null) return;
				if(label.getParent().getParent() != selectedRow) return;
				
				movingElement = (JPanel) label.getParent(); 
				label.setBackground(getBackground());
				selectedRow = null;
				
				ChangeEvent changeEvent = new ChangeEvent(this);
				listeners.forEach(l -> l.stateChanged(changeEvent));
			}
			
			if(movingElement == null) return;
			
			if(movingRow == null) {
				movingRow = (Row) movingElement.getParent();
				
				movingRow.node.setExpanded(false);

				//											new LineBorder(getForeground().darker())
				movingElement.setBorder(new CompoundBorder(ColorLookupTable.PLAIN_BORDER, movingElement.getBorder()));
				movingRow.remove(movingElement);
				movingRow.getComponent(0).setVisible(false);
				movingRow.setSize(0, 0);
				updateCollapse();
				
				Dimension current = movingElement.getPreferredSize(); movingElement.setPreferredSize(null);
				movingElement.setSize(movingElement.getPreferredSize());
				ComponentTree.super.addImpl(movingElement, null, 0);
				movingElement.setPreferredSize(current);
			}
			
			movingElement.setLocation(p.x - grabPoint.x, p.y - grabPoint.y);
			repaint();
			
			Point ps = e.getLocationOnScreen();
			notionTo = findLabel(ps, ComponentTree.this, true);
			
			if(notionTo == null) return;
			
			Row endRow = (Row) notionTo.getParent().getParent();
			if(endRow.node.getRoot() == null) notionTo = null;
			if(endRow.level != movingRow.level) notionTo = null; // TODO: Locks to same level
			
			if(notionTo == null) return;
			isNotionToTop = ps.y - notionTo.getLocationOnScreen().getY() < notionTo.getHeight() / 3;
			
			if(notionTo != timerNotion) {
				nodeOpener.stop();
				timerNotion = notionTo;
				nodeOpener.start();
			}
		}

		public void mouseMoved(MouseEvent e) {
			if(highlight != null) highlight.setBackground(getBackground());
			highlight = findLabel(e.getLocationOnScreen(), ComponentTree.this, false);
			if(highlight != null) highlight.setBackground(SELECT_BG_COLOUR);
			
			if(selectedRow != null) ((JComponent) selectedRow.getComponent(1)).getComponent(0).setBackground(SELECT_BG_COLOUR);
		}
		
		private JLabel findLabel(Point ps, Container comp, boolean allowEmpty) {
			for(Component sub : comp.getComponents()) {
				if(!sub.isShowing()) continue;
				if(sub == movingElement) continue;
				
				Point sLoc = sub.getLocationOnScreen();
				if(!sub.contains(ps.x - sLoc.x, ps.y - sLoc.y)) continue;
				
				if(sub instanceof JLabel) {
					if(!allowEmpty && ((JLabel) sub).getText().endsWith("<Empty>")) continue; 
					else return (JLabel) sub;
				}
				
				if(!(sub instanceof JPanel)) continue;
				
				return findLabel(ps, (Container) sub, allowEmpty);
			}
			
			return null;
		}

		@SuppressWarnings("unchecked")
		public void mousePressed(MouseEvent e) { 
			if(e.isPopupTrigger() && popupAction != null) {
				JLabel label = findLabel(e.getLocationOnScreen(), ComponentTree.this, false);
				if(label == null) return;
				Row row = (Row) label.getParent().getParent();
				
				e.setSource(row.node);
				popupAction.accept(e);
				return;
			}
			
			JLabel label = findLabel(e.getLocationOnScreen(), ComponentTree.this, false);
			if(label == null) return;

			if(selectedRow != null) ((JComponent) selectedRow.getComponent(1)).getComponent(0).setBackground(BACKGROUND_COLOR);
			selectedRow = (Row) label.getParent().getParent();
			
			if(e.isPopupTrigger() && popupAction != null) {
				e.setSource(selectedRow.node);
				popupAction.accept(e);
			}
			
			Point ps = e.getLocationOnScreen(), sLoc = label.getLocationOnScreen();
			grabPoint = new Point(ps.x - sLoc.x, ps.y - sLoc.y);

			ChangeEvent changeEvent = new ChangeEvent(this);
			listeners.forEach(l -> l.stateChanged(changeEvent));
		}
		
		@SuppressWarnings("unchecked")
		public void mouseReleased(MouseEvent e) {
			if(e.isPopupTrigger() && popupAction != null) {
				JLabel label = findLabel(e.getLocationOnScreen(), ComponentTree.this, false);
				if(label == null) return;
				Row row = (Row) label.getParent().getParent();
				
				e.setSource(row.node);
				popupAction.accept(e);
				return;
			}
			
			if(selectedRow != null) return;

			dontAdd:
			if(notionTo != null) {
				Row endRow = (Row) notionTo.getParent().getParent();
				
				if(isNotionToTop && endRow.node.getPrev() == movingRow.node)
					break dontAdd;
				
				if(isNotionToTop) endRow.node.insertBefore(movingRow.node);
				else endRow.node.insertAfter(movingRow.node);
			}
			
			movingRow = null;
			movingElement = null;
			notionTo = lastNotionTo = null;
			grabPoint = null;
			
			fullRebuild();
		}

		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }
		public void mouseClicked(MouseEvent e) { }
	}
	
	protected void paintChildren(Graphics g) {
		if(lastNotionTo != null) { lastNotionTo.setBackground(getBackground()); lastNotionTo = null; }
		if(notionTo != null) { notionTo.setBackground(SELECT_BG_COLOUR); lastNotionTo = notionTo; }
		
		super.paintChildren(g);

		Graphics2D g2d = (Graphics2D) g;
		if(notionTo != null) {
			g2d.setStroke(new BasicStroke(3));
			g2d.setColor(isNotionToTop ? Color.WHITE : getForeground());
			
			Point me = ComponentTree.this.getLocationOnScreen();
			Point p1 = notionTo.getLocationOnScreen();
			
			p1.x -= me.x + 1; p1.y -= me.y + (isNotionToTop ? 1 : 0);
			if(!isNotionToTop) p1.y += notionTo.getHeight();
			
			Point p2 = new Point(p1.x, p1.y);
			p2.x += notionTo.getWidth() + 1;

			g2d.draw(new Line2D.Float(p1, p2));
		}
	}
	
	private class UI extends PanelUI {
		public void paint(Graphics g, JComponent c) {
			Graphics2D g2d = (Graphics2D) g;

			int width = c.getWidth();
			int height = c.getHeight();

			g2d.setColor(c.getBackground());
			g2d.fillRect(0, 0, width, height);
			
			Border border = c.getBorder();
			if(border != null) {
				border.paintBorder(c, g2d, 0, 0, width, height);
	
				Insets insets = border.getBorderInsets(c);
				width -= insets.left + insets.right;
				height -= insets.top + insets.bottom;
			
				g2d.translate(insets.left, insets.top);
			}
		}
	}
	
	public void assignIcon(int level, Icon icon) {
		assignIcon(level, icon, true);
		assignIcon(level, icon, false);
	}
	
	public void assignIcon(int level, Icon icon, boolean forContainer) {
		if(iconLevelMap.size() < (level + 1) * 2) 
			for(int i = iconLevelMap.size(); i < (level + 1) * 2; i ++)
				iconLevelMap.add(HIDDEN_ICON);
		iconLevelMap.set(level * 2 + (forContainer ? 0 : 1), icon);
		fullRebuild();
	}
	
	private Icon lookupIcon(TreeNodeMetadata metadata, int level) {
		return metadata.getIcon() != null ? metadata.getIcon() : 
			level * 2 >= iconLevelMap.size() ? HIDDEN_ICON : iconLevelMap.get(level * 2 + (metadata.isContainer() ? 0 : 1));
	}
	
	public Dimension getPreferredSize() {
		int sumWidth = 0, sumHeight = 0;
		
		for(Component component : getComponents()) {
			if(component.isVisible()) { 
				sumWidth = Math.max(sumWidth, component.getWidth());
				sumHeight += component.getHeight();
			}
		}
		
		Border border = getBorder();
		if(border != null) {
			Insets insets = border.getBorderInsets(this);
			sumWidth  += insets.left + insets.right;
			sumHeight += insets.top + insets.bottom;
		}
		
		return new Dimension(sumWidth, sumHeight);
	}
	
	protected void addImpl(Component comp, Object constraints, int index) { throw new UnsupportedOperationException(); }
	public void setLayout(LayoutManager mgr) { throw new UnsupportedOperationException(); }
//	public void remove(int index) { throw new UnsupportedOperationException(); }
}