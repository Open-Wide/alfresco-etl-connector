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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import fr.openwide.talendalfresco.rest.RestConstants;


/**
 * Base for client-side REST commands.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public abstract class ClientCommandBase implements ClientCommand {

   enum HttpMType {
      GET,
      POST
   }

   protected HashSet<String> defaultResultElementSet = new HashSet<String>(Arrays.asList(
         new String[] { RestConstants.TAG_COMMAND, RestConstants.TAG_CODE,
               RestConstants.TAG_ERROR, RestConstants.TAG_MESSAGE}));
   protected HashMap<String,String> elementValueMap = new HashMap<String,String>(6);
   
   protected String name;
   private HttpMType methodType;
   protected ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(3);
   protected HashMap<String,NameValuePair> paramMap = new HashMap<String,NameValuePair>(3);
   protected RequestEntity requestEntity = null;
   
   protected String resultCode = null;
   protected String resultError = null;
   protected String resultMessage = null;

   // state vars used in result parsing :
   protected boolean isInCommandResponseContent = false;
   protected int commandResponseContentDepth = 0;
   protected StringBuffer singleLevelTextBuf = null;
   

   public ClientCommandBase(String name, HttpMType httpMethod) {
      this.name = name;
      this.methodType = httpMethod;
      //this.params.add("paramName", "paramValue");
      //this.defaultResultElementSet.add("myelt");
   }

   public String getName() {
      return this.name;
   }
   
   public HttpMType getMethodType() {
      return this.methodType;
   }
   
   public List<NameValuePair> getParams() {
      return params;
   }
   
   protected NameValuePair getParam(String paramName) {
      return paramMap.get(paramName);
   }
   
   protected void setParam(NameValuePair paramValue) {
      if (paramValue == null) {
         return;
      }
      NameValuePair oldValue = paramMap.put(paramValue.getName(), paramValue);
      int oldValueIndex = params.indexOf(oldValue);
      oldValueIndex = (oldValueIndex == -1) ? params.size() : oldValueIndex;
      params.add(oldValueIndex, paramValue);
   }
   
   
   /**
    * Can be overriden to tune the default behaviour,
    * check or build parameters...
    * The client should set the params (query string) itself.
    */
   public HttpMethodBase createMethod() throws RestClientException {
      // instantiating a new method and configuring it
      HttpMethodBase method;
      switch(this.getMethodType()) {
      case POST:
      //case PUT:
         method = new PostMethod();
         break;
      case GET:
      default :
         method = new GetMethod();
         method.setFollowRedirects(true);
      }
      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
           new DefaultHttpMethodRetryHandler(3, false));
      //method.setQueryString(params.toArray(new NameValuePair[0])); // done in client
      //method.getParams().setContentCharset(this.restEncoding); // done in client

      // set request entity (allow to fill method body for posts) :
      if (requestEntity != null && method instanceof EntityEnclosingMethod) {
         ((EntityEnclosingMethod) method).setRequestEntity(requestEntity);
      }
      
      return method;
   }

   public String getResultCode() {
      return resultCode;
   }

   public String getResultError() {
      return resultError;
   }

   public String getResultMessage() {
      return resultMessage;
   }
   
   /**
    * Override if needed with any command specific behaviour.
    * Default is same as top level.
    * @param event
    */
   protected void handleResponseContentEvent(XMLEvent event) {
      handleResponseTopLevelEvent(event);
   }
   
   public final void handleResponse(XMLEventReader xmlReader) throws XMLStreamException {
      // Read the response body and parse it
      while(xmlReader.hasNext()) {
         XMLEvent event = xmlReader.nextEvent();
           
         if (isInCommandResponseContent) {
              
            // within command content tag case
            switch (event.getEventType()) {
            case XMLEvent.END_ELEMENT :
               commandResponseContentDepth--;
               if (commandResponseContentDepth == -1) {
                  // means the content tag has been closed
                  isInCommandResponseContent = false;
               }
               break;
            case XMLEvent.START_ELEMENT :
               commandResponseContentDepth++;
            }
            // delegate impl of command content tag case
            handleResponseContentEvent(event);
            
         } else {

            // outside command content tag case
            handleResponseTopLevelEvent(event);
         }
      }

      resultCode  = elementValueMap.get(RestConstants.TAG_CODE);
      resultError  = elementValueMap.get(RestConstants.TAG_ERROR);
      resultMessage  = elementValueMap.get(RestConstants.TAG_MESSAGE);
   }

   protected void handleResponseTopLevelEvent(XMLEvent event) {
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
         if (defaultResultElementSet.contains(elementName)) {
            // reinit buffer at start of meaningful elements
            singleLevelTextBuf = new StringBuffer();
         } else if (RestConstants.TAG_CONTENT.equals(elementName)) {
            // switch to content mode
            isInCommandResponseContent = true;
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
         if (defaultResultElementSet.contains(elementName)) {
            String value = singleLevelTextBuf.toString();
            elementValueMap.put(elementName, value);
            // TODO test if it is code and it is not OK, break to error handling
         }
         // singleLevelTextBuf = new StringBuffer(); // no ! in start
         break;
      }
   }

   @SuppressWarnings("unchecked")
	public String toString() {
      StringBuffer sbuf = new StringBuffer();
      sbuf.append("Command ");
      sbuf.append(name);
      sbuf.append(" with parameters ");
      sbuf.append(String.valueOf(Arrays.asList(params)));
      sbuf.append(" and resultCode ");
      sbuf.append(resultCode);
      return sbuf.toString();
   }

}
