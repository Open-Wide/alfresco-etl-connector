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

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


/**
 * Requires the net. Checks HttpClient can get google.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestHttpTest extends TestCase {

   public RestHttpTest() {
      super(RestHttpTest.class.getName());
   }

   @Override
   protected void setUp() throws Exception {
      
   }

   @Override
   protected void tearDown() throws Exception {
      
   }
   
   
   public void testRestAuthentication() {
      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Create a method instance.
      GetMethod method = new GetMethod("http://www.google.com");
      
      // Provide custom retry handler is necessary
      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
           new DefaultHttpMethodRetryHandler(3, false));

      try {
        // Execute the method.
        int statusCode = client.executeMethod(method);

        assertEquals(statusCode, HttpStatus.SC_OK);

        // Read the response body.
        byte[] responseBody = method.getResponseBody();

        assertTrue(responseBody != null && responseBody.length != 0);

        // Deal with the response.
        // Use caution: ensure correct character encoding and is not binary data
        System.out.println(new String(responseBody));

      } catch (HttpException e) {
        fail("Fatal protocol violation: " + e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        fail("Fatal transport error: " + e.getMessage());
        e.printStackTrace();
      } finally {
        // Release the connection.
        method.releaseConnection();
      }  
    }
   
}
