package com.bbn.openmap.image;

import java.util.Arrays;
import java.util.Collection;

import com.bbn.openmap.util.http.HttpConnection;

public class PNG32ImageFormatter extends ImageIOFormatter {

    public PNG32ImageFormatter() {
        setFormatName("png");
    }
    
    public ImageFormatter makeClone() {
        return new PNG32ImageFormatter();
    }

    public Collection<String> getContentTypes() {
        return Arrays.asList(HttpConnection.CONTENT_PNG + "; mode=32bit", HttpConnection.CONTENT_PNG + "32");
    }

    public String getFormatLabel() {
        return WMTConstants.IMAGEFORMAT_PNG + "32";
    }
    
	@Override
	protected boolean imageFormatSupportAlphaChannel() {
		return true;
	}

	@Override
	protected boolean imageFormatSupportTransparentPixel() {
		return true;
	}


}
