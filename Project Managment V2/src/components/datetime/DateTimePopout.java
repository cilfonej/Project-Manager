package components.datetime;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

public class DateTimePopout extends JDialog implements ActionListener {
	private static final long serialVersionUID = 7067134823367227722L;
	
	private JButton cancleButton;
	private JButton selectButton;
	private JButton resetButton;
	
	private DatePicker datePicker;
	private TimePicker timePicker;
	
	private LocalDateTime dateTime;

	public DateTimePopout() {
		setTitle("Select a DateTime");
		getContentPane().setLayout(new BorderLayout(0, 0));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(true);
		setSize(550, 400);
		setLocationRelativeTo(null);
		
		JPanel bottomPanel = new JPanel();
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BorderLayout(0, 3));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new EmptyBorder(3, 0, 6, 6));
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		selectButton = new JButton("Select");
		buttonPanel.add(selectButton);
		buttonPanel.add(Box.createHorizontalStrut(6));
		cancleButton = new JButton("Cancel");
		buttonPanel.add(cancleButton);
		
		JSeparator separator = new JSeparator();
		bottomPanel.add(separator, BorderLayout.NORTH);
		
		JPanel resetPanel = new JPanel();
		resetPanel.setBorder(new EmptyBorder(3, 6, 6, 3));
		bottomPanel.add(resetPanel, BorderLayout.WEST);
		resetPanel.setLayout(new BoxLayout(resetPanel, BoxLayout.X_AXIS));
		
		resetButton = new JButton("Reset");
		resetPanel.add(resetButton);
		
		JSplitPane inputPanel = new JSplitPane();
		inputPanel.setResizeWeight(.5);
		add(inputPanel, BorderLayout.CENTER);
		
		datePicker = new DatePicker(null);
		inputPanel.setLeftComponent(datePicker);

		timePicker = new TimePicker(null);
		inputPanel.setRightComponent(timePicker);
		
		selectButton.addActionListener(this);
		cancleButton.addActionListener(this);
		resetButton .addActionListener(this);
		
		addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) { actionPerformed(new ActionEvent(cancleButton, 0, "")); }
		});
	}

	public LocalDateTime get() { return dateTime; }
	
	public void set(LocalDateTime dateTime) {
		this.dateTime = dateTime;
		datePicker.setDate(dateTime.toLocalDate());
		timePicker.setTime(dateTime.toLocalTime());
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == resetButton) {
			datePicker.reset();
			timePicker.reset();
			return;
		}
		
		if(e.getSource() == cancleButton) {
			setVisible(false);
			return;
		}
		
		if(e.getSource() == selectButton) {
			dateTime = LocalDateTime.of(datePicker.getDate(), timePicker.getTime());
			setVisible(false);
			return;
		}
	}
}
