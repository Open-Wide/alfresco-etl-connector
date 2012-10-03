/*
 * Copyright (C) 2008 Open Wide SA
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
 * More information at http://forge.alfresco.com/projects/etlconnector/
 */

package fr.openwide.talendalfresco.rest.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.XmlHelper;


/**
 * Alfresco REST client that builds on Apache httpclient and Commands.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class AlfrescoRestClient {

   public static final String DEFAULT_SERVER_URL = "http://localhost:8080/alfresco";

   public static final String TICKET_PARAM = "ticket";

   protected static final NameValuePair[] EMPTY_NAME_VALUE_PAIR = new NameValuePair[0];
   
   protected int timeout = 5000;
   protected String serverUrl;
   /**
    * must be the same as the server's. NB. tomcat default requires ISO-8859-1 encoding.
    * Used in httpclient method for data sent and Stax reader for data received.
    */
   protected String restEncoding = "ISO-8859-1";
   
   protected String commandServletUrl;
   protected String restProcessorUrl;
   protected String restCommandUrlPrefix;

   private String username = null;
   private HttpClient client;

   private String ticket;


   /**
    * To ease tests
    */
   public AlfrescoRestClient() {
      this(DEFAULT_SERVER_URL);
   }
   
   public AlfrescoRestClient(String serverUrl, String restEncoding) {
      this(serverUrl);
      this.restEncoding = restEncoding;
   }
   
   public AlfrescoRestClient(String serverUrl) {
      this.serverUrl = serverUrl;
      this.commandServletUrl = serverUrl + "/command";
      this.restProcessorUrl = commandServletUrl + "/rest";
      this.restCommandUrlPrefix = restProcessorUrl + "/";

      // create client and configure it
      client = new HttpClient();
      client.getHttpConnectionManager().
          getParams().setConnectionTimeout(timeout);
   }

   public void setTimeout(int timeout) {
      this.timeout = timeout;
   }

   public int getTimeout() {
      return timeout;
   }

   public String getServerUrl() {
      return serverUrl;
   }
   
   public String getUsername() {
      return username;
   }

   public String getTicket() {
      return ticket;
   }

   
   public void execute(ClientCommand clientCommand) throws RestClientException {

      int statusCode = -1;
      HttpMethodBase method = null;
      try {
         // building method (and body entity if any)
         method = clientCommand.createMethod();
         
         // setting server URL
         method.setURI(new URI(restCommandUrlPrefix + clientCommand.getName(), false));
         method.getParams().setContentCharset(this.restEncoding);
         
         // building params (adding ticket)
         List<NameValuePair> params = clientCommand.getParams();
         params.add(new NameValuePair(TICKET_PARAM, ticket));
         method.setQueryString(params.toArray(EMPTY_NAME_VALUE_PAIR));
         
         // Execute the method.
         statusCode = client.executeMethod(method);
         
         // checking HTTP status
         if (statusCode != HttpStatus.SC_OK) {
            throw new RestClientException("Bad HTTP Status : " + statusCode);
         }

         // parsing response
         XMLEventReader xmlReader = null;
         try {
            xmlReader = XmlHelper.createXMLEventReader(
                  method.getResponseBodyAsStream(),
                  this.restEncoding);
           
            clientCommand.handleResponse(xmlReader);

            if (!RestConstants.CODE_OK.equals(clientCommand.getResultCode())) {
               String msg = "Business error in command " + clientCommand.toString();
               //logger.error(msg, e);
               throw new RestClientException(clientCommand.getResultMessage(),
                     new RestClientException(clientCommand.getResultError()));
            }
         } catch (XMLStreamException e) {
            String msg = "XML parsing error on response body : ";
            try {
               msg += new String(method.getResponseBody());
            } catch (IOException ioex) {
               msg += "[unreadable]";
            };
            //logger.error(msg, e);
            throw new RestClientException(msg, e);
         } catch (IOException e) {
            String msg = "IO Error when parsing XML response body : ";
            //logger.error(msg, e);
            throw new RestClientException(msg, e);
           
         } finally {
            if (xmlReader != null) {
               try { xmlReader.close(); } catch (Throwable t) {}
            }
         }

      } catch (RestClientException rcex) {
         throw rcex;
      } catch (URIException e) {
         throw new RestClientException("URI error while executing command " + clientCommand, e);
      } catch (HttpException e) {
         throw new RestClientException("HTTP error while executing command " + clientCommand, e);
      } catch (IOException e) {
         throw new RestClientException("IO error while executing command " + clientCommand, e);
      } finally {
         if (method != null) {
            method.releaseConnection();
         }
      }
   }
   
   
   public void login(String username, String password) throws RestClientException {
      ClientLoginCommand cmd = new ClientLoginCommand(username, password);
      
      try {
         this.execute(cmd);
         this.username = username;
         this.ticket = cmd.getTicket();
      } catch (RestClientException rcex) {
         throw rcex;
      }
    }
   
   
   public void logout() throws RestClientException {
      if (this.username == null) {
         // not logged in
         this.ticket = null;
         return;
      }
      
      try {
         this.execute(new ClientLogoutCommand());
         this.username = null;
         this.ticket = null;
      } catch (RestClientException rcex) {
         throw rcex;
      }
    }
   
}
