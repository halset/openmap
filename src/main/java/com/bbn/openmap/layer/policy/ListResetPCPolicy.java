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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/ListResetPCPolicy.java,v $
// $RCSfile: ListResetPCPolicy.java,v $
// $Revision: 1.8 $
// $Date: 2005/09/13 14:33:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

/**
 * ProjectionChangePolicy that uses a Layer SwingWorker to kick off a thread to
 * call layer.prepare() and deletes the current OMGraphicList between projection
 * changes. The standard behavior for layers that gather new OMGraphics for new
 * projections.
 */
public class ListResetPCPolicy
      implements ProjectionChangePolicy {

   protected Logger logger = LoggerFactory.getLogger("com.bbn.openmap.layer.policy.ProjectionChangePolicy");

   /**
    * Don't let this be null.
    */
   protected OMGraphicHandlerLayer layer;

   /**
    * You MUST set a layer at some point.
    */
   public ListResetPCPolicy() {
   }

   /**
    * Don't pass in a null layer.
    */
   public ListResetPCPolicy(OMGraphicHandlerLayer layer) {
      this.layer = layer;
   }

   public void setLayer(OMGraphicHandlerLayer l) {
      layer = l;
   }

   public OMGraphicHandlerLayer getLayer() {
      return layer;
   }

   public void projectionChanged(ProjectionEvent pe) {
      if (layer != null) {
         Projection proj = layer.setProjection(pe);
         // proj will be null if the projection hasn't changed, a
         // signal that work does not need to be done.
         if (proj != null) {
            if (logger.isDebugEnabled()) {
               logger.debug(getLayer().getName() + ": projectionChanged with NEW projection, resetting list.");
            }
            layer.setList(null);
            // Check to see if the projection is worth reacting to.
            if (layer.isProjectionOK(proj)) {
               layer.doPrepare();
            }
         } else {
            if (logger.isDebugEnabled()) {
               logger.debug(getLayer().getName() + ": projectionChanged with OLD projection, repainting.");
            }
            if (!layer.isWorking()) {
               // This repaint may look redundant, but it handles
               // the situation where a layer is removed from a
               // map and readded when the projection doesn't
               // change. Since it already had the projection,
               // and remove() hasn't been called yet, the proj
               // == null. When the new layer is added, it
               // receives a projectionChanged call, and even
               // though it's all set, it still needs to call
               // repaint to have itself show up on the map.
               layer.repaint();
               layer.fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
            }
         }
      } else {
         logger.error("NULL layer, can't do anything.");
      }
   }

   /**
    * This is a subtle call, that dictates what should happen when the
    * LayerWorker has completed working in it's thread. The LayerWorker.get()
    * method returns whatever was returned in the OMGraphicHandler.prepare()
    * method, an OMGraphicList. In most cases, this object should be set as the
    * Layer's list at this time. Some Layers, working asynchronously with their
    * data sources, might want nothing to happen and should use a policy that
    * overrides this method so that nothing does.
    */
   public void workerComplete(OMGraphicList aList) {
      if (layer != null) {
         layer.setList(aList);
      }
   }
}