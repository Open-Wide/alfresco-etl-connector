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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import junit.framework.TestCase;
import fr.openwide.talendalfresco.acpxml.AcpXmlException;
import fr.openwide.talendalfresco.acpxml.AcpXmlWriter;
import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.client.AlfrescoRestClient;
import fr.openwide.talendalfresco.rest.client.ClientImportCommand;
import fr.openwide.talendalfresco.rest.client.RestClientException;


/**
 * Tests the ACP XML writer using explicit, "unmapped" methods.
 * Requires a running alfresco with talendalfresco ext.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestClientUnmappedAcpXmlWriterImportTest extends TestCase {
   
   private AlfrescoRestClient alfrescoRestClient;


   public RestClientUnmappedAcpXmlWriterImportTest() {
      super(RestClientUnmappedAcpXmlWriterImportTest.class.getName());
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
   
   public void testWriterImport() throws RestClientException {
      ByteArrayOutputStream acpXmlBos = new ByteArrayOutputStream();

      try {
         
         AcpXmlWriter acpXmlWriter = new AcpXmlWriter(acpXmlBos, "ISO-8859-1");
         acpXmlWriter.writeNamespace("cm", "http://www.alfresco.org/model/content/1.0");
         acpXmlWriter.writeNamespace("app", "http://www.alfresco.org/model/application/1.0");

         acpXmlWriter.writeStartContent("cm:content");
         acpXmlWriter.writeStartPermissions(false);
         acpXmlWriter.writePermission(new HashMap<String, String>() { {
            put("USERORGROUP", "GROUP_EVERYONE");
            put("PERMISSION", "Consumer");
            } }, null);
         acpXmlWriter.writeEndPermissions();
         acpXmlWriter.writeAspects(new ArrayList<String>() { {
            add("app:uifacets"); } });

         acpXmlWriter.writeProperty("cm:name", "d:text", "my_file_writer.pdf");
         acpXmlWriter.writeProperty("cm:content", "d:content", "classpath:alfresco/bootstrap/Alfresco-Tutorial.pdf");

         acpXmlWriter.writeEndContent();
         
         acpXmlWriter.close();
      } catch (AcpXmlException e) {
         throw new RestClientException("Error creating XML result", e);
      }
      
      String content = acpXmlBos.toString();
      
      ByteArrayInputStream acpXmlIs = new ByteArrayInputStream(content.getBytes());
      
      ClientImportCommand cmd = new ClientImportCommand("/Alfresco/test1", acpXmlIs);
   
      // Execute the command.
      alfrescoRestClient.execute(cmd);
      
      assertTrue(RestConstants.CODE_OK.equals(cmd.getResultCode()));
      System.out.println(cmd.toString() + " " + cmd.getResultMessage() + " " + cmd.getResultError());
      
      ArrayList<String[]> resultLogs = cmd.getResultLogs();
      assertTrue(resultLogs != null);
      assertTrue(resultLogs.size() == 1);
      //assertTrue(cmd.getErrorLogs().isEmpty());
      System.out.println("Result :");
      for (String[] resultLog : cmd.getResultLogs()) {
         System.out.println("   " + Arrays.asList(resultLog));
      }
      System.out.println("\n");
   }
   
}
