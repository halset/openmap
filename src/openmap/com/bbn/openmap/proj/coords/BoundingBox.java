package com.bbn.openmap.proj.coords;


/**
 * A Bounding Box.
 */
public class BoundingBox {

    private double minx;

    private double miny;

    private double maxx;

    private double maxy;

    public BoundingBox(double minx, double miny, double maxx, double maxy) {
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
    }

    public double getMinX() {
        return minx;
    }

    public double getMinY() {
        return miny;
    }

    public double getMaxX() {
        return maxx;
    }

    public double getMaxY() {
        return maxy;
    }
    
    public static BoundingBox fromString(String bbox) {
       if (bbox == null) {
           throw new IllegalArgumentException("Missing BBOX parameter");
       }

       String[] arrayBBox = bbox.split(",");

       if (arrayBBox.length != 4) {
           throw new IllegalArgumentException(
                   "Invalid BBOX parameter. BBOX must contain exactly 4 values separated with comas.");
       }

       double minX = Double.parseDouble(arrayBBox[0]);
       double minY = Double.parseDouble(arrayBBox[1]);
       double maxX = Double.parseDouble(arrayBBox[2]);
       double maxY = Double.parseDouble(arrayBBox[3]);

       return new BoundingBox(minX, minY, maxX, maxY);
   }
    
   @Override
   public String toString() {
      return getMinX() + "," + getMinY() + "," + getMaxX() + "," + getMaxY();
   }


}
