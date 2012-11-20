package com.bbn.openmap.util.http;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.bbn.openmap.util.http.IHttpResponse;

/**
 * A version of {@link IHttpResponse} that does not do any sending before the
 * {@link #flush()} method are called.
 * <p>
 * This is to be able to see how much time are spent on creating the image not
 * including the time used to send it to the client. And also to be able to
 * generate content and send it to the client in different threads. It will also
 * set the content length on the response.
 * <p>
 * NB: The flush method will return before all of the data are sent to the
 * client.
 */
public class BufferingHttpResponse
      implements IHttpResponse {

   private HttpServletResponse httpResponse;

   private String contentType;

   private byte[] response;

   public BufferingHttpResponse(HttpServletResponse httpResponse) {
      this.httpResponse = httpResponse;
   }

   public void writeHttpResponse(String contentType, String response)
         throws IOException {
      writeHttpResponse(contentType, response.getBytes("UTF-8"));
   }

   public void writeHttpResponse(String contentType, byte[] response)
         throws IOException {
      this.contentType = contentType;
      this.response = response;
   }

   /**
    * Write the content out to the {@link HttpServletResponse}
    * 
    * @throws IOException
    */
   public void flush()
         throws IOException {
      if ((contentType == null) || (response == null)) {
         // nothing to flush out
         return;
      }
      httpResponse.setContentType(getContentType());
      httpResponse.setContentLength(getContentLength());
      OutputStream out = httpResponse.getOutputStream();
      out.write(response, 0, response.length);
      out.flush();

      contentType = null;
      response = null;
   }

   public String getContentType() {
      return contentType;
   }

   public int getContentLength() {
      return (response == null) ? 0 : response.length;
   }

}
