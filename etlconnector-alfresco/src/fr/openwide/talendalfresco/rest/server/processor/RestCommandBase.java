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
package fr.openwide.talendalfresco.rest.server.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.servlet.command.CommandFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import fr.openwide.talendalfresco.rest.server.RestRuntimeException;
import fr.openwide.talendalfresco.rest.server.RestServerHelper;


/**
 * Handles encoding, done by HTTPServletRequest in input, done in RestCommandResult for output.
 * Hardwires encoding to RestServerHelper.DEFAULT_REST_ENCODING
 * (i.e. ISO-8859-1, which is required because tomcat uses it
 * by default, and changing it there would impact the whole webapp).
 * NB. otherwise could take it from request.
 * By default, requires servlet request-wide transactions.
 *
 * @author Marc Dutoo - Open Wide SA
 *
 */
public abstract class RestCommandBase implements RestCommand {

    protected static Log logger = LogFactory.getLog(RestCommandBase.class);

    protected static NamespaceService namespaceService;
    protected static DictionaryService dictionaryService;
    protected static NodeService nodeService;
    protected static ContentService contentService;
    protected static SearchService searchService;
    protected static FileFolderService fileFolderService;
    protected static AuthorityService authorityService;
    protected static PermissionService permissionService;
    protected static ActionService actionService;
    protected static AuthenticationService authenticationService;
    protected static PersonService personService;
    protected static TransactionService transactionService;
    
    protected static Pattern forbidenChar = Pattern.compile("(.*[\"*\\><?/:|%&+;]+.*)|(.*[.]?.*[.]+$)|(.*[ ]+$)");

    private String commandName;

    protected HttpServletRequest httpRequest;
    protected HttpServletResponse httpResponse;
    protected Set<String> propertyNameSet = null;

    protected RestCommandResult xmlResult;
    protected XMLStreamWriter xmlWriter;

   protected Map<String, String> args = null;
   protected String[] urlElements = null;
    
    
    public RestCommandBase(String commandName) {
       this.commandName = commandName;

       // init XML result
       // so it is ready if parameter validation error
       xmlResult = new RestCommandResult(getCommandName(), RestServerHelper.DEFAULT_REST_ENCODING);
       // NB. encoding ISO-8859-1 is required because tomcat uses it
       // by default, and changing it there would impact the whole webapp
       xmlWriter = xmlResult.getXmlWriter();
    }
    
    public void registerCommandClass() {
       CommandFactory.getInstance().registerCommand(commandName, this.getClass());
    }


    public void setHttpRequest(HttpServletRequest req) {
       this.httpRequest = req;
    }
    public void setHttpResponse(HttpServletResponse res) {
       this.httpResponse = res;
    }
    public RestCommandResult getRestCommandResult() {
       return xmlResult;
    }
    
    
    public String getCommandName() {
       return this.commandName;
    }
    
    public abstract String[] getPropertyNames();
    public Set<String> getPropertyNameSet() {
       if (propertyNameSet == null) {
          String[] propertyNames = this.getPropertyNames();
          propertyNameSet = new HashSet<String>(propertyNames.length);
          for (String propertyName : propertyNames) {
             propertyNameSet.add(propertyName);
          }
       }
       return propertyNameSet;
    }

    
    protected void init(ServiceRegistry serviceRegistry, HttpServletRequest req, HttpServletResponse res) {
       this.httpRequest = req;
       this.httpResponse = res;
    }

    protected Object getBean(String name) {
       return WebApplicationContextUtils.getRequiredWebApplicationContext(
             this.httpRequest.getSession().getServletContext()).getBean(name);
    }
    
    /**
     * Enriches the xmlResult with info (though it is not
     * accessible with the original CommandServlet)
     */
    public final boolean validateArguments(ServletContext sc,
          Map<String, String> args, String[] urlElements) {
       this.args = args;
       this.urlElements = urlElements;
       
       // asking impl ;  no need to provied sc, it is in the req
       boolean res = this.validateArgumentsImpl();
       
       if (!res && xmlResult.isSuccess()) {
          // wrong parameters, but xmlResult error not yet set specifically,
          // let's do it in a generic manner :
          String msg = "Bad parameters for command "
             + getCommandName() + " which requires" + getPropertyNames()
             + "but got " + args;
          xmlResult.setError(RestCommandResult.CODE_ERROR_WRONG_PARAMETERS, msg, null);
          logger.error(msg);
       }
       return res;
    }

    protected boolean validateArgumentsImpl() {
      return true;
   }


   /**
     * Original Command method, impl'd on top of REST one for better
     * perfs and flexibility
     */
    public final String execute(ServiceRegistry serviceRegistry, Map<String, Object> params) {
       if (this.args == null) {
          this.args = new HashMap<String, String>();
          for (String paramName : params.keySet()) {
             Object paramValue = params.get(paramName);
             this.args.put(paramName, (paramValue == null) ? null : String.valueOf(paramValue));
          }
       }
       this.executeRest(serviceRegistry, null, null);
       return xmlResult.toString();
    }

    /**
     * Rest command template
     */
    public final void executeRest(ServiceRegistry serviceRegistry,
          HttpServletRequest req, HttpServletResponse res) {
       try {
          init(serviceRegistry, req, res);
          executeImpl();
       } catch (RestRuntimeException rrex) {
          // means we should trigger rollback
          logger.error("Unknown runtime RestCommand error", rrex);
          throw rrex;
       } catch (XMLStreamException xmlsex) {
          // means won't even be able to send a REST XML response
          // so we lose nothing by rethrowing it to trigger rollback
          String msg = "Error writing XML result";
          logger.error(msg, xmlsex);
          throw new RestRuntimeException(msg, xmlsex);
       } catch (RuntimeException rex) {
          // means any uncatched, unforeseen error.
          // things we don't know whant to do with it,
          // rethrow it to trigger rollback.
          logger.error("Unknown runtime error", rex);
          throw rex;
       }
    }

    protected abstract void executeImpl() throws XMLStreamException;
    

   public void setNamespaceService(NamespaceService namespaceService) {
      RestCommandBase.namespaceService = namespaceService;
   }

   public void setDictionaryService(DictionaryService dictionaryService) {
      RestCommandBase.dictionaryService = dictionaryService;
   }

   public void setNodeService(NodeService nodeService) {
      RestCommandBase.nodeService = nodeService;
   }

   public void setContentService(ContentService contentService) {
      RestCommandBase.contentService = contentService;
   }

   public void setSearchService(SearchService searchService) {
      RestCommandBase.searchService = searchService;
   }

   public void setFileFolderService(FileFolderService fileFolderService) {
      RestCommandBase.fileFolderService = fileFolderService;
   }

   public void setAuthorityService(AuthorityService authorityService) {
      RestCommandBase.authorityService = authorityService;
   }

   public void setPermissionService(PermissionService permissionService) {
      RestCommandBase.permissionService = permissionService;
   }

   public void setActionService(ActionService actionService) {
      RestCommandBase.actionService = actionService;
   }

   public void setAuthenticationService(AuthenticationService authenticationService) {
      RestCommandBase.authenticationService = authenticationService;
   }

   public void setPersonService(PersonService personService) {
      RestCommandBase.personService = personService;
   }

   public void setTransactionService(TransactionService transactionService) {
      RestCommandBase.transactionService = transactionService;
   }

	public boolean isTransactional() {
		return true;
	}
    
}
