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
package fr.openwide.talendalfresco.alfresco.importer;

import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ImportNode;
import org.alfresco.repo.importer.Importer;
import org.alfresco.repo.importer.view.NodeContext;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;

import fr.openwide.talendalfresco.acpxml.AcpXmlUtil;
import fr.openwide.talendalfresco.alfresco.NamePathService;
import fr.openwide.talendalfresco.alfresco.util.ContentDataUtil;
import fr.openwide.talendalfresco.importer.ContentImporterConfiguration;


/**
 * Imports content (cm:object).
 * Extended features beyond the parent's :
 *    * allows to use namepath (with company home as its root) in addition to xpath to specify targetLocation (binding.location)
 *    * missing containers in targetLocation (binding.location) are created
 *    * wraps every node import (more precisely NodeImporter.importNode(), i.e. node creation,
 * aspects, properties, linking, content : everything but containment associations) in its own
 * transaction
 *    * each node import & transaction can be logged with detailed info
 *    * missing content mimetype is guessed from the content url
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class ContentImporterComponent extends ContentImporterComponentBase {
   protected static final QName DATATYPE_CONTENT = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "content");
   
   /** to wrap node imports with txns */
   protected TransactionService transactionService;
   /** to guess content mimetype */
   protected MimetypeService mimetypeService;
   /** to find existing nodes using namePathes */
   protected NamePathService namePathService;

   /**
    * Get the target location nodeRef using a quick xpath (db) search.
    * In addition to parent : create it if it does not exist, allow namePath.
    */
   @Override
   protected NodeRef getNodeRef(Location location, ImporterBinding binding) {
      // can't use super, throw undiferrenciated exceptions
      //NodeRef targetLocationNodeRef = super.getNodeRef(location, binding);
      
      // see super
      ParameterCheck.mandatory("Location", location);
      
      // Establish node to import within
      NodeRef nodeRef = location.getNodeRef();
      if (nodeRef == null)
      {
          // If a specific node has not been provided, default to the root
          nodeRef = nodeService.getRootNode(location.getStoreRef());
      }
      
      // Resolve to path within node, if one specified
      String path = location.getPath();
      if (path != null) {
          // [talendalfresco] allowing empty path i.e. content root
          
          // Create a valid path and search
          path = bindPlaceHolder(path, binding);
          if (path.indexOf(":") == -1) { // TODO always
             // [talendalfresco] not an xpath ; assuming namepath syntax
             nodeRef = getOrCreateTargetContainer(path, (ContentImporterBinding) binding);
             if (nodeRef == null) {
                throw new ImporterException("Name path " + path + " does not exist and its creation failed, aborting import.");
             }
             
          } else {
             // [talendalfresco] xpath syntax : allowing original behaviour
             path = createValidPath(path);
             List<NodeRef> nodeRefs = searchService.selectNodes(nodeRef, path, null, namespaceService, false);
             if (nodeRefs.size() == 0) {
                throw new ImporterException("Path " + path + " within node " + nodeRef + " does not exist - the path must resolve to a valid location "
                      + "when using xpath syntax, or be provided in namepath syntax.");
             } else if (nodeRefs.size() > 1) {
                 throw new ImporterException("Path " + path + " within node " + nodeRef + " found too many locations - the path must resolve to one location");
             } else {
                nodeRef = nodeRefs.get(0);
             }
          }
      }
      
      return nodeRef;
   }
   

   /**
    * Imports contents, therefore below the companyHome,
    * therefore a namepath should never start with /Alfresco
    * (namePath version)
    * TODO txn or in sub
    * @param path a namepath with the company home as its root
    * @param contentImporterBinding 
    * @return null if failed (errors have been logged in parallel)
    */
   protected NodeRef getOrCreateTargetContainer(String namePath,
         ContentImporterBinding contentImporterBinding) {
      // getting company home as the root of the path :
      NodeRef companyHomeNodeRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());
      // NB. can't use store root as the root of the path, because it does not use cm:contains
      
      NodeRef currentNodeRef = companyHomeNodeRef;
      ArrayList<String> pathNames = AcpXmlUtil.stringToNamePath(namePath,
            contentImporterBinding.clientPathDelimiter); // better than StringUtils and split()
      for (String pathName : pathNames) {
         NodeRef foundOrCreatedNodeRef = namePathService.getChildByPathName(pathName, currentNodeRef, 
               contentImporterBinding.targetLocationChildAssociationTypeQName);
         
         if (foundOrCreatedNodeRef == null) {
            // if update only mode, this is an error
            if (ContentImporterConfiguration.CONTAINER_MODE_UPDATE_ONLY.equals(contentImporterBinding.containerMode)) {
               contentImporterBinding.contentImporterResultHandler.containerError(namePath, companyHomeNodeRef,
                     new Exception("Container at path " + namePath
                           + " doesn't exist and can't be created in container mode "
                           + contentImporterBinding.containerMode));
               return null;
            }
            
            // else create the container :
            UserTransaction userTransaction = null;
            try {
               long start = System.nanoTime();
               // get transaction : not getNonPropagatingUserTransaction which creates zombie nodes !!
               userTransaction = transactionService.getUserTransaction();
               userTransaction.begin();
               
               foundOrCreatedNodeRef = this.createChildFolder(currentNodeRef, pathName, contentImporterBinding);

               userTransaction.commit();
               if (!nodeService.exists(foundOrCreatedNodeRef)) {
                	// commit ends well but node not created because it did some underlaying non propagated transactions of its own
                	logger.error("ContentImporterComponent : zombie noderef " + foundOrCreatedNodeRef + " created (container " + pathName + ")");
                }
               long end = System.nanoTime();
               // success handling
               // TODO log mode
               contentImporterBinding.contentImporterResultHandler.containerSuccess(
                     foundOrCreatedNodeRef, "in " + (end - start) + "ns"); // TODO context
               
           } catch (Throwable t) {
               try {
                   if (userTransaction != null) {
                       userTransaction.rollback();
                   }
               } catch (Exception ex) {
                  logger.debug("Unknown exception while rollbacking", ex);
               }
               
               // error handling
               contentImporterBinding.contentImporterResultHandler.containerError(pathName, currentNodeRef, t);
               return null;
           }
         }
         
         // looping
         currentNodeRef = foundOrCreatedNodeRef;
      }
      return currentNodeRef;
   }


   /**
    * (namePath version)
    * Gets the existing subcontainer with the given name,
    * or create it if it doesn't exist yet.
    * TODO rights, mode, txns
    * @param parentContainerNodeRef
    * @param childContainerName
    * @param contentImporterBinding 
    * @return
    */
   protected NodeRef createChildFolder(NodeRef parentContainerNodeRef, String childContainerName,
         ContentImporterBinding contentImporterBinding) {
       // building properties : only name
       Map<QName, Serializable> childFolderProperties = new HashMap<QName, Serializable>(1);
       childFolderProperties.put(ContentModel.PROP_NAME, childContainerName);
       // creating childName from name
       QName childAssociationQName = QName.createQName(contentImporterBinding.targetLocationContainerTypeQName.getNamespaceURI(),
             QName.createValidLocalName(childContainerName)); // TODO é ??
       // creating node and association
       ChildAssociationRef childFolderAssoc = nodeService.createNode(parentContainerNodeRef,
             contentImporterBinding.targetLocationChildAssociationTypeQName, childAssociationQName,
             contentImporterBinding.targetLocationContainerTypeQName, childFolderProperties);
       return childFolderAssoc.getChildRef();
  }
   

   
   /**
    * [talendalfresco] uses custom NodeImporter
    * Perform Import via Parser
    * 
    * @param nodeRef node reference to import under
    * @param childAssocType the child association type to import under
    * @param inputStream the input stream to import from
    * @param streamHandler the content property import stream handler
    * @param binding import configuration
    * @param progress import progress
    */
   public void parserImport(NodeRef nodeRef, QName childAssocType, Reader viewReader, ImportPackageHandler streamHandler, ImporterBinding binding, ImporterProgress progress)
   {
       //ParameterCheck.mandatory("Node Reference", nodeRef);
       if (nodeRef == null) return; // [talendalfresco] fails because of previous errors (getOrCreateContainer or importNode)
       ParameterCheck.mandatory("View Reader", viewReader);
       ParameterCheck.mandatory("Stream Handler", streamHandler);
       
       Importer nodeImporter = new ContentNodeImporter(nodeRef, childAssocType, binding, streamHandler, progress); // [talendalfresco]
       try
       {
           nodeImporter.start();
           viewParser.parse(viewReader, nodeImporter);
           nodeImporter.end();
       }
       catch(RuntimeException e)
       {
    	   logger.warn("Import error", e);
           nodeImporter.error(e);
           throw e;
       }
   }

   /**
    * TODO handle modes, ex. by overriding createNodeImporterStrategy
    * @author mdutoo
    *
    */
   protected class ContentNodeImporter extends NodeImporter {
      /** to ease non-casted access to our contentImporterBinding */
      protected ContentImporterBinding contentImporterBinding;
      /** to ease access to our result handler */
      protected ContentImporterResultHandler contentImporterResultHandler;

      public ContentImporterResultHandler getContentImporterResultHandler() {
			return contentImporterResultHandler;
		}

		protected ContentNodeImporter(NodeRef rootRef, QName rootAssocType,
            ImporterBinding binding, ImportPackageHandler streamHandler,
            ImporterProgress progress) {
         super(rootRef, rootAssocType, binding, streamHandler, progress);
         this.contentImporterBinding = (ContentImporterBinding) binding;
         this.contentImporterResultHandler = this.contentImporterBinding.contentImporterResultHandler;
         
         // specific import strategy allowing to handle modes :
         this.importStrategy = new ContainerDocumentCreationModeWrappingImportStrategy(
               new CreateNewNodeImporterStrategy(false), this.updateStrategy);
      }

      /**
       * Need to override to handle node import failure
       */
      public NodeRef importNode(ImportNode context) {
         if (context.getParentContext().getParentRef() == null) {
            // import of parent already failed, so can't import this node
            return null;
         }
         
         NodeRef importedNodeRef = null;
         UserTransaction userTransaction = null;
         try {
             // import it in a transaction
             long start = System.nanoTime();
               // get transaction : not getNonPropagatingUserTransaction which creates zombie nodes !!
             userTransaction = transactionService.getUserTransaction();
             userTransaction.begin();
             
             importedNodeRef = super.importNode(context);
             
             userTransaction.commit();
             if (!nodeService.exists(importedNodeRef)) {
                	// commit ends well but node not created because it did some underlaying non propagated transactions of its own
             	logger.error("ContentImporterComponent : zombie noderef " + importedNodeRef + " created (" + context.getProperties() + ")");
             }
             long end = System.nanoTime();
             
             // finally success handling
             if (importedNodeRef != null) {
                // TODO log mode
                this.contentImporterResultHandler.nodeSuccess(context, importedNodeRef, "in " + (end - start) + "ns");
             } // else { // import of parent failed beforehand
             //   this.contentImporterResultHandler.nodeError(context, new Exception("Failed because parent doesn't exist"));
             //}
             
         } catch (Throwable t) {
            // node import has failed
            // let's rollback, log errors and skip over to the next node
             try {
                 if (userTransaction != null) {
                     userTransaction.rollback();
                 }
             } catch (Exception ex) {
                logger.debug("Unknown exception while rollbacking", ex);
             }
             
             // error handling
             this.contentImporterResultHandler.nodeError(context, t);
             
             // NB. good error handling besides here : in ViewParser.importNode(), non created node is not registered as importId OK !
         }
         
         return importedNodeRef;
      }
      
      /**
       * [talendalfresco] impl'd on the super method, but adds before
       * guessed mimetype if none yet.
       * NB. adds an unnecessary bindPlaceHolder(), but not an issue
       */
      protected void importContent(NodeRef nodeRef, QName propertyName, String importContentData) {
         // bind import content data description
         // [talendalfresco] duplicated with super but not an issue
          importContentData = bindPlaceHolder(importContentData, binding);
          if (importContentData != null && importContentData.length() > 0) {
              // guessing mimetype if none yet :
              // NB. done before conversion to ContentData, else conversion fails on missing mimetype
              ContentData contentDataWithGuessedMimetype =
                 ContentDataUtil.createContentPropertyWithGuessedMimetype(importContentData, mimetypeService);
              super.importContent(nodeRef, propertyName, contentDataWithGuessedMimetype.toString());
          }
      }

      
      /**
       * Handles modes
       * @author mdutoo
       *
       */
      protected class ContainerDocumentCreationModeWrappingImportStrategy implements NodeImporterStrategy {
         /** create new node, explode (because of name constraint) if one already exists with the same name */
         protected NodeImporterStrategy createImportStrategy;
         /** if a node with the context's UUID exists, update it, else do the create strategy */
         protected NodeImporterStrategy createOrUpdateImportStrategy;

         /**
          * 
          * @param createImportStrategy create new node, explode
          * (because of name constraint) if one already exists with the same name
          * @param createOrUpdateImportStrategy if a node with the
          * context's UUID exists, update it, else do the create strategy
          */
         public ContainerDocumentCreationModeWrappingImportStrategy(
               CreateNewNodeImporterStrategy createImportStrategy,
               UpdateExistingNodeImporterStrategy createOrUpdateImportStrategy) {
            this.createImportStrategy = createImportStrategy;
            this.createOrUpdateImportStrategy = createOrUpdateImportStrategy;
         }

         public NodeRef importNode(ImportNode context) {
            NodeImporterStrategy importStrategy; // to be used
            
            String childName = context.getChildName();
            
            if (ContentImporterConfiguration.CHILD_NAME_CONTAINER.equals(childName)) {
               // container mode
               String name = (String) context.getProperties().get(ContentModel.PROP_NAME);
               NodeRef existingNodeRef = namePathService.getChildByPathName(name,
                     context.getParentContext().getParentRef(),
                     ContentModel.ASSOC_CONTAINS);
               
               if (ContentImporterConfiguration.CONTAINER_MODE_CREATE_OR_UPDATE.equals(contentImporterBinding.containerMode)) {
                  if (existingNodeRef != null) {
                     ((NodeContext) context).setUUID(existingNodeRef.getId());
                  }
                  // if a node with the context's UUID exists, update it, else do the create strategy
                  importStrategy = this.createOrUpdateImportStrategy;
                  
               } else { // ContentImporterConfiguration.CONTAINER_MODE_UPDATE_ONLY
                  if (existingNodeRef != null) {
                     ((NodeContext) context).setUUID(existingNodeRef.getId());
                  } else {
                     throw new AlfrescoRuntimeException("Trying to import container in mode "
                           + ContentImporterConfiguration.CONTAINER_MODE_UPDATE_ONLY
                           + ", but node does not exists with name " + name);
                  }
                  importStrategy = this.createOrUpdateImportStrategy;
               }
               
               
            } else if (ContentImporterConfiguration.CHILD_NAME_DOCUMENT.equals(childName)) {
               // document mode
               String name = (String) context.getProperties().get(ContentModel.PROP_NAME);
               NodeRef existingNodeRef = namePathService.getChildByPathName(name,
                     context.getParentContext().getParentRef(),
                     ContentModel.ASSOC_CONTAINS);
               
               if (ContentImporterConfiguration.DOCUMENT_MODE_CREATE_ONLY.equals(contentImporterBinding.documentMode)) {
                  if (existingNodeRef != null) {
                     throw new AlfrescoRuntimeException("Trying to import document in mode "
                           + ContentImporterConfiguration.DOCUMENT_MODE_CREATE_ONLY
                           + ", but node already exists with name " + name + " and noderef " + existingNodeRef);
                  }
                  // create new node, explode (because of name constraint) if one already exists with the same name
                  importStrategy = this.createImportStrategy;
                  
               } else { // if (ContentImporterConfiguration.DOCUMENT_MODE_CREATE_OR_UPDATE
                  if (existingNodeRef != null) {
                     ((NodeContext) context).setUUID(existingNodeRef.getId());
                  }
                  // if a node with the context's UUID exists, update it, else do the create strategy
                  importStrategy = this.createOrUpdateImportStrategy;
               }
               
            
            } else {
               // old behaviour is preserved
               importStrategy = this.createImportStrategy;
            }
            
            return importStrategy.importNode(context);
         }
         
      }
      
      
      /**
       * [talendalfresco] overriden to transactionalize reference resolving
       * (non-Javadoc)
       * @see org.alfresco.repo.importer.Importer#end()
       */
      @SuppressWarnings("unchecked")
      public void end()
      {
          // Bind all node references to destination space
          for (ImportedNodeRef importedRef : nodeRefs)
          {
             // [talendalfresco] starting transaction
             UserTransaction userTransaction = null;
             try {
                long start = System.nanoTime();
                // get transaction : not getNonPropagatingUserTransaction which creates zombie nodes !!
                userTransaction = transactionService.getUserTransaction();
                userTransaction.begin();
                

              Serializable refProperty = null;
              if (importedRef.value != null)
              {
                  if (importedRef.value instanceof Collection)
                  {
                      Collection<String> unresolvedRefs = (Collection<String>)importedRef.value;
                      List<NodeRef> resolvedRefs = new ArrayList<NodeRef>(unresolvedRefs.size());
                      for (String unresolvedRef : unresolvedRefs)
                      {
                          if (unresolvedRef != null)
                          {
                              NodeRef nodeRef = resolveImportedNodeRef(importedRef.context.getNodeRef(), unresolvedRef);
                              // TODO: Provide a better mechanism for invalid references? e.g. report warning
                              if (nodeRef != null)
                              {
                                  resolvedRefs.add(nodeRef);
                              }
                          }
                      }
                      refProperty = (Serializable)resolvedRefs;
                  }
                  else
                  {
                      refProperty = resolveImportedNodeRef(importedRef.context.getNodeRef(), (String)importedRef.value);
                      // TODO: Provide a better mechanism for invalid references? e.g. report warning
                  }
              }
              
              // Set node reference on source node
              Set<QName> disabledBehaviours = getDisabledBehaviours(importedRef.context);
              try
              {
                  for (QName disabledBehaviour: disabledBehaviours)
                  {
                      behaviourFilter.disableBehaviour(importedRef.context.getNodeRef(), disabledBehaviour);
                  }
                  nodeService.setProperty(importedRef.context.getNodeRef(), importedRef.property, refProperty);
                  if (progress != null)
                  {
                      progress.propertySet(importedRef.context.getNodeRef(), importedRef.property, refProperty);
                  }
              }
              finally
              {
                  behaviourFilter.enableBehaviours(importedRef.context.getNodeRef());
              }


              // [talendalfresco] ending transaction
              userTransaction.commit();
              long end = System.nanoTime();
              // success handling
              // TODO log mode
              contentImporterBinding.contentImporterResultHandler.referenceSuccess(
                    importedRef.context.getNodeRef(), importedRef.property, importedRef.value, "in " + (end - start) + "ns");
              
           } catch (Throwable t) {
              try {
                  if (userTransaction != null) {
                      userTransaction.rollback();
                  }
              } catch (Exception ex) {
                 logger.debug("Unknown exception while rollbacking", ex);
              }
              
              // error handling
              contentImporterBinding.contentImporterResultHandler.referenceError(
                    importedRef.context.getNodeRef(), importedRef.property, importedRef.value, t);
           }
          }
          
          
          reportCompleted();
      }
      
   }
   

   public void setTransactionService(TransactionService transactionService) {
      this.transactionService = transactionService;
   }

   public void setMimetypeService(MimetypeService mimetypeService) {
      this.mimetypeService = mimetypeService;
   }

   public void setNamePathService(NamePathService namePathService) {
      this.namePathService = namePathService;
   }
   
}
