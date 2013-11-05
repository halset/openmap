/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.proj;

import java.util.Properties;

import com.bbn.openmap.proj.coords.AxisOrder;
import com.bbn.openmap.proj.coords.CoordinateReferenceSystem;
import com.bbn.openmap.proj.coords.LatLonPoint;

import junit.framework.TestCase;

public class CoordinateReferenceSystemTest
      extends TestCase {

   public void testGetCodes() {
      assertFalse(CoordinateReferenceSystem.getCodes().isEmpty());
   }

   public void testUTM() {
      CoordinateReferenceSystem utm33n = CoordinateReferenceSystem.getForCode("EPSG:32633");
      assertNotNull(utm33n);
      assertEquals(AxisOrder.eastBeforeNorth, utm33n.getAxisOrder());
      
      Properties props = new Properties();
      props.put(ProjectionFactory.CENTER, new LatLonPoint.Double(71, 8));
      props.put(ProjectionFactory.WIDTH, "100");
      props.put(ProjectionFactory.HEIGHT, "100");
      
      GeoProj proj = utm33n.createProjection(props);
      assertTrue(proj instanceof UTMProjection);
      UTMProjection utmproj = (UTMProjection) proj;
      assertTrue(utmproj.northern);
      assertEquals(33, utmproj.zoneNumber);
   }

}
