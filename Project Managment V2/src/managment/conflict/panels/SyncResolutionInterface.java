package managment.conflict.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import components.color.ColorLookupTable;

import javax.swing.JLabel;

public class SyncResolutionInterface extends JDialog implements ActionListener {
	private static final long serialVersionUID = -1202291427612122276L;

	private JButton okButton;
	private JButton cancelButton;
	
	private boolean overwrite;
	
	public SyncResolutionInterface() {
		setModalityType(DEFAULT_MODALITY_TYPE);
		
		setSize(450, 300);
		getContentPane().setLayout(new BorderLayout());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel lblVersionUsedTo = new JLabel("<HTML>Version used to make current changes is different from version stored to Drive. <BR>Would you like to overwrite  changes made to Drive?</HTML>");
		lblVersionUsedTo.setFont(ColorLookupTable.NORMAL_FONT);
		contentPanel.add(lblVersionUsedTo);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		okButton = new JButton("Overwrite");
		okButton.setToolTipText("Overwrites version saved to Drive");
		buttonPane.add(okButton);
		
		cancelButton = new JButton("Discard");
		cancelButton.setToolTipText("Discards Current Changes");
		buttonPane.add(cancelButton);
		
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		setLocationRelativeTo(null);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == okButton) {
			overwrite = true;
			setVisible(false);
			return;
		}
		
		if(e.getSource() == cancelButton) {
			overwrite = false;
			setVisible(false);
			return;
		}
	}
	
	public boolean shouldOverwrite() { return overwrite; }
}
