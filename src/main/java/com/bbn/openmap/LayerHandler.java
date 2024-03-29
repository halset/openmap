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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/LayerHandler.java,v $
// $RCSfile: LayerHandler.java,v $
// $Revision: 1.16 $
// $Date: 2006/08/09 21:08:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.awt.Component;
import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContext;
import java.io.Serializable;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.event.LayerSupport;
import com.bbn.openmap.plugin.PlugIn;
import com.bbn.openmap.plugin.PlugInLayer;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * The LayerHandler is a component that keeps track of all Layers for the
 * MapBean, whether or not they are currently part of the map or not. It is able
 * to dynamically add and remove layers from the list of available layers.
 * Whether a layer is added to the MapBean depends on the visibility setting of
 * the layer. If Layer.isVisible() is true, the layer will be added to the
 * MapBean. There are methods within the LayerHandler that let you change the
 * visibility setting of a layer.
 * <p/>
 * <p/>
 * The LayerHandler is able to take a Properties object, and create layers that
 * are defined within it. The key property is "layers", which may or may not
 * have a prefix for it. If that property does have a prefix (prefix.layers,
 * i.e. openmap.layers), then that prefix has to be known and passed in to the
 * constructor or init method. This layers property should fit the general
 * openmap marker list paradigm, where the marker names are listed in a space
 * separated list, and then each marker name is used as a prefix for the
 * properties for a particular layer. As a minimum, each layer needs to have the
 * class and prettyName properties defined. The class property should define the
 * class name to use for the layer, and the prettyName property needs to be a
 * name for the layer to be used in the GUI. Any other property that the
 * particular layer can use should be listed in the Properties, with the
 * applicable marker name as a prefix. Each layer should have its available
 * properties defined in its documentation. For example:
 * <p/>
 * <p/>
 * 
 * <pre>
 *           &lt;p/&gt;
 *             openmap.layers=marker1 marker2 (etc)
 *             marker1.class=com.bbn.openmap.layer.GraticuleLayer
 *             marker1.prettyName=Graticule Layer
 *             # false is default
 *             marker1.addToBeanContext=false
 *           &lt;p/&gt;
 *             marker2.class=com.bbn.openmap.layer.shape.ShapeLayer
 *             marker2.prettyName=Political Boundaries
 *             marker2.shapeFile=pathToShapeFile
 *             marker2.spatialIndex=pathToSpatialIndexFile
 *             marker2.lineColor=FFFFFFFF
 *             marker2.fillColor=FFFF0000
 *           &lt;p/&gt;
 * </pre>
 * 
 * <p/>
 * <p/>
 * <p/>
 * The LayerHandler is a SoloMapComponent, which means that for a particular
 * map, there should only be one of them. When a LayerHandler is added to a
 * BeanContext, it will look for a MapBean to connect to itself as a
 * LayerListener so that the MapBean will receive LayerEvents - this is the
 * mechanism that adds and removes layers on the map. If more than one MapBean
 * is added to the BeanContext, then the last MapBean added will be added as a
 * LayerListener, with any prior MapBeans added as a LayerListener removed from
 * the LayerHandler. The MapHandler controls the behavior of multiple
 * SoloMapComponent addition to the BeanContext.
 */
public class LayerHandler extends OMComponent implements SoloMapComponent,
        Serializable {

    public static Logger logger = LoggerFactory.getLogger("com.bbn.openmap.LayerHandler");

    /**
     * Property for space separated layers. If a prefix is needed, just use the
     * methods that let you use the prefix - don't worry about the period, it
     * will be added automatically.
     */
    public static final String layersProperty = "layers";
    /**
     * Property for space separated layers to be displayed at startup. If a
     * prefix is needed, just use the methods that let you use the prefix -
     * don't worry about the period, it will be added automatically.
     */
    public static final String startUpLayersProperty = "startUpLayers";
    /**
     * Flag to set synchronous threading on the LayerHandler, telling it to
     * react to layer order changes and layer visibility requests within the
     * calling thread. By default, this action is true. Setting it to false may
     * eliminate pauses in GUI reactions by off-loading work done by layers
     * being added to the MapBean, but there have been reports that the
     * asynchronous nature of the threading queue may be causing an unexpected
     * state in layer order and/or availability under certain intense layer
     * management conditions (created by automated processes, for example).
     */
    public static final String SynchronousThreadingProperty = "synchronousThreading";
    /**
     * The object holding on to all LayerListeners interested in the layer
     * arrangement and availability. Not expected to be null.
     */
    protected transient LayerSupport listeners = new LayerSupport(this);
    /**
     * The list of all layers, even the ones that are not part of the map.
     */
    protected Layer[] allLayers = new Layer[0];
    /**
     * This handle is only here to keep it appraised of layer prefix names.
     */
    protected PropertyHandler propertyHandler;

    /**
     * If you use this constructor, the LayerHandler expects that the layers
     * will be created and added later, either by addLayer() or init().
     */
    public LayerHandler() {
    }

    /**
     * Start the LayerHandler, and have it create all the layers as defined in a
     * properties file.
     * 
     * @param props
     *            properties as defined in an openmap.properties file.
     */
    public LayerHandler(Properties props) {
        init(null, props);
    }

    /**
     * Start the LayerHandler, and have it create all the layers as defined in a
     * properties file.
     * 
     * @param prefix
     *            the prefix for the layers and startUpLayers properties, as if
     *            they are listed as prefix.layers, and prefix.startUpLayers.
     * @param props
     *            properties as defined in an openmap.properties file.
     */
    public LayerHandler(String prefix, Properties props) {
        init(prefix, props);
    }

    /**
     * Start the LayerHandler with configured layers.
     */
    public LayerHandler(Layer[] layers) {
        init(layers);
    }

    /**
     * Extension of the OMComponent. If the LayerHandler is created by the
     * ComponentFactory (via the PropertyHandler), this method will be called
     * automatically. For the OpenMap applications, this method is rigged to
     * handle the openmap.layers property by calling init("openmap", props). If
     * you are using the LayerHandler in a different setting, then you might
     * want to just call init() directly, or extend this class and have
     * setProperties do what you want.
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        // Whoa! We used to replace the prefix provided to this method with
        // 'openmap',
        // AKA Environment.OpenMapPrefix, but that seems rude and hackish. We're
        // going to have the getLayers(prefix, props) method use the prefix
        // passed in, and if the layerHandler prefix.layers can't be found,
        // we'll revert to looking for the openmap.layers property, just to be
        // backward compatible.
        // init(Environment.OpenMapPrefix, props);
        init(prefix, props);
    }

    /**
     * Initialize the LayerHandler by having it construct it's layers from a
     * properties object. The properties should be created from an
     * openmap.properties file.
     * 
     * @param prefix
     *            the prefix to use for the layers and startUpLayers properties.
     * @param props
     *            properties as defined in an openmap.properties file.
     */
    public void init(String prefix, Properties props) {
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        init(getLayers(prefix, props));

        getListeners()
                .setSynchronous(
                                PropUtils
                                        .booleanFromProperties(
                                                               props,
                                                               prefix
                                                                       + SynchronousThreadingProperty,
                                                               getListeners()
                                                                       .isSynchronous()));
    }

    /**
     * Initialize the LayerHandler by having it construct it's layers from a URL
     * containing an openmap.properties file.
     * 
     * @param url
     *            a url for a properties file.
     */
    public void init(java.net.URL url) {
        init(null, url);
    }

    /**
     * Initialize the LayerHandler by having it construct it's layers from a URL
     * containing an openmap.properties file.
     * 
     * @param prefix
     *            the prefix to use for the layers and startUpLayers properties.
     * @param url
     *            a url for a properties file.
     */
    public void init(String prefix, java.net.URL url) {
        try {
            java.io.InputStream in = url.openStream();
            Properties props = new Properties();
            props.load(in);
            init(getLayers(prefix, props));
        } catch (java.net.MalformedURLException murle) {
            logger.error("LayerHandler.init(URL): " + url
                    + " is not a valid URL");
        } catch (java.io.IOException e) {
            logger.error("LayerHandler.init(URL): Caught an IOException");
        }
    }

    /**
     * Initialize from an array of layers. This will cause the LayerListeners,
     * if they exist, to update themselves with the current list of layers. This
     * will check to add layers to the MapHandler.
     * 
     * @param layers
     *            the initial array of layers.
     */
    public void init(Layer[] layers) {

        // Should get rid of the old layers properly.
        removeAll();

        // OK, we need to check the allLayers array, because at this point it
        // could still be holding non-removable layers. If we just replace them,
        // we've broken the contract of non-removal. Move the non-removable
        // layers
        // to the bottom and put the new layers on top. We also need to check to
        // make sure that any duplicate layers on either list are parsed down to
        // one layer. We use the Vector.contains() method for that check.
        if (allLayers != null && allLayers.length > 0) {
            int lLength = (layers != null ? layers.length : 0);
            Vector<Layer> newLayers = new Vector<Layer>(allLayers.length
                    + lLength);
            if (layers != null) {
                for (int i = 0; i < lLength; i++) {
                    if (!newLayers.contains(layers[i])) {
                        newLayers.add(layers[i]);
                    }
                }
            }

            for (int i = 0; i < allLayers.length; i++) {
                if (!newLayers.contains(allLayers[i])) {
                    newLayers.add(allLayers[i]);
                }
            }

            layers = new Layer[newLayers.size()];
            layers = (Layer[]) newLayers.toArray(layers);
        }

        setLayers(layers);

        // This should work for layers being reloaded from the PropertyHandler,
        // it's better than doing it in the getLayers(...) method below
        // (getLayers() is called before init()). For the
        // initial LayerHandler construction and Layer creation in an
        // application, the BeanContext should be null at this point, so this
        // method call will do nothing. But for resetting the layers with new
        // ones, they will get dumped into the BeanContext/MapHandler.
        addLayersToBeanContext(layers);
    }

    public void setPropertyHandler(PropertyHandler ph) {
        propertyHandler = ph;
    }

    public PropertyHandler getPropertyHandler() {
        return propertyHandler;
    }

    /**
     * This is the method that gets used to parse the layer properties from an
     * openmap.properties file, where the layer marker names are listed under a
     * layers property, and each layer is then represented by a marker.class
     * property, and a maker.prettyName property.
     * 
     * @param p
     *            properties containing layers property, the startupLayers
     *            property listing the layers to make visible immediately, and
     *            the layer properties as well.
     * @return Layer[] of layers created from the properties.
     */
    protected Layer[] getLayers(Properties p) {
        return getLayers(null, p);
    }

    /**
     * This is the method that gets used to parse the layer properties from an
     * openmap.properties file, where the layer marker names are listed under a
     * prefix.layers property, and each layer is then represented by a
     * marker.class property, and a maker.prettyName property.
     * 
     * @param prefix
     *            the prefix to use to use for the layer list (layers) property
     *            and the startUpLayers property. If it is not null, this will
     *            cause the method to look for prefix.layers and
     *            prefix.startUpLayers.
     * @param p
     *            the properties to build the layers from.
     * @return Layer[]
     */
    protected Layer[] getLayers(String prefix, Properties p) {
        logger.debug("Getting new layers from properties...");

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String layersValueString = p.getProperty(prefix + layersProperty);

        String startupLayersValueString = p.getProperty(prefix
                + startUpLayersProperty);

        if (layersValueString == null) {
            layersValueString = p.getProperty(PropUtils
                    .getScopedPropertyPrefix(Environment.OpenMapPrefix)
                    + layersProperty);
        }

        if (startupLayersValueString == null) {
            startupLayersValueString = p.getProperty(PropUtils
                    .getScopedPropertyPrefix(Environment.OpenMapPrefix)
                    + startUpLayersProperty);
        }

        Vector<String> startuplayers = PropUtils
                .parseSpacedMarkers(startupLayersValueString);
        Vector<String> layersValue = PropUtils
                .parseSpacedMarkers(layersValueString);

        if (startuplayers.isEmpty()) {
            logger.info("No layers on startup list");
        }

        if (layersValue.isEmpty()) {
            logger.info("No property \"" + layersProperty
                    + "\" found in properties.");
            return new Layer[0];
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Layer markers found = " + layersValue);
            }
        }

        Layer[] layers = getLayers(layersValue, startuplayers, p);

        // You don't want to call addLayersToBeanContext here, it sets up a
        // cycle. The layers are not yet set in the LayerHandler, so the
        // LayerHandle won't know to ignore them when they show up in
        // findAndInit(). The one thing this call did, however, is get the
        // BeanContext to the layers before the startup layers were added to the
        // MapBean. It's possible that without this call, layers that build
        // their OMGraphicLists once may not have the BeanContext resources they
        // need in order to build that list.

        // addLayersToBeanContext(layers);

        return layers;
    }

    /**
     * A static method that lets you pass in a Properties object, along with two
     * Vectors of strings, each Vector representing marker names for layers
     * contained in the Properties.
     * <p/>
     * If a PlugIn is listed in the properties, the LayerHandler will create a
     * PlugInLayer for it and set the PlugIn in that layer.
     * 
     * @param layerList
     *            Vector of marker names to use to inspect the properties with.
     * @param visibleLayerList
     *            Vector of marker names representing the layers that should
     *            initially be set to visible when created, so that those layers
     *            are initially added to the map.
     * @param p
     *            Properties object containing the layers properties.
     * @return Layer[]
     */
    public static Layer[] getLayers(Vector<String> layerList,
                                    Vector<String> visibleLayerList,
                                    Properties p) {

        int nLayerNames = layerList.size();
        Vector<Layer> layers = new Vector<Layer>(nLayerNames);

        for (String layerName : layerList) {
            String classProperty = layerName + ".class";
            String className = p.getProperty(classProperty);
            if (className == null) {
                logger.info("Failed to locate property \"" + classProperty
                        + "\"\n  Skipping layer \"" + layerName + "\"");
                continue;
            }

            Object obj = ComponentFactory.create(className, layerName, p);
            Layer l;

            if (obj instanceof Layer) {
                l = (Layer) obj;
            } else if (obj instanceof PlugIn) {

                PlugInLayer pl = new PlugInLayer();
                pl.setProperties(layerName, p);
                pl.setPlugIn((PlugIn) obj);
                l = pl;
            } else {
                logger.info("Skipped \""
                        + layerName
                        + "\" "
                        + (obj == null ? " - unable to create " : ", type "
                                + obj.getClass().getName()
                                + " is not a layer or plugin"));
                continue;
            }

            // Figure out of the layer is on the startup list,
            // and make it visible if it is...
            l.setVisible(visibleLayerList.contains(layerName));
            // The ComponentFactory does this now
            // l.setProperties(layerName, p);

            layers.addElement(l);

            if (logger.isDebugEnabled()) {
                logger.debug("layer " + l.getName()
                        + (l.isVisible() ? " is visible" : " is not visible"));
            }
        }

        int nLayers = layers.size();
        if (nLayers == 0) {
            return new Layer[0];
        } else {
            Layer[] value = new Layer[nLayers];
            layers.copyInto(value);
            return value;
        }
    }

    /**
     * Add a LayerListener to the LayerHandler, in order to be told about layers
     * that need to be added to the map. The new LayerListener will receive two
     * events, one telling it all the layers available, and one telling it which
     * layers are active (visible).
     * 
     * @param ll
     *            LayerListener, usually the MapBean or other GUI components
     *            interested in providing layer controls.
     */
    public void addLayerListener(LayerListener ll) {
        logger.debug("adding layer listener");
        listeners.add(ll);
        // Usually, the listeners are interested in one type of event
        // or the other. So fire both, and let the listener hash it
        // out.
        ll.setLayers(new LayerEvent(this, LayerEvent.ALL, allLayers));
        ll.setLayers(new LayerEvent(this, LayerEvent.ADD, getMapLayers()));
    }

    /**
     * Add a LayerListener to the LayerHandler, in order to be told about layers
     * that need to be added to the map.
     * 
     * @param ll
     *            LayerListener, usually the MapBean or other GUI components
     *            interested in providing layer controls.
     */
    public void removeLayerListener(LayerListener ll) {
        if (listeners != null) {
            listeners.remove(ll);
        }
    }

    /**
     * Set all the layers held by the LayerHandler. The visible layers will be
     * sent to listeners interested in visible layers (LayerEvent.REPLACE), and
     * the list of all layers will be sent to listeners interested in
     * LayerEvent.ALL events.
     * <p/>
     * <p/>
     * This method will not add the layers to the MapHandler, so you can call
     * this if you know the layers are already in the MapHandler or don't need
     * to be. If you want layers to be added to the MapHandler (if the
     * LayerHandler knows about it), call init(Layer[]) instead.
     * <p/>
     * <p/>
     * Also, this method will disregard layer non-removable status for any
     * layers currently held, and will simply replace all layers with the ones
     * provided. If you want the non-removable flag to be adhered to, call
     * init(Layers[]).
     * 
     * @param layers
     *            Layer array of all the layers to be held by the LayerHandler.
     */
    public synchronized void setLayers(Layer[] layers) {
        allLayers = organizeBackgroundLayers(layers);

        logger.debug("setting layers: " + layers);

        // The setLayers needs to push a new runnable into a fifo
        // stack. A copy of the layers should be made, and then passed
        // to the runnable. When the runnable finishes, it should kick
        // off any other threads waiting to get started.
        Layer[] layersCopy = new Layer[allLayers.length];
        System.arraycopy(allLayers, 0, layersCopy, 0, allLayers.length);

        getListeners().pushLayerEvent(LayerEvent.ALL, layersCopy);
        getListeners().pushLayerEvent(LayerEvent.REPLACE, getMapLayers());
    }

    /**
     * Checks to see if there are background layers on top of foreground layers.
     * 
     * @param layers
     * @return true if background layers need to be pushed down
     */
    protected boolean isForegroundUnderBackgroundLayer(Layer[] layers) {
        boolean ret = false;
        boolean foundBackgroundLayer = false;
        for (int i = 0; i < layers.length; i++) {
            boolean isBackgroundLayer = layers[i].getAddAsBackground();
            if (isBackgroundLayer) {
                foundBackgroundLayer = true;
            } else if (foundBackgroundLayer) {
                ret = true;
                break;
            }
        }

        return ret;
    }

    /**
     * Does the check to see of foreground layers are below background layers,
     * and then iterates through the Layer[] switching layers around until they
     * are in the appropriate order.
     * 
     * @param layers
     * @return Layer[] of layers with background layers moved to back.
     */
    protected Layer[] organizeBackgroundLayers(Layer[] layers) {
        while (isForegroundUnderBackgroundLayer(layers)) {
            for (int i = layers.length - 1; i > 0; i--) {
                Layer layer1 = layers[i - 1];
                Layer layer2 = layers[i];

                if (!layer2.getAddAsBackground() && layer1.getAddAsBackground()) {
                    layers[i - 1] = layer2;
                    layers[i] = layer1;
                }
            }
        }

        return layers;
    }

    /**
     * Returns the object responsible for holding on to objects listening to
     * layer changes.
     * 
     * @return LayerSupport containing pointers to all objects interested in the
     *         status (order, visibility) of the available layers.
     */
    protected LayerSupport getListeners() {
        return listeners;
    }

    /**
     * If you are futzing with the layer visibility outside the purview of the
     * LayerHandler (not using the turnLayerOn() methods) then you can call this
     * to get all the listeners using the current set of visible layers.
     */
    public void setLayers() {
        setLayers(allLayers);
    }

    /**
     * Get a layer array, of potential layers that CAN be added to the map, not
     * the ones that are active on the map. A new array is returned, containing
     * the current layers.
     * 
     * @return new Layer[] containing new layers.
     */
    public synchronized Layer[] getLayers() {
        if (allLayers == null) {
            return new Layer[0];
        } else {
            Layer[] layers = new Layer[allLayers.length];
            System.arraycopy(allLayers, 0, layers, 0, allLayers.length);
            return layers;
        }
    }

    /**
     * Get the layers that are currently part of the Map - the ones that are
     * visible.
     * 
     * @return an Layer[] of visible Layers.
     */
    public Layer[] getMapLayers() {

        int numEnabled = 0;
        int cakeIndex = 0;
        Layer[] cake = null;
        Layer[] layers = getLayers();

        // First loop finds out how many visible layers there are,
        // Second loop creates the layer cake of visible layers.
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < layers.length; i++) {
                if (layers[i] != null && layers[i].isVisible()) {
                    if (j == 0) {
                        numEnabled++;
                    } else if (cake != null) {
                        cake[cakeIndex++] = layers[i];
                    }
                }
            }
            if (j == 0) {
                cake = new Layer[numEnabled];
            }
        }

        logger.debug("providing map layers: " + cake);

        return cake;
    }

    /**
     * Move a layer to a certain position. Returns true if the layer exists in
     * the LayerHandler, false if is doesn't. No action is taken if the layer
     * isn't already added to the LayerHandler stack. If the position is 0 or
     * less the layer is moved on top. If the position is greater or equal to
     * the number of layers, the layer is moved to the bottom of the pile.
     * 
     * @param layer
     *            the layer to move.
     * @param toPosition
     *            the array index to place it, shifting the other layers up or
     *            down, depending on where the layer is originally.
     * @return true if the layer is already contained in the LayerHandler, false
     *         if not.
     */
    public boolean moveLayer(Layer layer, int toPosition) {
        boolean found = false;

        if (allLayers == null) {
            return false;
        }

        int i = 0;
        for (i = 0; i < allLayers.length; i++) {
            if (layer == allLayers[i]) {
                found = true;
                break;
            }
        }

        if (found) {
            // i should be set to the index of the layer.
            int pos = toPosition;

            if (pos < 0) {
                pos = 0;
            } else if (pos >= allLayers.length) {
                pos = allLayers.length - 1;
            }

            if (pos == i) {
                return true;
            }

            Layer movedLayer = allLayers[i];

            int direction;
            if (i > pos) {
                direction = -1;
            } else {
                direction = 1;
            }

            while (i != pos) {
                allLayers[i] = allLayers[i + direction];
                i += direction;
            }
            allLayers[pos] = movedLayer;
            setLayers(allLayers);
        }

        return found;
    }

    /**
     * Add a layer to the bottom of the layer stack. If the layer is already
     * part of the layer stack, nothing is done.
     * 
     * @param layer
     *            the layer to add.
     */
    public void addLayer(Layer layer) {
        if (allLayers == null) {
            addLayer(layer, 0);
            return;
        }

        int i = 0;
        for (i = 0; i < allLayers.length; i++) {
            if (layer == allLayers[i]) {
                return;
            }
        }

        addLayer(layer, allLayers.length + 1);
    }

    /**
     * Add a layer to a certain position in the layer array. If the position is
     * 0 or less, the layer is put up front (on top). If the position is greater
     * than the length of the current array, the layer is put at the end, (on
     * the bottom). The layer is placed on the map if it's visibility is true. A
     * Layer can only be added once. If you add a layer that is already added to
     * the LayerHandler, it will be moved to the requested position.
     * 
     * @param layer
     *            the layer to add.
     * @param position
     *            the array index to place it.
     */
    public void addLayer(Layer layer, int position) {
        if (moveLayer(layer, position)) {
            return;
        }

        if (allLayers == null) {
            allLayers = new Layer[0];
        }

        Layer[] newLayers = new Layer[allLayers.length + 1];

        if (position >= allLayers.length) {
            // Put the new layer on the bottom
            System.arraycopy(allLayers, 0, newLayers, 0, allLayers.length);
            newLayers[allLayers.length] = layer;
        } else if (position <= 0) {
            // Put the new layer on top
            System.arraycopy(allLayers, 0, newLayers, 1, allLayers.length);
            newLayers[0] = layer;
        } else {
            newLayers[position] = layer;
            System.arraycopy(allLayers, 0, newLayers, 0, position);
            System.arraycopy(allLayers, position, newLayers, position + 1,
                             allLayers.length - position);
        }

        if (propertyHandler != null) {
            String pre = layer.getPropertyPrefix();
            if (pre != null && pre.length() > 0) {
                propertyHandler.addUsedPrefix(pre);
            }
        }

        // Need to make this call before thinking about adding the Layer to the
        // BeanContext, so when the Layer shows up in the findAndInit() method,
        // it's already a part of the Layer list. One potential problem that may
        // occur is that the Layer might not be ready to be added to the map and
        // to other application components that get LayerEvents from the
        // LayerHandler, and they will all know about the layer being in the
        // stack after the setLayers() call.
        setLayers(newLayers);

        // Add the layer to the BeanContext, if it wants to be and it's not
        // already in a BeanContext. Thought about making the BC check look for
        // the same BC as the LayerHandler is a part of, but it's probably
        // better just to do a null check in case the Layer is a member of a
        // more restricted BeanContext with limited access.
        BeanContext bc = getBeanContext();
        if (bc != null && layer.getAddToBeanContext()
                && layer.getBeanContext() == null) {
            bc.add(layer);
        }

    }

    /**
     * Remove a layer from the list of potentials.
     * 
     * @param layer
     *            to remove.
     */
    public void removeLayer(Layer layer) {
        if (layer != null && layer.isRemovable()) {
            int index = -1;
            for (int i = 0; i < allLayers.length; i++) {
                if (layer == allLayers[i]) {
                    index = i;
                    break;
                }
            }
            // If the layer is actually there...
            if (index != -1) {
                removeLayer(allLayers, index);
            }
        } else {
            if (layer != null) {
                logger.error("received command to remove " + layer.getName()
                        + ", which has been designated as *NOT* removeable");
                throw new com.bbn.openmap.util.HandleError(
                        "LayerHandler commanded to delete a layer ("
                                + layer.getName() + ") that is not removeable");
            }
        }
    }

    /**
     * Remove a layer from the list of potentials.
     * 
     * @param index
     *            of layer in the layer array. Top-most is first.
     */
    public void removeLayer(int index) {
        if (index >= 0 && index < allLayers.length) {
            removeLayer(allLayers, index);
        }
    }

    public boolean hasLayer(Layer l) {
        Layer[] layers = allLayers;
        for (int i = 0; i < layers.length; i++) {
            if (layers[i] == l) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all the layers (that are marked as removable).
     */
    public void removeAll() {
        if (allLayers == null || allLayers.length == 0) {
            return;
        }

        BeanContext bc = getBeanContext();
        Layer[] oldLayers = allLayers;
        Vector<Layer> nonRemoveableLayers = null;

        for (int i = 0; i < oldLayers.length; i++) {
            Layer layer = oldLayers[i];
            if (layer.isRemovable()) {
                turnLayerOn(false, layer);
                layer.clearListeners();
                if (bc != null) {
                    // Remove the layer from the BeanContext
                    bc.remove(layer);
                } else {
                    oldLayers[i] = null;
                }
            } else {
                if (nonRemoveableLayers == null) {
                    nonRemoveableLayers = new Vector<Layer>(oldLayers.length);
                }
                nonRemoveableLayers.add(layer);
            }
        }

        if (nonRemoveableLayers != null) {
            allLayers = new Layer[nonRemoveableLayers.size()];
            allLayers = (Layer[]) nonRemoveableLayers.toArray(allLayers);
        } else {
            allLayers = new Layer[0];
        }

        setLayers(allLayers);

        // I know this is bad but it seems to work, forcing the
        // memory from old, deleted layers to be freed. With such a
        // drastic method call as removeAll, this should be OK.
        System.gc();
    }

    /**
     * The version that does the work. The other two functions do sanity checks.
     * Calls setLayers(), and removes the layer from the BeanContext.
     * 
     * @param currentLayers
     *            the current layers handled in the LayersMenu.
     * @param index
     *            the validated index of the layer to remove.
     */
    protected void removeLayer(Layer[] currentLayers, int index) {
        Layer rLayer = currentLayers[index];

        if (!rLayer.isRemovable()) {
            logger.error("received command to remove " + rLayer.getName()
                    + ", which has been designated as *NOT* removeable");
            return;
        }

        rLayer.setVisible(false);

        Layer[] newLayers = new Layer[currentLayers.length - 1];
        System.arraycopy(currentLayers, 0, newLayers, 0, index);
        System.arraycopy(currentLayers, index + 1, newLayers, index,
                         currentLayers.length - index - 1);

        // Remove the layer to the BeanContext, if it wants to be.
        BeanContext bc = getBeanContext();
        if (bc != null) {
            bc.remove(rLayer);
        }
        turnLayerOn(false, rLayer);
        rLayer.clearListeners();
        rLayer = null;

        // Shouldn't call this, but it's the only thing that seems to
        // make it work...
        // if (Debug.debugging("helpgc")) {
        // System.gc();
        // }

        setLayers(newLayers);
    }

    /**
     * Take a layer that the LayersMenu knows about, that may or may not be a
     * part of the map, and change its visibility by adding/removing it from the
     * MapBean.
     * 
     * @param setting
     *            true to add layer to the map.
     * @param index
     *            the index of the layer to turn on/off.
     * @return true of index represented a layer, false if not or if something
     *         went wrong.
     */
    public boolean turnLayerOn(boolean setting, int index) {
        try {
            turnLayerOn(setting, allLayers[index]);
            return true;
        } catch (ArrayIndexOutOfBoundsException aoobe) {
            // Do nothing...
        } catch (NullPointerException npe) {
            // Do nothing...
        }
        return false;
    }

    /**
     * Take a layer that the LayersMenu knows about, that may or may not be a
     * part of the map, and change its visibility by adding/removing it from the
     * MapBean. If the layer is not found, it's added and the visibility depends
     * on the setting parameter.
     * 
     * @param setting
     *            true to add layer to the map.
     * @param layer
     *            the layer to turn on.
     * @return true if the layer was found, false if not or if something went
     *         wrong.
     */
    public boolean turnLayerOn(boolean setting, Layer layer) {

        if ((setting && !layer.isVisible()) || (!setting && layer.isVisible())) {
            if (logger.isDebugEnabled()) {
                logger.debug("turning " + layer.getName()
                        + (setting ? " on" : " off"));
            }

            layer.setVisible(setting);

            getListeners().pushLayerEvent(LayerEvent.REPLACE, getMapLayers());
            return true;
        }
        return false;
    }

    /**
     * Called from childrenAdded(), when a new component is added to the
     * BeanContext, and from setBeanContext() when the LayerHandler is initially
     * added to the BeanContext. This method takes the iterator provided when
     * those methods are called, and looks for the objects that the LayerHandler
     * is interested in, namely, the MapBean, the PropertyHandler, or any other
     * LayerListeners. The LayerHandler handles multiple LayerListeners, and if
     * one is found, it is added to the LayerListener list. If a PropertyHandler
     * is found, then init() is called, effectively resetting the layers held by
     * the LayerHandler.
     * 
     * @param someObj
     *            an Object being added to the MapHandler/BeanContext.
     */
    public void findAndInit(Object someObj) {

        if (someObj instanceof LayerListener) {
            logger.debug("LayerHandler found a LayerListener.");
            addLayerListener((LayerListener) someObj);
        }

        if (someObj instanceof Layer) {
            if (logger.isDebugEnabled()) {
                logger.debug("LayerHandler found a Layer |"
                        + ((Layer) someObj).getName() + "|");
            }
            if (!hasLayer((Layer) someObj)) {
                addLayer((Layer) someObj, 0);
            }
        }

        if (someObj instanceof PlugIn) {
            PlugIn pi = (PlugIn) someObj;
            if (pi.getComponent() == null) {
                PlugInLayer pil = new PlugInLayer();
                pil.setPlugIn(pi);
                addLayer(pil, 0);
            }
        }

        if (someObj instanceof PropertyHandler) {
            // Used to notify the PropertyHandler of used property
            // prefix names.
            setPropertyHandler((PropertyHandler) someObj);
        }
    }

    /**
     * A BeanContextMembershipListener interface method, which is called when
     * new objects are removed from the BeanContext. If a LayerListener or Layer
     * is found on this list, it is removed from the list of LayerListeners.
     * 
     * @param someObj
     *            an Object being removed from the MapHandler/BeanContext.
     */
    public void findAndUndo(Object someObj) {

        if (someObj instanceof LayerListener) {
            logger.debug("LayerListener object is being removed");
            removeLayerListener((LayerListener) someObj);
        }

        if (someObj instanceof Layer) {
            removeLayer((Layer) someObj);
        }

        if (someObj instanceof PlugIn) {
            PlugIn pi = (PlugIn) someObj;
            Component comp = pi.getComponent();
            if (comp instanceof Layer && hasLayer((Layer) comp)) {
                removeLayer((Layer) comp);
            }
        }

        if (someObj instanceof PropertyHandler
                && someObj == getPropertyHandler()) {
            setPropertyHandler(null);
        }
    }

    /**
     * Add layers to the BeanContext, if they want to be. Since the BeanContext
     * is a Collection, it doesn't matter if a layer is already there because
     * duplicates aren't allowed.
     * 
     * @param layers
     *            layers to add, if they want to be.
     */
    public void addLayersToBeanContext(Layer[] layers) {
        BeanContext bc = getBeanContext();
        if (bc == null || layers == null) {
            return;
        }

        for (int i = 0; i < layers.length; i++) {
            if (layers[i].getAddToBeanContext()
                    && layers[i].getBeanContext() == null) {
                bc.add(layers[i]);
            }
        }
    }

    /**
     * Add layers to the BeanContext, if they want to be. Since the BeanContext
     * is a Collection, it doesn't matter if a layer is already there because
     * duplicates aren't allowed.
     * 
     * @param layers
     *            layers to add, if they want to be.
     */
    public void removeLayersFromBeanContext(Layer[] layers) {
        BeanContext bc = getBeanContext();
        if (bc == null || layers == null) {
            return;
        }

        for (int i = 0; i < layers.length; i++) {
            bc.remove(layers[i]);
        }
    }

    /**
     * Called when the LayerHandler is added to a BeanContext. This method calls
     * findAndInit() to hook up with any objects that may already be added to
     * the BeanContext. A BeanContextChild method.
     * 
     * @param in_bc
     *            BeanContext.
     */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
        if (in_bc != null) {
            logger.debug("setting bean context");
            in_bc.addBeanContextMembershipListener(this);
            beanContextChildSupport.setBeanContext(in_bc);

            // This will cause findAndInit to be called on the layers and
            // plugins after they are added to the MapHandler, so they can find
            // the components they need before they get added to the
            // map (if they are to be added at startup).
            addLayersToBeanContext(getLayers());

            // Calling this here may (will) cause the MapBean to get
            // loaded with its initial layers, since it is a
            // LayerListener.
            findAndInit(in_bc.iterator());
        }
    }

    public void setSynchronousThreading(boolean s) {
        getListeners().setSynchronous(s);
    }

    public boolean isSynchronousThreading() {
        return getListeners().isSynchronous();
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        props.put(PropUtils.getScopedPropertyPrefix(this)
                + SynchronousThreadingProperty, Boolean.toString(getListeners()
                .isSynchronous()));

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        String internString = i18n.get(LayerHandler.class,
                                       SynchronousThreadingProperty,
                                       I18n.TOOLTIP,
                                       "Launch new threads to do work.");
        props.put(SynchronousThreadingProperty, internString);
        internString = i18n.get(LayerHandler.class,
                                SynchronousThreadingProperty,
                                "Synchronous Threading");
        props.put(SynchronousThreadingProperty + LabelEditorProperty,
                  internString);
        props.put(SynchronousThreadingProperty + EditorProperty,
                  "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        return props;
    }
}