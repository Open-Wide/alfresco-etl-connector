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
package fr.openwide.talendalfresco.alfresco.util;

import java.util.Locale;
import java.util.StringTokenizer;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * ContentData util
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class ContentDataUtil {

   /**
    * [talendalfresco] In comparison to ContentData.createContentProperty(),
    * if there is no mimetype it guesses it using the mimetypeService, therefore
    * not exploding if no mimetype.
    * 
    * Construct a content property from a string
    * 
    * @param contentPropertyStr the string representing the content details
    * @return Returns a bean version of the string
    */
   public static ContentData createContentPropertyWithGuessedMimetype(String contentPropertyStr,
         MimetypeService mimetypeService)
   {
       String contentUrl = null;
       String mimetype = null;
       long size = 0L;
       String encoding = null;
       Locale locale = null;
       // now parse the string
       StringTokenizer tokenizer = new StringTokenizer(contentPropertyStr, "|");
       while (tokenizer.hasMoreTokens())
       {
           String token = tokenizer.nextToken();
           if (token.startsWith("contentUrl="))
           {
               contentUrl = token.substring(11);
               if (contentUrl.length() == 0)
               {
                   contentUrl = null;
               }
           }
           else if (token.startsWith("mimetype="))
           {
               mimetype = token.substring(9);
               if (mimetype.length() == 0)
               {
                   mimetype = null;
               }
           }
           else if (token.startsWith("size="))
           {
               String sizeStr = token.substring(5);
               if (sizeStr.length() > 0)
               {
                   size = Long.parseLong(sizeStr);
               }
           }
           else if (token.startsWith("encoding="))
           {
               encoding = token.substring(9);
               if (encoding.length() == 0)
               {
                   encoding = null;
               }
           }
           else if (token.startsWith("locale="))
           {
               String localeStr = token.substring(7);
               if (localeStr.length() > 0)
               {
                   locale = I18NUtil.parseLocale(localeStr);
               }
           }
       }

       // [talendalfresco] if no mimetype, let's guess it
       if (mimetype == null) {
          mimetype = mimetypeService.guessMimetype(contentUrl);
       }
       
       ContentData property = new ContentData(contentUrl, mimetype, size, encoding, locale);
       // done
       return property;
   }
}
