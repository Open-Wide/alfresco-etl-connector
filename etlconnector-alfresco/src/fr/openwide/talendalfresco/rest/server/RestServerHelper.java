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
package fr.openwide.talendalfresco.rest.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.web.app.Application;
import org.alfresco.web.app.ResourceBundleWrapper;

import fr.openwide.talendalfresco.rest.server.processor.RestCommandResult;


/**
 * REST helper
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestServerHelper {

   /**
    * Default REST request and response encoding is ISO-8859-1
    * because tomcat uses it by default, and changing it there
    * would impact the whole webapp.
    * 
    * Used in RestCommandBase for all but request content, which is in ImportCommand.
    */
   public static final String DEFAULT_REST_ENCODING = "ISO-8859-1";

   /** I18N messages */
   public static final String MSG_ERROR_MISSING = "error_login_missing";
   public static final String MSG_ERROR_UNKNOWN_USER = "error_login_user";
   //public static final String MSG_USERNAME_CHARS = "login_err_username_chars";
   public static final String MSG_USERNAME_LENGTH = "login_err_username_length";
   //public static final String MSG_PASSWORD_CHARS = "login_err_password_chars";
   public static final String MSG_PASSWORD_LENGTH = "login_err_password_length";
   public static final String MSG_USER_ERR = "user_err_user_name";
   

   /**
    * Validate password field data is acceptable
 * @param session 
 * @param pass 
 * @throws RestAuthenticationException 
    */
   public static void validatePassword(HttpSession session, String pass) throws RestRuntimeException
   {
      if (pass == null || pass.length() < 3 || pass.length() > 32)
      {
         String err = MessageFormat.format(RestServerHelper.getMessage(session, MSG_PASSWORD_LENGTH),
               new Object[]{3, 32});
         throw new RestRuntimeException(err);
      }
   }

   /**
    * Validate Username field data is acceptable
 * @param session 
 * @param name 
 * @throws RestAuthenticationException 
    */
   public static void validateUsername(HttpSession session, String name) throws RestRuntimeException
   {
      if (name == null || name.length() < 2 || name.length() > 32)
      {
         String err = MessageFormat.format(RestServerHelper.getMessage(session, MSG_USERNAME_LENGTH),
               new Object[]{2, 32});
         throw new RestRuntimeException(err);
      }
      if (name.indexOf('\'') != -1 || name.indexOf('"') != -1 || name.indexOf('\\') != -1)
      {
         String err = MessageFormat.format(RestServerHelper.getMessage(session, MSG_USER_ERR),
               new Object[]{"', \", \\"});
         throw new RestRuntimeException(err);
      }
   }
   

   /**
    * Get the specified I18N message string from the default message bundle for this user.
    * NB. this is a version of Application.getMessage() that is not i18n'd
    * 
    * @param session        HttpSession
    * @param msg            Message ID
    * 
    * @return String from message bundle or $$msg$$ if not found
    */
   public static String getMessage(String msg) {
      return getDefaultBundle().getString(msg);
   }
   

   /**
    * Get the specified I18N message string from the default message bundle for this user.
    * NB. this is a version of Application.getMessage() that accepts a null session.
    * 
    * @param session        HttpSession
    * @param msg            Message ID
    * 
    * @return String from message bundle or $$msg$$ if not found
    */
   public static String getMessage(HttpSession session, String msg) {
      if (session == null) {
         return getDefaultBundle().getString(msg);
      }
      return Application.getBundle(session).getString(msg);
   }
   
   /**
    * Get the specified the default message bundle.
    * NB. this is a version of Application.getBundle() that accepts a null session.
    * 
    * @param session        HttpSession
    * 
    * @return ResourceBundle for this user
    */
   public static ResourceBundle getDefaultBundle() {
      return ResourceBundleWrapper.getResourceBundle(Application.MESSAGE_BUNDLE, Locale.getDefault());
   }


   /**
    * Helper for top-level kind of REST errors
    * @param httpRes
    * @param msg
    * @param t
    * @throws IOException
    */
   public static void outputErrorResponse(HttpServletResponse httpRes,
        String msg, Throwable t) throws IOException, ServletException {
      try {
         // attempting to reset response
         httpRes.reset();
      
         // building error XML message
         RestCommandResult xmlResult = new RestCommandResult("internal error");
         xmlResult.setError(msg, t);
         
         // filling response
         httpRes.setContentType("text/xml");
         PrintWriter out = httpRes.getWriter();
         out.write(xmlResult.toString());
         out.close();
         
      } catch(IllegalStateException isex) {
         // reset failed, can only throw exception
         if (t instanceof IOException) {
            throw (IOException) t;
         } else if (t instanceof ServletException) {
            throw (ServletException) t;
         } else {
            throw new ServletException(msg, t);
         }
      }
   }
   
}
