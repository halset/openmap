// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/SaveAsImageFileChooser.java,v $
// $RCSfile: SaveAsImageFileChooser.java,v $
// $Revision: 1.5 $
// $Date: 2004/12/10 14:08:54 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.BorderLayout;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.bbn.openmap.gui.DimensionQueryPanel;
import com.bbn.openmap.util.PaletteHelper;

/**
 * A class extended from a JFileChooser that adds fields for
 * specifying the image size.
 */
public class SaveAsImageFileChooser extends JFileChooser {

    DimensionQueryPanel dqp = new DimensionQueryPanel();

    /**
     * Create file chooser with the image size fields filled in.
     */
    public SaveAsImageFileChooser(int width, int height) {
        super();
        dqp.setHeight(height);
        dqp.setWidth(width);
        JPanel imageSizePanel = PaletteHelper.createPaletteJPanel(" Set Image Size ");
        imageSizePanel.setLayout(new BorderLayout());
        imageSizePanel.add(dqp, BorderLayout.CENTER);
        setAccessory(imageSizePanel);
    }

    /**
     * Set the value of the image width setting from the GUI.
     */
    public void setImageWidth(int w) {
        dqp.setWidth(w);
    }

    /**
     * Get the value of the image width setting from the GUI.
     */
    public int getImageWidth() {
        return dqp.getWidth();
    }

    /**
     * Set the value of the image height setting from the GUI.
     */
    public void setImageHeight(int h) {
        dqp.setHeight(h);
    }

    /**
     * Get the value of the image height setting from the GUI.
     */
    public int getImageHeight() {
        return dqp.getHeight();
    }

}