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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkLine.java,v $
// $RCSfile: LinkLine.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link;

import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;

import java.awt.BasicStroke;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Read and write a Link protocol version of a line.
 */
public class LinkLine implements LinkGraphicConstants, LinkPropertiesConstants {

    /** 
     * Write a line using lat/lon endpoints.  The lat/lons are in
     * decimal degrees.
     *.
     * @param lat_1 latitude of placement of start of line.
     * @param lon_1 longitude of placement of start of line.
     * @param lat_2 latitude of placement of end of line.
     * @param lon_2 longitude of placement of end of line.
     * @param lineType type of line - straight, rhumb, great circle..
     * @param properties Properties containing attributes.
     * @param dos DataOutputStream to write to.
     * @throws IOException 
     */
    public static void write (float lat_1, float lon_1, 
			      float lat_2, float lon_2, 
			      int lineType,
			      LinkProperties properties,
			      DataOutputStream dos) throws IOException {
	LinkLine.write(lat_1, lon_1, lat_2, lon_2, lineType, -1, properties, dos);
    }

    /** 
     * Write a line using lat/lon endpoints.  The lat/lons are in
     * decimal degrees.  This method gives you the option of
     * specifying a number of segments to use in approximating a
     * curved line.
     *
     * @param lat_1 latitude of placement of start of line.
     * @param lon_1 longitude of placement of start of line.
     * @param lat_2 latitude of placement of end of line.
     * @param lon_2 longitude of placement of end of line.
     * @param lineType type of line - straight, rhumb, great circle..
     * @param nsegs number of points to use to approximate curved line..
     * @param properties Properties containing attributes.
     * @param dos DataOutputStream to write to.
     * @throws IOException 
     */
    public static void write (float lat_1, float lon_1, 
			      float lat_2, float lon_2, 
			      int lineType, int nsegs,
			      LinkProperties properties,
			      DataOutputStream dos) throws IOException {

	dos.write(Link.LINE_HEADER.getBytes());
	dos.writeInt(GRAPHICTYPE_LINE);
	dos.writeInt(RENDERTYPE_LATLON);
	dos.writeInt(lineType);
	dos.writeFloat(lat_1);
	dos.writeFloat(lon_1);
	dos.writeFloat(lat_2);
	dos.writeFloat(lon_2);
	dos.writeInt(nsegs);
	properties.write(dos);
    }

    /** Write a line with x/y pixel endpoints.
     * @param x1 Horizontal pixel placement of start of line.
     * @param y1 Vertical pixel placement of start of line.
     * @param x2 Horizontal pixel placement of end of line.
     * @param y2 Vertical pixel placement of end of line.
     * @param properties Properties containing attributes.
     * @param dos DataOutputStream to write to.
     * @throws IOException
     */
    public static void write (int x1, int y1, 
			      int x2, int y2,
			      LinkProperties properties,
			      DataOutputStream dos) throws IOException {

	dos.write(Link.LINE_HEADER.getBytes());
	dos.writeInt(GRAPHICTYPE_LINE);
	dos.writeInt(RENDERTYPE_XY);
	dos.writeInt(x1);
	dos.writeInt(y1);
	dos.writeInt(x2);
	dos.writeInt(y2);
	properties.write(dos);
    }

    /**
     * Write a line located at an x/y pixel offset from a lat/lon location.
     * @param lat_1 latitude of placement of line.
     * @param lon_1 longitude of placement of line.
     * @param x1 Horizontal pixel offset of start of line.
     * @param y1 Vertical pixel offset of start of line.
     * @param x2 Horizontal pixel offset of end of line.
     * @param y2 Vertical pixel offset of end of line.
     * @param properties Properties containing attributes.
     * @param dos DataOutputStream to write to.
     * @throws IOException
     */
    public static void write (float lat_1, float lon_1, 
			      int x1, int y1, 
			      int x2, int y2, 
			      LinkProperties properties,
			      DataOutputStream dos) throws IOException {
	
	dos.write(Link.LINE_HEADER.getBytes());
	dos.writeInt(GRAPHICTYPE_LINE);
	dos.writeInt(RENDERTYPE_OFFSET);
	dos.writeFloat(lat_1);
	dos.writeFloat(lon_1);
	dos.writeInt(x1);
	dos.writeInt(y1);
	dos.writeInt(x2);
	dos.writeInt(y2);
	properties.write(dos);
    }


    /**
     * Write an OMLine to the link.
     */
    public static void write(OMLine line, Link link, LinkProperties props)
	throws IOException {

	switch (line.getRenderType()) {
	case OMLine.RENDERTYPE_LATLON:
	    float[] ll = line.getLL();
	    LinkLine.write(ll[0], ll[1], ll[2], ll[3], 
			   line.getLineType(), line.getNumSegs(),
			   props, link.dos);
	    break;
	case OMLine.RENDERTYPE_XY:
	    int[] pts = line.getPts();
	    LinkLine.write(pts[0], pts[1], pts[2], pts[3], props, link.dos);
	    break;
	case OMLine.RENDERTYPE_OFFSET:
	    ll = line.getLL();
	    pts = line.getPts();
	    LinkLine.write(ll[0], ll[1], pts[0], pts[1], pts[2], pts[3],
			   props, link.dos);
	    break;
	default:
	    Debug.error("LinkLine.write: line rendertype unknown.");
	}
    }

    /**  
     * Read the line Link protocol off a DataInputStream, and create an
     * OMLine from it.  Assumes that the header has already been read.
     * @param dis DataInputStream to read from.
     * @return OMLine
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMLine 
     */
    public static OMLine read (DataInputStream dis) throws IOException {

	OMLine line = null;

	float lat_1 = 0.0f;
	float lon_1 = 0.0f;
	float lat_2 = 0.0f;
	float lon_2 = 0.0f;

	int x1 = 0;
	int y1 = 0;
	int x2 = 0;
	int y2 = 0;
	int nsegs = -1;

	int renderType = dis.readInt();
	
	switch (renderType){
	case RENDERTYPE_LATLON:
	    int lineType = dis.readInt();
	    lat_1 = dis.readFloat();
	    lon_1 = dis.readFloat();
	    lat_2 = dis.readFloat();
	    lon_2 = dis.readFloat();
	    nsegs = dis.readInt();
	    
	    line = new OMLine(lat_1, lon_1, lat_2, lon_2, 
			      lineType, nsegs);
	    break;
	case RENDERTYPE_XY:
	    x1 = dis.readInt();
	    y1 = dis.readInt();
	    x2 = dis.readInt();
	    y2 = dis.readInt();
	    
	    line = new OMLine(x1, y1, x2, y2);
	    break;
	case RENDERTYPE_OFFSET:
	    lat_1 = dis.readFloat();
	    lon_1 = dis.readFloat();
	    x1 = dis.readInt();
	    y1 = dis.readInt();
	    x2 = dis.readInt();
	    y2 = dis.readInt();
	    
	    line = new OMLine(lat_1, lon_1, x1, y1, x2, y2);
	    break;
	default:
	}

	LinkProperties properties = new LinkProperties(dis);

	if (line != null){
	    line.setLinePaint(ColorFactory.parseColorFromProperties(
		properties, LPC_LINECOLOR,
		BLACK_COLOR_STRING, true));
	    line.setFillPaint(ColorFactory.parseColorFromProperties(
		properties, LPC_FILLCOLOR,
		CLEAR_COLOR_STRING, true));
	    line.setSelectPaint(ColorFactory.parseColorFromProperties(
		properties, LPC_HIGHLIGHTCOLOR,
		BLACK_COLOR_STRING, true));
	    line.setStroke(new BasicStroke(LayerUtils.intFromProperties(
		properties, LPC_LINEWIDTH, 1)));
	    line.setAppObject(properties);
	}
	return line;
    }
}
