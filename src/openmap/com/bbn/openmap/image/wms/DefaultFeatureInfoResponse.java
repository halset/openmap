package com.bbn.openmap.image.wms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.bbn.openmap.util.http.HttpConnection;

public class DefaultFeatureInfoResponse
      implements FeatureInfoResponse {

   private StringBuffer out;
   private String contentType;

   private final List<StringBuffer> parts = new ArrayList<StringBuffer>();

   public void setOutput(String contentType, StringBuffer out) {
      this.out = out;
      this.contentType = contentType;

      appendHeader();
   }

   public void flush() {
      for (Iterator<StringBuffer> it = parts.iterator(); it.hasNext();) {
         write(it.next());
         if (it.hasNext()) {
            appendSeparator();
         }
      }
      appendFooter();
   }

   public Collection<String> getInfoFormats() {
      return Arrays.asList(HttpConnection.CONTENT_HTML, HttpConnection.CONTENT_PLAIN);
   }

   public void output(LayerFeatureInfoResponse layerFeatureInfoResponse) {
      // store in local buffer and collect all the parts. important for formats
      // with separators between matches like (geo)json.
      StringBuffer part = new StringBuffer();
      layerFeatureInfoResponse.output(getContentType(), part);
      if (part.length() > 0) {
         parts.add(part);
      }
   }

   protected void write(CharSequence s) {
      out.append(s);
   }
   
   public String getContentType() {
      return contentType;
   }

   protected void appendHeader() {
      if (getContentType().equals(HttpConnection.CONTENT_HTML)) {
         write("<html><head>\n");
         write("<meta http-equiv=\"content-type\"\n");
         write("      content=\"text/html; charset=UTF-8\">\n");
         write("</head><body>\n");
      } else if (getContentType().equals(HttpConnection.CONTENT_JSON)) {
         write("{\n");
         write("  \"type\": \"FeatureCollection\",\n");
         write("  \"features\": [\n");
      }
   }

   protected void appendFooter() {
      if (getContentType().equals(HttpConnection.CONTENT_HTML)) {
         write("</body></html>");
      } else if (getContentType().equals(HttpConnection.CONTENT_JSON)) {
         write("  ]\n");
         write("}\n");
      }
   }

   /**
    * Append the separator to be used between output of two {@link LayerFeatureInfoResponse}s
    */
   protected void appendSeparator() {
      if (getContentType().equals(HttpConnection.CONTENT_JSON)) {
         write(",\n");
      }
   }

}
