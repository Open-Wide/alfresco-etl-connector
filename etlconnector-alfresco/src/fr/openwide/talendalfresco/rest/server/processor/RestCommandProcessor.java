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

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.app.servlet.command.Command;
import org.alfresco.web.app.servlet.command.CommandFactory;
import org.alfresco.web.app.servlet.command.ExtCommandProcessor;


/**
 * CommandProcessor for RestCommands.
 * 
 * NB. We do not use webscripts for our REST uses, because although
 * they allow execution of java code, their returned content must
 * be templated with freemarker, which prevents generating XML trees of
 * arbitrary complexity (like any kind of alfresco node subtree).
 * 
 * NB. its commands must be registered using a CommandRegistrator in Spring
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestCommandProcessor implements ExtCommandProcessor {
   
   //private static final String URL_ELEMENTS = "URL_ELEMENTS";

   private RestCommand restCommand = null;
   
   
   /**
    * Only saves all urlElements in this processor instance. Validation itself may be
    * done in RestCommand.validateArguments().
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#validateArguments(javax.servlet.ServletContext, java.lang.String, java.util.Map, java.lang.String[])
    */
   public boolean validateArguments(ServletContext sc, String commandName, Map<String, String> args, String[] urlElements)
   {
      Command command = CommandFactory.getInstance().createCommand(commandName);
      if (command == null)
      {
         throw new AlfrescoRuntimeException("Unregistered login command specified: " + commandName);
      }
      
      if (!(command instanceof RestCommand)) {
         throw new AlfrescoRuntimeException("Can't work with commands that are not RestCommands");
      }
      this.restCommand = (RestCommand) command;
      
      return this.restCommand.validateArguments(sc, args, urlElements);
   }
   
   
   public void outputStatus(PrintWriter out) {
      String outXml = this.restCommand.getRestCommandResult().toString();
      out.write(String.valueOf(outXml));
   }


   public void process(ServiceRegistry serviceRegistry,
         HttpServletRequest request, String command) {
      throw new AlfrescoRuntimeException("Can't work as a non-Ext CommandProcessor");
   }

   public void process(ServiceRegistry serviceRegistry,
         HttpServletRequest req, HttpServletResponse res,
         String command) {
      //Map<String, Object> params = params = CommandUtils.toCommandParameters(req);
      //this.args.put(URL_ELEMENTS, this.urlElements);
         
      this.restCommand.executeRest(serviceRegistry, req, res);
   }

}
