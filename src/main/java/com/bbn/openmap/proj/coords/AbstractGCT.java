//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: AbstractGCT.java,v $
//$Revision: 1.2 $
//$Date: 2008/01/29 22:04:13 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.proj.coords;

import java.awt.geom.Point2D;

import com.bbn.openmap.OMComponent;

public abstract class AbstractGCT extends OMComponent implements GeoCoordTransformation {

    public Point2D forward(double lat, double lon) {
        return forward(lat, lon, new Point2D.Double());
    }

    public abstract Point2D forward(double lat, double lon, Point2D ret);

    public LatLonPoint inverse(double x, double y) {
        return inverse(x, y, new LatLonPoint.Double());
    }

    public abstract LatLonPoint inverse(double x, double y, LatLonPoint ret);
    
    public Point2D center(double lowerLeftX, double lowerLeftY, double upperRightX, double upperRightY) {
       double medy = med(lowerLeftY, upperRightY);
       
       double minx = lowerLeftX;
       double maxx = upperRightX;
       double width = width();
       
       while (width > 0 && minx > maxx) {
          maxx = maxx + width;
       }
       
       double medx = med(minx, maxx);
       return new Point2D.Double(medx, medy);
    }
    
    private static double med(double v1, double v2) {
       return ((v2 - v1) / 2d) + v1;
    }
    
   /**
    * Width of the map in map units. Like 360d for latlon. A value less than
    * zero means unknown.
    */
    protected double width() {
       return -1d;
    }

}
