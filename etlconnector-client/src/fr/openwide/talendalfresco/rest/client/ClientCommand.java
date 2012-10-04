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

import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;


/**
 * Client-side interface for REST commands.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public interface ClientCommand {
   
   String getName();

   /**
    * Should only be called by client.
    * Should create and set all params of the method.
    * A default impl could be overriden to tune the default behaviour,
    * check or build parameters...
    */
   HttpMethodBase createMethod() throws RestClientException;
   List<NameValuePair> getParams();
   
   /**
    * Should only be called by client.
    * Allows to parse the XML message returned in the command response.
    */
   void handleResponse(XMLEventReader xmlReader) throws XMLStreamException;

   String getResultCode();
   String getResultError();
   String getResultMessage();
}
