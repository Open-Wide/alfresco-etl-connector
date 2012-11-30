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
package fr.openwide.talendalfresco.rest.server.command;

import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.server.RestServerHelper;
import fr.openwide.talendalfresco.rest.server.processor.RestCommandBase;
import fr.openwide.talendalfresco.rest.server.processor.RestCommandResult;

/**
 * Login command
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class LoginCommand extends RestCommandBase {

   // static logger
   private static Log logger = LogFactory.getLog(LoginCommand.class);
   
   private static final String[] PROPERTIES = new String[] {
      RestConstants.PROP_LOGIN_USERNAME, RestConstants.PROP_LOGIN_PASSWORD, RestConstants.PROP_LOGIN_TICKET };
   
   public String[] getPropertyNames() {
      return PROPERTIES;
   }
   

   public LoginCommand() {
      super(RestConstants.CMD_LOGIN);
   }
   

   /**
    * 
    * @return
    */
   protected boolean validateArgumentsImpl() {
      try {
         RestServerHelper.validateUsername(null, args.get(RestConstants.PROP_LOGIN_USERNAME));
         RestServerHelper.validatePassword(null, args.get(RestConstants.PROP_LOGIN_PASSWORD));
         return true;
      } catch (Exception ex) { // RestRuntimeException
         String msg = "Bad parameters for command "
            + getCommandName() + " which requires" + getPropertyNames()
            + "but got " + args + " : " + ex.getMessage();
         xmlResult.setError(RestCommandResult.CODE_ERROR_WRONG_PARAMETERS, msg, ex);
         logger.error(msg);
         return false;
      }
   }

   public void executeImpl() {
      String username = args.get(RestConstants.PROP_LOGIN_USERNAME);
      String password = args.get(RestConstants.PROP_LOGIN_PASSWORD);
      String ticket = args.get(RestConstants.PROP_LOGIN_TICKET);

      try {
         User user = this.authenticate(username, password, ticket);
         if (user == null) {
            // error whose message has been written in xmlResult
            return;
         }
         
         xmlResult.writeTag("jsessionid", httpRequest.getSession(true).getId());
         xmlResult.writeTag("ticket", user.getTicket());
         xmlResult.writeTag("personNodeRef", user.getPerson().toString());
         xmlResult.writeTag("homeNodeRef", "workspace://SpacesStore/" + user.getHomeSpaceId());
         xmlResult.writeTag("login", user.getUserName());

         xmlResult.writeTag("relativelServerUrlPrefix", httpRequest.getContextPath());
         xmlResult.writeTag("fullServerUrlPrefix", httpRequest.getScheme()
               + "://" + httpRequest.getServerName() + ":" + httpRequest.getServerPort()
               + httpRequest.getContextPath());
         //xmlResult.setMessage("Successfully authenticated"); // opt
         
      } catch (Exception ex) {
         // error while logging in or writing alfresco node XML
         String msg = "Error while authenticating " + username;
         logger.error(msg, ex);
         xmlResult.setError(RestConstants.CODE_ERROR_AUTH, msg, ex);
      }
   }
   

   private User authenticate(String username, String password, String ticket) {
      
      // case of existing session user : getting alfresco ticket
      User existingSessionUser = null;
      HttpSession session = httpRequest.getSession(false);
      if (session != null) {
         existingSessionUser = (User)session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
         if (existingSessionUser != null) {
            String existingSessionTicket = existingSessionUser.getTicket();
            // alternatives :
            // 1. using alfresco ticket rather than sso ticket to speed up things
            // NB. this means that before logging in a different user an explicit logout must be done
            // 2. using sso ticket rather than alfresco one
            // this requires never to give the ticket but when we want to relog, which is bothersome
            if (existingSessionTicket != null) {
               ticket = existingSessionTicket;
            }
         }
      }
      
      UserTransaction tx = null;
      try {
         // Authenticate via the authentication service, then save the details of user in an object
         // in the session - this is used by the servlet filter etc. on each page to check for login
         if (username != null && password != null) {
            // authentication using login (alfresco or sso), since user/pwd params (even empty ones) have been supplied
            // validation :
            RestServerHelper.validateUsername(session, username);
            RestServerHelper.validatePassword(session, password);
            // login :
            authenticationService.authenticate(username, password.toCharArray());
            
         } else if (ticket != null && ticket.length() != 0) {
            // authentication using ticket (alfresco or sso), since non empty ticket has been supplied
            authenticationService.validate(ticket);
            
         } else {
            xmlResult.setError(RestCommandResult.CODE_ERROR_AUTH_MISSING,
                  RestServerHelper.getMessage(session, RestServerHelper.MSG_ERROR_MISSING)
                  + " : " + username, null);
            return null;
         }
         
         // Set the user name as stored by the back end 
         username = authenticationService.getCurrentUserName();
         
         if (existingSessionUser != null && existingSessionUser.getUserName().equals(username)) {
            // user was already logged in, nothing else to do
            return existingSessionUser;
         }
      
         
         // now setting up logged in user elements
         // using non propagated tx because already inside a tx (commandServlet)
         tx = transactionService.getNonPropagatingUserTransaction();
         tx.begin();
         
         // remove the session invalidated flag (used to remove last username cookie by AuthenticationFilter)
         if (session != null) {
            session.removeAttribute(AuthenticationHelper.SESSION_INVALIDATED);
         }
         
         // setup User object and Home space ID
         User user = new User(username,
               authenticationService.getCurrentTicket(),
               personService.getPerson(username));
         
         NodeRef homeSpaceRef = (NodeRef) nodeService.getProperty(personService.getPerson(username),
               ContentModel.PROP_HOMEFOLDER);
         
         // check that the home space node exists - else user cannot login
         if (nodeService.exists(homeSpaceRef) == false) {
            throw new InvalidNodeRefException(homeSpaceRef);
         }
         user.setHomeSpaceId(homeSpaceRef.getId());
         
         tx.commit();
         tx = null; // clear this so we know not to rollback 
         
         // put the User object in the Session - the authentication servlet will then allow
         // the app to continue without redirecting to the login page
         if (session == null) {
            session = httpRequest.getSession(true); // creating session if none yet
         }
         session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);

         // Set the current locale for Alfresco web app. NB. session exists now.
         I18NUtil.setLocale(Application.getLanguage(session, true));
         
         return user;
         
      } catch (AuthenticationException ae) {
         xmlResult.setError(RestCommandResult.CODE_ERROR_AUTH_UNKNOWN_USER,
               RestServerHelper.getMessage(session, RestServerHelper.MSG_ERROR_UNKNOWN_USER)
               + " : " + username, ae);
         
      } catch (InvalidNodeRefException inre) {
         xmlResult.setError(RestCommandResult.CODE_ERROR_AUTH_UNKNOWN_USER,
               RestServerHelper.getMessage(session, Repository.ERROR_NOHOME)
               + " : " + inre.getNodeRef().getId() + " (" + username + ")", inre);
         
      } catch (Throwable e) {
         // Some other kind of serious failure
         xmlResult.setError("Unknown technical error when authenticating user "
               + username, null);
         
      } finally {
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      return null;
   }

}
