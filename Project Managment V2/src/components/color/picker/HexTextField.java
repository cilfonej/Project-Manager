package components.color.picker;

import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MaskFormatter;

public class HexTextField extends JFormattedTextField implements DocumentListener {
	private static final long serialVersionUID = -6694532039668603522L;
	
	private boolean skipEvent, filtering;

	public HexTextField(int bitCount) {
		super(createMask(bitCount));
		super.getDocument().addDocumentListener(this);
	}

	private static MaskFormatter createMask(int bitCount) {
		String mask = "";
		for(int i = 0; i < bitCount / 4; i ++)
			mask += "H";
		
		try { return new MaskFormatter(mask); }
		catch(ParseException e) { return null; }
	}
	
	public void changedUpdate(DocumentEvent e) { filtering = true; fireChangeEvent(); filtering = false; }
	public void insertUpdate(DocumentEvent e)  { filtering = true; fireChangeEvent(); filtering = false; }
	public void removeUpdate(DocumentEvent e)  { filtering = true; fireChangeEvent(); filtering = false; }
	
	public int getIntValue() {
		String text = getText().replace(" ", "");
		if(text.length() == 0) return 0;
		
		return Integer.parseInt(text, 16);
	}
	
	public void setText(String text) {
		if(filtering) return;
		
		skipEvent = true;
		super.setText(text);
	}
	
	private ArrayList<ChangeListener> listeners;
	
	public void addChangeListenser(ChangeListener listener) {
		if(listeners == null) listeners = new ArrayList<>();
		listeners.add(listener);
	}
	
	public void fireChangeEvent() {
		if(skipEvent) { skipEvent = false; return; }
		if(listeners == null) return;
		
		ChangeEvent event = new ChangeEvent(this);
		for(ChangeListener listener : listeners)
			listener.stateChanged(event);
	}
}
