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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/LocationHandler.java,v $
// $Revision: 1.3 $ $Date: 2003/12/23 22:55:24 $ $Author: wjeuerle $
// **********************************************************************


package com.bbn.openmap.layer.location;

import com.bbn.openmap.PropertyConsumer;

/*  Java Core  */
import java.util.Properties;
import java.util.Vector;

/**
 * The LocationHandler is the LocationLayer interface to the data.
 * It is the bearer of knowledge about how the location data is
 * stored, and has the smarts on creating the locations and links to
 * represent the data on the map. It also provides controls for
 * changing the display of the data, provided through the getGUI()
 * method, and therefore controls how the data is displayed at a
 * supervisory level.  Each location handler should have its own set of
 * properties:
 * <pre>
 * # Properties for LocationHandler
 * # Show the graphics for all the locations.
 * handler.showLocations=true
 * # Show the labels for all the locations.
 * handler.showNames=true
 * </pre>
 */
public interface LocationHandler extends PropertyConsumer {

    /** A default button name used to trigger more information about a
     *  location to come up in a web browser. */
    public final static String showdetails = "Show Details";
    /** A button name used to turn a location label on/off. */
    public final static String showname = "Always Show Name";

    /** Property setting to show name data on startup. */
    public static final String ShowNamesProperty = "showNames";
    /** Property to use to change the color for name data. */
    public static final String NameColorProperty = "nameColor";
    /** The default line color for names.  */
    public final static String defaultNameColorString = "FF339159"; // greenish

    public static final String ForceGlobalProperty = "override";

    /** Property setting to show location splots on startup */
    public static final String ShowLocationsProperty = "showLocations";
    /** Property to use to set the color of the location splot */
    public static final String LocationColorProperty = "locationColor";
    /** The default line color for locations.  */
    public final static String defaultLocationColorString = "FFCE4F3F"; // reddish

    public final static String showLocationsCommand = "showLocations";
    public final static String showNamesCommand = "showNames";
    public final static String forceGlobalCommand = "forceGlobal";
    public final static String readDataCommand = "readData";

    /**
     * Fill a vector of OMGraphics to represent the data from this
     * handler. 
     *
     * @param nwLat NorthWest latitude of area of interest.
     * @param nwLon NorthWest longitude of area of interest.
     * @param seLat SouthEast latitude of area of interest.
     * @param seLon SouthEast longitude of area of interest.
     * @param graphicList Vector to add Locations to.  If null, the
     * LocationHandler should create a new Vector to place graphics
     * into.
     * @return Either the Vector passed in, or the new onw that was created.
     */
    public Vector get(float nwLat, float nwLon, float seLat, float seLon, 
		      Vector graphicList);
    /**
     * A trigger function to tell the handler that new data is
     * available. 
     */
    public void reloadData();

    /**
     * The location layer passes a LocationPopupMenu to the handler
     * when on of its locations has been clicked on.  This is an
     * opportunity for the handler to add options to the menu that can
     * bring up further information about the location, or to change
     * the appearance of the location.
     * 
     * @param lpm LocationPopupMenu to add buttons to.
     */
    public void fillLocationPopUpMenu(LocationPopupMenu lpm);

    /**
     * Return the layer that the handler is responding to.
     */
    public LocationLayer getLayer();

    /**
     * Set the layer the handler is responding to.  This is needed in
     * case the handler has updates that it wants to show, and needs
     * to trigger a repaint.  It can also be used to communicate with
     * the information delegator.
     *
     * @param layer a LocationLayer
     */
    public void setLayer(LocationLayer layer);

    /**
     * See if the handler is displaying labels at a global level.
     */
    public boolean isShowNames();

    /**
     * Set the handler to show/hide labels at a global level.
     */
    public void setShowNames(boolean set);

    /** 
     * See if the handler is displaying location graphics at a global
     * level.  
     */
    public boolean isShowLocations();

    /**
     * Set the handler to show/hide location graphics at a global
     * level.
     */
    public void setShowLocations(boolean set);

    /**
     * Find out whether global settings should override local ones.
     */
    public boolean isForceGlobal();

    /**
     * Set whether global settings should override local ones.
     */
    public void setForceGlobal(boolean set);

    /**
     * A set of controls to manipulate and control the display of data
     * from the handler.
     *
     * @return Components used for control.
     */
    public java.awt.Component getGUI();

    /**
     * Called by the LocationLayer when the Layer has been removed
     * from the Map.  The LocationHandler should clear out memory/CPU
     * intensive resources.
     *
     * @param cont Container being removed from.
     */
    public void removed(java.awt.Container cont);

}

