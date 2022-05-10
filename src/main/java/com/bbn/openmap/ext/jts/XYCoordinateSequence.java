/* 
 * <copyright>
 *  Copyright 2012 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.ext.jts;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;

import com.bbn.openmap.util.DeepCopyUtil;

class XYCoordinateSequence
      implements CoordinateSequence {

   private float[] xpts;
   private float[] ypts;

   public XYCoordinateSequence(float[] xpts, float[] ypts) {
      this.xpts = xpts;
      this.ypts = ypts;
   }

   public XYCoordinateSequence(int size) {
      this(new float[size], new float[size]);
   }

   public int getDimension() {
      return 2;
   }

   public Coordinate getCoordinate(int i) {
      Coordinate c = new Coordinate();
      getCoordinate(i, c);
      return c;
   }

   public Coordinate getCoordinateCopy(int i) {
      return getCoordinate(i);
   }

   public void getCoordinate(int index, Coordinate coord) {
      coord.x = xpts[index];
      coord.y = ypts[index];
      coord.z = Double.NaN;
   }

   public double getX(int index) {
      return xpts[index];
   }

   public double getY(int index) {
      return ypts[index];
   }

   public double getOrdinate(int index, int ordinateIndex) {
      switch (ordinateIndex) {
         case 0:
            return xpts[index];
         case 1:
            return ypts[index];
         default:
            return Double.NaN;
      }
   }

   public int size() {
      return xpts.length;
   }

   public void setOrdinate(int index, int ordinateIndex, double value) {
      switch (ordinateIndex) {
         case 0:
            xpts[index] = (float) value;
            break;
         case 1:
            ypts[index] = (float) value;
            break;
      }
   }

   public Coordinate[] toCoordinateArray() {
      Coordinate[] cs = new Coordinate[size()];
      for (int i = 0; i < cs.length; i++) {
         cs[i] = getCoordinate(i);
      }
      return cs;
   }

   public Envelope expandEnvelope(Envelope env) {
      for (int i = 0; i < xpts.length; i++) {
         env.expandToInclude(xpts[i], ypts[i]);
      }
      return env;
   }

   @Override
   public Object clone() {
      return copy();
   }

   @Override
   public CoordinateSequence copy() {
      return new XYCoordinateSequence(DeepCopyUtil.deepCopy(xpts), DeepCopyUtil.deepCopy(ypts));
   }

}
