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

import org.alfresco.repo.importer.view.ParentContext;
import org.alfresco.service.namespace.QName;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Parses content (cm:object) ACP XML.
 * Extended features beyond the parent's :
 *    * catches and logs association reference resolving error
 *    
 * @author Marc Dutoo - Open Wide SA
 */
public class ContentViewParser extends ViewParserBase {

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

}
