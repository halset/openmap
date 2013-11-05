package com.bbn.openmap.maptileservlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.http.HttpConnection;

/**
 * MapTileServlet is a servlet class that fields requests for map tiles.
 * 
 * @author dietrick
 */
public class MapTileServlet
      extends HttpServlet {
   public final static String TILE_SET_DESCRIPTION_ATTRIBUTE = "TileSetDefinitions";
   protected Map<String, MapTileSet> mapTileSets;

   /**
    * A do-nothing constructor - init does all the work.
    */
   public MapTileServlet() {
      super();

      mapTileSets = Collections.synchronizedMap(new HashMap<String, MapTileSet>());
   }

   public void init(ServletConfig config)
         throws ServletException {
      super.init(config);
      ServletContext context = config.getServletContext();

      String descriptions = context.getInitParameter(TILE_SET_DESCRIPTION_ATTRIBUTE);
      Logger logger = getLogger();
      logger.info("descriptions: " + descriptions);
      if (descriptions != null) {
         Collection<String> descriptionV = PropUtils.parseMarkers(descriptions, ";");
         logger.info("got " + descriptionV.toString());
         for (String desc : descriptionV) {
            logger.info("looking for " + desc);
            try {
               URL descURL = PropUtils.getResourceOrFileOrURL(desc);
               if (descURL != null) {
                  logger.info("found url for " + descURL);

                  Properties descProps = new Properties();
                  logger.info("going to read props");
                  descProps.load(descURL.openStream());

                  logger.info("loaded " + desc + " " + descProps.toString());

                  MapTileSet mts = create(descProps);

                  if (mts != null && mts.allGood()) {
                     String mtsName = mts.getName();
                     mapTileSets.put(mts.getName(), mts);
                     logger.info("Adding " + mtsName + " dataset");
                  }
               }
            } catch (MalformedURLException murle) {
               logger.warning("MalformedURLException reading " + desc);
            } catch (IOException ioe) {
               logger.warning("IOException reading " + desc);
            }
         }
      }

   }

   public MapTileSet create(Properties props) {
      String className = props.getProperty(MapTileSet.CLASS_ATTRIBUTE);
      if (className == null) {
         MapTileSet mts = new StandardMapTileSet();
         mts.setProperties(props);
         return mts;
      } else {
         getLogger().info("Creating special map tile set: " + className);
         try {
            Object obj = ComponentFactory.create(className, null, props);

            if (obj instanceof MapTileSet) {
               return (MapTileSet) obj;
            } else {
               getLogger().info("Had trouble creating " + (obj == null ? className : obj.getClass().getName())
                                      + ", not a MapTileSet");
            }

         } catch (Exception e) {
            getLogger().severe("Problem creating " + className + ", " + e.getMessage());
         }
      }

      return null;
   }

   /**
    * Handles
    */
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {

      OutputStream out = resp.getOutputStream();

      String pathInfo = req.getPathInfo();
      Logger logger = getLogger();
      if (logger.isLoggable(Level.FINE)) {
         getLogger().fine("received: " + pathInfo);
      }

      MapTileSet mts = getMapTileSetForRequest(pathInfo);

      if (mts != null) {

         try {
            byte[] imageData = mts.getImageData(pathInfo);
            OutputStreamWriter osw = new OutputStreamWriter(out);
            out.write(imageData, 0, imageData.length);
            osw.flush();
         } catch (Exception e) {
            if (logger.isLoggable(Level.FINE)) {
               getLogger().fine("Tile not found: " + pathInfo);
            }
            HttpConnection.writeHttpResponse(out, HttpConnection.CONTENT_PLAIN, "Problem loading " + pathInfo);
         }
      } else {
         HttpConnection.writeHttpResponse(out, HttpConnection.CONTENT_PLAIN, "Map Tile Set not found for " + pathInfo);
      }
   }

   protected MapTileSet getMapTileSetForRequest(String pathInfo) {
      if (pathInfo.startsWith("/")) {
         pathInfo = pathInfo.substring(1);
      }

      String key = pathInfo;

      // That first part of the path is the MapTileSet name.
      int slash = pathInfo.indexOf('/');
      if (slash > 0) {
         key = pathInfo.substring(0, slash);
      }

      return mapTileSets.get(key);
   }

   /**
    * Holder for this class's Logger. This allows for lazy initialization of the
    * logger.
    */
   private static final class LoggerHolder {
      /**
       * The logger for this class
       */
      private static final Logger LOGGER = Logger.getLogger(MapTileServlet.class.getName());

      /**
       * Prevent instantiation
       */
      private LoggerHolder() {
         throw new AssertionError("This should never be instantiated");
      }
   }

   /**
    * Get the logger for this class.
    * 
    * @return logger for this class
    */
   private static Logger getLogger() {
      return LoggerHolder.LOGGER;
   }
}
