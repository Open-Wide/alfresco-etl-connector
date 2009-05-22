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
package fr.openwide.talendalfresco.rest.server.command;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.alfresco.repo.importer.ImportTimerProgress;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.openwide.talendalfresco.alfresco.NamePathService;
import fr.openwide.talendalfresco.alfresco.XmlContentImporterResultHandler;
import fr.openwide.talendalfresco.alfresco.importer.ContentImporterBinding;
import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.server.RestServerHelper;
import fr.openwide.talendalfresco.rest.server.processor.RestCommandBase;
import fr.openwide.talendalfresco.rest.server.processor.RestCommandResult;


/**
 * Imports ACP XML.
 * Handles encoding of the request content.
 * Hardwires encoding to RestServerHelper.DEFAULT_REST_ENCODING
 * (i.e. ISO-8859-1, which is required because tomcat uses it
 * by default, and changing it there would impact the whole webapp)
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class ImportCommand extends RestCommandBase {

   // static logger
   private static Log logger = LogFactory.getLog(ImportCommand.class);
   
   private static final String[] PROPERTIES = new String[] { RestConstants.PROP_IMPORT_PATH };

   public String[] getPropertyNames() {
      return PROPERTIES;
   }
   
   public final static StoreRef WORKSPACE_STOREREF = new StoreRef("workspace", "SpacesStore");


   protected static ImporterService contentImporterService;
   protected static NamePathService namePathService;
   

   public ImportCommand() {
      super("import");
   }
   

   /**
    * 
    * @return
    */
   protected boolean validateArgumentsImpl() {
      try {
         
         return true;
      } catch (Exception ex) { // RestRuntimeException
         String msg = "Bad parameters for command "
            + getCommandName() + " which requires" + getPropertyNames()
            + "but got " + args + " : " + ex.getMessage();
         xmlResult.setError(RestCommandResult.CODE_ERROR_WRONG_PARAMETERS, msg, ex);
         logger.error(msg);
         return false;
      }
   }
   
   
   public void executeImpl() {
      String path = getArgument(args, RestConstants.PROP_IMPORT_PATH, ""); // TODO check ?

      // getting params and putting them in conf in binding :
      ContentImporterBinding contentImporterBinding = new ContentImporterBinding();
      setImportParameters(contentImporterBinding, args);

      try {
         // getting ACP XML from request content
         InputStream acpXmlIs = httpRequest.getInputStream();
         Reader viewReader = new BufferedReader(new InputStreamReader(acpXmlIs,
               RestServerHelper.DEFAULT_REST_ENCODING));
         // NB. encoding ISO-8859-1 is required because tomcat uses it
         // by default, and changing it there would impact the whole webapp
         
         // building XML result handler
         contentImporterBinding.contentImporterResultHandler = new XmlContentImporterResultHandler(
               xmlWriter, contentImporterBinding, namePathService);
         // providing NamePathService ; required for resolving namePathes
         NamePathService namePathService = (NamePathService) this.getBean("talendalfresco.namePathService");
         contentImporterBinding.setNamePathService(namePathService);
         
         // building location
         Location location = new Location(WORKSPACE_STOREREF);
         location.setChildAssocType(contentImporterBinding.targetLocationChildAssociationTypeQName);
         location.setPath(path);

         ImporterProgress importProgress = null;
         if (logger.isDebugEnabled()) {
             importProgress = new ImportTimerProgress(logger);
             logger.debug("Importing view to " + path);
         }

         // actual import
         contentImporterService.importView(viewReader, location, contentImporterBinding, importProgress);
         
         if (logger.isDebugEnabled()) {
            logger.debug("   Finished importing view to " + path);
         }
         
         viewReader.close();
         
      } catch (Exception ex) {
         // error while logging in or writing alfresco node XML
         String msg = "Technical error while importing to path " + path;
         logger.error(msg, ex);
         xmlResult.setError(RestConstants.CODE_ERROR_TECHNICAL, msg, ex);
      }
   }


   protected void setImportParameters(ContentImporterBinding contentImporterBinding, Map<String, String> args) {
      // NB. encoding (besides for the REST exchanges) is handled in the client side
      contentImporterBinding.clientPathDelimiter = getArgument(args, RestConstants.PROP_IMPORT_CLIENT_PATH_DELIMITER, contentImporterBinding.clientPathDelimiter); // assuming default client is windows
      contentImporterBinding.targetLocationBase = getArgument(args, RestConstants.PROP_IMPORT_TARGET_LOCATION_BASE, contentImporterBinding.targetLocationBase); // "/";
      // NB. mapTargetLocationFromColumn and targetLocationColumn have been used by the client to build ACP XML
      contentImporterBinding.documentMode = getArgument(args, RestConstants.PROP_IMPORT_DOCUMENT_MODE, contentImporterBinding.documentMode); // create only (def), create or update
      contentImporterBinding.containerMode = getArgument(args, RestConstants.PROP_IMPORT_CONTAINER_MODE, contentImporterBinding.containerMode); // create or update (create if doesn't exist), update only (use existing, def)
      // NB. alfrescoModels namespaces are provided in the ACP XML
      contentImporterBinding.targetLocationContainerType = getArgument(args, RestConstants.PROP_IMPORT_TARGET_LOCATION_CONTAINER_TYPE, contentImporterBinding.targetLocationContainerType); // opt, default cm:folder
      contentImporterBinding.targetLocationChildAssociationType = getArgument(args, RestConstants.PROP_IMPORT_TARGET_LOCATION_CHILD_ASSOCIATION_TYPE, contentImporterBinding.targetLocationChildAssociationType); // opt, default cm:contains
      contentImporterBinding.logSuccessResults = Boolean.valueOf(getArgument(args, RestConstants.PROP_IMPORT_LOG_SUCCESS_RESULTS, contentImporterBinding.logSuccessResults + "")); // opt, default true
      contentImporterBinding.logIndirectErrorResults = Boolean.valueOf(getArgument(args, RestConstants.PROP_IMPORT_LOG_INDIRECT_ERROR_RESULTS, contentImporterBinding.logIndirectErrorResults + "")); // opt, default false

      contentImporterBinding.targetLocationContainerTypeQName = QName.createQName(contentImporterBinding.targetLocationContainerType, namespaceService); // opt, default ContentModel.TYPE_FOLDER
      contentImporterBinding.targetLocationChildAssociationTypeQName = QName.createQName(contentImporterBinding.targetLocationChildAssociationType, namespaceService); // opt, default ContentModel.ASSOC_CONTAINS
   }


   protected String getArgument(Map<String, String> args, String argName, String defaultValue) {
      String argValue = args.get(argName);
      return (argValue != null && argValue.length() != 0) ? argValue : defaultValue;
   }
   


   public void setContentImporterService(ImporterService contentImporterService) {
      ImportCommand.contentImporterService = contentImporterService;
   }

   public void setNamePathService(NamePathService namePathService) {
      ImportCommand.namePathService = namePathService;
   }

}
