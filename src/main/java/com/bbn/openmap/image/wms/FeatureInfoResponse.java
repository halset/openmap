package com.bbn.openmap.image.wms;

import java.util.Collection;

public interface FeatureInfoResponse {

    /**
     * Return a {@link Collection} of the supported INFO_FORMAT supported by
     * this {@link FeatureInfoResponse}
     * 
     * @return collection of strings for info formats
     */
    public Collection<String> getInfoFormats();

    /**
     * Initiate a new feature info response output with the given content type
     * and output buffer
     * 
     * @param contentType
     * @param out
     */
    public void setOutput(String contentType, StringBuffer out);

    public void flush();

    public void output(LayerFeatureInfoResponse layerFeatureInfoResponse);
    
   /**
    * The content type to use for the response. Typically the same as in
    * {@link #setOutput(String, StringBuffer)}, but could vary. As an example,
    * it might be wise to return a UTF-8 version of text/{plain,html}
    */
    public String getContentType();

}
