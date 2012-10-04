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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.server.processor.RestCommandProcessor;


/**
 * Auth filter for command servlet.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class CommandAuthenticationFilter implements Filter {
   
   private static final String ARG_TICKET   = "ticket";
   private static final String LOGIN_COMMAND_NAME   = "login";

   protected Log logger = LogFactory.getLog(this.getClass());
   
   private ServletContext context;
   
   
   /**
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig config) throws ServletException {
      this.context = config.getServletContext();
   }
   /**
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy() {
      this.context = null;
   }
   /**
    * Return the ServiceRegistry helper instance
    * 
    * @param sc      ServletContext
    * @return ServiceRegistry
    */
   public static ServiceRegistry getServiceRegistry(ServletContext sc)  {
      WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
      return (ServiceRegistry) wc.getBean(ServiceRegistry.SERVICE_REGISTRY);
   }
   

   /**
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException {
      
      HttpServletRequest httpReq  = (HttpServletRequest)req;
      HttpServletResponse httpRes = (HttpServletResponse)res;
      
      // checking whether this request is has enough info for ticket auth by CommandServlet
      boolean ticketAuth = false;
      String ticket = req.getParameter(ARG_TICKET);
      if (ticket == null || ticket.length() == 0) {
         // no ticket sent, maybe there's still a User in session with one ?
         HttpSession session = httpReq.getSession(false);
         if (session != null) {
            ticketAuth = (session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER) != null);
         }
      } else {
         ticketAuth = true;
      }
      
      if (ticketAuth) {
         // this request is ready for ticket auth by the CommandServlet
         // so let's forward it to the CommandServlet, which will auth its ticket
         // and then do whatever Command it wants
         try {
            chain.doFilter(req, res);
         } catch (Throwable t) {
            String msg = "Unknown internal error";
            logger.error(msg, t);
            // attempting to output response
            RestServerHelper.outputErrorResponse(httpRes, msg, t); 
         }
         return;
      } // else not logged in yet
      
      
      // now attempting login :
      try {
         RestCommandProcessor processor = this.login(httpReq, httpRes);

         // output processor state with Command result
         res.setContentType("text/xml");
         PrintWriter out = httpRes.getWriter();
         processor.outputStatus(out);
         out.close();
         
      } catch (Throwable t) {
         String msg = "Error during command auth filter processing: " + t.getMessage();
         logger.error(msg, t);
         // attempting to output response
         RestServerHelper.outputErrorResponse(httpRes, msg, t);
      }
   }
   
   
   /**
    * 
    * @param httpReq
    * @param httpRes
    * @return processor which can output to the res
    * @throws Throwable if error in txn
    */
   private RestCommandProcessor login(HttpServletRequest httpReq, HttpServletResponse httpRes)
         throws Throwable {
      // getting login parameters
      Map<String, String> args = new HashMap<String, String>(3, 1.0f);
      args.put(RestConstants.PROP_LOGIN_USERNAME, httpReq.getParameter("username"));
      args.put(RestConstants.PROP_LOGIN_PASSWORD, httpReq.getParameter("password"));
      
      RestCommandProcessor processor = new RestCommandProcessor();
      
      // validate that the processor has everything it needs to run the command
      if (!processor.validateArguments(this.context, LOGIN_COMMAND_NAME, args, null)) {
         // returning processor with error state
         return processor;
      }
      
      ServiceRegistry serviceRegistry = getServiceRegistry(this.context);
      UserTransaction txn = null;
      try  {
         txn = serviceRegistry.getTransactionService().getUserTransaction();
         txn.begin();
         processor.process(serviceRegistry, httpReq, httpRes, LOGIN_COMMAND_NAME);
         txn.commit();
         return processor;
         
      } catch (Throwable txnErr) {
         try { if (txn != null) {txn.rollback();} } catch (Exception tex) {}
         throw txnErr;
      }
   }

}
