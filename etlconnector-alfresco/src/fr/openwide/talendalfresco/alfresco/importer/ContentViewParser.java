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

import java.io.IOException;

import javax.transaction.UserTransaction;

import org.alfresco.repo.importer.view.NodeContext;
import org.alfresco.repo.importer.view.ParentContext;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import fr.openwide.talendalfresco.alfresco.importer.ContentImporterComponent.ContentNodeImporter;

/**
 * Parses content (cm:object) ACP XML.
 * Extended features beyond the parent's :
 *    * catches and logs association reference resolving error
 *    
 * @author Marc Dutoo - Open Wide SA
 */
public class ContentViewParser extends ViewParserBase {
	
	protected TransactionService transactionService;

	public ContentViewParser() {
      super();
   }

   @Override
   protected void processStartReference(XmlPullParser xpp, QName refName, ParserContext parserContext) throws XmlPullParserException, IOException {
      ParentContext parentContext = (ParentContext)parserContext.elementStack.peek();
      ContentImporterResultHandler contentImporterResultHandler =
         ((ContentImporterComponent.ContentNodeImporter) parentContext.getImporter()).contentImporterResultHandler;
      try {
         super.processStartReference(xpp, refName, parserContext);
         contentImporterResultHandler.referenceSuccess(parentContext.getParentRef(), parentContext.getAssocType(), null, "");
      } catch (Throwable t) {
         contentImporterResultHandler.referenceError(parentContext.getParentRef(), parentContext.getAssocType(), null, t);
      }
   }

	@Override
	protected void processEndType(ParserContext parserContext, NodeContext node) {
      importNode(parserContext, node); // copied from base
      
      // transactionally apply behaviour & rules :
      NodeRef nodeRef = node.getNodeRef();
      if (nodeRef == null) {
         // import of node already failed, so can't apply its behaviour & rules
         return;
      }
      UserTransaction userTransaction = null;
      try {
          // import it in a transaction
          long start = System.nanoTime();
            // get transaction : not getNonPropagatingUserTransaction which creates zombie nodes !!
          userTransaction = transactionService.getUserTransaction();
          userTransaction.begin();

          node.getImporter().childrenImported(nodeRef); // behaviour & rules ; copied from base
          
          userTransaction.commit();
          long end = System.nanoTime();
          
          // success not reported for behaviour & rules, only errors 
          //((ContentNodeImporter) node.getImporter()).getContentImporterResultHandler().nodeSuccess(node, nodeRef, "behaviour & rules in " + (end - start) + "ns");
          
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
          ((ContentNodeImporter) node.getImporter()).getContentImporterResultHandler().nodeError(node,
         		 new Exception("behaviour & rules : " + t.getMessage(), t));
          
          // NB. good error handling besides here : in ViewParser.importNode(), non created node is not registered as importId OK !
      }
	}

   public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

}
