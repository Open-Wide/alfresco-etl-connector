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

package fr.openwide.talendalfresco.acpxml;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import fr.openwide.talendalfresco.rest.XmlHelper;


/**
 * Writes ACP XML using stax
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class AcpXmlWriter {

   /**
    * Alfresco date format.
    * NB. Alfresco date (see ISO8601DateFormat : sYYYY-MM-DDThh:mm:ss.sssTZD
    */
   protected static SimpleDateFormat alfrescoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); // Z
   protected static final String ISO8601_TIMEZONE_SUFFIX = "+00:00";

   protected XMLStreamWriter xmlWriter;

   /**
    * Possibly also encoding
    * @param acpXmlOs
    * @param contentNamespaceMap list of mapped PREFIX, URI
    * @param encoding NB. default to work with tomcat is ISO-8859-1
    * @throws XMLStreamException
    */
   public AcpXmlWriter(OutputStream acpXmlOs,
         String encoding) throws AcpXmlException {
      this(acpXmlOs, encoding, null);
   }
   public AcpXmlWriter(OutputStream acpXmlOs, String encoding,
         List<Map<String,String>> mappedContentNamespaces) throws AcpXmlException {
      try {
         xmlWriter = XmlHelper.createXMLStreamWriter(acpXmlOs, encoding);
         
         // write document start
         
         //xmlWriter.setNamespaceContext(nsCtx); // not useful
         xmlWriter.writeStartDocument(); // default version ; possibly encoding
         
         xmlWriter.writeStartElement("view:view");
         xmlWriter.writeNamespace("view", "http://www.alfresco.org/view/repository/1.0");
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when starting ACP", e);
      }
      if (mappedContentNamespaces != null) {
         for (Map<String,String> mappedContentNamespace : mappedContentNamespaces) {
            String contentNsPrefix = mappedContentNamespace.get("PREFIX");
            String contentNsUri = mappedContentNamespace.get("URI");
            this.writeNamespace(contentNsPrefix, contentNsUri);
         }
      }
   }
   
   public void close() throws AcpXmlException {
      try {
         xmlWriter.writeEndElement(); // end view
         
         xmlWriter.writeEndDocument();
         xmlWriter.close();
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when ending ACP", e);
      }
   }
   
   
   public void writeNamespace(String contentNsPrefix, String contentNsUri) throws AcpXmlException {
      try {
         xmlWriter.writeNamespace(contentNsPrefix, contentNsUri);
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content", e);
      }
   }
   
   /**
    * 
    * @param targetLocation
    * @param alfrescoType prefixed alfresco type ; if null cm:content ?
    * @param alfrescoAspects prefixed alfresco aspects
    * @param configurePermissions if false none
    * @param inheritPermissions
    * @param permissions list of mapped USERORGROUP (here should be default or runtime found value TODO or other param ?),
    * USERORGROUPCOLUMN (not used), PERMISSION
    * @throws AcpXmlException
    */
   public void writeStartContent(String alfrescoType, String childName) throws AcpXmlException {
      writeStartContent(alfrescoType);
      try {
         if (childName != null && childName.length() != 0) {
            xmlWriter.writeAttribute("view:childName", childName); // opt childname
         }
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content", e);
      }
   }
   public void writeStartContent(String alfrescoType) throws AcpXmlException{
      try {
         xmlWriter.writeStartElement(alfrescoType); // "cm:content"
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content", e);
      }
   }
   public void writeStartPermissions(boolean inheritPermissions) throws AcpXmlException {
      try {
         xmlWriter.writeStartElement("view:acl");
         xmlWriter.writeAttribute("view:inherit", String.valueOf(inheritPermissions));
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content", e);
      }
   }
   public void writePermission(Map<String, String> permissionMapping,
         Object optUserOrGroupColumnValue) throws AcpXmlException {
      String authority = (optUserOrGroupColumnValue != null) ?
            String.valueOf(optUserOrGroupColumnValue) : permissionMapping.get("USERORGROUP");
      String permission = permissionMapping.get("PERMISSION");
      writePermission(authority, permission);
   }
   /**
    * To allow direct access to permission writing, ex. to write the container's
    * @param permissionMapping
    * @param optUserOrGroupColumnValue
    * @throws AcpXmlException
    */
   public void writePermission(String authority, String permission) throws AcpXmlException {
      try {
         xmlWriter.writeStartElement("view:ace");
         xmlWriter.writeAttribute("view:access", "ALLOWED"); // only ALLOWED is possible
         xmlWriter.writeStartElement("view:authority");
         xmlWriter.writeCharacters(authority); // "GROUP_EVERYONE"
         xmlWriter.writeEndElement();
         xmlWriter.writeStartElement("view:permission");
         xmlWriter.writeCharacters(permission); // "Consumer"
         xmlWriter.writeEndElement();
         xmlWriter.writeEndElement(); // end ace
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content", e);
      }
   }
   public void writeEndPermissions() throws AcpXmlException {
      try {
         xmlWriter.writeEndElement(); // end acl
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content", e);
      }
   }
   /**
    * 
    * @param alfrescoAspects list of mapped NAME
    * @throws AcpXmlException
    */
   public void writeMappedAspects(List<Map<String,String>> alfrescoAspects) throws AcpXmlException{
      // aspects
      if (alfrescoAspects != null) {
         for (Map<String,String> alfrescoAspect : alfrescoAspects) {
            String alfrescoAspectName = alfrescoAspect.get("NAME");
            this.writeAspect(alfrescoAspectName); // "app:uifacets"
         }
      }
   }
   public void writeAspects(List<String> alfrescoAspectNames) throws AcpXmlException{
      // aspects
      if (alfrescoAspectNames != null) {
         for (String alfrescoAspectName : alfrescoAspectNames) {
            this.writeAspect(alfrescoAspectName); // "app:uifacets"
         }
      }
   }
   public void writeAspect(String alfrescoAspectName) throws AcpXmlException{
      try {
         xmlWriter.writeEmptyElement(alfrescoAspectName); // "app:uifacets"
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content", e);
      }
   }
   
   /**
    * 
    * @param propertyMappings prefixed alfresco property ; mapped
    * NAME, TITLE (not used), TYPE (used for conversion ?? TODO),
    * MANDATORY (could be used for check ?), DEFAULT (not used ?),
    * COLUMN (not used), VALUE (here is talend-side default if any)
    * @param value list if multiple, else toString() serializes it ;
    * if null takes talend-side default (VALUE)
    * @throws AcpXmlException
    */
   public void writeMappedProperty(Map<String, String> propertyMapping,
         Object value) throws AcpXmlException{
      String propertyName = propertyMapping.get("NAME");
      String propertyType = propertyMapping.get("TYPE");
      
      if (value == null) {
         // getting talend-side default
         value = propertyMapping.get("VALUE");
      }
      writeProperty(propertyName, propertyType, value);
   }
   /**
    * To be used for direct access, ex. write name of containers.
    * @param propertyName
    * @param propertyType
    * @param value
    * @throws AcpXmlException
    */
   public void writeProperty(String propertyName, String propertyType, Object value) throws AcpXmlException {
      try {
         if (value instanceof List) {
            // multivalued property ; or based on MULTIPLE TODO in model
        	 
         	// converting it to Strings first, to prevent any error during actual writing
      	   List<Object> values = (List<Object>) value;
            ArrayList<String> stringValues = new ArrayList<String>(values.size());
            try {
               for (Object currentValue : (List<Object>) values) {
                  stringValues.add(valueToString(currentValue, propertyType));
               }
            } catch (AcpXmlException acpxmlex) {
               throw new AcpXmlException("Aborted writing"
                     + " multi valued property " + propertyName, acpxmlex);
            }

            // actual writing
            xmlWriter.writeStartElement(propertyName); // "cm:name"
            xmlWriter.writeStartElement("view:values");
            for (String stringValue : stringValues) {
               xmlWriter.writeStartElement("view:value");
               xmlWriter.writeCharacters(stringValue); // "/cm:generalclassifiable/cm:Languages/cm:French"
               xmlWriter.writeEndElement(); // end view:value
            }
            xmlWriter.writeEndElement(); // end view:values
            xmlWriter.writeEndElement();

         } else if (value != null) {
            // single valued property (or multiple property with only one value)

            // converting it to Strings first, to prevent any error during actual writing
            String stringValue;
            try {
               stringValue = valueToString(value, propertyType);
            } catch (AcpXmlException acpxmlex) {
               throw new AcpXmlException("Aborted writing"
                     + " single valued property " + propertyName, acpxmlex);
            }

            // actual writing
            xmlWriter.writeStartElement(propertyName); // "cm:name"
            xmlWriter.writeCharacters(stringValue); // "my_file_writer.pdf" ; NB. CDATA works, but rather using auto escaping
            xmlWriter.writeEndElement();
            
         } // else null : don't write it (allowed to ease mapping from etl)
         
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content : property " + propertyName, e);
      }
   }
   
   /**
    * Converts a single value of the given property type from its Talend
    * representation to its String-serialized Alfresco representation.
    * For a content property (d:content), it also returns mimetype etc. 
    * An Alfresco date (d:date) expects the Talend type to be DATE (java.util.Date).
    * @param value
    * @param alfrescoPropertyType
    * @return
    * @throws AcpXmlException ex. if date property value not a Date
    */
   protected String valueToString(Object value, String alfrescoPropertyType) throws AcpXmlException {
      if ("d:content".equals(alfrescoPropertyType)) {
         // content property ; ex. contentUrl=classpath:alfresco/bootstrap/Alfresco-Tutorial.pdf|mimetype=application/pdf|size=|encoding=
         String contentUrl = String.valueOf(value); // "classpath:alfresco/bootstrap/Alfresco-Tutorial.pdf"
         return "contentUrl=" + contentUrl  + "|mimetype=|size=|encoding="; // TODO mimetype ? (encoding ??)

      } else if ("d:noderef".equals(alfrescoPropertyType) || "d:category".equals(alfrescoPropertyType)) {
         // reference ; let's mark it to be resolved (will be done by contentImporterBinding on the server)
         if (value instanceof String) {
            return AcpXmlUtil.toNamePathRef((String) value);
         }
         throw new AcpXmlException("Value of " + alfrescoPropertyType
               + " property should be of type String but is " + value);
         
      } else if ("d:date".equals(alfrescoPropertyType)) {
         if (value instanceof Date) {
            return alfrescoDateFormat.format((Date) value) + ISO8601_TIMEZONE_SUFFIX;
         }
         throw new AcpXmlException("Value of d:date property should be of type Date but is " + value);

      /* TODO byte array encoding
      } else if (value instanceof byte[]) {
         return String.valueOf((byte[]) value, encoding);
         */
         
      } else {
         // other types : only stringify
         return String.valueOf(value); // "my_file_writer.pdf" ; NB. CDATA works, but rather using auto escaping
      }
   }
   
   /**
    * 
    * @param associationMappings prefixed alfresco (non primary child) associations ; list of mapped
    * NAME, CHILD (not used), TITLE (not used), TYPE (used for conversion ?? TODO),
    * MANDATORY (could be used for check ?), MANY (not used),
    * COLUMN (not used), new ! VALUE (here should be runtime found namepath ref TODO or other param ?)
    * @param ref if List, multiple TODO ? else anything that the server importer binding can understand
    * (in alfresco talend, namepathref is OK TODO)
    * @throws AcpXmlException
    */
   public void writeMappedAssociation(Map<String, String> associationMappings,
         Object value) throws AcpXmlException {
      String associationName = associationMappings.get("NAME");
      writeAssociation(associationName, value);
   }
   public void writeAssociation(String associationName, Object value) throws AcpXmlException {
      try {
         if (value instanceof List) {
            // multivalued asso

            // converting it to Strings first, to prevent any error during actual writing
            List<String> valueNamePathes;
            try {
               valueNamePathes = AcpXmlUtil.toStringList((List<Object>) value);
            } catch (AcpXmlException acpxmlex) {
               throw new AcpXmlException("Aborted writing" + " multi valued association " + associationName, acpxmlex);
            }

            // actual writing
            xmlWriter.writeStartElement(associationName); // "cm:contains"
            for (Object currentNamePath : valueNamePathes) {
               xmlWriter.writeStartElement("view:reference");
               xmlWriter.writeAttribute("view:pathref", AcpXmlUtil.toNamePathRef((String) currentNamePath));
               xmlWriter.writeEndElement(); // end reference
            }
            xmlWriter.writeEndElement(); // end association

         } else if (value != null) {
            // single valued association (or multiple association with only one value)

            // converting it to String first, to prevent any error during actual writing
            if (!(value instanceof String)) {
               throw new AcpXmlException("Reference value " + value + " of association " + associationName + " should be of type String");
            }

            // actual writing
            xmlWriter.writeStartElement(associationName); // "cm:contains"
            xmlWriter.writeStartElement("view:reference");
            xmlWriter.writeAttribute("view:pathref", AcpXmlUtil.toNamePathRef((String) value));
            xmlWriter.writeEndElement(); // end reference
            xmlWriter.writeEndElement(); // end association
            
         } // else null : don't write it (allowed to ease mapping from etl)

      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content : association " + associationName, e);
      }
   }

   /**
    * containment association.
    * Stacks up path name. Requires the "cm:name" property to have already been set.
    * @param parentChildAssociationType
    * @throws AcpXmlException
    */
   public void writeStartPrimaryChildAssociation(String parentChildAssociationType) throws AcpXmlException{
      try {
         xmlWriter.writeStartElement(parentChildAssociationType); // "cm:contains"
         //xmlWriter.writeAttribute("view:childName", parentChildAssociationType); // default childname
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content", e);
      }
   }
   
   /**
    * Stacks down path name.
    * @throws AcpXmlException
    */
   public void writeEndPrimaryChildAssociation() throws AcpXmlException{
      try {
         xmlWriter.writeEndElement();
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content", e);
      }
   }
   
   public void writeEndContent() throws AcpXmlException{
      try {
         xmlWriter.writeEndElement();
      } catch (XMLStreamException e) {
         throw new AcpXmlException("XML writing error when writing content", e);
      }
   }
   
}
