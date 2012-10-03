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

package fr.openwide.talendalfresco.acpxml;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Helper for using ACP XML to import content.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class AcpXmlUtil {

   /**
    * Returns the list of non empty path names in the given namePath.
    * Better than split() (may return "" in the middle) and Spring StringUtils
    * (may return "" anywhere)
    * @param targetLocation
    * @param pathDelimiter
    * @return
    */
   public final static ArrayList<String> stringToNamePath(String namePath, String pathDelimiter) {
      StringTokenizer stok = new StringTokenizer(namePath, pathDelimiter);
      ArrayList<String> pathNames = new ArrayList<String>();
      while (stok.hasMoreTokens()) {
         String pathName = stok.nextToken();
         if (pathName.length() == 0) {
            // happens when //
            continue;
         }
         pathNames.add(pathName);
      }
      return pathNames;
   }

   /**
    * Returns ex. from /guest/my_file.pdf , ${/guest/my_file.pdf}
    * @param value
    * @return
    */
   public static String toNamePathRef(String namePath) {
      StringBuffer sbuf = new StringBuffer();
      sbuf.append("${");
      sbuf.append(namePath);
      sbuf.append("}");
      return sbuf.toString();
   }

   /**
    * Converts from an Object to a String List
    * 
    * @param values
    * @return
    * @throws AcpXmlException if a value is not a String (null not allowed)
    */
   public static List<String> toStringList(List<Object> values) throws AcpXmlException {
      ArrayList<String> stringValues = new ArrayList<String>(values.size());
      for (Object value : (List<Object>) values) {
         if (value instanceof String) {
            stringValues.add((String) value);
            continue;
         }
         throw new AcpXmlException("Value " + value + " of list " + values + " should be of type String");
      }
      return stringValues;
   }

   
}
