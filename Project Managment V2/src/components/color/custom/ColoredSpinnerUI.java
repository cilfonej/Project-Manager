package components.color.custom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

import components.color.ColorLookupTable;

public class ColoredSpinnerUI extends BasicSpinnerUI {
	public static ComponentUI createUI(JComponent c) { return new ColoredSpinnerUI(); }
	
	public void installUI(JComponent c) {
		super.installUI(c);
		c.setBorder(ColorLookupTable.PLAIN_BORDER);
	}

	protected void installDefaults() {
		super.installDefaults();
	}

	protected JComponent createEditor() {
		JComponent component = super.createEditor();
		JComponent editor = (JComponent) component.getComponent(0);

		component.setBorder(new EmptyBorder(0, 0, 0, 3));
		component.setBackground(ColorLookupTable.TEXT_BG_COLOR);

		editor.setBorder(null);

		return component;
	}

	protected Component createPreviousButton() {
		Component c = createArrowButton(SwingConstants.SOUTH);
		c.setName("Spinner.previousButton");
		installPreviousButtonListeners(c);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(0, 1, 1, 1));
		panel.add(c, BorderLayout.CENTER);
		panel.setBackground(new Color(255, 255, 255, 0));
		panel.setOpaque(false);
		
		return panel;
	}

	protected Component createNextButton() {
		Component c = createArrowButton(SwingConstants.NORTH);
		c.setName("Spinner.nextButton");
		installNextButtonListeners(c);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(1, 1, 0, 1));
		panel.add(c, BorderLayout.CENTER);
		panel.setBackground(new Color(255, 255, 255, 0));
		panel.setOpaque(false);
		
		return panel;
	}

	private Component createArrowButton(int direction) {
		JButton b = ColorLookupTable.createArrowButton(direction);
		b.setBorder(new EmptyBorder(0, 0, 0, 0));
		return b;
	}
}
