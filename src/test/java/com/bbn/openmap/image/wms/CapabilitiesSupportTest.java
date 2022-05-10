/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.image.wms;

import java.util.Properties;

import com.bbn.openmap.image.PNG8ImageFormatter;
import com.bbn.openmap.image.WMTConstants;
import com.bbn.openmap.layer.DemoLayer;

import junit.framework.TestCase;

public class CapabilitiesSupportTest extends TestCase {

    public void testCapabilitiesHeader111() throws Exception {
        Properties configProps = configProps();
        Properties requestProps = new Properties();
        requestProps.setProperty(WMTConstants.VERSION, "1.1.1");
        assertCapabilitiesHeader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd\">\n"
                        + "<WMT_MS_Capabilities updateSequence=\"1\" version=\"1.1.1\">",
                configProps, requestProps);
    }
    
    public void testCapabilitiesHeader111SchemaLocation() throws Exception {
        Properties configProps = configProps();
        configProps.setProperty(CapabilitiesSupport.WMSOgcSchemaLocationPrefix,
                "http://my.ogcmirror.com/schema");
        Properties requestProps = new Properties();
        requestProps.setProperty(WMTConstants.VERSION, "1.1.1");
        assertCapabilitiesHeader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"http://my.ogcmirror.com/schema/wms/1.1.1/WMS_MS_Capabilities.dtd\">\n"
                        + "<WMT_MS_Capabilities updateSequence=\"1\" version=\"1.1.1\">",
                configProps, requestProps);
    }

    public void testCapabilitiesHeader130() throws Exception {
        Properties configProps = configProps();
        Properties requestProps = new Properties();
        requestProps.setProperty(WMTConstants.VERSION, "1.3.0");
        assertCapabilitiesHeader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                        + "<WMS_Capabilities updateSequence=\"1\" version=\"1.3.0\" "
                        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wms http://schemas.opengis.net/wms/1.3.0/capabilities_1_3_0.xsd\" "
                        + "xmlns=\"http://www.opengis.net/wms\">", configProps, requestProps);
    }

    public void testCapabilitiesHeader130SchemaLocation() throws Exception {
        Properties configProps = configProps();
        configProps.setProperty(CapabilitiesSupport.WMSOgcSchemaLocationPrefix,
                "http://my.ogcmirror.com/schema");
        Properties requestProps = new Properties();
        requestProps.setProperty(WMTConstants.VERSION, "1.3.0");
        assertCapabilitiesHeader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                        + "<WMS_Capabilities updateSequence=\"1\" version=\"1.3.0\" "
                        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wms http://my.ogcmirror.com/schema/wms/1.3.0/capabilities_1_3_0.xsd\" "
                        + "xmlns=\"http://www.opengis.net/wms\">", configProps, requestProps);
    }

    private Properties configProps() {
        Properties configProps = new Properties();
        configProps.setProperty("png.class", PNG8ImageFormatter.class.getName());
        configProps.setProperty("formatters", "png");
        configProps.setProperty("demo.class", DemoLayer.class.getName());
        configProps.setProperty("openmap.layers", "demo");
        return configProps;
    }

    private void assertCapabilitiesHeader(String header, Properties configProps,
            Properties requestProps) throws Exception {
        String scheme = "http";
        String hostName = "localhost";
        int port = 80;
        String path = "/wms";

        WmsRequestHandler requestHandler = new WmsRequestHandler(scheme, hostName, port, path,
                configProps);
        String response = requestHandler.handleGetCapabilitiesRequest(requestProps);
        
        assertTrue(response.length() >= header.length());
        assertEquals(header, response.substring(0, header.length()));
    }

}
