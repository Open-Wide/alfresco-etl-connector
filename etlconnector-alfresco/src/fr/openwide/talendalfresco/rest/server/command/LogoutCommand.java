/*
 * Copyright (C) 2008 Open Wide SA
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
 * More information at http://forge.alfresco.com/projects/etlconnector/
 *
 */
package fr.openwide.talendalfresco.rest.server.command;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLStreamException;

import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.repository.User;

import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.server.processor.RestCommandBase;


/**
 * TODO remove command or FacesContext
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class LogoutCommand extends RestCommandBase {
   
   private static final String[] PROPERTIES = new String[] {  };
   public String[] getPropertyNames() {
      return PROPERTIES;
   }

   public LogoutCommand() {
      super(RestConstants.CMD_LOGOUT);
   }

   
   public void executeImpl() throws XMLStreamException {
        // Invalidate Session for this user.
        if (Application.inPortalServer() == false) {
           // This causes the sessionDestroyed() event to be processed by ContextListener
           // which is responsible for invalidating the ticket and clearing the security context
           HttpSession session = httpRequest.getSession(false);
           session.invalidate(); // session exists, since this command requires login
           
        } else if (FacesContext.getCurrentInstance() != null) {
           // explicit security cleanup so the HTTP session will still be alive
           Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
           
           User user = (User) sessionMap.get(AuthenticationHelper.AUTHENTICATION_USER);
           if (user != null) {
              // invalidate ticket and clear the Security context for this thread
              authenticationService.invalidateTicket(user.getTicket());
              authenticationService.clearCurrentSecurityContext();
           }
           
           // remove all objects from our session by hand
           // we do this as invalidating the Portal session would invalidate all other portlets!
           for (Object key : sessionMap.keySet()) {
              sessionMap.remove(key);
           }
           
           // set language to last used
           String language = "fr_FR"; //TODO better
           if (language != null && language.length() != 0) {
              Application.setLanguage(FacesContext.getCurrentInstance(), language);
           }
           
           // Request that the username cookie state is removed - this is not
           // possible from JSF - so instead we setup a session variable
           // which will be detected by the login.jsp/Portlet as appropriate.
           sessionMap.put(AuthenticationHelper.SESSION_INVALIDATED, true);
        }
   }

}
