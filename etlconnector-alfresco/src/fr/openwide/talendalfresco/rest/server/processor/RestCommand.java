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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.app.servlet.command.Command;


/**
 * Defines a RestCommand.
 * its Command methods must be implemented on top of its RestCommand
 * ones, so these last ones have optimal performances and flexibility.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public interface RestCommand extends Command {
   
   RestCommandResult getRestCommandResult();

   boolean validateArguments(ServletContext sc,
         Map<String, String> args, String[] urlElements);

   void executeRest(ServiceRegistry serviceRegistry,
         HttpServletRequest req, HttpServletResponse res);

}
