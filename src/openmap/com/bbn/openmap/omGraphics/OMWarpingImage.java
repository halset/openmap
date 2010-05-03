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
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import com.bbn.openmap.dataAccess.image.WorldFile;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.omGraphics.util.ImageWarp;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.proj.coords.LatLonGCT;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.PropUtils;

/**
 * An OMGraphic that wraps an ImageWarp object to display the resulting
 * OMRaster, in any projection. For use as a background object, doesn't react to
 * mouse events, distance queries, etc.
 * 
 * <pre>
 * try {
 *    String imageFile = &quot;/data/geoimages/NBenguela.2004357.aqua.250m.jpg&quot;;
 *    WorldFile worldFile = WorldFile.get(PropUtils.getResourceOrFileOrURL(imageFile));
 *    OMWarpingImage omwi = new OMWarpingImage(imageFile, LatLonGCT.INSTANCE, worldFile);
 *    omList.add(omwi);
 * } catch (MalformedURLException e) {
 *    e.printStackTrace();
 * } catch (InterruptedException e) {
 *    e.printStackTrace();
 * }
 * 
 * try {
 *    String imageFile = &quot;/data/images/earthmap4k.jpg&quot;;
 *    OMWarpingImage omwi = new OMWarpingImage(imageFile);
 *    omList.add(omwi);
 * } catch (MalformedURLException e) {
 *    e.printStackTrace();
 * } catch (InterruptedException e) {
 *    e.printStackTrace();
 * }
 * 
 * </pre>
 * 
 * @author dietrick
 */
public class OMWarpingImage
      extends OMGraphicAdapter
      implements OMGraphic {

   private static final long serialVersionUID = 1L;
   protected ImageWarp warp;
   protected OMRaster raster;

   /**
    * Create an OMWarpingImage from path to image (resource, file or URL).
    * 
    * @param imagePath
    * @throws MalformedURLException
    * @throws InterruptedException
    */
   public OMWarpingImage(String imagePath)
         throws MalformedURLException, InterruptedException {
      setWarp(imagePath, LatLonGCT.INSTANCE, new DataBounds(-180, -90, 180, 90));
   }

   /**
    * Create an OMWarpingImage from path to image (resource, file or URL).
    * 
    * @param imagePath
    * @param transform the transform describing the image's projection.
    * @param imageBounds the bounds of the image, in its coordinate system.
    * @throws MalformedURLException
    * @throws InterruptedException
    */
   public OMWarpingImage(String imagePath, GeoCoordTransformation transform, DataBounds imageBounds)
         throws MalformedURLException, InterruptedException {
      setWarp(imagePath, transform, imageBounds);
   }

   /**
    * Create an OMWarpingImage from path to image (resource, file or URL).
    * 
    * @param imagePath
    * @param transform transform the transform describing the image's
    *        projection.
    * @param worldfile The WorldFile describing the image's location.
    * @throws MalformedURLException
    * @throws InterruptedException
    */
   public OMWarpingImage(String imagePath, GeoCoordTransformation transform, WorldFile worldfile)
         throws MalformedURLException, InterruptedException {
      setWarp(imagePath, transform, worldfile);
   }

   /**
    * Takes an image, assumed to be a world image in the LLXY projection (equal
    * arc) covering -180, 180 longitude to -90, 90 latitude.
    * 
    * @param bi
    */
   public OMWarpingImage(BufferedImage bi) {
      setWarp(bi, LatLonGCT.INSTANCE, new DataBounds(-180, -90, 180, 90));
   }

   /**
    * Create an OMWarpingImage from a BufferedImage.
    * 
    * @param bi a BufferedImage
    * @param transform the transform describing the image's projection.
    * @param imageBounds the bounds of the image, in its coordinate system.
    * @throws MalformedURLException
    * @throws InterruptedException
    */
   public OMWarpingImage(BufferedImage bi, GeoCoordTransformation transform, DataBounds imageBounds) {
      setWarp(bi, transform, imageBounds);
   }

   /**
    * Create an OMWarpingImage from a BufferedImage.
    * 
    * @param bi BufferedImage
    * @param transform transform the transform describing the image's
    *        projection.
    * @param worldfile The WorldFile describing the image's location.
    * @throws MalformedURLException
    * @throws InterruptedException
    */
   public OMWarpingImage(BufferedImage bi, GeoCoordTransformation transform, WorldFile worldfile) {
      setWarp(bi, transform, worldfile);
   }

   /**
    * Takes an array of ARGB integer values representing an image, assumed to be
    * a world image in the LLXY projection (equal arc) covering -180, 180
    * longitude to -90, 90 latitude.
    * 
    * @param pix int[] ARGB pixel array for image
    * @param width pixel width of image
    * @param height pixel height of image
    */
   public OMWarpingImage(int[] pix, int width, int height) {
      setWarp(pix, width, height, LatLonGCT.INSTANCE, new DataBounds(-180, -90, 180, 90));
   }

   /**
    * Create an OMWarpingImage from a BufferedImage.
    * 
    * @param pix int[] ARGB pixel array for image
    * @param width pixel width of image
    * @param height pixel height of image
    * @param transform the transform describing the image's projection.
    * @param imageBounds the bounds of the image, in its coordinate system.
    */
   public OMWarpingImage(int[] pix, int width, int height, GeoCoordTransformation transform, DataBounds imageBounds) {
      setWarp(pix, width, height, transform, imageBounds);
   }

   /**
    * Create an OMWarpingImage from a BufferedImage.
    * 
    * @param pix int[] ARGB pixel array for image
    * @param width pixel width of image
    * @param height pixel height of image
    * @param transform transform the transform describing the image's
    *        projection.
    * @param worldfile The WorldFile describing the image's location.
    */
   public OMWarpingImage(int[] pix, int width, int height, GeoCoordTransformation transform, WorldFile worldfile) {
      setWarp(pix, width, height, transform, worldfile);
   }

   /**
    * Set all the information needed.
    * 
    * @param imagePath
    * @param transform
    * @param imageBounds
    * @throws MalformedURLException
    * @throws InterruptedException
    */
   public void setWarp(String imagePath, GeoCoordTransformation transform, DataBounds imageBounds)
         throws MalformedURLException, InterruptedException {
      URL imageURL = PropUtils.getResourceOrFileOrURL(imagePath);
      BufferedImage bi = BufferedImageHelper.getBufferedImage(imageURL);
      warp = new ImageWarp(bi, transform, imageBounds);
   }

   public void setWarp(String imagePath, GeoCoordTransformation transform, WorldFile worldfile)
         throws MalformedURLException, InterruptedException {
      URL imageURL = PropUtils.getResourceOrFileOrURL(imagePath);
      BufferedImage bi = BufferedImageHelper.getBufferedImage(imageURL);
      warp = new ImageWarp(bi, transform, worldfile);
   }

   /**
    * Set all the information needed.
    * 
    * @param pix int[] ARGB pixel array for image
    * @param width pixel width of image
    * @param height pixel height of image
    * @param transform
    * @param imageBounds
    */
   public void setWarp(int[] pix, int width, int height, GeoCoordTransformation transform, DataBounds imageBounds) {
      warp = new ImageWarp(pix, width, height, transform, imageBounds);
   }

   /**
    * Set all the information needed.
    * 
    * @param pix int[] ARGB pixel array for image
    * @param width pixel width of image
    * @param height pixel height of image
    * @param transform
    * @param worldFile
    */
   public void setWarp(int[] pix, int width, int height, GeoCoordTransformation transform, WorldFile worldfile) {
      warp = new ImageWarp(pix, width, height, transform, worldfile);
   }

   public void setWarp(BufferedImage bi, GeoCoordTransformation transform, DataBounds imageBounds) {
      warp = new ImageWarp(bi, transform, imageBounds);
   }

   public void setWarp(BufferedImage bi, GeoCoordTransformation transform, WorldFile worldfile) {
      warp = new ImageWarp(bi, transform, worldfile);
   }

   public void setWarp(ImageWarp wrp) {
      warp = wrp;
   }

   public ImageWarp getWarp() {
      return warp;
   }

   @Override
   public boolean generate(Projection proj) {
      if (warp != null) {
         raster = warp.getOMRaster(proj);
      }

      return true;
   }

   @Override
   public void render(Graphics g) {
      if (raster != null) {
         raster.render(g);
      }
   }

}
