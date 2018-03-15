package components.datetime;

import static components.color.ColorLookupTable.*;
import static components.color.ColorLookupTable.PLAIN_BORDER;
import static components.color.ColorLookupTable.TEXT_BG_COLOR;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import components.color.custom.ColoredSliderUI;
import managment.Assignment;
import managment.Priority;
import panels.EditAssignmentPanel;

public class TimeLine extends JPanel implements ActionListener, ComponentListener, ChangeListener {
	private static final long serialVersionUID = 6565208292000274287L;
	
	private Assignment assignment;
	private int[] inflectionPoints;
	
	private JCheckBox passiveCheckBox;
	private JSlider importanceSlider;
	
	public TimeLine() {
		setBorder(PLAIN_BORDER);
		setLayout(null);
		
		passiveCheckBox = new JCheckBox("Passive");
		passiveCheckBox.setBorder(new EmptyBorder(5, 5, 0, 0));
		passiveCheckBox.setSize(passiveCheckBox.getPreferredSize());
		passiveCheckBox.addActionListener(this);
		add(passiveCheckBox);

		importanceSlider = new JSlider();
		importanceSlider.setMaximum(100);
		importanceSlider.addChangeListener(this);
		add(importanceSlider);
		
		inflectionPoints = new int[Priority.values().length];
		setAssignment(null);
		
		addComponentListener(this);
	}
	
	private boolean setting;
	public void setAssignment(Assignment assignment) { 
		setting = true;
			this.assignment = assignment; 
			updateInflectionPoints(); 
			passiveCheckBox.setEnabled(assignment != null);
			
			if(assignment != null)
				importanceSlider.setValue((int) (assignment.getImportance() * 100));
		setting = false;
	}
	
	public void updateInflectionPoints() {
		if(assignment == null) return;
		
		long totalTime = assignment.getTotalTime();
		float importance = assignment.getImportance();
		
		if(assignment.isPassive()) {
			passiveCheckBox.setSelected(true);
			
			Arrays.fill(inflectionPoints, 0);
			inflectionPoints[assignment.getPriority().ordinal()] = (int) totalTime;
			
		} else {
			passiveCheckBox.setSelected(false);
			
			int lastStage = 0;
			for(int i = 0; i < totalTime; i += 2) {
				int stage = Assignment.stage(i, totalTime, importance);
				
				if(stage != lastStage) { 
					inflectionPoints[stage - 1] = i;
					lastStage = stage; 
				}
			}
			
			inflectionPoints[lastStage] = (int) totalTime;
		}
		
		repaint();
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		int width  = getWidth() + 2;
		int height = getHeight() + 2 - importanceSlider.getHeight();
		
		g.setColor(TEXT_BG_COLOR);
		g.fillRect(0, 0, width, height);
		
		g2d.setStroke(new BasicStroke(2));
		
		int barCount = Math.round(width / 15f);
		int hStep = width / barCount;
		
		int sqHeight = height / hStep * hStep;
		if(height - sqHeight < hStep / 2) sqHeight -= hStep;
		int vStep = sqHeight / 4;
		
		for(int i = 0, count = (int) Math.ceil((float) height / hStep); i < count; i ++) {
			int h = height - hStep * i;
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .35f));
			g2d.setColor(BUTTON_ROLLOVER_BG_COLOUR);
			g2d.drawLine(0, h, width, h);
		}
		
		for(int i = 0; i < barCount + 1; i ++) {
			int w = hStep * i;
			g2d.setColor(BUTTON_ROLLOVER_BG_COLOUR);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .15f));
			g2d.drawLine(w, 0, w, height);
		}
		
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
		if(assignment == null) return;
		
		double unitSpan = width / (double) assignment.getTotalTime();
		
		for(int i = 1; i < 5; i ++) {
			int h = height - vStep * i;
			g2d.setColor(Priority.values()[i].getPirmaryColor());
			
			int start = (int) (inflectionPoints[i - 1] * unitSpan);
			int end = (int) (inflectionPoints[i] * unitSpan);
			
			if(end < start) continue;
			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
			g2d.fillRect(start, h - 1, end - start, vStep * i);
			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
			g2d.drawLine(start + 1, h, end - 1, h);
		}

		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
		
		int w = (int) (unitSpan * assignment.getExpressedTime()) - 2;
		g2d.setStroke(new BasicStroke(4));
		g2d.setColor(SELECT_BG_COLOUR);
		g2d.drawLine(w, 0, w, height);
	}

	private void actionOccured() {
		if(setting) return;
		assignment.setPassive(passiveCheckBox.isSelected());
		assignment.setImportance(importanceSlider.getValue() / 100f);
		updateInflectionPoints();
		assignment.update();
		
		if(getParent() instanceof EditAssignmentPanel) {
			((EditAssignmentPanel) getParent()).reloadPriotity();
		}
	}
	
	public void actionPerformed(ActionEvent e) 	{ actionOccured(); }
	public void stateChanged(ChangeEvent e)		{ actionOccured(); }

	public void componentResized(ComponentEvent e) {
		importanceSlider.setSize(getWidth(), ColoredSliderUI.THUMB_SIZE + 2);
		importanceSlider.setLocation(0, getHeight() - importanceSlider.getHeight());
	}

	public void componentMoved(ComponentEvent e) { }
	public void componentShown(ComponentEvent e) { }
	public void componentHidden(ComponentEvent e) { }
}
