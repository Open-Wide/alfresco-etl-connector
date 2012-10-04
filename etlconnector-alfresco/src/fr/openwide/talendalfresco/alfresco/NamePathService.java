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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * NamePath service. Works using cm:name.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public interface NamePathService {

   NodeRef resolveNamePath(String namePath);
   /**
    * Resolves the given namePath (under company home), ex. /guest/my_file.pdf
    * @param namePath
    * @param pathDelimiter
    * @return
    */
   NodeRef resolveNamePath(String namePath, String pathDelimiter);
   /**
    * Uses Lucene (rather than db) to avoid loading a distant subtree only to get the referenced node.
    * Single char pathNames are resolved using db (lucene can't) with a cm:contains child association.
    */
   NodeRef resolveNamePath(String namePath, String pathDelimiter, NodeRef rootNodeRef);
   /**
    * Uses Lucene (rather than db) to avoid loading a distant subtree only to get the referenced node.
    * @param targetLocationChildAssociationTypeQName used to resolve single char pathNames
    * (lucene can't) with db
    */
   NodeRef resolveNamePath(String namePath, String pathDelimiter, NodeRef rootNodeRef,
         QName targetLocationChildAssociationTypeQName);

   /**
    * Uses Lucene (rather than db) to avoid loading a distant subtree only to get the referenced node.
    * @param pathName must be at least 2 chars long (lucene doesn't index single chars,
    * in this case use the other method or nodeService)
    * @param parentNodeRef
    * @return
    */
   NodeRef getChildByPathName(String pathName, NodeRef parentNodeRef);
   /**
    * Uses db for 1 char length pathName and lucene for others
    * @param pathName not null
    * @param parentNodeRef
    * @return
    */
   NodeRef getChildByPathName(String pathName, NodeRef currentNodeRef, QName targetLocationChildAssociationTypeQName);
   
   /**
    * Returns the namePath, starts below the company home ("Alfresco").
    * @param importNodeContext
    * @return null if null or non existing noderef
    * @return
    */
   String getNamePath(NodeRef nodeRef, String pathDelimiter);

}
