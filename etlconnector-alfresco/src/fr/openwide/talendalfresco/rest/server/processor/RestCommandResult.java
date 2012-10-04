/*
 * Copyright (C) 2008-2012 Open Wide SA
 *
 * This program is FREE software. You can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your opinion) any later version.
 *
 * This program is distributed in the HOPE that it will be USEFUL,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, write to the Free Software Foundation,
 * Inc. ,59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * More information at http://knowledge.openwide.fr/bin/view/Main/AlfrescoETLConnector/
 *
 */
package fr.openwide.talendalfresco.rest.server.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.XmlHelper;
import fr.openwide.talendalfresco.rest.server.RestRuntimeException;


/**
 * Writes the following tags :
 * command, code (CODE_OK = 00 unless error), content (if error empty), error (only if error), message (if any).
 * 
 * Allows to handle commandResult output encoding (default is ISO-8859-1).
 * NB. encoding ISO-8859-1 is required because tomcat uses it
 * by default (so all responses are encoded such), and changing it there would impact the whole webapp
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestCommandResult implements RestConstants {

   private static final String DEFAULT_SUCCESS_MESSAGE = "Success";
   private static final String DEFAULT_ERROR_MESSAGE = "Unspecified error";
   
   private String commandName;
   
   private String code = CODE_OK;
   private String message = DEFAULT_SUCCESS_MESSAGE;
   private Throwable error = null;

   private ByteArrayOutputStream xmlBos;
   private XMLStreamWriter xmlWriter;
   private String stringResult = null;
   private String restEncoding = "ISO-8859-1";
   

   /**
    * Uses default encoding ISO-8859-1
    * @param commandName
    */
   public RestCommandResult(String commandName) {
      this(commandName, null);
   }
   
   /**
    * 
    * @param commandName
    * @param restEncoding default is ISO-8859-1
    * NB. encoding ISO-8859-1 is required because tomcat uses it
    * by default, and changing it there would impact the whole webapp
    */
   public RestCommandResult(String commandName, String restEncoding) {
      this.commandName = commandName;
      this.restEncoding = (restEncoding != null) ? restEncoding : this.restEncoding;
      // NB. encoding ISO-8859-1 is required because tomcat uses it
      // by default, and changing it there would impact the whole webapp
      
      try {
         // init
         xmlBos = new ByteArrayOutputStream();
         xmlWriter = XmlHelper.createXMLStreamWriter(xmlBos, restEncoding);
         
         // write result start
         xmlWriter.writeStartElement(TAG_ROOT);
         writeTag(TAG_COMMAND, commandName);
         writeTag(TAG_CODE, CODE_OK); // OK except if reset later
         xmlWriter.writeStartElement(TAG_CONTENT);
      } catch (XMLStreamException e) {
         throw new RestRuntimeException("Error creating XML result", e);
      }
   }

   public String getCommandName() {
      return commandName;
   }
   
   public boolean isSuccess() {
      return code == CODE_OK;
   }

   public XMLStreamWriter getXmlWriter() {
      return xmlWriter;
   }
   

   public void setError(String code, String message, Throwable error) {
      this.code = (code != null) ? code : CODE_ERROR_UNSPECIFIED;
      this.message = (message != null) ? message : DEFAULT_ERROR_MESSAGE;
      this.error = error;
   }

   public void setError(String message, Throwable error) {
      this.setError(CODE_ERROR_UNSPECIFIED, DEFAULT_ERROR_MESSAGE, error);
   }

   public void setError(String message) {
      this.setError(message, null);
   }

   public void setMessage(String message) {
      this.message = message;
   }

   
   public String toString() {
      if (stringResult  != null) {
         return stringResult;
      }
      
      // write result end
      try {

         if (error == null) {
            // ending CONTENT tag
            xmlWriter.writeEndElement(); // end TAG_CONTENT
            
         } else {
            // reiniting writer for error message
            xmlWriter.close();
            xmlBos = new ByteArrayOutputStream();
            xmlWriter = XmlHelper.createXMLStreamWriter(xmlBos, restEncoding);
            // NB. encoding ISO-8859-1 is required because tomcat uses it
            // by default, and changing it there would impact the whole webapp

            // write result start
            xmlWriter.writeStartElement(TAG_ROOT);
            writeTag(TAG_COMMAND, commandName);
            writeTag(TAG_CODE, code); // written before content so the client can detect errors
            xmlWriter.writeEmptyElement(TAG_CONTENT);
            
            // write error
            writeTag(TAG_ERROR, XmlHelper.toString(error));
         }
         
         if (message != null) {
            writeTag(TAG_MESSAGE, message);
         }
         
         xmlWriter.writeEndElement(); // end TAG_ROOT

         xmlWriter.flush();
         xmlWriter.close();

         stringResult = xmlBos.toString();
         return stringResult;
      }
      catch (XMLStreamException e) {
         throw new RestRuntimeException("Error creating XML result", e);
      }

   }
   
   public void writeTag(String tag, Object value) throws XMLStreamException {
      XmlHelper.writeTag(xmlWriter, tag, value);
   }

   public void writeStringContent(String stringCommandResult) {
      try {
         if(stringCommandResult == null) {
            xmlWriter.writeEmptyElement(TAG_CONTENT);
            return;
         }
         
         xmlWriter.flush();
         xmlBos.write(new String("<" + TAG_CONTENT + ">").getBytes());
         xmlBos.write(stringCommandResult.toString().getBytes());
         xmlBos.write(new String("</" + TAG_CONTENT + ">").getBytes());          
         // xmlNodeBos.flush(); // not required
      } catch (XMLStreamException e) {
         throw new RestRuntimeException("Error writing XML result", e);
      } catch (IOException e) {
         // can't happen
      }
   }
   
}
