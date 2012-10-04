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

import java.io.Serializable;

import org.alfresco.repo.importer.ImportNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Handles and may log node import results
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public interface ContentImporterResultHandler {

   /**
    * 
    * @param context
    * @param t
    */
   void nodeError(ImportNode context, Throwable t);
   void nodeError(String nodeName, NodeRef parentNodeRef, String typeInfo, Throwable t);
   /**
    * NB. Requires NodeRef an ImportNode does not provide the NodeRef
    * (even in the success case).
    * @param context
    * @param nodeRef
    * @param msg
    */
   void nodeSuccess(ImportNode context, NodeRef nodeRef, String msg);
   void nodeSuccess(NodeRef nodeRef, String typeInfo, String msg);
   /**
    * Specific method for tracing error when creating containers
    * participating to the target location base ; allows to trace info got
    * from the import conf.
    * @param name
    * @param parent
    * @param t
    */
   void containerError(String name, NodeRef parent, Throwable t);
   /**
    * Specific method for tracing success when creating containers
    * participating to the target location base ; allows to trace info got
    * from the import conf.
    * @param nodeRef
    * @param msg
    */
   void containerSuccess(NodeRef nodeRef, String msg);

   /**
    * Successes resolving references are not traced
    */
   void referenceSuccess(NodeRef nodeRef, QName propertyOrAssociation,
         Serializable value, String msg);
   /**
    * Logs it as an error with a subpath of the base node
    */
   void referenceError(NodeRef context, QName propertyOrAssociation,
         Serializable value, Throwable t);
   
}
