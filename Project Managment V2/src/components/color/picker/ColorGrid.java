package components.color.picker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorGrid extends JPanel implements MouseListener {
	private static final long serialVersionUID = 4924805034391841689L;
	
	private static final int SIZE = 25;
	private static final int SPACE = 3;
	
	private Color[] stack;
	private int rowSize;
	
	public ColorGrid(int rows, int cols) {
		this.rowSize = rows;
		stack = new Color[rows * cols];
		Arrays.fill(stack, Color.WHITE);
		
		setPreferredSize(new Dimension((SIZE + SPACE) * cols, (SIZE + SPACE) * rows));
		setLayout(new GridLayout(rows, cols, SPACE, SPACE));

		for(int i = 0; i < cols; i ++) {
		for(int j = 0; j < rows; j ++) {
			JPanel colorPanelWrapper = new JPanel();
			colorPanelWrapper.setBackground(Color.WHITE);
			colorPanelWrapper.setLayout(new BoxLayout(colorPanelWrapper, BoxLayout.X_AXIS));
			colorPanelWrapper.setPreferredSize(new Dimension(SIZE, SIZE));
			colorPanelWrapper.setMinimumSize(new Dimension(SIZE, SIZE));
			
			JPanel colorPanel = new JPanel();
			colorPanel.setBackground(get(j, i));
//			colorPanelWrapper.setMinimumSize(new Dimension(SIZE, SIZE));
			colorPanel.setUI(new ColorSamplePanelUI());
			colorPanelWrapper.add(colorPanel);
			
			colorPanel.setName("colorPanel[" + i + "," + j + "]");
			colorPanel.addMouseListener(this);
			
			add(colorPanelWrapper);
		}}
	}

	public void push(Color color) { 
		System.arraycopy(stack, 0, stack, 1, stack.length - 1);
		stack[0] = color;
		
		Component[] components = getComponents(); 
		for(int i = 0; i < stack.length / rowSize; i ++) {
		for(int j = 0; j < rowSize; j ++) {
			((Container) components[j + i * rowSize]).getComponents()[0].setBackground(stack[j + i * rowSize]);
		}}
	}

	public Color get(int row, int col) { return stack[col * rowSize + row]; }

	private ArrayList<ChangeListener> listeners;
	public void addChangeListenser(ChangeListener listener) {
		if(listeners == null) listeners = new ArrayList<>();
		listeners.add(listener);
	}
	
	public void fireChangeEvent(int row, int col) {
		if(listeners == null) return;
		
		ChangeEvent event = new ChangeEvent(get(row, col));
		for(ChangeListener listener : listeners)
			listener.stateChanged(event);
	}
	
	public void mouseClicked(MouseEvent e) {
		if(!(e.getSource() instanceof JPanel)) return;
		JPanel panel = (JPanel) e.getSource();
		
		if(!panel.getName().startsWith("colorPanel")) return;
		String[] parts = panel.getName().split(",");
		
		int col = Integer.parseInt(parts[0].substring(parts[0].indexOf("[") + 1));
		int row = Integer.parseInt(parts[1].substring(0, parts[1].indexOf("]")));
		
		fireChangeEvent(row, col);
	}

	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
}
