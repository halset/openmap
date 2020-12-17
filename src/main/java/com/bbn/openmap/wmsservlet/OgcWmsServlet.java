// **********************************************************************
// <copyright>
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// </copyright>
// **********************************************************************
// $Source: /cvs/distapps/openmap/src/wmsservlet/WEB-INF/src/com/bbn/openmap/wmsservlet/OgcWmsServlet.java,v $
// $Revision: 1.5 $ $Date: 2008/09/19 14:20:14 $ $Author: dietrick $
// **********************************************************************
package com.bbn.openmap.wmsservlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Properties;

import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.image.WMTConstants;
import com.bbn.openmap.image.wms.WMSException;
import com.bbn.openmap.image.wms.WmsRequestHandler;
import com.bbn.openmap.util.Debug;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 */
public class OgcWmsServlet extends HttpServlet {

    /**
     * A do-nothing constructor - init does all the work.
     */
    public OgcWmsServlet() {
        super();
    }

    /**
     * 
     */
    protected Properties parsePropertiesFromRequest(HttpServletRequest request) {
        Properties props = new Properties();
        java.util.Enumeration keys = request.getParameterNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = request.getParameter(key);
            if (value != null) {
                // A wms client can send lowercase request parameters.
                key = key.toUpperCase();
                props.put(key, value);
            }
        }
        return props;
    }
    
    /**
	 * Get a {@link Properties} object with the content of openmap.properties.
	 * No request specific properties are included.
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected Properties getProperties() throws MalformedURLException,
			IOException {

		// use context parameter "mapDefinition" for path to openmap.properties.
		// default to "openmap.properties".
		String mapDefinition = getServletContext().getInitParameter(
				"mapDefinition");
		if (mapDefinition == null) {
			mapDefinition = "openmap.properties";
		}
		Debug.message("wms", "Using map definition:" + mapDefinition);

		PropertyHandler propHandler = new PropertyHandler(mapDefinition);
		Properties props = propHandler.getProperties();

		return props;
	}

    protected WmsRequestHandler createRequestHandler(HttpServletRequest request) throws ServletException,
            IOException {
        Debug.message("wms", "OgcWmsServlet.createRequestHandler : ");

        
        String schema = request.getScheme();
        if (schema == null) {
            throw new ServletException("schema is not specified");
        }

        String hostName = request.getServerName();
        if (hostName == null) {
            throw new ServletException("server name not specified");
        }

        int serverPort = request.getServerPort();

        String contextPath = request.getContextPath();
        if (contextPath == null) {
            throw new ServletException("context path is not specified");
        }

        String servletPath = request.getServletPath();
        if (servletPath == null) {
            throw new ServletException("servlet path is not specified");
        }
        
        // can be used to encode extra things in the path info. only usable by
        // subclassing OgcWmsServlet
        String servletPathInfo = request.getPathInfo();
        if (servletPathInfo == null) {
            servletPathInfo = "";
        }
        
        // locale from request Accept-Language HTTP header and allow override
        // with LANGUAGE parameter.
        Locale locale = request.getLocale();
        String languageParameter = request.getParameter(WMTConstants.LANGUAGE);
        if (languageParameter != null) {
            Locale localeFromLanguageParameter = Locale.forLanguageTag(languageParameter);
            if (localeFromLanguageParameter != null) {
                locale = localeFromLanguageParameter;
            }
        }

        try {
            WmsRequestHandler wmsRequestHandler = new WmsRequestHandler(schema, hostName,
                    serverPort, contextPath + servletPath + servletPathInfo, getProperties(),
                    locale);
            return wmsRequestHandler;
        } catch (java.net.MalformedURLException me) {
            Debug.message("wms", "MS: caught MalformedURLException - \n" + me.getMessage());
            throw me;
        } catch (java.io.IOException ioe) {
            Debug.message("wms", "MS: caught IOException - \n" + ioe.getMessage());
            throw ioe;
        } catch (WMSException wmse) {
            Debug.message("wms", "MS: caught WMSException - \n" + wmse.getMessage());
            throw new ServletException(wmse);
        }

    }

    /**
     * 
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Debug.message("wms", "OgcWmsServlet.doGet");
        WmsRequestHandler wmsRequestHandler = createRequestHandler(request);

        Properties properties = parsePropertiesFromRequest(request);
        HttpResponse httpResponse = new HttpResponse(response);
        wmsRequestHandler.handleRequest(properties, httpResponse);
    }

}
