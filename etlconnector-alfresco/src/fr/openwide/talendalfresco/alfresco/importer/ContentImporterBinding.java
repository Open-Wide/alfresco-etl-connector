/*
 * Copyright (C) 2008 Open Wide SA
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
 * More information at http://forge.alfresco.com/projects/etlconnector/
 *
 */
package fr.openwide.talendalfresco.alfresco.importer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.namespace.QName;

import fr.openwide.talendalfresco.alfresco.NamePathService;
import fr.openwide.talendalfresco.importer.ContentImporterConfiguration;


/**
 * Resolves references for Alfresco Importer.
 * Also holds content importer mode properties.
 * NB. content importer mode properties that have been used to build the
 * incoming ACP XML (ex. alfresco models namespaces) are not required
 * anymore and therefore not here.
 * Requires namePathService to be able to resolve namePath references.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class ContentImporterBinding extends ContentImporterConfiguration implements ImporterBinding {

   // for the next import configuration parameters, see inherited ContentImporterConfiguration
   /*
   // NB. encoding (besides for the REST exchanges) is handled in the client side
   public String clientPathDelimiter = "\\"; // assuming default client is windows
   public String targetLocationBase = "/"; // workspace root
   // NB. mapTargetLocationFromColumn and targetLocationColumn have been used by the client to build ACP XML
   public String documentMode; // create only, create or update (?)
   public String containerMode; // create or update (create if doesn't exist), update only (use existing)
   // NB. alfrescoModels namespaces are provided in the ACP XML
   public String targetLocationContainerType = "cm:folder"; // opt
   public String targetLocationChildAssociationType = "cm:contains"; // opt
   // NB. container permissions are provided in the ACP XML
   public boolean logSuccessResults = true;
   public boolean logIndirectErrorResults = false;
   */

   public QName targetLocationContainerTypeQName = ContentModel.TYPE_FOLDER; // opt
   public QName targetLocationChildAssociationTypeQName = ContentModel.ASSOC_CONTAINS; // opt
   
   // res :
   public ContentImporterResultHandler contentImporterResultHandler;
   
   
   
   protected NamePathService namePathService;
   
   public ContentImporterBinding() {
      
   }

   
   public boolean allowReferenceWithinTransaction() {
      // don't search referenced methods within current trxn (?)
      return false;
   }

   public QName[] getExcludedClasses() {
      // classes exluded from import
      return null;
      // NB. same as return new QName[] { ContentModel.ASPECT_REFERENCEABLE, ContentModel.ASPECT_VERSIONABLE };
   }

   public UUID_BINDING getUUIDBinding() {
      // node creation strategy
      return UUID_BINDING.CREATE_NEW;
   }

   /**
    * Resolves namepath references, ex. ${/guest}
    * @throws AlfrescoRuntimeException if not found, so the current node import will fail
    */
   public String getValue(String key) {
      String path = key;
      if (path.indexOf(":") != -1) {
         // xpath syntax : allowing original behaviour, ex. for categories
         return path;
      }
      // not an xpath ; assuming namepath syntax
      // Only used for resolving namepathes, so assuming key is one
      String namePath = path;
      NodeRef foundNodeRef = this.namePathService.resolveNamePath(namePath, this.clientPathDelimiter);
      if (foundNodeRef == null) {
         // let's make the current node import or reference resolving fail :
         throw new AlfrescoRuntimeException("Unable to resolve bound namePath reference " + namePath);
      }
      return foundNodeRef.toString();
   }

   public void setNamePathService(NamePathService namePathService) {
      this.namePathService = namePathService;
   }

}
