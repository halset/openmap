/*
 *                     RESTRICTED RIGHTS LEGEND
 *
 *                        BBNT Solutions LLC
 *                        A Verizon Company
 *                        10 Moulton Street
 *                       Cambridge, MA 02138
 *                         (617) 873-3000
 *
 * Copyright BBNT Solutions LLC 2005 All Rights Reserved
 * 
 */

package com.bbn.openmap.geo;

/**
 * A geographic (great circle) line segment. Used in Path Iterators.
 * 
 * @author mthome@bbn.com
 */

public interface GeoSegment extends GeoExtent {
    /**
     * @return the current segment as a two-element array of Geo The
     *         first point is the "current point" and the second is
     *         the next. If there isn't another point available, will
     *         throw an indexOutOfBounds exception.
     */
    Geo[] getSeg();

    /**
     * @return the current segment as a float[]. The first point is
     *         the "current point" and the second is the next. If
     *         there isn't another point available, will throw an
     *         indexOutOfBounds exception.
     */
    float[] getSegArray();

    /**
     * @return an opaque indicator for which segment is being current.
     *         Different implementations may document the type to be
     *         returned.
     */
    Object getSegId();

    public static class Impl implements GeoSegment {
        protected Geo[] seg;
        protected Object id = GeoSegment.Impl.this;

        /**
         * Create a GeoSegment.Impl with an array of 2 Geos.
         * 
         * @param segment Geo[2].
         */
        public Impl(Geo[] segment) {
            seg = segment;
        }
        
        public Geo[] getSeg() {
            return seg;
        }

        public float[] getSegArray() {
            return new float[] { (float) seg[0].getLatitude(),
                    (float) seg[0].getLongitude(),
                    (float) seg[1].getLatitude(), (float) seg[1].getLongitude() };
        }

        public BoundingCircle getBoundingCircle() {
            return new BoundingCircle.Impl(seg);
        }
        
        public void setSegId(Object segId) {
            id = segId;
        }
        
        public Object getSegId() {
            return id;
        }
    }
}
