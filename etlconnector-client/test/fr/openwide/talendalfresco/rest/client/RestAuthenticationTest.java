/*
 * Copyright (C) 2008-2012 Open Wide SA
 *  
 * This library is free software; you can redistribute 
 * it and/or modify it under the terms of version 2.1 of 
 * the GNU Lesser General Public License as published by  
 * the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General 
 * Public License along with this library; if not, write to the 
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 * Boston, MA  02111-1307  USA
 * 
 * More information at http://knowledge.openwide.fr/bin/view/Main/AlfrescoETLConnector/
 */

package fr.openwide.talendalfresco.rest.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import junit.framework.TestCase;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.XmlHelper;


/**
 * Requires a running alfresco with talendalfresco ext.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestAuthenticationTest extends TestCase {

   private int timeout;

   private String serverUrl;
   private String commandServletUrl;
   private String restProcessorUrl;
   private String restCommandUrlPrefix;
   
   private String ticket = null;


   public RestAuthenticationTest() {
      super(RestAuthenticationTest.class.getName());
   }

   @Override
   protected void setUp() throws Exception {
      timeout = 5000;
      
      serverUrl = "http://localhost:8080/alfresco"; // TODO or props
      commandServletUrl = serverUrl + "/command"; // TODO or props
      restProcessorUrl = commandServletUrl + "/rest"; // TODO or props
      restCommandUrlPrefix = restProcessorUrl + "/"; // TODO or props
   }

   @Override
   protected void tearDown() throws Exception {
      
   }
   
   
   public void testRestLogin() {
      // create client and configure it
      HttpClient client = new HttpClient();
      client.getHttpConnectionManager().
          getParams().setConnectionTimeout(timeout);
      
      // instantiating a new method and configuring it
      GetMethod method = new GetMethod(restCommandUrlPrefix + "login");
      method.setFollowRedirects(true); // ?
      // Provide custom retry handler is necessary (?)
      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
           new DefaultHttpMethodRetryHandler(3, false));
      NameValuePair[] params = new NameValuePair[] {
            new NameValuePair("username", "admin"),
            new NameValuePair("password", "admin") };
      method.setQueryString(params);

      try {
        // Execute the method.
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
          System.err.println("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        byte[] responseBody = method.getResponseBody(); 
        System.out.println(new String(responseBody)); // TODO rm

        HashSet<String> defaultElementSet = new HashSet<String>(Arrays.asList(
              new String[] { RestConstants.TAG_COMMAND, RestConstants.TAG_CODE,
                    RestConstants.TAG_CONTENT, RestConstants.TAG_ERROR,
                    RestConstants.TAG_MESSAGE}));
        HashMap<String,String> elementValueMap = new HashMap<String,String>(6);
        
        try {
         XMLEventReader xmlReader = XmlHelper.getXMLInputFactory()
              .createXMLEventReader(new ByteArrayInputStream(responseBody));
           StringBuffer singleLevelTextBuf = null;
           while(xmlReader.hasNext()) {
              XMLEvent event = xmlReader.nextEvent();
              switch (event.getEventType()) {
              case XMLEvent.CHARACTERS :
              case XMLEvent.CDATA :
                 if (singleLevelTextBuf != null) {
                    singleLevelTextBuf.append(event.asCharacters().getData());
                 } // else element not meaningful
                 break;
              case XMLEvent.START_ELEMENT :
                 StartElement startElement = event.asStartElement();
                 String elementName = startElement.getName().getLocalPart();
                 if (defaultElementSet.contains(elementName)
                       // TODO another command specific level
                       || "ticket".equals(elementName)) {
                    // reinit buffer at start of meaningful elements
                    singleLevelTextBuf = new StringBuffer();
                 } else {
                    singleLevelTextBuf = null; // not useful
                 }
                 break;
              case XMLEvent.END_ELEMENT :
                 if (singleLevelTextBuf == null) {
                    break; // element not meaningful
                 }

                 // TODO or merely put it in the map since the element has been tested at start
                 EndElement endElement = event.asEndElement();
                 elementName = endElement.getName().getLocalPart();
                 if (defaultElementSet.contains(elementName)) {
                    String value = singleLevelTextBuf.toString();
                    elementValueMap.put(elementName, value);
                    // TODO test if it is code and it is not OK, break to error handling
                 }
                 // TODO another command specific level
                 else if ("ticket".equals(elementName)) {
                    ticket = singleLevelTextBuf.toString();
                 }
                 // singleLevelTextBuf = new StringBuffer(); // no ! in start
                 break;
              }
           }
      } catch (XMLStreamException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (Throwable t) {
         // TODO Auto-generated catch block
         t.printStackTrace();
         //throw t;
      }
      
      String code = elementValueMap.get(RestConstants.TAG_CODE);
      assertTrue(RestConstants.CODE_OK.equals(code));
      System.out.println("got ticket " + ticket);

      } catch (HttpException e) {
         // TODO
        e.printStackTrace();
      } catch (IOException e) {
         // TODO
        e.printStackTrace();
      } finally {
        // Release the connection.
        method.releaseConnection();
      }  
    }
   
   
   public void testRestLogout() {
      // first login
      testRestLogin();
      
      // create client and configure it
      HttpClient client = new HttpClient();
      client.getHttpConnectionManager().
          getParams().setConnectionTimeout(timeout);
      
      // instantiating a new method and configuring it
      GetMethod method = new GetMethod(restCommandUrlPrefix + "logout");
      method.setFollowRedirects(true); // ?
      // Provide custom retry handler is necessary (?)
      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
           new DefaultHttpMethodRetryHandler(3, false));
      NameValuePair[] params = new NameValuePair[] {
            new NameValuePair("ticket", ticket) }; // TODO always provide ticket
      method.setQueryString(params);

      try {
        // Execute the method.
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
          System.err.println("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        byte[] responseBody = method.getResponseBody();

        // Deal with the response.
        // Use caution: ensure correct character encoding and is not binary data
        System.out.println(new String(responseBody));

      } catch (HttpException e) {
         // TODO
        e.printStackTrace();
      } catch (IOException e) {
         // TODO
        e.printStackTrace();
      } finally {
        // Release the connection.
        method.releaseConnection();
      }  
    }
   
}
