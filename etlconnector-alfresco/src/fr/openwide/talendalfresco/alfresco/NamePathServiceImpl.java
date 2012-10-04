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

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.springframework.util.StringUtils;


/**
 * NamePathService impl
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class NamePathServiceImpl implements NamePathService {
   
   protected NodeService nodeService;
   protected SearchService searchService;
   
   protected boolean alwaysUseDb = true;


	public NamePathServiceImpl() {
      
   }

	/**
	 * Resolves given namePath using server File.separator
	 * @see resolveNamePath(String, String)
	 * @see getChildByPathName(String, NodeRef, QName)
	 */
   public NodeRef resolveNamePath(String namePath) {
      return resolveNamePath(namePath, File.pathSeparator);
   }
   /**
    * Resolves given namePath from root
    * @see resolveNamePath(String, String, NodeRef)
    */
   public NodeRef resolveNamePath(String namePath, String pathDelimiter) {
      NodeRef companyHomeNodeRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());
      return resolveNamePath(namePath, pathDelimiter, companyHomeNodeRef);
   }
   /**
    * Resolves given namePath using default cm:contains association
    * @see resolveNamePath(String, String, NodeRef, QName)
	 * @see getChildByPathName(String, NodeRef, QName)
    */
   public NodeRef resolveNamePath(String namePath, String pathDelimiter, NodeRef rootNodeRef) {
      return resolveNamePath(namePath, pathDelimiter, rootNodeRef, ContentModel.ASSOC_CONTAINS);
   }
   /**
    * Resolves given namePath
    * @param targetLocationChildAssociationTypeQName
	 * @see getChildByPathName(String, NodeRef, QName)
    */
   public NodeRef resolveNamePath(String namePath, String pathDelimiter, NodeRef rootNodeRef,
         QName targetLocationChildAssociationTypeQName) {
      StringTokenizer stok = new StringTokenizer(namePath, pathDelimiter);
      NodeRef currentNodeRef = rootNodeRef;
      while (stok.hasMoreTokens() && currentNodeRef != null) {
         String pathName = stok.nextToken();
         currentNodeRef = getChildByPathName(pathName, currentNodeRef, targetLocationChildAssociationTypeQName);
      }
      return currentNodeRef;
   }

   /**
    * @obsolete rather use db (nodeService), else custom lucene analyzers make it fail, e.g.
    * like those for French locale in 3.2 which remove ending "s".
    * Uses Lucene (rather than db) to avoid loading a distant subtree only to get the referenced node.
    * @param pathName must be not null and at least 2 chars long (lucene doesn't index single chars,
    * in this case use the other method or nodeService)
    * @param parentNodeRef
    * @return
    */
   public NodeRef getChildByPathName(String pathName, NodeRef parentNodeRef) {
      StringBuffer sbuf = new StringBuffer();
      sbuf.append("@cm\\:name:\"");
      sbuf.append(pathName);
      sbuf.append("\" AND ");
      sbuf.append("PRIMARYPARENT:\"");
      sbuf.append(parentNodeRef);
      sbuf.append("\"");
      ResultSet rset = searchService.query(parentNodeRef.getStoreRef(), SearchService.LANGUAGE_LUCENE, sbuf.toString());
      List<NodeRef> foundNodeRefs = (rset != null) ? rset.getNodeRefs() : null;
      if (foundNodeRefs == null || foundNodeRefs.isEmpty()) {
         // not found !
         return null;
      }
      NodeRef foundNodeRef = rset.getNodeRef(0);
      rset.close();
      return foundNodeRef;
   }

   /**
    * If not alwaysUseDb (which is not advised since custom lucene analyzers may make it fail,
    * e.g. like those for French locale in 3.2 which remove ending "s"), uses lucene when char
    * length pathName > 1 (else lucene can't) as an optimization.
    * @param pathName not null
    * @param parentNodeRef
    * @return
    */
   public NodeRef getChildByPathName(String pathName, NodeRef currentNodeRef,
         QName targetLocationChildAssociationTypeQName) {
   	if (alwaysUseDb) {
         return nodeService.getChildByName(currentNodeRef,
               targetLocationChildAssociationTypeQName, pathName);
   	}
      switch (pathName.length()) {
      case 0 :
         // happens when //
         return null;
      case 1 :
         // single char : use db
         return nodeService.getChildByName(currentNodeRef,
               targetLocationChildAssociationTypeQName, pathName);
      }
      return getChildByPathName(pathName, currentNodeRef);
   }
   
   
   /**
    * Returns the namePath, starts below the company home ("Alfresco").
    * Impl : could use a cache for better perfs, but Alfresco props are already cached (EHCache),
    * so for now it's ok like this.
    * @param importNodeContext
    * @return null if null or non existing noderef
    */
   public String getNamePath(NodeRef nodeRef, String pathDelimiter) {
      if (nodeRef == null || !nodeService.exists(nodeRef)) {
         return null;
      }
      java.util.ArrayList<String> pathNames = new java.util.ArrayList<String>();
      for (NodeRef currentParentRef = nodeRef; currentParentRef != null;
            currentParentRef = nodeService.getPrimaryParent(currentParentRef).getParentRef()) {
         String parentName = (String) nodeService.getProperty(currentParentRef, ContentModel.PROP_NAME);
         pathNames.add(0, parentName);
      }
      pathNames.remove(0); // remove root node
      pathNames.remove(0); // remove company node "Alfresco"
      return pathDelimiter + StringUtils.collectionToDelimitedString(pathNames, pathDelimiter);
   }


   public void setNodeService(NodeService nodeService) {
      this.nodeService = nodeService;
   }
   
   public void setSearchService(SearchService searchService) {
      this.searchService = searchService;
   }
   
   public void setAlwaysUseDb(boolean alwaysUseDb) {
		this.alwaysUseDb = alwaysUseDb;
	}
   
}
