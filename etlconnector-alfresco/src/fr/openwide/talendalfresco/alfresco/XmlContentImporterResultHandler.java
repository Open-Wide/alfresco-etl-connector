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
package fr.openwide.talendalfresco.alfresco;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ImportNode;
import org.alfresco.repo.importer.ImporterComponent;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import fr.openwide.talendalfresco.alfresco.importer.ContentImporterBinding;
import fr.openwide.talendalfresco.alfresco.importer.ContentImporterResultHandler;
import fr.openwide.talendalfresco.rest.RestConstants;


/**
 * Writes individual XML logs for each reported success or error.
 * ex. :
 * <error namepath="/Alfresco/myfile.pdf" message="..." date="yyyy-MM-dd HH:mm:ss.SSS" [noderef="xxx"]  [doctype="yyy"]/>
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class XmlContentImporterResultHandler implements
      ContentImporterResultHandler {
   
   protected static final Log logger = LogFactory.getLog(ImporterComponent.class);
   
   protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   private XMLStreamWriter xmlWriter;
   private ContentImporterBinding contentImporterBinding;
   private NamePathService namePathService;

   /**
    * 
    * @param xmlWriter where logs are written in XML
    * @param contentImporterBinding used as import command conf provider, TODO could pass command in it
    * @param nodeService used for tracing
    */
   public XmlContentImporterResultHandler(XMLStreamWriter xmlWriter,
         ContentImporterBinding contentImporterBinding,
         NamePathService namePathService) {
      this.xmlWriter = xmlWriter;
      this.contentImporterBinding = contentImporterBinding;
      this.namePathService = namePathService;
   }
   
   
   /**
    * 
    * @param namePath
    * @param context null if container case
    * @param t
    */

   public void nodeError(ImportNode nodeContext, Throwable t) {
      String typeInfo = null;
      // node case : take info from context
      TypeDefinition typeDef = nodeContext.getTypeDefinition();
      if (typeDef != null) {
         String nodeAspects = StringUtils.collectionToDelimitedString(nodeContext.getNodeAspects(), " ");
         typeInfo = typeDef.getName() + ";" + nodeAspects;
      }
      String nodeName = (String) nodeContext.getProperties().get(ContentModel.PROP_NAME);
      // NB. parent noderef exists since we've being there and created it already
      NodeRef parentNodeRef = nodeContext.getParentContext().getParentRef();
      this.nodeError(nodeName, parentNodeRef, typeInfo, t);
   }
   public void nodeError(String nodeName, NodeRef parentNodeRef, String typeInfo, Throwable t) {
      String namePath = getNamePath(parentNodeRef)
            + contentImporterBinding.clientPathDelimiter + nodeName;
      String errMsg = (t == null) ? "" : ((t.getMessage() == null) ? t.toString() : t.getMessage());
      logger.trace("error on " + namePath, t);
      try {
         xmlWriter.writeStartElement(RestConstants.RES_IMPORT_ERROR);
         xmlWriter.writeAttribute(RestConstants.RES_IMPORT_NAMEPATH, namePath);
         xmlWriter.writeAttribute(RestConstants.RES_IMPORT_MESSAGE, errMsg);
         xmlWriter.writeAttribute(RestConstants.RES_IMPORT_DATE, dateFormat.format(new Date()));

         // now tracing detailed info :
         if (typeInfo != null) {
            xmlWriter.writeAttribute(RestConstants.RES_IMPORT_DOCTYPE, typeInfo);
         }
         xmlWriter.writeEndElement();
      } catch (XMLStreamException e) {
         logger.error("Unable to write error XML result for " + namePath, e);
      }
   }


   /**
    * @param context null if container case
    * @param nodeRef can't be null
    * @param msg
    */
   public void nodeSuccess(ImportNode context, NodeRef nodeRef, String msg) {
      String typeInfo = null;
      // node case : take info from context
      TypeDefinition typeDef = context.getTypeDefinition();
      if (typeDef != null) {
         String nodeAspects = StringUtils.collectionToDelimitedString(context.getNodeAspects(), " ");
         typeInfo = typeDef.getName() + " " + nodeAspects;
      }
      this.nodeSuccess(nodeRef, typeInfo, msg);
   }
   public void nodeSuccess(NodeRef nodeRef, String typeInfo, String msg) {
      if (!this.contentImporterBinding.logSuccessResults) {
         return;
      }
      String namePath = getNamePath(nodeRef);
      logger.trace("success on " + namePath);
      try {
         xmlWriter.writeStartElement(RestConstants.RES_IMPORT_SUCCESS);
         xmlWriter.writeAttribute(RestConstants.RES_IMPORT_NAMEPATH, namePath);
         xmlWriter.writeAttribute(RestConstants.RES_IMPORT_MESSAGE, msg);
         xmlWriter.writeAttribute(RestConstants.RES_IMPORT_DATE, dateFormat.format(new Date()));

         // now tracing detailed info :
         xmlWriter.writeAttribute(RestConstants.RES_IMPORT_NODEREF, nodeRef.toString());
         if (typeInfo != null) {
            xmlWriter.writeAttribute(RestConstants.RES_IMPORT_DOCTYPE, typeInfo);
         }
         xmlWriter.writeEndElement();
      } catch (XMLStreamException e) {
         logger.error("Unable to write success XML result for " + namePath, e);
      }
   }


   public void containerError(String name, NodeRef parent, Throwable t) {
      this.nodeError(name, parent, contentImporterBinding.targetLocationContainerType.toString(), t);
   }

   public void containerSuccess(NodeRef nodeRef, String msg) {
      this.nodeSuccess(nodeRef, contentImporterBinding.targetLocationContainerType.toString(), msg);
   }

   
   /**
    * Helper for trace, handles parent whose creation failed previously
    * @param nodeRef
    * @return
    */
   protected String getNamePath(NodeRef nodeRef) {
      String namePath = namePathService.getNamePath(nodeRef, contentImporterBinding.clientPathDelimiter);
      namePath = (namePath == null) ? "[node was not created]" : namePath;
      return namePath;
   }


   /**
    * Logs it as an error with a subpath of the base node
    */
   public void referenceError(NodeRef context, QName propertyOrAssociation,
         Serializable value, Throwable t) {
      String namePath = getNamePath(context) + contentImporterBinding.clientPathDelimiter
            + "[" + propertyOrAssociation.toPrefixString() + "]";
      StringBuffer errMsgBuf = new StringBuffer();
      errMsgBuf.append("Error resolving reference for property or association ");
      errMsgBuf.append(propertyOrAssociation.toPrefixString());
      errMsgBuf.append(" of node with namepath ");
      errMsgBuf.append(namePath);
      if (value != null) {
         errMsgBuf.append(" to target path ");
         errMsgBuf.append(value);
      }
      errMsgBuf.append(" : ");
      errMsgBuf.append(t.getMessage());

      logger.trace("error on " + namePath, t);
      try {
         xmlWriter.writeStartElement(RestConstants.RES_IMPORT_ERROR);
         xmlWriter.writeAttribute(RestConstants.RES_IMPORT_NAMEPATH, namePath);
         xmlWriter.writeAttribute(RestConstants.RES_IMPORT_MESSAGE, errMsgBuf.toString());
         xmlWriter.writeAttribute(RestConstants.RES_IMPORT_DATE, dateFormat.format(new Date()));

         // now tracing detailed info :
         if (propertyOrAssociation != null) {
            xmlWriter.writeAttribute(RestConstants.RES_IMPORT_DOCTYPE, propertyOrAssociation.toPrefixString());
         }
         xmlWriter.writeEndElement();
      } catch (XMLStreamException e) {
         logger.error("Unable to write error XML result for " + namePath, e);
      }
   }

   /**
    * Successes resolving references are not traced
    */
   public void referenceSuccess(NodeRef context, QName property,
         Serializable value, String msg) {
      // not tracing reference successes
   }
   
}
