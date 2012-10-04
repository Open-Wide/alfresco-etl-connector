/*
 * Copyright (C) 2008-2012 Open Wide SA
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
 * More information at http://knowledge.openwide.fr/bin/view/Main/AlfrescoETLConnector/
 */

package fr.openwide.talendalfresco.rest.client;

import org.apache.commons.httpclient.NameValuePair;

import fr.openwide.talendalfresco.rest.RestConstants;


/**
 * Login command
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class ClientLoginCommand extends ClientCommandBase {

   public ClientLoginCommand(String username, String password) {
      super(RestConstants.CMD_LOGIN, HttpMType.GET);
      this.setParam(new NameValuePair("username", username));
      this.setParam(new NameValuePair("password", password));
      this.defaultResultElementSet.add(RestConstants.RES_LOGIN_TICKET);
   }

   public String getTicket() {
      return elementValueMap.get(RestConstants.RES_LOGIN_TICKET);
   }
   
}
