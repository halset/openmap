// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/BasicMapPanel.java,v $
// $RCSfile: BasicMapPanel.java,v $
// $Revision: 1.5 $
// $Date: 2003/09/08 20:53:56 $
// $Author: blubin $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.net.URL;
import java.util.Properties;
import java.util.Collection;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import com.bbn.openmap.BufferedLayerMapBean;
import com.bbn.openmap.Environment;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.gui.menu.MenuList;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.util.Debug;

/**
 * The BasicMapPanel is a MapPanel and OMComponentPanel that is the
 * heart of the OpenMap application framework.  It can be used in a
 * application or applet.  The Panel has a BorderLayout, and creates a
 * MapBean for its center area.  It creates a MapHandler to use to
 * hold all of its OpenMap components, and uses the PropertyHandler
 * given to it in its constructor to create and configure all of the
 * application components.  The best way to add components to the
 * MapPanel is to get the MapHandler from it and add the component to
 * that.  The BasocMapPanel also adds itself to its MapHandler, so
 * when the PropertyHandler adds components to the MapHandler, the
 * BasicMapPanel is able to find them via the findAndInit method.  By
 * default, the BasicMapPanel looks for MapPanelChildren and asks them
 * for where they would prefer to be located (BorderLayout.NORTH,
 * BorderLayout.SOUTH, BorderLayout.EAST, BorderLayout.WEST).
 */
public class BasicMapPanel extends OMComponentPanel implements MapPanel {

    protected MapHandler mapHandler;
    protected MapBean mapBean;
    protected PropertyHandler propertyHandler;
    protected MenuList menuList;

    /**
     * Create a MapPanel that creates its own PropertyHandler, which
     * will then search the classpath, config directory and user home
     * directory for the openmap.properties file to configure
     * components for the MapPanel.
     */
    public BasicMapPanel() {
	this(false);
    }

    /**
     * Create a MapPanel with the option of delaying the search for properties
     * until the <code>create()</code> call is made.
     * @param delayCreation true to let the MapPanel know that the artful 
     * programmer will call <code>create()</code>
     */
    public BasicMapPanel(boolean delayCreation) {
	this(null, delayCreation);
    }

    /**
     * Create a MapPanel that configures itself with the properties
     * contained in the PropertyHandler provided. If the
     * PropertyHandler is null, a new one will be created.
     */
    public BasicMapPanel(PropertyHandler propertyHandler) {
	this(propertyHandler, false);
    }

    /**
     * Create a MapPanel that configures itself with properties
     * contained in the PropertyHandler provided, and with the option
     * of delaying the search for properties until the
     * <code>create()</code> call is made.
     * @param delayCreation true to let the MapPanel know that the artful 
     * programmer will call <code>create()</code>
     */
    public BasicMapPanel(PropertyHandler propertyHandler, 
			 boolean delayCreation) {
	setPropertyHandler(propertyHandler);
	if (!delayCreation) {
	    create();
	}
    }

    /**
     * The method that triggers setLayout() and createComponents() to
     * be called.  If you've told the BasicMapPanel to delay creation,
     * you should call this method to trigger the PropertyHandler to
     * create components based on the contents of its properties.
     */
    public void create() {
	setLayout(createLayoutManager());
	createComponents();	
    }

    /**
     * The constructor calls this method that sets the LayoutManager
     * for this MapPanel.  It returns a BorderLayout by default, but
     * this method can be overridden to change how the MapPanel places
     * components.  If you change what this method returns, you should
     * also change how components are added in the findAndInit()
     * method.
     */
    protected LayoutManager createLayoutManager() {
	return new BorderLayout();
    }

    /**
     * Position the map bean in this panel according to the layout manger.
     * Defaults to BorderLayout.CENTER.
     */
    protected void addMapBeanToPanel(MapBean map) {
	add(map, BorderLayout.CENTER);	
    }

    /**
     * The constructor calls this method that creates the MapHandler
     * and MapBean, and then tells the PropertyHandler to create the
     * components described in its properties.  This method calls
     * getMapHandler() and getMapBean().  If the PropertyHandler is
     * not null, it will be called to created components based on its
     * properties, and those components will be added to the
     * MapHandler in this MapPanel.
     */
    protected void createComponents() {
	// make sure the MapBean is created and added to the
	// MapHandler.
	getMapBean();
	getMapHandler().add(this);
	getPropertyHandler().createComponents(getMapHandler());
    }

    /**
     * MapPanel method.  Get the MapBean used for the MapPanel.  If
     * the MapBean is null, calls createMapBean() which will create a
     * BufferedLayerMapBean and add it to the MapHandler via a
     * setMapBean call.  If you want something different, override
     * this method.
     */      
    public MapBean getMapBean() {
	if (mapBean == null) {
	    setMapBean(BasicMapPanel.createMapBean());
	}
	return mapBean;
    }

    /**
     * Set the map bean used in this map panel, replace the map
     * bean in the MapHandler if there isn't already one, or if the
     * policy allows replacement.
     * @throws MultipleSoloMapComponentException if there is already a 
     * map bean in the map handler and the policy is to reject duplicates 
     * (since the MapBean is a SoloMapComponent).
     */
    public void setMapBean(MapBean bean) {
	mapBean = bean;
	getMapHandler().add(mapBean);
	addMapBeanToPanel(mapBean);
    }

    /**
     * Get the PropertyHandler containing properties used to configure
     * the panel, creating it if it doesn't exist.
     */
    public PropertyHandler getPropertyHandler() {
	if (propertyHandler == null) {
	    setPropertyHandler(new PropertyHandler());
	}
	return propertyHandler;
    }

    /**
     * Set the PropertyHandler containing the properties used to configure
     * this panel.
     */
    public void setPropertyHandler(PropertyHandler handler) {
	propertyHandler = handler;
	if (handler != null) {
	    getMapHandler().add(handler);
	}
    }

    /**
     * MapPanel method.  Get the MapHandler used for the MapPanel.
     * Creates a standard MapHandler if it hasn't been created yet.
     */      
    public MapHandler getMapHandler() {
	if (mapHandler == null) {
	    mapHandler = new MapHandler();
	}
	return mapHandler;
    }

    /**
     * MapPanel method.  Get a JMenuBar containing menus created from
     * properties.
     */
    public JMenuBar getMapMenuBar() {
	if (menuList != null) {
	    return menuList.getMenuBar();
	} else {
	    return null;
	}
    }

    /**
     * MapPanel method.  Get a JMenu containing sub-menus created from
     * properties.
     */
    public JMenu getMapMenu() {
	if (menuList != null) {
	    return menuList.getMenu();
	} else {
	    return null;
	}
    }

    //Map Component Methods:
    ////////////////////////

    /**
     * Adds a component to the map bean context.  This makes the
     * <code>mapComponent</code> available to the map layers and other
     * components.
     * @param mapComponent a component to be added to the map bean
     * context
     * @throws MultipleSoloMapComponentException if mapComponent is a 
     * SoloMapComponent and another instance already exists and the policy
     * is a reject policy.
     */
    public void addMapComponent(Object mapComponent) {
	if (mapComponent != null) {
	    getMapHandler().add(mapComponent);
	}
    }

    /**
     * Remove a component from the map bean context.
     * @param mapComponent a component to be removed to the map bean
     * context
     * @return true if the mapComponent was removed.
     */
    public boolean removeMapComponent(Object mapComponent) {
	if (mapComponent != null) {
	    return getMapHandler().remove(mapComponent);
	}
	return true;
    }

    /**
     * Given a Class, find the object in the MapHandler.  If the class
     * is not a SoloMapComponent and there are more than one of them
     * in the MapHandler, you will get the first one found.
     */
    public Object getMapComponentByType(Class c) {
	return getMapHandler().get(c);
    }

    /**
     * Get all of the mapComponents that are of the given class type.
     **/
    public Collection getMapComponentsByType(Class c) {
	return getMapHandler().getAll(c);
    }

    /**
     * Find the object with the given prefix.  For now looks up
     * in the mapHandler -- should look up in the new Librarian when
     * we upgrade to the latest jar.
     **/
    public Object getMapComponent(String prefix) {
	return getPropertyHandler().get(prefix);
    }

    /**
     * The BasicMapPanel looks for MapPanelChild components, finds out
     * from them where they prefer to be placed, and adds them.
     */
    public void findAndInit(Object someObj) {
	if (someObj instanceof MapPanelChild && someObj instanceof Component) {
	    if (Debug.debugging("basic")) {
		Debug.output("MapPanel: adding " + 
			     someObj.getClass().getName());
	    }
	    MapPanelChild mpc = (MapPanelChild) someObj;
	    addMapPanelChild(mpc);
	    invalidate();
	}

	if (someObj instanceof MenuList) {
	    menuList = (MenuList)someObj;
	}
    }

    /**
     * Add a child to the MapPanel.
     */
    protected void addMapPanelChild(MapPanelChild mpc) {
	add((Component)mpc, mpc.getPreferredLocation());
    }

    /**
     * The MapPanel looks for MapPanelChild components and removes
     * them from iteself.
     */
    public void findAndUndo(Object someObj) {
	if (someObj instanceof MapPanelChild && someObj instanceof Component) {
	    if (Debug.debugging("basic")) {
		Debug.output("MapPanel: removing " + 
			     someObj.getClass().getName());
	    }
	    remove((Component)someObj);
	    invalidate();
	}

	if (someObj instanceof MenuList && menuList == someObj) {
	    menuList = null;
	}
    }

    //MapBean Methods:
    //////////////////

    /**
     * A static method that creates a MapBean with it's projection set
     * to the values set in the Environment.  Also creates a
     * BevelBorder.LOWERED border for the MapBean.
     */
    public static MapBean createMapBean() {
	int envWidth = Environment.getInteger(Environment.Width, 
					      MapBean.DEFAULT_WIDTH);
	int envHeight = Environment.getInteger(Environment.Height,
					       MapBean.DEFAULT_HEIGHT);
	// Initialize the map projection, scale, center
	// with user prefs or defaults
	Projection proj = ProjectionFactory.makeProjection(
	    ProjectionFactory.getProjType(Environment.get(Environment.Projection, Mercator.MercatorName)),
	    Environment.getFloat(Environment.Latitude, 0f),
	    Environment.getFloat(Environment.Longitude, 0f),
	    Environment.getFloat(Environment.Scale, Float.POSITIVE_INFINITY),
	    envWidth, envHeight);

	if (Debug.debugging("mappanel")) {
	    Debug.output("MapPanel: creating MapBean with initial projection " + proj);
	}
	
	return createMapBean(proj, new BevelBorder(BevelBorder.LOWERED));
    }

    /**
     * A static method that creates a MapBean and sets its projection
     * and border to the values given.
     */
    public static MapBean createMapBean(Projection proj, Border border) {
	MapBean mapBeano = new BufferedLayerMapBean();
	mapBeano.setBorder(border);
	mapBeano.setProjection(proj);
	mapBeano.setPreferredSize(new Dimension(proj.getWidth(), proj.getHeight()));
	return mapBeano;
    }


    //Property Functions:
    /////////////////////
	
    /**
     * Get the current properties.
     */
    public Properties getProperties() {
	return getPropertyHandler().getProperties();
    }

    /**
     * Remove an existing property if it exists.
     * @return true if a property was actually removed.
     */
    public boolean removeProperty(String property) {
	return getPropertyHandler().removeProperty(property);
    }

    /** 
     * Add (or overwrite) a property to the current properties
     */
    public void addProperty(String property, String value) {
	getPropertyHandler().addProperty(property, value);
    }

    /** 
     * Add in the properties from the given URL.  Any existing
     * properties will be overwritten except for openmap.components,
     * openmap.layers and openmap.startUpLayers which will be
     * appended.
     */
    public void addProperties(URL urlToProperties) {
	getPropertyHandler().addProperties(urlToProperties);
    }

    /** 
     * Add in the properties from the given source, which can be a
     * resorce, file or URL.  Any existing properties will be
     * overwritten except for openmap.components, openmap.layers and
     * openmap.startUpLayers which will be appended.
     * @throws MalformedURLException if propFile doesn't resolve properly.
     */
    public void addProperties(String propFile) 
	throws java.net.MalformedURLException {
	getPropertyHandler().addProperties(propFile);
    }

    /**
     * remove a marker from a space delimated set of properties.
     */
    public void removeMarker(String property, String marker) {
	getPropertyHandler().removeMarker(property, marker);
    }

    /** 
     * Add in the properties from the given Properties object.  Any
     * existing properties will be overwritten except for
     * openmap.components, openmap.layers and openmap.startUpLayers
     * which will be appended.
     */
    public void addProperties(Properties p) {
	getPropertyHandler().addProperties(p);
    }

    /**
     * Append the given property into the current properties
     */
    public void appendProperty(String property, Properties src) {
	getPropertyHandler().appendProperty(property, src);
    }

    /**
     * Append the given property into the current properties
     */
    public void appendProperty(String property, String value) {
	getPropertyHandler().appendProperty(property, value);
    }

    /**
     * Prepend the given property into the current properties
     */
    public void prependProperty(String property, Properties src) {
	getPropertyHandler().prependProperty(property, src);
    }

    /**
     * Prepend the given property into the current properties
     */
    public void prependProperty(String property, String value) {
	getPropertyHandler().prependProperty(property, value);
    }
}
