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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/MapHandlerMenuItem.java,v $
// $RCSfile: MapHandlerMenuItem.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui.menu;

import com.bbn.openmap.LightMapHandlerChild;
import com.bbn.openmap.MapHandler;

import java.beans.beancontext.BeanContext;
import javax.swing.JMenuItem;

/**
 * A MapHandlerMenuItem is a JMenuItem that uses the MapHandler to
 * find objects it needs to operate.  It's a LightMapHandlerChild so
 * that it's parent AbstractOpenMapMenu will provide the MapHandler
 * and other MapHandler objects to it.  The MapHandlerMenuItem doesn't
 * really need to be added to the MapHandler unless something else
 * needs to find it from the MapHandler.  Instead, it will see
 * everything else in the MapHandler.
 */
public abstract class MapHandlerMenuItem extends JMenuItem
    implements LightMapHandlerChild {

    protected MapHandler mapHandler = null;

    public MapHandlerMenuItem(String title) {
        super(title);
    }

    public void setMapHandler(BeanContext in_mapHandler) {
        if (in_mapHandler instanceof MapHandler) {
            mapHandler = (MapHandler)in_mapHandler;
        }
        setEnabled(mapHandler != null);
    }

    public MapHandler getMapHandler() {
        return mapHandler;
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof MapHandler) {
            setMapHandler((MapHandler) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapHandler) {
            setMapHandler(null);
        }
    }
}
