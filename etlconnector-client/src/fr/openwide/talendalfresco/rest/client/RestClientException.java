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

package fr.openwide.talendalfresco.rest.client;


/**
 * Client-side exception for REST errors.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestClientException extends Exception {
   private static final long serialVersionUID = 6885009883499314382L;

   public RestClientException(String message, Throwable cause) {
      super(message, cause);
   }

   public RestClientException(String message) {
      super(message);
   }

}
