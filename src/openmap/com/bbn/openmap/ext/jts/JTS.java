/* 
 * <copyright>
 *  Copyright 2012 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.ext.jts;

import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.proj.Projection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A collection of utility methods for using JTS in OpenMap
 */
public class JTS {

   private JTS() {

   }

   /**
    * Create a JTS {@link Geometry} for the current view of the given OpenMap
    * {@link Projection} expanded with the given pixel buffer. The coordinates
    * are all in pixel space.
    * 
    * @param gf
    * @param proj
    * @param buffer
    * @return
    */
   public static Polygon createXYViewPolygon(GeometryFactory gf, Projection proj, int buffer) {
      Coordinate[] coords = new Coordinate[5];
      coords[0] = new Coordinate(-buffer, proj.getHeight() + buffer);
      coords[1] = new Coordinate(proj.getWidth() + buffer, proj.getHeight() + buffer);
      coords[2] = new Coordinate(proj.getWidth() + buffer, -buffer);
      coords[3] = new Coordinate(-buffer, -buffer);
      coords[4] = coords[0];
      LinearRing ring = gf.createLinearRing(coords);
      return gf.createPolygon(ring, null);
   }
   
   
   public static Geometry createRectangle(GeometryFactory gf, double minx, double miny, double maxx, double maxy) {
      Coordinate[] coords = new Coordinate[5];
      coords[0] = new Coordinate(minx, maxy);
      coords[1] = new Coordinate(maxx, maxy);
      coords[2] = new Coordinate(maxx, miny);
      coords[3] = new Coordinate(minx, miny);
      coords[4] = coords[0];
      LinearRing ring = gf.createLinearRing(coords);
      return gf.createPolygon(ring, null);
   }

   /**
    * Clip the given List of xypnts to match the given clipGeometry. The result
    * is manipulated in to the xypnts List.
    * 
    * @param gf
    * @param clipGeometry
    * @param xypnts
    */
   public static void clip(GeometryFactory gf, Geometry clipGeometry, List<float[]> xypnts) {
      int size = xypnts.size();
      List<float[]> newxypnts = new ArrayList<float[]>(size);

      for (int i = 0; i < size; i += 2) {
         float[] xpts = xypnts.get(i);
         float[] ypts = xypnts.get(i + 1);

         // check if the geometry needs to be clipped
         if (contains(clipGeometry.getEnvelopeInternal(), xpts, ypts)) {
            newxypnts.add(xpts);
            newxypnts.add(ypts);
            continue;
         }
         
         CoordinateSequence coords = new XYCoordinateSequence(xpts, ypts);
         Geometry original = null;

         // try to figure out if it is a line or a polygon
         if (coords.getCoordinate(0).equals2D(coords.getCoordinate(coords.size() - 1))) {
            LinearRing ring = gf.createLinearRing(coords);
            original = gf.createPolygon(ring, null);
         } else {
            original = gf.createLineString(coords);
         }

         Geometry intersection = original.intersection(clipGeometry);

         // the intersection might result in multiple geometries
         int numGeometries = intersection.getNumGeometries();
         for (int j = 0; j < numGeometries; j++) {
            Geometry intersectionPart = intersection.getGeometryN(j);
            int numPoints = intersectionPart.getNumPoints();

            float[] newxpts = new float[numPoints];
            float[] newypts = new float[numPoints];

            XYCoordianteSequenceExtractorFilter filter = new XYCoordianteSequenceExtractorFilter(newxpts, newypts);
            intersectionPart.apply(filter);

            newxypnts.add(newxpts);
            newxypnts.add(newypts);
         }

      }
      
      xypnts.clear();
      xypnts.addAll(newxypnts);
   }

   private static boolean contains(Envelope envelope, float[] xpts, float[] ypts) {
      for (int i = 0; i < xpts.length; i++) {
         if (!envelope.contains(xpts[i], ypts[i])) {
            return false;
         }
      }
      return true;
   }

}
