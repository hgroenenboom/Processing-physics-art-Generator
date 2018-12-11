package cg;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.xml.bind.DatatypeConverter;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JComboBox;

/**
 * Creates a simple GUI for changing settings for other programs.
 * 
 * At this time the primitives it supports are integers, floats and booleans.
 * Doubles are interpreted as floats.
 * Other functions it supports are a import/export function that stringifies
 * to the text area beneath the buttons, and presets, that use the exported
 * strings to save useful settings.
 * 
 * @author Max
 */
public class SimpleSettingsGUI extends JFrame {
	private static final long serialVersionUID = -617108494131124686L;
	private ArrayList<String> names = new ArrayList<String>();
	private ArrayList<Object> values = new ArrayList<Object>();
	private ArrayList<Component> comps = new ArrayList<Component>();
	private ResultListener listener;
	private JTextArea impExpField;
	private HashMap<String, String> presets = new HashMap<String, String>();
	private boolean fixedSize;
	
	/**
	 * Empty constructor to let the GUI decide the size.
	 */
	public SimpleSettingsGUI() {}
	
	/**
	 * Constructor that allows the user to change the GUI's size.
	 * @param width The width of the GUI.
	 * @param height The height of the GUI.
	 */
	public SimpleSettingsGUI(int width, int height) {
		this.setSize(width, height);
		fixedSize = true;
	}
	
	/**
	 * Adds classes, names and values to the lists of data. All arrays must
	 * have the same amount of elements, or some data will be ignored.
	 * @param names The array of names.
	 * @param values The array of values.
	 */
	public void addData(String[] names, Object[] values) {
		for (int i = 0; i < names.length; i++) {
			addData(names[i], values[i]);
		}
	}
	
	/**
	 * Adds a name-value pair to the GUI.
	 * Booleans and Integers will be recognized, and both Doubles and Floats
	 * will be interpreted as Floats.
	 * @param name The name for the setting.
	 * @param value The default value for the setting.
	 */
	public void addData(String name, Object value) {
		names.add(name);
		if (value instanceof Double) {
			value = new Float((Double) value);
		}
		values.add(value);
	}
	
	/**
	 * Adds a preset to the GUI.
	 * @param name The name that will show on the button.
	 * @param data The stringified export code of the preset.
	 */
	public void addPreset(String name, String data) {
		presets.put(name, data);
	}
	
	/**
	 * Sets the result listener for the GUI.
	 * @param l The listener.
	 */
	public void setResultListener(ResultListener l) {
		listener = l;
	}
	
	/**
	 * Creates the GUI.
	 * @param name The title the GUI will have.
	 * @param name1 The text to show on the first button.
	 * @param name2 The text to show on the second button.
	 */
	public void makeGUI(String name, String name1, String name2) {
		int presetSize = presets.size();
		int settingsSize = names.size();
		int buttonsSize = 3;
		this.setTitle(name);
		this.setLayout(new GridLayout(presetSize + settingsSize + buttonsSize, 1));
		if (!fixedSize) {
			this.setSize(500, 50 + (presetSize + settingsSize + buttonsSize) * 25);
		}
		
		// Create the textfield and the component for each of the settings.
		for (int i = 0; i < names.size(); i++) {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(1,2));
			panel.add(new JLabel(names.get(i)));
			panel.add(makeTypePanel(values.get(i)));
			this.add(panel);
		}
		
		// Create the two action buttons.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,2));
		JButton button1 = new JButton(name1);
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clickButton1();
			}
		});
		buttonPanel.add(button1);
		
		JButton resetButton = new JButton(name2);
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clickButton2();
			}
		});
		buttonPanel.add(resetButton);
		
		this.add(buttonPanel);
		
		// Create the import and export buttons.
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,2));
		JButton imp = new JButton("import");
		imp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imp();
			}
		});
		buttonPanel.add(imp);
		
		JButton exp = new JButton("export");
		exp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exp();
			}
		});
		buttonPanel.add(exp);
		
		this.add(buttonPanel);
		
		// Create the textarea for the import and export buttons.
		JTextArea impExpField = new JTextArea();
		this.add(impExpField);
		this.impExpField = impExpField;
		
		// Create the buttons for all of the presets.
		String[] presetKeys = new String[presets.size()];
		presets.keySet().toArray(presetKeys);
		for (String key : presetKeys) {
			final String preset = presets.get(key);
			
			JButton presetButton = new JButton(key);
			presetButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					importSettings(preset);
				}
			});
			this.add(presetButton);
		}
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	/**
	 * Makes a Component for the data type.
	 * @param o The default value and the type of the component.
	 * @return The resulting component.
	 */
	private Component makeTypePanel(Object o) {
		if (o instanceof Boolean) { // For booleans a checkbox is made.
			JCheckBox jcb = new JCheckBox();
			jcb.setSelected((Boolean) o);
			comps.add(jcb);
			return jcb;
		} else if (o instanceof Integer) { // For integers a formatted textfield is made.
			JFormattedTextField jftf = new JFormattedTextField(o);
			comps.add(jftf);
			return jftf;
		} else if (o instanceof Float) { // For floats a formatted textfield is made.
			JFormattedTextField jftf = new JFormattedTextField(o);
			comps.add(jftf);
			return jftf;
		} else if (o instanceof String[]) { // For String arrays a dropdown menu is made.
			String[] data = (String[]) o;
			JComboBox<String> jcb = new JComboBox<String>();
			for (String s : data) {
				jcb.addItem(s);
			}
			comps.add(jcb);
			return jcb;
		}
		// Return a jlabel showing that the type was not recognized.
		return new JLabel("Type not regognized: " + o.getClass());
	}
	
	/**
	 * Returns a Settings object containing the settings in the GUI.
	 * @return The resulting object.
	 */
	public Settings getSettings() {
		Settings settings = new Settings();
		
		for (int i = 0; i < names.size(); i++) {
			String key = names.get(i);
			Component comp = comps.get(i);
			if (comp instanceof JCheckBox) {
				settings.set(key, ((JCheckBox) comp).isSelected());
			}
			if (comp instanceof JFormattedTextField) {
				settings.set(key, ((JFormattedTextField) comp).getValue());
			}
			if (comp instanceof JComboBox) {
				settings.set(key, ((JComboBox<?>) comp).getSelectedIndex());
			}
		}
		return settings;
	}
	
	/**
	 * Emulates a button 1 click.
	 */
	public void clickButton1() {
		listener.run1(getSettings());
	}
	
	/**
	 * Emulates a button 2 click.
	 */
	public void clickButton2() {
		listener.run2(getSettings());
	}
	
	/**
	 * Import the data from the text area.
	 */
	private void imp() {
		String data = impExpField.getText();
		importSettings(data);
	}
	
	/**
	 * Export the data to the text area.
	 */
	private void exp() {
		String result = exportSettings();
		impExpField.setText(result);
	}
	
	/**
	 * Import the data from the string.
	 * @param data The string representing the data.
	 */
	@SuppressWarnings("unchecked")
	private void importSettings(String data) {
		try {
			byte[] bytes = DatatypeConverter.parseBase64Binary(data);
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream in = new ObjectInputStream(bais);
			Settings settings = new Settings((HashMap<String, Object>) in.readObject());
			
			for (int i = 0; i < names.size(); i++) {
				String key = names.get(i);
				Object value = settings.get(key);
				if (value == null) {
					continue;
				}
				Component comp = comps.get(i);
				if (comp instanceof JCheckBox) {
					((JCheckBox) comp).setSelected((Boolean) value);
				}
				if (comp instanceof JFormattedTextField) {
					((JFormattedTextField) comp).setValue(value);
				}
				if (comp instanceof JComboBox) {
					((JComboBox<?>) comp).setSelectedIndex((Integer) value);
				}
			}
		} catch (Exception e) {
			System.out.println("Could not deserialize");
			e.printStackTrace();
		}
	}
	
	/**
	 * Export the data to a string.
	 * @return The String representing the settings.
	 */
	private String exportSettings() {
		Settings settings = getSettings();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(settings.getData());
			out.flush();
			out.close();
			
			String result = new String(DatatypeConverter.printBase64Binary(baos.toByteArray()));
			return result;
		} catch (IOException e) {
			System.out.println("Could not serialize");
			e.printStackTrace();
		}
		return "";
	}
}
