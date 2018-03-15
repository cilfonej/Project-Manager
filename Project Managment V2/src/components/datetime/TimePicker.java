package components.datetime;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Rectangle2D;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicButtonUI;

import components.color.ColorLookupTable;
import components.color.ColoredIconUtil;

public class TimePicker extends JComponent implements ActionListener {
	private static final long serialVersionUID = 2646323180075637612L;
	
	private static final Image ARROW_IMAGE = new ImageIcon(TimePicker.class.getResource("ArrowHigh.png")).getImage();
	
	private static final Icon NEXT_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.brighter(ColorLookupTable.FOREGROUND_COLOR, .25f), 24, Math.PI, null);
	private static final Icon PREV_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.brighter(ColorLookupTable.FOREGROUND_COLOR, .25f), 24,       0, null);
	private static final Icon NEXT_ROLLOVER_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.TEXT_SELECT_BG_COLOR, 24, Math.PI, null);
	private static final Icon PREV_ROLLOVER_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.TEXT_SELECT_BG_COLOR, 24,       0, null);
	private static final Icon NEXT_PRESSED_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.mix(ColorLookupTable.TEXT_SELECT_BG_COLOR, ColorLookupTable.BUTTON_PRESSED_BG_COLOUR, .75f), 24, Math.PI, null);
	private static final Icon PREV_PRESSED_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.mix(ColorLookupTable.TEXT_SELECT_BG_COLOR, ColorLookupTable.BUTTON_PRESSED_BG_COLOUR, .75f), 24,       0, null);
	
	private static final DateTimeFormatter H_FORMATTER = DateTimeFormatter.ofPattern("hh");
	private static final DateTimeFormatter M_FORMATTER = DateTimeFormatter.ofPattern("mm");
	private static final DateTimeFormatter A_FORMATTER = DateTimeFormatter.ofPattern("a");
	
	private LocalTime selected, inishal;
	private JPanel hPanel, mPanel, aPanel;
	
	public TimePicker(LocalTime inishal) {
		this.inishal = inishal == null ? LocalTime.now() : inishal;
		this.selected = this.inishal;
		
		setLayout(new BorderLayout(0, 0));
		
		JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		JLabel colinLabel = new JLabel(":");
		colinLabel.setFont(ColorLookupTable.HUGE_FONT);
		JLabel spaceLabel = new JLabel(" ");
		spaceLabel.setFont(ColorLookupTable.HUGE_FONT);
		
		header.add(hPanel = createNumberSpinner('h'));
		header.add(colinLabel);
		header.add(mPanel = createNumberSpinner('m'));
		header.add(spaceLabel);
		header.add(aPanel = createNumberSpinner('a'));

		add(header, BorderLayout.NORTH);
		add(new TimePanel(), BorderLayout.CENTER);
	}
	
	public void reset() {  this.selected = inishal; redraw(); }
	public LocalTime getTime() { return selected; }
	public void setTime(LocalTime time) { selected = inishal = time; redraw(); }
	
	private static JButton createButton() {
		JButton button = new JButton();
		
		button.setUI(new BasicButtonUI());
		button.setContentAreaFilled(false);
		button.setBorder(null);
		
		return button;
	}
	
	private JPanel createNumberSpinner(char action) {
		JButton incressButton = createButton();
		incressButton.setIcon(NEXT_ICON);
		incressButton.setRolloverIcon(NEXT_ROLLOVER_ICON);
		incressButton.setPressedIcon(NEXT_PRESSED_ICON);
		incressButton.addActionListener(this);
		incressButton.setActionCommand("i" + action);
		
		JButton decressButton = createButton();
		decressButton.setIcon(PREV_ICON);
		decressButton.setRolloverIcon(PREV_ROLLOVER_ICON);
		decressButton.setPressedIcon(PREV_PRESSED_ICON);
		decressButton.addActionListener(this);
		decressButton.setActionCommand("d" + action);
		
		JTextField label = new JTextField("00");
		label.setFont(ColorLookupTable.HUGE_FONT);
		label.setBackground(ColorLookupTable.BACKGROUND_COLOR);
		label.setActionCommand(" " + action);
		label.addActionListener(this);
		label.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) {
			actionPerformed(new ActionEvent(label, 0, " " + action));
		}});
		label.setBorder(null);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(incressButton, BorderLayout.NORTH);
		panel.add(label, BorderLayout.CENTER);
		panel.add(decressButton, BorderLayout.SOUTH);
		
		updateText(panel, action);
		return panel;
	}
	
	private void updateText(JPanel panel, char action) {
		JTextField label = (JTextField) ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
		
		if(action == 'h') 		label.setText(H_FORMATTER.format(selected));
		else if(action == 'm') 	label.setText(M_FORMATTER.format(selected));
		else					label.setText(A_FORMATTER.format(selected));
	}
	
	private boolean isAM() { return selected.getHour() < 12; }
	
	private void redraw() {
		updateText(hPanel, 'h');
		updateText(mPanel, 'm');
		updateText(aPanel, 'a');

		repaint();
	}
	
	public void actionPerformed(ActionEvent e) {
		char act = e.getActionCommand().charAt(1);
		
		if(e.getSource() instanceof JTextField) {
			JTextField field = (JTextField) e.getSource();
			
			if(act == 'a') {
				selected = selected.withHour(selected.getHour() + (field.getText().equalsIgnoreCase("am") == isAM() ? 0 : 12));
				
			} else {
				try {
					int value = Integer.parseInt(field.getText());
					if(act == 'h') 		selected = selected.withHour(value + (isAM() ? 0 : 12));
					else if(act == 'm') selected = selected.withMinute(value);
					
				} catch(NumberFormatException | DateTimeException ex) { }
			}
			
			redraw();
			return;
		}
		
		if(e.getActionCommand().charAt(0) == 'i') {
			if(act == 'h') 		selected = selected.plusHours(1);
			else if(act == 'm') selected = selected.plusMinutes(1);
			else				selected = selected.plusHours(12);
		} else {
			if(act == 'h') 		selected = selected.minusHours(1);
			else if(act == 'm') selected = selected.minusMinutes(1);
			else				selected = selected.minusHours(12);
		}
		
		redraw();
	}
	
	private class TimePanel extends JComponent {// implements MouseInputListener {
		private static final long serialVersionUID = -8719192903455000926L;
		private static final int TRIM = 3;
		
		private int radius;
		private int topPadding, leftPadding;
		
		private Color bgColor, emptyBGColor, emptyLabelColor;
		
		public TimePanel() {
//			addMouseListener(this);
//			addMouseMotionListener(this);
			
			bgColor = ColorLookupTable.mix(ColorLookupTable.BACKGROUND_COLOR, ColorLookupTable.FOREGROUND_COLOR, .75f);
			emptyBGColor = ColorLookupTable.mix(bgColor, ColorLookupTable.FOREGROUND_COLOR, .85f);
			emptyLabelColor = ColorLookupTable.mix(emptyBGColor, ColorLookupTable.FOREGROUND_COLOR, .25f);
			setBackground(ColorLookupTable.BACKGROUND_COLOR);
			setForeground(ColorLookupTable.FOREGROUND_COLOR);
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(ColorLookupTable.BACKGROUND_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
	
			g.setFont(ColorLookupTable.HEADER_FONT);
			FontMetrics metrics = g.getFontMetrics();
			
			Graphics2D g2d = (Graphics2D) g;
			int width = getWidth() - 5;
			int height = getHeight() - 5;
			
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			radius = Math.min(width, height) / 2;
			leftPadding = (width - radius * 2) / 2;
			topPadding = (height - radius * 2) / 2;
			
			g2d.translate(leftPadding, topPadding);
			
			g.setColor(emptyLabelColor);
			g2d.fillOval(0, 0, radius * 2, radius * 2);
			
			radius -= TRIM;
			g2d.translate(TRIM, TRIM);
			
			g.setColor(bgColor);
			g2d.fillOval(0, 0, radius * 2, radius * 2);
			
			int lineHeight = (int) (metrics.getHeight() * 1.5f);
			g2d.translate(radius - lineHeight / 2, radius - lineHeight / 2);
			
			g.setColor(getForeground());
			for(int i = 1; i <= 12; i ++) {
				float a = (float) Math.toRadians(360f / 12 * i - 90); 
				
				int x = (int) (Math.cos(a) * radius - (TRIM * 2 + lineHeight / 2) * Math.cos(a));
				int y = (int) (Math.sin(a) * radius - (TRIM * 2 + lineHeight / 2) * Math.sin(a));

				g.setColor(emptyBGColor);
				g2d.fillOval(x, y, lineHeight, lineHeight);

				g.setColor(getForeground());
				String text = i + "";
				Rectangle2D bound = metrics.getStringBounds(text, g);
				g2d.drawString(text, 
						(lineHeight - (float) bound.getWidth()) / 2 + x, 
						(lineHeight - (float) bound.getHeight()) / 2 - (float) bound.getY() + y
					);
			}
			
			float mAngle = (float) Math.toRadians(selected.getMinute() / 60f * 360 - 90);
			float hAngle = (float) Math.toRadians(selected.getHour() / 12f * 360 - 90) + 
					(float) Math.toRadians(selected.getMinute() / 60f * 360 / 12);
			
			int cx = lineHeight / 2;
			int cy = lineHeight / 2;
			
			g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
			
			g2d.rotate(hAngle, cx, cy);
			g.setColor(ColorLookupTable.SELECT_BG_COLOUR);
			g.drawLine(cx, cy, cx + radius - (int) (lineHeight * 1.5f), cy);
			g2d.rotate(-hAngle, cx, cy);
			
			g2d.rotate(mAngle, cx, cy);
			g.setColor(ColorLookupTable.TEXT_SELECT_BG_COLOR);
			g.drawLine(cx, cy, cx + radius - (int) (lineHeight * .5f), cy);
			g2d.rotate(-mAngle, cx, cy);
		}
		
//		public void mouseClicked(MouseEvent e) {
//			Point p = decomposeGrid(e.getPoint());
//			if(p == null) return;
//			
//			selected = display.withDayOfMonth(date);
//			repaint();
//		}
//
//		public void mouseMoved(MouseEvent e) {
//			highlight = decomposeGrid(e.getPoint());
//			repaint();
//		}
//		
//		private Point decomposeGrid(Point loc) {
//			loc.x -= leftPadding;
//			loc.y -= headerHeight;
//			
//			if(loc.x < 0 || loc.y < 0) return null;
//			if(loc.x > gridSize * 7) return null;
//			if(loc.y > gridSize * 6) return null;
//			
//			return new Point(loc.x / gridSize, loc.y / gridSize);
//		}
		
//		public void mousePressed(MouseEvent e)  { }
//		public void mouseReleased(MouseEvent e) { }
//
//		public void mouseEntered(MouseEvent e) { }
//		public void mouseExited(MouseEvent e)  { }
//		public void mouseDragged(MouseEvent e) { }
	}
}
