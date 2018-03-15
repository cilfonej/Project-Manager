package components.color.picker;

import static components.color.ColorLookupTable.FOREGROUND_COLOR;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import components.color.ColorLookupTable;

public class ColorPicker extends JDialog implements ActionListener, ChangeListener {
	public static final ColorPicker INSTANCE = new ColorPicker();
	private static final long serialVersionUID = 2976563988811928019L;
	
//	public static void main(String[] args) {
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch(UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
//			e.printStackTrace();
//		}
//		
//		new ColorPicker().setVisible(true);
//	}
	
	private SVSelectorPanel colorPanel;
	
	private JButton cancleButton;
	private JButton selectButton;
	private JButton resetButton;
	
	private JSlider hueSlider;
	private JSlider satSlider;
	private JSlider valSlider;

	private JSpinner hueSpinner;
	private JSpinner satSpinner;
	private JSpinner valSpinner;
	
	private JSlider redSlider;
	private JSlider greenSlider;
	private JSlider blueSlider;
	
	private JSpinner redSpinner;
	private JSpinner greenSpinner;
	private JSpinner blueSpinner;
	
	private JSlider alphaSlider;
	private JSpinner alphaSpinner;
	
	private HexTextField hexTextField;
	private ColorGrid colorGrid;
	
	private JPanel newColorSample;
	private JPanel oldColorSample;
	
	private Color current, start;
	
	private float hue, sat, val;
	private int red, green, blue;

	public ColorPicker() {
		setTitle("Color Picker");
		getContentPane().setLayout(new BorderLayout(0, 0));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(true);
		
		JPanel bottomPanel = new JPanel();
//		bottomPanel.setBackground(ColorLookupTable.TEXT_BG_COLOR);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BorderLayout(0, 3));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new EmptyBorder(3, 0, 6, 6));
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
//		buttonPanel.setBackground(ColorLookupTable.TEXT_BG_COLOR);
		
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
//		resetPanel.setBackground(ColorLookupTable.TEXT_BG_COLOR);
		
		resetButton = new JButton("Reset");
		resetPanel.add(resetButton);
		
		JPanel inputPanel = new JPanel();
		inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(inputPanel, BorderLayout.EAST);
		GridBagLayout gbl_inputPanel = new GridBagLayout();
		gbl_inputPanel.columnWidths = new int[]{1, 1, 1, 1, 0};
		gbl_inputPanel.rowHeights = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0};
		gbl_inputPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_inputPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		inputPanel.setLayout(gbl_inputPanel);
		
		JSeparator hsvRGBSerporator = new JSeparator();
		GridBagConstraints gbc_hsvRGBSerporator = new GridBagConstraints();
		gbc_hsvRGBSerporator.insets = new Insets(2, 0, 5, 0);
		gbc_hsvRGBSerporator.gridwidth = 4;
		gbc_hsvRGBSerporator.fill = GridBagConstraints.BOTH;
		gbc_hsvRGBSerporator.gridx = 0;
		gbc_hsvRGBSerporator.gridy = 3;
		inputPanel.add(hsvRGBSerporator, gbc_hsvRGBSerporator);
		
		hueSlider = new JSlider();
		GridBagConstraints gbc_hueSlider = new GridBagConstraints();
		gbc_hueSlider.insets = new Insets(0, 3, 5, 5);
		gbc_hueSlider.gridwidth = 2;
		gbc_hueSlider.fill = GridBagConstraints.BOTH;
		gbc_hueSlider.gridx = 1;
		gbc_hueSlider.gridy = 0;
		inputPanel.add(hueSlider, gbc_hueSlider);
		
		redSlider = new JSlider();
		GridBagConstraints gbc_redSlider = new GridBagConstraints();
		gbc_redSlider.insets = new Insets(0, 3, 5, 5);
		gbc_redSlider.gridwidth = 2;
		gbc_redSlider.fill = GridBagConstraints.BOTH;
		gbc_redSlider.gridx = 1;
		gbc_redSlider.gridy = 4;
		inputPanel.add(redSlider, gbc_redSlider);
		
		satSlider = new JSlider();
		GridBagConstraints gbc_satSlider = new GridBagConstraints();
		gbc_satSlider.insets = new Insets(0, 3, 5, 5);
		gbc_satSlider.gridwidth = 2;
		gbc_satSlider.fill = GridBagConstraints.BOTH;
		gbc_satSlider.gridx = 1;
		gbc_satSlider.gridy = 1;
		inputPanel.add(satSlider, gbc_satSlider);
		
		JLabel redLabel = new JLabel("Red:");
		GridBagConstraints gbc_redLabel = new GridBagConstraints();
		gbc_redLabel.insets = new Insets(0, 0, 5, 5);
		gbc_redLabel.anchor = GridBagConstraints.EAST;
		gbc_redLabel.fill = GridBagConstraints.VERTICAL;
		gbc_redLabel.gridx = 0;
		gbc_redLabel.gridy = 4;
		inputPanel.add(redLabel, gbc_redLabel);
		
		valSlider = new JSlider();
		GridBagConstraints gbc_valSlider = new GridBagConstraints();
		gbc_valSlider.insets = new Insets(0, 3, 5, 5);
		gbc_valSlider.gridwidth = 2;
		gbc_valSlider.fill = GridBagConstraints.BOTH;
		gbc_valSlider.gridx = 1;
		gbc_valSlider.gridy = 2;
		inputPanel.add(valSlider, gbc_valSlider);
		
		JLabel alphaLabel = new JLabel("Alpha:");
		GridBagConstraints gbc_alphaLabel = new GridBagConstraints();
		gbc_alphaLabel.insets = new Insets(0, 0, 5, 5);
		gbc_alphaLabel.anchor = GridBagConstraints.EAST;
		gbc_alphaLabel.fill = GridBagConstraints.VERTICAL;
		gbc_alphaLabel.gridx = 0;
		gbc_alphaLabel.gridy = 8;
		inputPanel.add(alphaLabel, gbc_alphaLabel);
		
		alphaSlider = new JSlider();
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.insets = new Insets(0, 3, 5, 5);
		gbc_slider.gridwidth = 2;
		gbc_slider.fill = GridBagConstraints.BOTH;
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 8;
		inputPanel.add(alphaSlider, gbc_slider);
		
		alphaSpinner = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_spinner.fill = GridBagConstraints.BOTH;
		gbc_spinner.gridx = 3;
		gbc_spinner.gridy = 8;
		inputPanel.add(alphaSpinner, gbc_spinner);
		alphaSpinner.addChangeListener(this);
		
		JPanel samplePanel = new JPanel();
		samplePanel.setBorder(new CompoundBorder(ColorLookupTable.ETCHED_UP_BORDER, new EmptyBorder(3, 3, 3, 3)));
		GridBagConstraints gbc_samplePanel = new GridBagConstraints();
		gbc_samplePanel.fill = GridBagConstraints.BOTH;
		gbc_samplePanel.gridheight = 2;
		gbc_samplePanel.gridwidth = 2;
		gbc_samplePanel.gridx = 2;
		gbc_samplePanel.gridy = 10;
		inputPanel.add(samplePanel, gbc_samplePanel);
//		samplePanel.setLayout(new MigLayout("", "[grow][][grow]", "[][][grow]"));
		GridBagLayout gbl_samplePanel = new GridBagLayout();
		gbl_samplePanel.columnWidths = new int[]{1, 1, 1, 0};
		gbl_samplePanel.rowHeights = new int[]{1, 1, 1, 0};
		gbl_samplePanel.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_samplePanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		samplePanel.setLayout(gbl_samplePanel);
		
		JSeparator labelColorSeporator = new JSeparator();
		GridBagConstraints gbc_labelColorSeporator = new GridBagConstraints();
		gbc_labelColorSeporator.insets = new Insets(3, 0, 0, 0);
		gbc_labelColorSeporator.gridwidth = 3;
		gbc_labelColorSeporator.fill = GridBagConstraints.BOTH;
		gbc_labelColorSeporator.gridx = 0;
		gbc_labelColorSeporator.gridy = 1;
		samplePanel.add(labelColorSeporator, gbc_labelColorSeporator);
		
		JPanel newColorSampleWrapper = new JPanel();
		GridBagConstraints gbc_newColorSampleWrapper = new GridBagConstraints();
		gbc_newColorSampleWrapper.insets = new Insets(5, 5, 5, 5);
		gbc_newColorSampleWrapper.fill = GridBagConstraints.BOTH;
		gbc_newColorSampleWrapper.gridx = 0;
		gbc_newColorSampleWrapper.gridy = 2;
		samplePanel.add(newColorSampleWrapper, gbc_newColorSampleWrapper);
		newColorSampleWrapper.setLayout(new BoxLayout(newColorSampleWrapper, BoxLayout.X_AXIS));
		
		newColorSample = new JPanel();
		newColorSample.setUI(new ColorSamplePanelUI());
		newColorSampleWrapper.add(newColorSample);
		
		JSeparator newOldSerparator = new JSeparator();
		newOldSerparator.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_newOldSerparator = new GridBagConstraints();
		gbc_newOldSerparator.fill = GridBagConstraints.VERTICAL;
		gbc_newOldSerparator.gridheight = 3;
		gbc_newOldSerparator.gridx = 1;
		gbc_newOldSerparator.gridy = 0;
		samplePanel.add(newOldSerparator, gbc_newOldSerparator);
		
		JLabel newColorLabel = new JLabel("New Color");
		GridBagConstraints gbc_newColorLabel = new GridBagConstraints();
		gbc_newColorLabel.gridx = 0;
		gbc_newColorLabel.gridy = 0;
		samplePanel.add(newColorLabel, gbc_newColorLabel);
		
		JPanel oldColorSampleWrapper = new JPanel();
		GridBagConstraints gbc_oldColorSampleWrapper = new GridBagConstraints();
		gbc_oldColorSampleWrapper.insets = new Insets(5, 5, 5, 5);
		gbc_oldColorSampleWrapper.fill = GridBagConstraints.BOTH;
		gbc_oldColorSampleWrapper.gridx = 2;
		gbc_oldColorSampleWrapper.gridy = 2;
		samplePanel.add(oldColorSampleWrapper, gbc_oldColorSampleWrapper);
		oldColorSampleWrapper.setLayout(new BoxLayout(oldColorSampleWrapper, BoxLayout.X_AXIS));
		
		oldColorSample = new JPanel();
		oldColorSample.setUI(new ColorSamplePanelUI());
		oldColorSampleWrapper.add(oldColorSample);
		
		JLabel oldColorLabel = new JLabel("Old Color");
		GridBagConstraints gbc_oldColorLabel = new GridBagConstraints();
		gbc_oldColorLabel.gridx = 2;
		gbc_oldColorLabel.gridy = 0;
		samplePanel.add(oldColorLabel, gbc_oldColorLabel);
		
		hexTextField = new HexTextField(24);
		GridBagConstraints gbc_hexTextField = new GridBagConstraints();
		gbc_hexTextField.insets = new Insets(0, 5, 5, 5);
		gbc_hexTextField.fill = GridBagConstraints.BOTH;
		gbc_hexTextField.gridx = 1;
		gbc_hexTextField.gridy = 10;
		inputPanel.add(hexTextField, gbc_hexTextField);
		hexTextField.setColumns(6);
		hexTextField.addChangeListenser(this);
		
		redSpinner = new JSpinner();
		redSpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
		GridBagConstraints gbc_redSpinner = new GridBagConstraints();
		gbc_redSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_redSpinner.fill = GridBagConstraints.BOTH;
		gbc_redSpinner.gridx = 3;
		gbc_redSpinner.gridy = 4;
		inputPanel.add(redSpinner, gbc_redSpinner);
		
		redSpinner  .addChangeListener(this);
		
		JPanel perviuosColors = new JPanel();
		perviuosColors.setBorder(new TitledBorder(ColorLookupTable.ETCHED_UP_BORDER, "Previous Colors", TitledBorder.CENTER, TitledBorder.TOP, ColorLookupTable.NORMAL_FONT, FOREGROUND_COLOR));
		GridBagConstraints gbc_perviuosColors = new GridBagConstraints();
		gbc_perviuosColors.insets = new Insets(5, 0, 0, 5);
		gbc_perviuosColors.gridwidth = 2;
		gbc_perviuosColors.fill = GridBagConstraints.BOTH;
		gbc_perviuosColors.gridx = 0;
		gbc_perviuosColors.gridy = 11;
		inputPanel.add(perviuosColors, gbc_perviuosColors);
		perviuosColors.setLayout(new BorderLayout());
		
		colorGrid = new ColorGrid(3, 4);
		colorGrid.setBorder(new EmptyBorder(3, 3, 3, 5));
		perviuosColors.add(colorGrid);
		colorGrid.addChangeListenser(this);
		
		blueSpinner = new JSpinner();
		blueSpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
		GridBagConstraints gbc_blueSpinner = new GridBagConstraints();
		gbc_blueSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_blueSpinner.fill = GridBagConstraints.BOTH;
		gbc_blueSpinner.gridx = 3;
		gbc_blueSpinner.gridy = 6;
		inputPanel.add(blueSpinner, gbc_blueSpinner);
		blueSpinner .addChangeListener(this);
		
		JLabel satLabel = new JLabel("Sat:");
		GridBagConstraints gbc_satLabel = new GridBagConstraints();
		gbc_satLabel.insets = new Insets(0, 0, 5, 5);
		gbc_satLabel.anchor = GridBagConstraints.EAST;
		gbc_satLabel.fill = GridBagConstraints.VERTICAL;
		gbc_satLabel.gridx = 0;
		gbc_satLabel.gridy = 1;
		inputPanel.add(satLabel, gbc_satLabel);
		
		valSpinner = new JSpinner();
		valSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
		GridBagConstraints gbc_valSpinner = new GridBagConstraints();
		gbc_valSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_valSpinner.fill = GridBagConstraints.BOTH;
		gbc_valSpinner.gridx = 3;
		gbc_valSpinner.gridy = 2;
		inputPanel.add(valSpinner, gbc_valSpinner);
		valSpinner.addChangeListener(this);
		
		JLabel valLabel = new JLabel("Val:");
		GridBagConstraints gbc_valLabel = new GridBagConstraints();
		gbc_valLabel.insets = new Insets(0, 0, 5, 5);
		gbc_valLabel.anchor = GridBagConstraints.EAST;
		gbc_valLabel.fill = GridBagConstraints.VERTICAL;
		gbc_valLabel.gridx = 0;
		gbc_valLabel.gridy = 2;
		inputPanel.add(valLabel, gbc_valLabel);
		
		JLabel hexLabel = new JLabel("Hex:");
		GridBagConstraints gbc_hexLabel = new GridBagConstraints();
		gbc_hexLabel.insets = new Insets(0, 3, 5, 5);
		gbc_hexLabel.anchor = GridBagConstraints.EAST;
		gbc_hexLabel.fill = GridBagConstraints.VERTICAL;
		gbc_hexLabel.gridx = 0;
		gbc_hexLabel.gridy = 10;
		inputPanel.add(hexLabel, gbc_hexLabel);
		
		blueSlider = new JSlider();
		GridBagConstraints gbc_blueSlider = new GridBagConstraints();
		gbc_blueSlider.insets = new Insets(0, 3, 5, 5);
		gbc_blueSlider.gridwidth = 2;
		gbc_blueSlider.fill = GridBagConstraints.BOTH;
		gbc_blueSlider.gridx = 1;
		gbc_blueSlider.gridy = 6;
		inputPanel.add(blueSlider, gbc_blueSlider);
		
		greenSlider = new JSlider();
		GridBagConstraints gbc_greenSlider = new GridBagConstraints();
		gbc_greenSlider.insets = new Insets(0, 3, 5, 5);
		gbc_greenSlider.gridwidth = 2;
		gbc_greenSlider.fill = GridBagConstraints.BOTH;
		gbc_greenSlider.gridx = 1;
		gbc_greenSlider.gridy = 5;
		inputPanel.add(greenSlider, gbc_greenSlider);
		
		greenSpinner = new JSpinner();
		greenSpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
		GridBagConstraints gbc_greenSpinner = new GridBagConstraints();
		gbc_greenSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_greenSpinner.fill = GridBagConstraints.BOTH;
		gbc_greenSpinner.gridx = 3;
		gbc_greenSpinner.gridy = 5;
		inputPanel.add(greenSpinner, gbc_greenSpinner);
		greenSpinner.addChangeListener(this);
		
		JLabel greenLabel = new JLabel("Green:");
		GridBagConstraints gbc_greenLabel = new GridBagConstraints();
		gbc_greenLabel.insets = new Insets(0, 0, 5, 5);
		gbc_greenLabel.anchor = GridBagConstraints.EAST;
		gbc_greenLabel.fill = GridBagConstraints.VERTICAL;
		gbc_greenLabel.gridx = 0;
		gbc_greenLabel.gridy = 5;
		inputPanel.add(greenLabel, gbc_greenLabel);
		
		hueSpinner = new JSpinner();
		GridBagConstraints gbc_hueSpinner = new GridBagConstraints();
		gbc_hueSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_hueSpinner.fill = GridBagConstraints.BOTH;
		gbc_hueSpinner.gridx = 3;
		gbc_hueSpinner.gridy = 0;
		inputPanel.add(hueSpinner, gbc_hueSpinner);
		hueSpinner.setModel(new SpinnerNumberModel(0, 0, 360, 1));
		
		hueSpinner.addChangeListener(this);
		
		JLabel blueLabel = new JLabel("Blue:");
		GridBagConstraints gbc_blueLabel = new GridBagConstraints();
		gbc_blueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_blueLabel.anchor = GridBagConstraints.EAST;
		gbc_blueLabel.fill = GridBagConstraints.VERTICAL;
		gbc_blueLabel.gridx = 0;
		gbc_blueLabel.gridy = 6;
		inputPanel.add(blueLabel, gbc_blueLabel);
		
		JLabel hueLabel = new JLabel("Hue:");
		GridBagConstraints gbc_hueLabel = new GridBagConstraints();
		gbc_hueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_hueLabel.anchor = GridBagConstraints.EAST;
		gbc_hueLabel.fill = GridBagConstraints.VERTICAL;
		gbc_hueLabel.gridx = 0;
		gbc_hueLabel.gridy = 0;
		inputPanel.add(hueLabel, gbc_hueLabel);
		
		JSeparator rgbHEXSeportator = new JSeparator();
		GridBagConstraints gbc_rgbHEXSeportator = new GridBagConstraints();
		gbc_rgbHEXSeportator.insets = new Insets(2, 0, 5, 0);
		gbc_rgbHEXSeportator.gridwidth = 4;
		gbc_rgbHEXSeportator.fill = GridBagConstraints.BOTH;
		gbc_rgbHEXSeportator.gridx = 0;
		gbc_rgbHEXSeportator.gridy = 7;
		inputPanel.add(rgbHEXSeportator, gbc_rgbHEXSeportator);
		
		satSpinner = new JSpinner();
		satSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
		GridBagConstraints gbc_satSpinner = new GridBagConstraints();
		gbc_satSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_satSpinner.fill = GridBagConstraints.BOTH;
		gbc_satSpinner.gridx = 3;
		gbc_satSpinner.gridy = 1;
		inputPanel.add(satSpinner, gbc_satSpinner);
		satSpinner.addChangeListener(this);
		
		colorPanel = new SVSelectorPanel();
		getContentPane().add(colorPanel, BorderLayout.CENTER);
		colorPanel.setBackground(Color.RED);
		
		pack();
		setLocationRelativeTo(null);
		setMinimumSize((Dimension) getSize().clone());
		colorPanel.addChangeListenser(this);
		
		selectButton.addActionListener(this);
		cancleButton.addActionListener(this);
		resetButton .addActionListener(this);
		
		redSlider  .setMaximum(255);
		greenSlider.setMaximum(255);
		blueSlider .setMaximum(255);
		
		hueSlider.setMaximum(360);
		satSlider.setMaximum(100);
		valSlider.setMaximum(100);

		alphaSlider.setMaximum(255);
		
		redSlider  .addChangeListener(this);
		greenSlider.addChangeListener(this);
		blueSlider .addChangeListener(this);

		hueSlider.addChangeListener(this);
		satSlider.addChangeListener(this);
		valSlider.addChangeListener(this);

		alphaSlider.addChangeListener(this);
		
		redSlider  .setUI(new ColorSliderUI(redSlider  , p -> new Color((int) (p * 255), green, blue), 255));
		greenSlider.setUI(new ColorSliderUI(greenSlider, p -> new Color(red,   (int) (p * 255), blue), 255));
		blueSlider .setUI(new ColorSliderUI(blueSlider , p -> new Color(red, green,  (int) (p * 255)), 255));
		
		hueSlider.setUI(new ColorSliderUI(hueSlider, p -> Color.getHSBColor(p, sat, val), 360));
		satSlider.setUI(new ColorSliderUI(satSlider, p -> Color.getHSBColor(hue, p, val), 100));
		valSlider.setUI(new ColorSliderUI(valSlider, p -> Color.getHSBColor(hue, sat, p), 100));

		alphaSlider.setUI(new ColorSliderUI(alphaSlider , p -> new Color((int) (p * 255), (int) (p * 255), (int) (p * 255)), 255));

		addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) { actionPerformed(new ActionEvent(cancleButton, 0, "")); }
		});
		
		setColor(Color.RED);
		
		redSpinner  .updateUI();
		greenSpinner.updateUI();
		blueSpinner .updateUI();
		
		hueSpinner.updateUI();
		satSpinner.updateUI();
		valSpinner.updateUI();
		
		alphaSpinner.updateUI(); 
	}
	
	private boolean updating;
	private void updateColor(Color color) {
		if(updating) return;
		updating = true;
		
		current = color;
		
		red = color.getRed();
		green = color.getGreen();
		blue = color.getBlue();
		
		float[] hsb = Color.RGBtoHSB(red, green, blue, null);
		
		hue = hsb[0];
		sat = hsb[1];
		val = hsb[2];
		
		newColorSample.setBackground(current);
		hexTextField.setText(Integer.toString(color.getRGB() & 0x00FFFFFF, 16).toUpperCase());
		colorPanel.setColor(color);
		
		hueSlider.setValue(Math.round(hue * 360));
		satSlider.setValue(Math.round(sat * 100));
		valSlider.setValue(Math.round(val * 100));
		
		hueSpinner.setValue(Math.round(hue * 360));
		satSpinner.setValue(Math.round(sat * 100));
		valSpinner.setValue(Math.round(val * 100));
		
		alphaSlider.setValue(color.getAlpha());
		alphaSpinner.setValue(color.getAlpha());
		
		redSlider  .setValue(red);
		greenSlider.setValue(green);
		blueSlider .setValue(blue);
		
		redSpinner  .setValue(red);  
		greenSpinner.setValue(green);
		blueSpinner .setValue(blue); 
		
		hueSlider.repaint();
		satSlider.repaint();
		valSlider.repaint();
		
		redSlider  .repaint();
		greenSlider.repaint();
		blueSlider .repaint();
		
		alphaSlider.repaint();
		alphaSpinner.repaint();
		
		colorPanel.repaint();
		
		updating = false;
	}
	
	public Color getColor() { return current; }
	
	public void setColor(Color color) {
		updateColor(color);
		
		this.start = color;
		oldColorSample.setBackground(color);
	}
	
	public void setVisible(boolean visible) {
		new Timer(1000, e -> updateColor(current)).start();
		super.setVisible(visible);
	}

	public void stateChanged(ChangeEvent e) {
		if(e.getSource() instanceof Color) {
			updateColor((Color) e.getSource());
			return;
		}
		
		if(e.getSource() == hexTextField) {
			updateColor(new Color(hexTextField.getIntValue()));
			return;
		}
		
		if(e.getSource() == hueSlider || e.getSource() == satSlider || e.getSource() == valSlider) {
			updateColor(Color.getHSBColor(hueSlider.getValue() / 360f, satSlider.getValue() / 100f, valSlider.getValue() / 100f));
			return;
		}
		
		if(e.getSource() == hueSpinner || e.getSource() == satSpinner || e.getSource() == valSpinner) {
			updateColor(Color.getHSBColor((int) hueSpinner.getValue() / 360f, (int) satSpinner.getValue() / 100f, (int) valSpinner.getValue() / 100f));
			return;
		}
		
		if(e.getSource() == redSlider || e.getSource() == greenSlider || e.getSource() == blueSlider) {
			updateColor(new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue()));
			return;
		}
		
		if(e.getSource() == redSpinner || e.getSource() == greenSpinner || e.getSource() == blueSpinner) {
			updateColor(new Color((int) redSpinner.getValue(), (int) greenSpinner.getValue(), (int) blueSpinner.getValue()));
			return;
		}
		
		if(e.getSource() == alphaSpinner) {
			updateColor(new Color(red, green, blue, (int) alphaSpinner.getValue()));
			return;
		}
		
		if(e.getSource() == alphaSlider) {
			updateColor(new Color(red, green, blue, alphaSlider.getValue()));
			return;
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == resetButton) {
			updateColor(start); 
			return;
		}
		
		if(e.getSource() == cancleButton) {
			updateColor(start); 
			setVisible(false);
			return;
		}
		
		if(e.getSource() == selectButton) {
			colorGrid.push(current);
			setVisible(false);
			return;
		}
	}
}
