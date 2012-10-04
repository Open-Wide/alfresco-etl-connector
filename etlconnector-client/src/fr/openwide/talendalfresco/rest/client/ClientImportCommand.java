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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;

import fr.openwide.talendalfresco.rest.RestConstants;


/**
 * Import command.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class ClientImportCommand extends ClientCommandBase {
   
   private ArrayList<String[]> resultLogs = new ArrayList<String[]>();
   private ArrayList<String[]> errorLogs = new ArrayList<String[]>();
   private ArrayList<String[]> successLogs = new ArrayList<String[]>();

   public ClientImportCommand(String path, InputStream acpXmlIs) {
      super(RestConstants.CMD_IMPORT, HttpMType.POST);
      this.setParam(new NameValuePair(RestConstants.PROP_IMPORT_PATH, path));
      this.setParam(new NameValuePair(RestConstants.PROP_IMPORT_CLIENT_PATH_DELIMITER, File.separator));

      this.requestEntity = new InputStreamRequestEntity(acpXmlIs);
      // NB. restEncoding is set in alfrescoRestClient
      //this.requestEntity = new InputStreamRequestEntity(acpXmlIs, "text/xml; charset=ISO-8859-1");
   }

   
   protected void handleResponseContentEvent(XMLEvent event) {
      String[] resultLog;
      boolean isSuccessLog;
      switch (event.getEventType()) {
      case XMLEvent.START_ELEMENT :
         StartElement startElement = event.asStartElement();
         String elementName = startElement.getName().getLocalPart();
         if (RestConstants.RES_IMPORT_SUCCESS.equals(elementName)) {
            isSuccessLog = true;
         } else if (RestConstants.RES_IMPORT_ERROR.equals(elementName)) {
            isSuccessLog = false;
         } else {
            break;
         }

         Attribute noderefAttr = startElement.getAttributeByName(new QName(RestConstants.RES_IMPORT_NODEREF));
         String noderef = (noderefAttr == null) ? null : noderefAttr.getValue();
         Attribute doctypeAttr = startElement.getAttributeByName(new QName(RestConstants.RES_IMPORT_DOCTYPE));
         String doctype = (doctypeAttr == null) ? null : doctypeAttr.getValue();
         resultLog = new String[] {
               elementName, // error or success
               startElement.getAttributeByName(new QName(RestConstants.RES_IMPORT_NAMEPATH)).getValue(),
               startElement.getAttributeByName(new QName(RestConstants.RES_IMPORT_MESSAGE)).getValue(),
               startElement.getAttributeByName(new QName(RestConstants.RES_IMPORT_DATE)).getValue(),
               noderef,
               doctype
         };
         resultLogs.add(resultLog);
         if (isSuccessLog) {
            successLogs.add(resultLog);
         } else { // errorLog
            errorLogs.add(resultLog);
         }
         break;
      }
   }


   public ArrayList<String[]> getResultLogs() {
      return resultLogs;
   }
   public ArrayList<String[]> getSuccessLogs() {
      return successLogs;
   }
   public ArrayList<String[]> getErrorLogs() {
      return errorLogs;
   }


   public String getClientPathDelimiter() {
      return getParam(RestConstants.PROP_IMPORT_CLIENT_PATH_DELIMITER).getValue();
   }
   public void setClientPathDelimiter(String clientPathDelimiter) {
      setParam(new NameValuePair(RestConstants.PROP_IMPORT_CLIENT_PATH_DELIMITER, clientPathDelimiter));
   }
   public String getTargetLocationBase() {
      return getParam(RestConstants.PROP_IMPORT_TARGET_LOCATION_BASE).getValue();
   }
   public void setTargetLocationBase(String targetLocationBase) {
      setParam(new NameValuePair(RestConstants.PROP_IMPORT_TARGET_LOCATION_BASE, targetLocationBase));
   }
   public String getDocumentMode() {
      return getParam(RestConstants.PROP_IMPORT_DOCUMENT_MODE).getValue();
   }
   public void setDocumentMode(String documentMode) {
      setParam(new NameValuePair(RestConstants.PROP_IMPORT_DOCUMENT_MODE, documentMode));
   }
   public String getContainerMode() {
      return getParam(RestConstants.PROP_IMPORT_CONTAINER_MODE).getValue();
   }
   public void setContainerMode(String containerMode) {
      setParam(new NameValuePair(RestConstants.PROP_IMPORT_CONTAINER_MODE, containerMode));
   }
   public String getTargetLocationContainerType() {
      return getParam(RestConstants.PROP_IMPORT_TARGET_LOCATION_CONTAINER_TYPE).getValue();
   }
   public void setTargetLocationContainerType(String targetLocationContainerType) {
      setParam(new NameValuePair(RestConstants.PROP_IMPORT_TARGET_LOCATION_CONTAINER_TYPE, targetLocationContainerType));
   }
   public String getTargetLocationChildAssociationType() {
      return getParam(RestConstants.PROP_IMPORT_TARGET_LOCATION_CHILD_ASSOCIATION_TYPE).getValue();
   }
   public void setTargetLocationChildAssociationType(
         String targetLocationChildAssociationType) {
      setParam(new NameValuePair(RestConstants.PROP_IMPORT_TARGET_LOCATION_CHILD_ASSOCIATION_TYPE, targetLocationChildAssociationType));
   }
   public boolean isLogSuccessResults() {
      return Boolean.valueOf(getParam(RestConstants.PROP_IMPORT_LOG_SUCCESS_RESULTS).getValue());
   }
   public void setLogSuccessResults(boolean logSuccessResults) {
      setParam(new NameValuePair(RestConstants.PROP_IMPORT_LOG_SUCCESS_RESULTS, String.valueOf(logSuccessResults)));
   }
   public boolean isLogIndirectErrorResults() {
      return Boolean.valueOf(getParam(RestConstants.PROP_IMPORT_LOG_INDIRECT_ERROR_RESULTS).getValue());
   }
   public void setLogIndirectErrorResults(boolean logIndirectErrorResults) {
      setParam(new NameValuePair(RestConstants.PROP_IMPORT_LOG_INDIRECT_ERROR_RESULTS, String.valueOf(logIndirectErrorResults)));
   }
   
}
