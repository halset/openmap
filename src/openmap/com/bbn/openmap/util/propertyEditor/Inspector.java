/*
 * File: Inspector.java
 * Date: Jul 2001
 * Author: Kai Lessmann <klessman@intevation.de>
 * Copyright 2001 Intevation GmbH, Germany
 *
 * This file is Free Software to be included into OpenMap 
 * under its Free Software license.
 * Permission is granted to use, modify and redistribute.
 *
 * Intevation hereby grants BBN a royalty free, worldwide right and license 
 * to use, copy, distribute and make Derivative Works
 * of this Free Software created by Kai Lessmann 
 * and sublicensing rights of any of the foregoing.
 * 
 */
package com.bbn.openmap.util.propertyEditor;

import com.bbn.openmap.*;
import com.bbn.openmap.layer.shape.*;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

/**
 * Class to inspect a PropertyConsumer. Used by the LayerAddPanel
 * class to interactively configure a Layer object before it gets
 * added to the map.  This class should suffice to "inspect" any
 * PropertyConsumer on a very basic level, handling is more convinient
 * if property editor classes are available.  The behavior of the
 * Inspector is configured through properties; the propertiesInfo
 * object of a PropertyConsumer may contain a initPropertiesProperty
 * which determines which properties are to be shown and in which
 * order, in a space seperated list, i.e. 
 *
 * <code>
 * initPropertiesProperty=class prettyName shapeFile
 *
 * </code>
 * If this property is not defined, then all the properties will be
 * displayed, in alphabetical order.
 *
 * For each property there may be a editorProperty entry giving a
 * PropertyEditor class to instanciate as an editor for the property,
 * i.e.
 * <code>
 * shapeFile.editor=com.bbn.openmap.util.propertyEditor.FilePropertyEditor
 * </code>.  
 */
public class Inspector implements ActionListener {

    /** A simple TextField as a String editor. */
    protected final String defaultEditorClass = "com.bbn.openmap.util.propertyEditor.TextPropertyEditor";

    /** 
     * The PropertyConsumer being inspected. Set in
     * inspectPropertyConsumer. 
     */
    PropertyConsumer propertyConsumer = null;
	
    /** Handle to the GUI.  Used for setVisible(true/false). */
    protected JFrame frame = null;

    /** Action command for the cancelButton. */
    //  public so it can be referenced from the actionListener
    public final static String cancelCommand = "cancelCommand";
    
    /** The action command for the doneButton. */
    //  public so it can be referenced from the actionListener
    public final static String doneCommand = "doneCommand";
        
    /**
     * Hashtable containing property names, and their editors.  Used
     * to fetch user inputs for configuring a property consumer.
     */
    protected Hashtable editors = null;

    /**
     * Handle to call back the object that invokes this
     * Inspector.
     */
    protected ActionListener actionListener = null;

    /**
     * Flag to print out the properties.  Used when the Inspector is
     * in stand-alone mode, so that the properties are directed to
     * stdout.
     */
    protected boolean print = false;
    
    /**
     * Set an Actionlistener for callbacks.  Once a Layer object is
     * configured, ie the "Add" button has been clicked, an
     * ActionListener that invoked this Inspector can register here to
     * be notified.  
     */
    public void addActionListener(java.awt.event.ActionListener al) {
	actionListener = al;
    }
    
    /** Does nothing. */
    public Inspector() {}
    
    /** Sets the actionListener. */
    public Inspector(ActionListener al) {
	actionListener = al;
    }
    
    /**
     * Inspect and configure a PropertyConsumer object.  Main method
     * of this class.  The argument PropertyConsumer is inspected
     * through the ProperyConsumer interface, the properties are
     * displayed to be edited. 
     */
    public void inspectPropertyConsumer(PropertyConsumer propertyConsumer) {
	String prefix = propertyConsumer.getPropertyPrefix();

	// construct GUI
	if (frame == null) {
	    frame = new JFrame("Inspector - " + prefix);
	} else {
	    frame.setVisible(false);
	    frame.dispose();
	    frame = null;
	    frame = new JFrame("Inspector - " + prefix);
	}

	frame.getContentPane().add(createPropertyGUI(propertyConsumer));
	frame.pack();

	if (frame.getHeight() > 500) {
	    frame.setSize(frame.getWidth(), 500);
	}

	frame.setVisible(true);
    }
    
    public Vector sortKeys(Collection keySet) {
	Vector vector = new Vector(keySet.size());

	//  OK, ok, this isn't the most efficient way to do this, but
	//  it's simple.  Shouldn't matter for what we are using it
	//  for...
	Iterator it = keySet.iterator();
	while (it.hasNext()) {
	    String key = (String) it.next();
	    int size = vector.size();
	    for (int i = 0; i <= size; i++) {
		if (i == size) {
//    		    System.out.println("Adding " + key + " at " + i);
		    vector.add(key);
		    break;
		} else {
		    int compare = key.compareTo((String)vector.elementAt(i));
		    if (compare < 0) {
//  			System.out.println(key + " goes before " + 
//  					   vector.elementAt(i) + " at " + i);
			vector.add(i, key);
			break;
		    }
		}
	    }	    
	}
	return vector;
    }

    /**
     * Creates a JComponent with the properties to be changed.  This
     * component is suitable for inclusion into a GUI.
     * @param pc The property consumer to create a gui for.
     * @return JComponent, a panel holding the interface to set the
     * properties.
     */
    public JComponent createPropertyGUI(PropertyConsumer pc) {
	// fill variables
	this.propertyConsumer = pc;
	Properties props = new Properties();
	props = pc.getProperties(props);
	Properties info = new Properties();
	info = pc.getPropertyInfo(info);
	String prefix = pc.getPropertyPrefix();

	return createPropertyGUI(prefix, props, info);
    }

    /**
     * Creates a JComponent with the properties to be changed.  This
     * component is suitable for inclusion into a GUI.  Don't use this
     * method directly!  Use the createPropertyGUI(PropertyConsumer)
     * instead.  You will get a NullPointerException if you use this
     * method without setting the PropertyConsumer in the Inspector.
     *
     * @param prefix the property prefix for the property consumer.
     * Received from the PropertyConsumer.getPropertyPrefix() method.
     * Properties that start with this prefix will have the prefix
     * removed from the display, so the GUI will only show the actual
     * property name.
     * @param props the properties received from the
     * PropertyConsumer.getProperties() method.  
     * @param info the properties received from the
     * PropertyConsumer.getPropertyInfo() method, containing
     * descriptions and any specific PropertyEditors that should be
     * used for a particular property named in the
     * PropertyConsumer.getProperties() properties.
     * @return JComponent, a panel holding the interface to set the
     * properties.  */
    public JComponent createPropertyGUI(
	String prefix, Properties props, Properties info) {

	// collect the info needed...
	Collection keySet = props.keySet();
	String propertyList = info.getProperty(PropertyConsumer.initPropertiesProperty);
	Vector sortedKeys;

	if (propertyList != null) {
	    Vector propertiesToShow = PropUtils.parseSpacedMarkers(propertyList);
	    for (int i=0; i < propertiesToShow.size(); i++) {
		propertiesToShow.set(i, prefix + "." + propertiesToShow.get(i));
	    }
	    sortedKeys = propertiesToShow;
	}  else {
	    // otherwise, show them all, in alphabetical order
	    sortedKeys = sortKeys(keySet);
	}

	int num = sortedKeys.size();

	editors = new Hashtable(num);

  	Iterator it = sortedKeys.iterator();

	JButton doneButton = null, cancelButton = null;
	
	JPanel component = new JPanel();
	component.setLayout(new BorderLayout());

	JPanel propertyPanel = new JPanel();

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(2, 20, 2, 20);
	propertyPanel.setLayout(gridbag);

	int i = 0;

	while (it.hasNext()) { // iterate properties
	    String prop = (String)it.next();

	    String marker = prop;
	    if (prefix != null && prop.startsWith(prefix)) {
		marker = prop.substring(prefix.length()+1);
	    }

//  	    if (marker.equalsIgnoreCase("class") ||
//  		marker.equalsIgnoreCase(Layer.PrettyNameProperty) ||
//  		marker.equalsIgnoreCase("plugin")) {
//  		continue;
//  	    }

	    String editorClass = info.getProperty(marker + "." + PropertyConsumer.EditorProperty);
	    if (editorClass == null) {
		editorClass = defaultEditorClass;
	    }

	    // instantiate PropertyEditor
	    Class propertyEditorClass = null;
	    PropertyEditor editor = null;
	    try {
		propertyEditorClass = Class.forName(editorClass);
		editor = (PropertyEditor)propertyEditorClass.newInstance();
		editors.put(prop, editor);
	    } catch(Exception e) {
		e.printStackTrace();
		editorClass = null;
	    }
	    
	    Component editorFace = null;
	    if (editor != null && editor.supportsCustomEditor()) {
		editorFace = editor.getCustomEditor();
	    } else {
		editorFace = new JLabel("Does not support custom editor");
	    }

	    if (editor != null) {
		editor.setValue(props.get(prop));
	    }
	    JLabel label = new JLabel(marker + ":");
	    label.setHorizontalAlignment(SwingConstants.RIGHT);

	    c.gridx = 0;
	    c.gridy = i++;
	    c.weightx = 0;
	    c.fill = GridBagConstraints.NONE;
	    c.anchor = GridBagConstraints.EAST;

	    gridbag.setConstraints(label, c);
	    propertyPanel.add(label);

	    c.gridx = 1;
	    c.anchor = GridBagConstraints.WEST;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.weightx = 1f;

	    gridbag.setConstraints(editorFace, c);
	    propertyPanel.add(editorFace);

	    String toolTip = (String)info.get(marker);
	    label.setToolTipText(toolTip==null ? "No further information available." : toolTip);
	}

	// create the palette's scroll pane
	JScrollPane scrollPane = new JScrollPane(
	    propertyPanel,
	    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
	    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
// 	scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
	scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
	component.add(scrollPane, BorderLayout.CENTER);

	JPanel buttons = new JPanel();
	if (print) {
	    doneButton = new JButton("Print");
	    cancelButton = new JButton("Quit");
	} else {
	    doneButton = new JButton("OK");
	    cancelButton = new JButton("Cancel");
	}
	doneButton.addActionListener(this);
	doneButton.setActionCommand(doneCommand);
	cancelButton.addActionListener(this);
	cancelButton.setActionCommand(cancelCommand);
	buttons.add(doneButton);
	buttons.add(cancelButton);

	component.add(buttons, BorderLayout.SOUTH);

	component.validate();
	return component;
    }
    
    /**
     * Implement the ActionListener interface.  The actions
     * registering here should be generated by the two buttons in the
     * Inspector GUI. 
     */
    public void actionPerformed(ActionEvent e) {
	final String actionCommand = e.getActionCommand();
	String prefix = propertyConsumer.getPropertyPrefix();
	
	if (actionCommand == doneCommand) {// confirmed
	    Properties props = collectProperties();

	    if (!print) {
		frame.setVisible(false);
		propertyConsumer.setProperties(prefix, props);
		if (actionListener != null) {
		    actionListener.actionPerformed(e);
		}
	    } else {
		Collection keys = props.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
		    String next = (String)it.next();
		    System.out.println(next + "=" + props.get(next));
		}
	    }

	} else if (actionCommand == cancelCommand) {// canceled
	    if (actionListener != null && actionListener != this) {
		actionListener.actionPerformed(e);
	    }
	    propertyConsumer = null; // to be garb. coll'd
	    frame.setVisible(false);
	    frame.dispose();
	    frame = null;

	    if (print) {
		System.exit(0);
	    }
	}
    }
    
    /** Extracts properties from textfield[]. */
    public Properties collectProperties() {
	Properties props = new Properties();

	Iterator values = editors.keySet().iterator();
	while (values.hasNext()) {
	    String key = (String) values.next();
	    PropertyEditor editor = (PropertyEditor)editors.get(key);
	    if (editor != null) {
		String stuff = editor.getAsText();
		// If it's not defined with text, don't put it in the
		// properties.  The layer should handle this and use
		// its default settings.
		if (!stuff.equals("")) {
		    props.put(key, stuff);
		}
	    }
	}
	return props;
    }
    
    public void setPrint(boolean p) {
	print = p;
    }

    public boolean getPrint() {
	return print;
    }
    
    /** test cases. */
    public static void main(String[] args) {
	String name = (args.length<1)?"com.bbn.openmap.layer.shape.ShapeLayer":args[0];
	PropertyConsumer propertyconsumer = null;
	try {
	    Class c = Class.forName(name);
	    propertyconsumer = (PropertyConsumer)c.newInstance();
	} catch(Exception e) {
	    e.printStackTrace(); System.exit(1);
	}
	
	Properties props=new Properties(), info=new Properties();
	System.out.println("Inspecting " + name);

	String pp = name.substring(name.lastIndexOf(".") + 1);
 	propertyconsumer.setPropertyPrefix(pp.toLowerCase());

	props = propertyconsumer.getProperties(props);
	info = propertyconsumer.getPropertyInfo(info);
	
	Inspector inspector = new Inspector();
	inspector.setPrint(true);
	inspector.addActionListener(inspector);
	inspector.inspectPropertyConsumer(propertyconsumer);
    }
}
