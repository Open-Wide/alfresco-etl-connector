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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.openwide.talendalfresco.importer.ContentImporterConfiguration;


/**
 * Content import driver that writes namepath'd documents as ACP XML.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class TalendAcpXmlWriter {

   /** TODO send to server ; TODO or talend param ? */
   protected static final String PATH_DELIMITER = File.separator;
   
   /** or in file ? */
   protected ByteArrayOutputStream acpXmlBos = new ByteArrayOutputStream();
   protected AcpXmlWriter acpXmlWriter = null;
   protected List<Map<String,String>> mappedContentNamespaces = null;
   /** NB. default to work with tomcat is ISO-8859-1 */
   protected String restEncoding;
   
   protected ArrayListStack<String> currentPathNameStack = new ArrayListStack<String>();
   /** Set in writeStartContent, unset in end, required in startPrimaryAssociation */
   //protected String currentPathName = null;
   
   protected String alfrescoType = "cm:content";
   protected List<String> alfrescoAspects = null;
   protected String containerType = "cm:folder";
   protected String containerChildAssociationType = "cm:contains";
   protected boolean configurePermission = false;
   protected boolean permissionOnDocumentAndNotContainer = false;
   protected boolean inheritPermissions = true;
   //protected List<Map<String,String>> permissions = null;
   
   public TalendAcpXmlWriter() {
      
   }
   
   public void start() throws AcpXmlException {
      this.acpXmlWriter = new AcpXmlWriter(acpXmlBos, restEncoding, mappedContentNamespaces);
   }

   public void writeNamespace(String contentNsPrefix, String contentNsUri) throws AcpXmlException {
      acpXmlWriter.writeNamespace(contentNsPrefix, contentNsUri);
   }

   
   /**
    * Moves to target location (from last writeEndDocument),
    * then writes type, permissions, aspects.
    * Out of this method remains to be done for this document :
    * properties
    * associations
    * end document
    * TODO even same with [][] ??
    * @param targetLocation
    * @param permissionValues
    * @throws AcpXmlException
    */
   public void writeStartDocument(String targetLocation,
         String[][] permissionValues) throws AcpXmlException {
      moveToTargetLocation(targetLocation, permissionValues);
      acpXmlWriter.writeStartContent(alfrescoType, ContentImporterConfiguration.CHILD_NAME_DOCUMENT);
      if (configurePermission && permissionOnDocumentAndNotContainer) {
         writeStartPermissions();
         for (String[] permissionValue : permissionValues) {
            String authority = permissionValue[0]; 
            String permission = permissionValue[1]; 
            acpXmlWriter.writePermission(authority, permission);
         }
         writeEndPermissions();
      }
      writeDocumentAspects();
   }

   
   public void close() throws AcpXmlException {
      while (!currentPathNameStack.isEmpty()) {
         currentPathNameStack.pop();
         writeEndContainer();
      }
      acpXmlWriter.close();
   }
   
   public String toString() {
      return acpXmlBos.toString();
   }
   
   

   /**
    * Moves to target location (from last writeEndDocument)
    * @param targetLocation
    * @param permissionValues full permission info requried
    * @throws AcpXmlException
    */
   protected void moveToTargetLocation(String targetLocation,
         String[][] permissionValues) throws AcpXmlException {
      ArrayList<String> pathNames = AcpXmlUtil.stringToNamePath(targetLocation, PATH_DELIMITER);
      
      // 1. getting last same pathName index
      int firstDifferentPathNameIndex = 0;
      int pathNameNb = pathNames.size();
      int currentPathNameStackSize = currentPathNameStack.size();
      for (; firstDifferentPathNameIndex < pathNameNb
            && firstDifferentPathNameIndex < currentPathNameStackSize; firstDifferentPathNameIndex++) {
         String pathName = pathNames.get(firstDifferentPathNameIndex);
         if (!currentPathNameStack.get(firstDifferentPathNameIndex).equals(pathName)) {
            // found firstDifferentPathNameIndex !
            break;
         }
      }

      // 2. unstacking (and ending containers) until last same path name
      for (int i = currentPathNameStackSize - 1; i >= firstDifferentPathNameIndex; i--) {
         currentPathNameStack.pop();
         writeEndContainer();
      }
      
      //3. stacking remaining target location pathNames (and starting containers)
      // if any, stack them up
      if (firstDifferentPathNameIndex != pathNameNb) {
         // stack all of them but the last one
         for (int i = firstDifferentPathNameIndex; i < pathNameNb - 1; i++) {
            String remainingPathName = pathNames.get(i);
            writeStartContainer(remainingPathName);
            currentPathNameStack.push(remainingPathName);
         }
         // now write last container and companion permissions if any :
         String lastPathName = pathNames.get(pathNameNb - 1);
         writeStartLastContainer(lastPathName, permissionValues);
         currentPathNameStack.push(lastPathName);
      }
   }

   protected void writeStartContainer(String name) throws AcpXmlException {
      acpXmlWriter.writeStartContent(containerType, ContentImporterConfiguration.CHILD_NAME_CONTAINER);
      // no custom permissions
      writeContainerUntilChildren(name);
   }
   protected void writeStartLastContainer(String name, String[][] permissionValues) throws AcpXmlException {
      acpXmlWriter.writeStartContent(containerType, ContentImporterConfiguration.CHILD_NAME_CONTAINER);
      if (configurePermission && !permissionOnDocumentAndNotContainer) {
         writeStartPermissions();
         for (String[] permissionValue : permissionValues) {
            String authority = permissionValue[0]; 
            String permission = permissionValue[1]; 
            acpXmlWriter.writePermission(authority, permission);
         }
         writeEndPermissions();
      }
      writeContainerUntilChildren(name);
   }
   protected void writeContainerUntilChildren(String name) throws AcpXmlException {
      // no additional aspects
      acpXmlWriter.writeStartProperties();
      acpXmlWriter.writeProperty("cm:name", "d:text", name);
      acpXmlWriter.writeEndProperties();
      // no other properties
      // starting containment child asso
      acpXmlWriter.writeStartAssociations();
      acpXmlWriter.writeStartPrimaryChildAssociation(containerChildAssociationType);
   }
   
   
   
   ////////////////////////
   // bare methods
   
   public void writeStartPermissions() throws AcpXmlException {
      acpXmlWriter.writeStartPermissions(inheritPermissions);
   }
   public void writePermission(Map<String, String> permissionMapping,
         Object optUserOrGroupColumnValue) throws AcpXmlException {
      acpXmlWriter.writePermission(permissionMapping, optUserOrGroupColumnValue);
   }
   public void writeEndPermissions() throws AcpXmlException {
      acpXmlWriter.writeEndPermissions();
   }
   public void writeDocumentAspects() throws AcpXmlException{
      acpXmlWriter.writeAspects(alfrescoAspects);
   }

   public void writeStartProperties() throws AcpXmlException {
      acpXmlWriter.writeStartProperties();
   }
   /**
    * Must be between writeStartProperties() & writeEndProperties() in a document
    * @param propertyMappings prefixed alfresco property ; mapped
    * NAME, TITLE (not used), TYPE (used for conversion ?? TODO),
    * MANDATORY (could be used for check ?), DEFAULT (not used ?),
    * COLUMN (not used), VALUE (here is talend-side default if any)
    * @param value list if multiple, else toString() serializes it ;
    * if null takes talend-side default (VALUE)
    * @throws AcpXmlException
    */
   public void writeMappedProperty(Map<String, String> propertyMapping,
         Object value) throws AcpXmlException {
      acpXmlWriter.writeMappedProperty(propertyMapping, value);
   }
   /**
    * Must be between writeStartProperties() & writeEndProperties() in a document
    * @param propertyName
    * @param propertyType
    * @param value
    * @throws AcpXmlException
    */
   public void writeProperty(String propertyName, String propertyType,
         Object value) throws AcpXmlException {
      acpXmlWriter.writeProperty(propertyName, propertyType, value);
   }
   public void writeEndProperties() throws AcpXmlException {
      acpXmlWriter.writeEndProperties();
   }

   public void writeStartAssociations() throws AcpXmlException {
      acpXmlWriter.writeStartAssociations();
   }
   /**
    * Must be between writeStartAssociations() & writeEndAssociations() in a document
    * @param associationMappings prefixed alfresco (non primary child) associations ; list of mapped
    * NAME, CHILD (not used), TITLE (not used), TYPE (used for conversion ?? TODO),
    * MANDATORY (could be used for check ?), MANY (not used),
    * COLUMN (not used), new ! VALUE (here should be runtime found namepath ref TODO or other param ?)
    * @param ref if List, multiple TODO ? else anything that the server importer binding can understand
    * (in alfresco talend, namepathref is OK TODO)
    * @throws AcpXmlException
    */
   public void writeMappedAssociation(Map<String, String> associationMappings,
         Object ref) throws AcpXmlException{
      acpXmlWriter.writeMappedAssociation(associationMappings, ref);
   }
   /**
    * Must be between writeStartAssociations() & writeEndAssociations() in a document
    * @param associationName
    * @param ref
    * @throws AcpXmlException
    */
   public void writeAssociation(String associationName,
         Object ref) throws AcpXmlException{
      acpXmlWriter.writeAssociation(associationName, ref);
   }
   public void writeEndAssociations() throws AcpXmlException {
      acpXmlWriter.writeEndAssociations();
   }

   public void writeEndDocument() throws AcpXmlException {
      acpXmlWriter.writeEndContent();
   }
   public void writeEndContainer() throws AcpXmlException {
      acpXmlWriter.writeEndPrimaryChildAssociation(); // end cm:contains
      acpXmlWriter.writeEndAssociations(); // end view:associations
      acpXmlWriter.writeEndContent(); // end cm:content
   }

   public void setMappedContentNamespaces(List<Map<String, String>> mappedContentNamespaces) {
      this.mappedContentNamespaces = mappedContentNamespaces;
   }

   public String getAlfrescoType() {
      return alfrescoType;
   }

   public void setAlfrescoType(String alfrescoType) {
      this.alfrescoType = alfrescoType;
   }

   public void setMappedAlfrescoAspects(List<Map<String, String>> mappedAlfrescoAspects) {
      if (mappedAlfrescoAspects != null) {
         this.alfrescoAspects = new ArrayList<String>(mappedAlfrescoAspects.size());
         for (Map<String,String> mappedAlfrescoAspect : mappedAlfrescoAspects) {
            String alfrescoAspectName = mappedAlfrescoAspect.get("NAME");
            this.alfrescoAspects.add(alfrescoAspectName); // "app:uifacets"
         }
      }
   }

   public void setAlfrescoAspects(List<String> alfrescoAspects) {
      this.alfrescoAspects = alfrescoAspects;
   }

   public String getContainerType() {
      return containerType;
   }

   public void setContainerType(String containerType) {
      this.containerType = containerType;
   }

   public String getContainerChildAssociationType() {
      return containerChildAssociationType;
   }

   public void setContainerChildAssociationType(
         String containerChildAssociationType) {
      this.containerChildAssociationType = containerChildAssociationType;
   }

   public boolean isConfigurePermission() {
      return configurePermission;
   }

   public void setConfigurePermission(boolean configurePermission) {
      this.configurePermission = configurePermission;
   }

   public boolean isPermissionOnDocumentAndNotContainer() {
      return permissionOnDocumentAndNotContainer;
   }

   public void setPermissionOnDocumentAndNotContainer(
         boolean permissionOnDocumentAndNotContainer) {
      this.permissionOnDocumentAndNotContainer = permissionOnDocumentAndNotContainer;
   }

   public boolean isInheritPermissions() {
      return inheritPermissions;
   }

   public void setInheritPermissions(boolean inheritPermissions) {
      this.inheritPermissions = inheritPermissions;
   }

   public String getRestEncoding() {
      return restEncoding;
   }

   public void setRestEncoding(String restEncoding) {
      this.restEncoding = restEncoding;
   }

   /*public List<Map<String, String>> getPermissions() {
      return permissions;
   }

   public void setPermissions(List<Map<String, String>> permissions) {
      this.permissions = permissions;
   }*/
   
}
