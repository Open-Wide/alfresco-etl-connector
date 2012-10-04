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

package fr.openwide.talendalfresco.rest;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * Helper for reading and writing XML using stax / woodstox.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class XmlHelper {
   
   private static XMLOutputFactory xmlOutputFactory;
   
   /**
    * woodstox XMLOutputFactory may be reused for better perfs,
    * see http://www.cowtowncoder.com/blog/archives/2006/06/entry_2.html
    * @return
    */
   public static XMLOutputFactory getXMLOutputFactory() {
      synchronized(XmlHelper.class) {
         if (xmlOutputFactory == null) {
            xmlOutputFactory = XMLOutputFactory.newInstance();
            // other configuration should be done threadsafely here
         }
      }
      return xmlOutputFactory;
   }
   
   /**
    * Creates a new XMLStreamWriter with the given encoding.
    * Encoding has to be the same on the server and on the client.
    * NB. encoding ISO-8859-1 is preferred because tomcat uses it
    * by default, and changing it there would impact the whole webapp
    * @param os
    * @param restEncoding
    * @return
    * @throws XMLStreamException
    */
   public static XMLStreamWriter createXMLStreamWriter(OutputStream os, String restEncoding) throws XMLStreamException {
      return XmlHelper.getXMLOutputFactory().createXMLStreamWriter(os, restEncoding);
   }
   
   
   private static XMLInputFactory xmlInputFactory;
   
   /**
    * woodstox XMLInputFactory may be reused for better perfs
    * @return
    */
   public static XMLInputFactory getXMLInputFactory() {
      synchronized(XmlHelper.class) {
         if (xmlInputFactory == null) {
            xmlInputFactory = XMLInputFactory.newInstance();
            // other configuration should be done threadsafely here
         }
      }
      return xmlInputFactory;
   }
   
   /**
    * Creates a new XMLEventReader with the given encoding.
    * Encoding has to be the same on the server and on the client.
    * NB. encoding ISO-8859-1 is preferred because tomcat uses it
    * by default, and changing it there would impact the whole webapp
    * @param is
    * @param restEncoding
    * @return
    * @throws XMLStreamException
    */
   public static XMLEventReader createXMLEventReader(InputStream is, String restEncoding) throws XMLStreamException {
      return XmlHelper.getXMLInputFactory().createXMLEventReader(is, restEncoding);
   }
   

   public static void writeTag(XMLStreamWriter xmlWriter, String tag,
         Object value) throws XMLStreamException {
      if (value == null) {
         xmlWriter.writeEmptyElement(tag);
      } else {
         xmlWriter.writeStartElement(tag);
         xmlWriter.writeCData(value.toString());
         xmlWriter.writeEndElement();  
      }        
   }


   public static String toString(Throwable error) {
      StringBuffer buf = new StringBuffer(512);
      while (error != null) {
         buf.append(error.getClass().getName());
         buf.append(error.getMessage() == null ? "" : error.getMessage());
         buf.append("\r\n");
         StackTraceElement[] stack = error.getStackTrace();
         for(int i = 0 ; i < stack.length ; i++){
            buf.append("   ");
            buf.append(stack[i].toString());
            buf.append("\r\n");
         }
         if (error.getCause() != null) {
            buf.append("\r\nCaused by :");
         }
         error = error.getCause();
      }
      return buf.toString();
   }
   
}
