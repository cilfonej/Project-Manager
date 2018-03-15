package components.datetime;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicButtonUI;

import components.color.ColorLookupTable;
import components.color.ColoredIconUtil;

public class DatePicker extends JComponent implements ActionListener {
	private static final long serialVersionUID = 4521641411892282673L;

	private static final Image ARROW_IMAGE = new ImageIcon(DatePicker.class.getResource("ArrowHigh.png")).getImage();
	
	private static final Icon NEXT_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.brighter(ColorLookupTable.FOREGROUND_COLOR, .25f), 24, -Math.PI / 2, null);
	private static final Icon PREV_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.brighter(ColorLookupTable.FOREGROUND_COLOR, .25f), 24,  Math.PI / 2, null);
	private static final Icon NEXT_ROLLOVER_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.TEXT_SELECT_BG_COLOR, 24, -Math.PI / 2, null);
	private static final Icon PREV_ROLLOVER_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.TEXT_SELECT_BG_COLOR, 24,  Math.PI / 2, null);
	private static final Icon NEXT_PRESSED_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.mix(ColorLookupTable.TEXT_SELECT_BG_COLOR, ColorLookupTable.BUTTON_PRESSED_BG_COLOUR, .75f), 24, -Math.PI / 2, null);
	private static final Icon PREV_PRESSED_ICON = ColoredIconUtil.prepImage(ARROW_IMAGE, ColorLookupTable.mix(ColorLookupTable.TEXT_SELECT_BG_COLOR, ColorLookupTable.BUTTON_PRESSED_BG_COLOUR, .75f), 24,  Math.PI / 2, null);
	
	private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMMM, yyyy");
	
	private LocalDate selected, inishal, display, today;
	private JButton incressButton, decressButton;
//	private JButton resetButton;
	
	private int maxWidth;
	private JPanel monthWrapper;
	private JLabel monthDisplay;
	
	public DatePicker(LocalDate inishal) {
		this.today = LocalDate.now();
		
		this.inishal = inishal == null ? today : inishal;
		this.selected = this.inishal;
		this.display = today;
		
		setLayout(new BorderLayout(0, 0));
		
		JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
		header.setBorder(new EmptyBorder(5, 0, 5, 0));
		
		incressButton = createButton();
		incressButton.setIcon(NEXT_ICON);
		incressButton.setRolloverIcon(NEXT_ROLLOVER_ICON);
		incressButton.setPressedIcon(NEXT_PRESSED_ICON);
		incressButton.addActionListener(this);
		
		decressButton = createButton();
		decressButton.setIcon(PREV_ICON);
		decressButton.setRolloverIcon(PREV_ROLLOVER_ICON);
		decressButton.setPressedIcon(PREV_PRESSED_ICON);
		decressButton.addActionListener(this);

		monthDisplay = new JLabel();
		monthDisplay.setHorizontalAlignment(JLabel.CENTER);
		monthDisplay.setFont(ColorLookupTable.HEADER_FONT);
		monthDisplay.setText(DISPLAY_FORMATTER.format(LocalDateTime.of(8888, 9, 1, 1, 1)));
		maxWidth = monthDisplay.getPreferredSize().width;
		monthDisplay.setText(DISPLAY_FORMATTER.format(display));
		monthWrapper = new JPanel(new BorderLayout());
		monthWrapper.add(monthDisplay);

		header.add(decressButton);
		header.add(monthWrapper);
		header.add(incressButton);
		
		
		add(header, BorderLayout.NORTH);
		add(new DatePanel(), BorderLayout.CENTER);
	}

	public void reset() { this.selected = display = inishal; actionPerformed(new ActionEvent(this, 0, "")); }
	public LocalDate getDate() { return selected; }
	public void setDate(LocalDate date) { selected = inishal = display = date; actionPerformed(new ActionEvent(this, 0, "")); }
	
	private static JButton createButton() {
		JButton button = new JButton();
		
		button.setUI(new BasicButtonUI());
		button.setContentAreaFilled(false);
		button.setBorder(null);
		
		return button;
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == incressButton) display = display.plusMonths(1);
		if(e.getSource() == decressButton) display = display.minusMonths(1);
		
		monthDisplay.setText(DISPLAY_FORMATTER.format(display));
		int width = (int) Math.ceil((maxWidth - monthDisplay.getPreferredSize().width) / 2f);
		monthWrapper.setBorder(new EmptyBorder(0, width, 0, width));
		repaint();
	}
	
	private class DatePanel extends JComponent implements MouseInputListener {
		private static final long serialVersionUID = -8719192903455000926L;
		
		private int gridSize;
		private int headerHeight, leftPadding;
		private Point highlight;
		
		private Color bgColor, emptyBGColor, labelColor, emptyLabelColor;
		
		public DatePanel() {
			addMouseListener(this);
			addMouseMotionListener(this);
			
			bgColor = ColorLookupTable.mix(ColorLookupTable.BACKGROUND_COLOR, ColorLookupTable.FOREGROUND_COLOR, .75f);
			emptyBGColor = ColorLookupTable.mix(bgColor, ColorLookupTable.FOREGROUND_COLOR, .85f);
			labelColor = ColorLookupTable.mix(emptyBGColor, ColorLookupTable.SELECT_BG_COLOUR, .5f);
			emptyLabelColor = ColorLookupTable.mix(emptyBGColor, ColorLookupTable.FOREGROUND_COLOR, .5f);
			setBackground(ColorLookupTable.BACKGROUND_COLOR);
			setForeground(ColorLookupTable.FOREGROUND_COLOR);
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(ColorLookupTable.BACKGROUND_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
	
			g.setFont(ColorLookupTable.NORMAL_FONT);
			FontMetrics metrics = g.getFontMetrics();
			
			Graphics2D g2d = (Graphics2D) g;
			headerHeight = (int) (metrics.getHeight() * 1.5f);
			int width = getWidth();
			int height = getHeight() - headerHeight;
	
			gridSize = Math.min(width / 7, height / 6);
			leftPadding = (width - gridSize * 7) / 2;
			g2d.translate(leftPadding, 0);
			
			g.setColor(emptyBGColor); 		g.fillRect(0, 0, gridSize * 7, gridSize * 6 + headerHeight);
			
			for(DayOfWeek dayOfWeek : DayOfWeek.values()) {
				g2d.setColor(labelColor);
				g2d.fillRect(0, 0, gridSize, headerHeight);
				g.setColor(getForeground());
				
				String label = dayOfWeek.toString().substring(0, 3);
				Rectangle2D bound = metrics.getStringBounds(label, g);
				g2d.drawString(label, 
						(gridSize - (float) bound.getWidth()) / 2, 
						(headerHeight - (float) bound.getHeight()) / 2 - (float) bound.getY()
					);

				g2d.drawRect(0, 0, gridSize, headerHeight);
				g2d.translate(gridSize, 0);
			}
			
			g2d.translate(-gridSize * 7, (int) (metrics.getHeight() * 1.5f));
			
			int week = 0, day = 0;
			LocalDate printDate = display.withDayOfMonth(1);
			printDate = printDate.minusDays(printDate.getDayOfWeek().getValue() - 1);
			
			for(int d = 0; d < 42; d ++) {
				int minX = day * gridSize;
				int minY = week * gridSize;
				
				g.setColor(
						printDate.getMonth() != display.getMonth() ? emptyBGColor :
						
						selected.isEqual(printDate) || 
						highlight != null && highlight.x == day && highlight.y == week ?
								ColorLookupTable.TEXT_SELECT_BG_COLOR : bgColor
				);
				
				g2d.fillRect(minX, minY, gridSize, gridSize);
				
				g.setColor(getForeground());
				g2d.drawRect(minX, minY, gridSize, gridSize);
				
				g.setColor(printDate.getMonth() != display.getMonth() ? emptyLabelColor : getForeground());
				
				String dayLabel = printDate.getDayOfMonth() + "";
				Rectangle2D bound = metrics.getStringBounds(dayLabel, g);
				g2d.drawString(dayLabel, 
						(gridSize - (float) bound.getWidth()) / 2 + minX, 
						(gridSize - (float) bound.getHeight()) / 2 + minY - (float) bound.getY()
					);
				
				if(today.isEqual(printDate)) {
					g2d.setStroke(new BasicStroke(2));
					g.setColor(ColorLookupTable.SELECT_BG_COLOUR);
					g.drawOval(minX + 5, minY + 5, gridSize - 10, gridSize - 10);
					g2d.setStroke(new BasicStroke(1));
				}
				
				printDate = printDate.plusDays(1);
				
				if(++ day % 7 == 0) {
					week ++;
					day %= 7;
				}
			}

//			g.setColor(getForeground()); 	g.drawRect(0, 0, gridSize * 7, gridSize * 6 + headerHeight);
		}
		
//		public Dimension getMaximumSize() { return new Dimension(gridSize * 7, gridSize * 6 + headerHeight); }

		public void mouseClicked(MouseEvent e) {
			Point p = decomposeGrid(e.getPoint());
			if(p == null) return;
			int startDay = display.withDayOfMonth(1).getDayOfWeek().getValue() - 1;
			
			int week = p.y * 7 - startDay;
			int day = p.x + 1;
			int date = week + day;
			
			if(date < 1 || date > display.lengthOfMonth()) return;
			selected = display.withDayOfMonth(date);
			
			repaint();
		}

		public void mouseMoved(MouseEvent e) {
			highlight = decomposeGrid(e.getPoint());
			repaint();
		}
		
		private Point decomposeGrid(Point loc) {
			loc.x -= leftPadding;
			loc.y -= headerHeight;
			
			if(loc.x < 0 || loc.y < 0) return null;
			if(loc.x > gridSize * 7) return null;
			if(loc.y > gridSize * 6) return null;
			
			return new Point(loc.x / gridSize, loc.y / gridSize);
		}
		
		public void mousePressed(MouseEvent e)  { }
		public void mouseReleased(MouseEvent e) { }

		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e)  { }
		public void mouseDragged(MouseEvent e) { }
	}
}
