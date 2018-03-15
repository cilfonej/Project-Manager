package components.datetime;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import components.color.ColorLookupTable;
import managment.Assignment;
import managment.Priority;

public class CalenderTimeLine extends JComponent {
	private static final long serialVersionUID = -6619194941663741185L;
	private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMMM, yyyy");
	
	private Assignment assignment;
	private LocalDate display;
	
	private int maxWidth;
	private JPanel monthWrapper;
	private JLabel monthDisplay;
	
	public CalenderTimeLine() {
		display = LocalDate.now();
		setLayout(new BorderLayout(0, 0));
		
		JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
		header.setBorder(new EmptyBorder(5, 0, 5, 0));
		
		monthDisplay = new JLabel();
		monthDisplay.setHorizontalAlignment(JLabel.CENTER);
		monthDisplay.setFont(ColorLookupTable.HEADER_FONT);
		monthDisplay.setText(DISPLAY_FORMATTER.format(LocalDateTime.of(8888, 9, 1, 1, 1)));
		maxWidth = monthDisplay.getPreferredSize().width;
		monthWrapper = new JPanel(new BorderLayout());
		monthWrapper.add(monthDisplay);

		header.add(monthWrapper);
		
//		add(header, BorderLayout.NORTH);
		add(new DatePanel(), BorderLayout.CENTER);
	}

	public void setAssignment(Assignment assignment) { this.assignment = assignment; updateDisplay(); }
	
	public void updateDisplay() {
		display = LocalDate.now();
		monthDisplay.setText(DISPLAY_FORMATTER.format(display));
		int width = (int) Math.ceil((maxWidth - monthDisplay.getPreferredSize().width) / 2f);
		monthWrapper.setBorder(new EmptyBorder(0, width, 0, width));
		repaint();
	}
	
	private class DatePanel extends JComponent {
		private static final long serialVersionUID = -8719192903455000926L;
		
		private int gridSize;
		private int headerHeight, leftPadding;
		
		private Color bgColor, emptyBGColor, labelColor, emptyLabelColor;
		private Color[] priorityInMonthColor, priorityNotMonthColor;
		
		public DatePanel() {
			bgColor = ColorLookupTable.mix(ColorLookupTable.BACKGROUND_COLOR, ColorLookupTable.FOREGROUND_COLOR, .75f);
			emptyBGColor = ColorLookupTable.mix(bgColor, ColorLookupTable.FOREGROUND_COLOR, .85f);
			labelColor = ColorLookupTable.mix(emptyBGColor, ColorLookupTable.SELECT_BG_COLOUR, .5f);
			emptyLabelColor = ColorLookupTable.mix(emptyBGColor, ColorLookupTable.FOREGROUND_COLOR, .5f);
			
			priorityInMonthColor = new Color[Priority.values().length];
			priorityNotMonthColor = new Color[Priority.values().length];
			
			for(int i = 0; i < priorityInMonthColor.length; i ++) {
				priorityInMonthColor[i] = ColorLookupTable.mix(		bgColor, Priority.values()[i].getPirmaryColor(), .65f); 
				priorityNotMonthColor[i]= ColorLookupTable.mix(emptyBGColor, Priority.values()[i].getPirmaryColor(), .65f);
			}
			
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
				
				String label = dayOfWeek.toString().substring(0, 2);
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
				
				Color color;
				
				boolean useInMonthColor = printDate.getMonth() != display.getMonth();
				LocalDateTime printDateTimeEND = printDate.atTime(23, 59);
				LocalDateTime printDateTimeSTART = printDate.atStartOfDay();
				if(assignment.getCreationDate().compareTo(printDateTimeEND) <= 0 && assignment.getDeadline().compareTo(printDateTimeSTART) >= 0) {
					Color[] colorArray = useInMonthColor ? priorityInMonthColor : priorityNotMonthColor;
					color = colorArray[assignment.isPassive() ? assignment.getPriority().ordinal() :
						Math.max(0, Assignment.stage(
							assignment.getCreationDate().until(printDate.atTime(12, 0), ChronoUnit.MINUTES), 
							assignment.getCreationDate().until(assignment.getDeadline(), ChronoUnit.MINUTES), 
							assignment.getImportance()
						))];
				} else {
					color = useInMonthColor ? bgColor  : emptyBGColor;
				}
				
				g.setColor(color);
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
				
				if(display.isEqual(printDate)) {
					g2d.setStroke(new BasicStroke(2));
					g.setColor(ColorLookupTable.FOREGROUND_COLOR);
					g.drawOval(minX + 5, minY + 5, gridSize - 10, gridSize - 10);
					g2d.setStroke(new BasicStroke(1));
				}
				
				printDate = printDate.plusDays(1);
				
				if(++ day % 7 == 0) {
					week ++;
					day %= 7;
				}
			}
		}
	}
}
