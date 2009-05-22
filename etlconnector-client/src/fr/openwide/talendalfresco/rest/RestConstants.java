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

package fr.openwide.talendalfresco.rest;


/**
 * Constants for interacting using REST commands.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public interface RestConstants {
   
   public static final String TAG_ROOT = "result";
   public static final String TAG_COMMAND = "command";
   public static final String TAG_CODE = "code";
   public static final String TAG_MESSAGE = "message";
   public static final String TAG_CONTENT = "content";
   public static final String TAG_ERROR = "error";

   public static final String CODE_OK = "00";
   //public static final String CODE_WARNING = "10"; // TODO functional warning != techical ; would need another tag ; but message can handle it
   public static final String CODE_ERROR_AUTH = "20";
   public static final String CODE_ERROR_AUTH_MISSING = "21";
   public static final String CODE_ERROR_AUTH_UNKNOWN_USER = "22";
   public static final String CODE_ERROR_WRONG_PARAMETERS = "30";
   public static final String CODE_ERROR_FUNCTIONAL = "40";
   public static final String CODE_FILE_ERROR = "50";
   public static final String CODE_PERMISSION_DENIED = "60";
   public static final String CODE_ERROR_TECHNICAL = "90";
   public static final String CODE_ERROR_UNSPECIFIED = "100";

   
   ///////////////////////////////////////////////////
   // login
   public static final String CMD_LOGIN = "login";

   public static final String PROP_LOGIN_USERNAME = "username";
   public static final String PROP_LOGIN_PASSWORD = "password";
   public static final String PROP_LOGIN_TICKET = "ticket";
   
   public static final String RES_LOGIN_TICKET = "ticket";

   
   ///////////////////////////////////////////////////
   // logout
   public static final String CMD_LOGOUT = "logout";
   
   
   ///////////////////////////////////////////////////
   // import
   public static final String CMD_IMPORT = "import";
   
   public static final String PROP_IMPORT_PATH = "path"; // opt, default to ""
   public static final String PROP_IMPORT_CLIENT_PATH_DELIMITER = "clientPathDelimiter"; // opt, default "\\"
   public static final String PROP_IMPORT_TARGET_LOCATION_BASE = "targetLocationBase"; // opt, default "/"
   public static final String PROP_IMPORT_DOCUMENT_MODE = "documentMode"; // opt, default "create only"
   public static final String PROP_IMPORT_CONTAINER_MODE = "containerMode"; // opt, default "create or update"
   public static final String PROP_IMPORT_TARGET_LOCATION_CONTAINER_TYPE = "targetLocationContainerType"; // opt, default ContentModel.TYPE_FOLDER
   public static final String PROP_IMPORT_TARGET_LOCATION_CHILD_ASSOCIATION_TYPE = "targetLocationChildAssociationType";
   public static final String PROP_IMPORT_LOG_SUCCESS_RESULTS = "logSuccessResults"; // opt, default to false
   public static final String PROP_IMPORT_LOG_INDIRECT_ERROR_RESULTS = "logIndirectErrorResults"; // opt, default to true

   public static final String RES_IMPORT_ERROR = "error";
   public static final String RES_IMPORT_SUCCESS = "success";
   public static final String RES_IMPORT_NAMEPATH = "namepath";
   public static final String RES_IMPORT_MESSAGE = "message";
   public static final String RES_IMPORT_DATE = "date";
   public static final String RES_IMPORT_NODEREF = "noderef";
   public static final String RES_IMPORT_DOCTYPE = "doctype";
   
}
