/* 
 * <copyright>
 *  Copyright 2012 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.ext.jts;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;

/**
 * A {@link CoordinateSequenceFilter} to extract float[] xpts and ypts from a
 * {@link CoordinateSequence}
 */
class XYCoordianteSequenceExtractorFilter
      implements CoordinateSequenceFilter {

   private final float[] xpts;
   private final float[] ypts;

   public XYCoordianteSequenceExtractorFilter(float[] xpts, float[] ypts) {
      this.xpts = xpts;
      this.ypts = ypts;
   }

   public void filter(CoordinateSequence seq, int i) {
      xpts[i] = (float) seq.getX(i);
      ypts[i] = (float) seq.getY(i);
   }

   public boolean isDone() {
      return false;
   }

   public boolean isGeometryChanged() {
      return false;
   }

}
