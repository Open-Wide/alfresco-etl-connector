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

package fr.openwide.talendalfresco.rest.client.importer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;
import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.client.AlfrescoRestClient;
import fr.openwide.talendalfresco.rest.client.ClientImportCommand;
import fr.openwide.talendalfresco.rest.client.RestClientException;


/**
 * Requires a running alfresco with talendalfresco ext.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestClientImportFileTest extends TestCase {
   
   private AlfrescoRestClient alfrescoRestClient;


   public RestClientImportFileTest() {
      super(RestClientImportFileTest.class.getName());
   }

   @Override
   protected void setUp() throws Exception {
      alfrescoRestClient = new AlfrescoRestClient();
      alfrescoRestClient.setTimeout(5000);
      // default server : localhost
      alfrescoRestClient.login("admin", "admin");
   }

   @Override
   protected void tearDown() throws Exception {
      alfrescoRestClient.logout();
      alfrescoRestClient = null;
   }
   
   public void ttestSingleFileXpathImport() throws RestClientException {
      FileInputStream acpXmlIs = null;
      try {
         acpXmlIs = new FileInputStream(
            "/home/mdutoo/dev/workspace/talendalfresco-alfresco/sample/single_file.xml");
      } catch (IOException ioex) {
         fail("ACP XML file not found " + ioex.getMessage());
      }
      
      ClientImportCommand cmd = new ClientImportCommand("/app:company_home", acpXmlIs);;
   
      // Execute the command.
      alfrescoRestClient.execute(cmd);
      
      assertTrue(RestConstants.CODE_OK.equals(cmd.getResultCode()));
      
      ArrayList<String[]> resultLogs = cmd.getResultLogs();
      assertTrue(resultLogs != null);
      System.out.println("Result :");
      for (String[] resultLog : cmd.getResultLogs()) {
         System.out.println("   " + Arrays.asList(resultLog));
      }
      System.out.println("\n");
      assertTrue(resultLogs.size() == 1);
      //assertTrue(cmd.getErrorLogs().isEmpty());
   }
   
   public void ttestSingleFileNamePathImport() throws RestClientException {
      FileInputStream acpXmlIs = null;
      try {
         acpXmlIs = new FileInputStream(
            "/home/mdutoo/dev/workspace/talendalfresco-alfresco/sample/single_file.xml");
      } catch (IOException ioex) {
         fail("ACP XML file not found " + ioex.getMessage());
      }
      
      ClientImportCommand cmd = new ClientImportCommand("/Alfresco", acpXmlIs);;
   
      // Execute the command.
      alfrescoRestClient.execute(cmd);
      
      assertTrue(RestConstants.CODE_OK.equals(cmd.getResultCode()));
      
      ArrayList<String[]> resultLogs = cmd.getResultLogs();
      assertTrue(resultLogs != null);
      assertTrue(resultLogs.size() == 1);
      //assertTrue(cmd.getErrorLogs().isEmpty());
   }
   
   public void testSingleFolderFileNamePathImport() throws RestClientException {
      FileInputStream acpXmlIs = null;
      try {
         acpXmlIs = new FileInputStream(
            "/home/mdutoo/dev/workspace/talendalfresco-alfresco/sample/single_folder_file.xml");
      } catch (IOException ioex) {
         fail("ACP XML file not found " + ioex.getMessage());
      }
      
      ClientImportCommand cmd = new ClientImportCommand("/Alfresco", acpXmlIs);;
   
      // Execute the command.
      alfrescoRestClient.execute(cmd);
      
      assertTrue(RestConstants.CODE_OK.equals(cmd.getResultCode()));
      
      ArrayList<String[]> resultLogs = cmd.getResultLogs();
      assertTrue(resultLogs != null);
      System.out.println("Result :");
      for (String[] resultLog : cmd.getResultLogs()) {
         System.out.println("   " + Arrays.asList(resultLog));
      }
      System.out.println("\n");
      assertTrue(resultLogs.size() == 2);
      //assertTrue(cmd.getErrorLogs().isEmpty());
   }
   
}
