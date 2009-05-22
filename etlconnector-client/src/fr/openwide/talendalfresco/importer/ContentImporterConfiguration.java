/*
 * Copyright (C) 2008 Open Wide SA
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
 * More information at http://forge.alfresco.com/projects/etlconnector/
 */

package fr.openwide.talendalfresco.importer;


/**
 * Content import driver configuration.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class ContentImporterConfiguration {
   
   public static final String DOCUMENT_MODE_CREATE_ONLY = "Create only";
   public static final String DOCUMENT_MODE_CREATE_OR_UPDATE = "Create or update";
   public static final String CONTAINER_MODE_CREATE_OR_UPDATE = "Create or update";
   public static final String CONTAINER_MODE_UPDATE_ONLY = "Update only";

   // used to differentiate between container and document mode on server side
   public static final String CHILD_NAME_CONTAINER = "cm:container";
   public static final String CHILD_NAME_DOCUMENT = "cm:document";

   // NB. encoding (besides for the REST exchanges) is handled in the client side
   public String clientPathDelimiter = "\\"; // assuming default client is windows
   public String targetLocationBase = "/"; // workspace root
   // NB. mapTargetLocationFromColumn and targetLocationColumn have been used by the client to build ACP XML
   public String documentMode = DOCUMENT_MODE_CREATE_ONLY; // create only (def), create or update
   public String containerMode = CONTAINER_MODE_CREATE_OR_UPDATE; // create or update (create if doesn't exist), update only (use existing, def)
   // NB. alfrescoModels namespaces are provided in the ACP XML
   public String targetLocationContainerType = "cm:folder"; // opt
   public String targetLocationChildAssociationType = "cm:contains"; // opt
   // NB. container permissions are provided in the ACP XML
   public boolean logSuccessResults = true;
   public boolean logIndirectErrorResults = false;
   
}
