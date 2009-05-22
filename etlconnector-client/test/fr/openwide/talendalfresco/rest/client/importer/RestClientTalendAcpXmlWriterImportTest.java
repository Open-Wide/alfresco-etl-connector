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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import fr.openwide.talendalfresco.acpxml.AcpXmlException;
import fr.openwide.talendalfresco.acpxml.TalendAcpXmlWriter;
import fr.openwide.talendalfresco.importer.ContentImporterConfiguration;
import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.client.AlfrescoRestClient;
import fr.openwide.talendalfresco.rest.client.ClientImportCommand;
import fr.openwide.talendalfresco.rest.client.RestClientException;


/**
 * Tests the Talend content import driver.
 * Requires a running alfresco with talendalfresco ext.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestClientTalendAcpXmlWriterImportTest extends TestCase {
   
   private AlfrescoRestClient alfrescoRestClient;


   public RestClientTalendAcpXmlWriterImportTest() {
      super(RestClientTalendAcpXmlWriterImportTest.class.getName());
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
      TalendAcpXmlWriter talendAcpXmlWriter = new TalendAcpXmlWriter();
      talendAcpXmlWriter.setMappedContentNamespaces(new ArrayList<Map<String, String>>() { { 
         add(new HashMap<String, String>() { {
            put("PREFIX", "cm");
            put("URI", "http://www.alfresco.org/model/content/1.0");
            } });
            add(new HashMap<String, String>() { {
               put("PREFIX", "app");
               put("URI", "http://www.alfresco.org/model/application/1.0");
               } });
         } });
      talendAcpXmlWriter.setAlfrescoType("cm:content");
      talendAcpXmlWriter.setMappedAlfrescoAspects(new ArrayList<Map<String, String>>() { {
         add(new HashMap<String, String>() { {
            put("NAME", "app:uifacets");
            } }); } });
      talendAcpXmlWriter.setConfigurePermission(true);
      //talendAcpXmlWriter.setPermissionOnDocumentAndNotContainer(false); // default
      talendAcpXmlWriter.setInheritPermissions(false);
      /*talendAcpXmlWriter.setPermissions(new ArrayList<Map<String, String>>() { {
         add(new HashMap<String, String>() { {
         put("USERORGROUP", "GROUP_EVERYONE");
         put("PERMISSION", "Consumer");
         } }); }});*/ // not required
      //talendAcpXmlWriter.setContainerType(containerType); // default
      //talendAcpXmlWriter.setContainerChildAssociationType(containerChildAssociationType); // default

      try {
         
         // layout under base target location :
         // my_file_writer.pdf
         // test2/my_file_writer.pdf
         // test2/test1/my_file_writer.pdf
         // test2/test1/test1/my_file_writer.pdf
         // test2/test2/my_file_writer.pdf

         talendAcpXmlWriter.start();
         
         talendAcpXmlWriter.writeStartDocument("", new String[][] { new String[] { "GROUP_EVERYONE", "Consumer" } });

         talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() { {
            put("NAME", "cm:name");
            put("TYPE", "d:text");
            } }, "my_file_writer.pdf");
         talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() { {
            put("NAME", "cm:content");
            put("TYPE", "d:content");
            } }, "classpath:alfresco/bootstrap/Alfresco-Tutorial.pdf");

         talendAcpXmlWriter.writeEndDocument();
         
         
         talendAcpXmlWriter.writeStartDocument("test2", new String[][] { new String[] { "GROUP_EVERYONE", "Consumer" } });

         talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() { {
            put("NAME", "cm:name");
            put("TYPE", "d:text");
            } }, "my_file_writer.pdf");
         talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() { {
            put("NAME", "cm:content");
            put("TYPE", "d:content");
            } }, "classpath:alfresco/bootstrap/Alfresco-Tutorial.pdf");

         talendAcpXmlWriter.writeEndDocument();
         
         
         talendAcpXmlWriter.writeStartDocument("test2/test1", new String[][] { new String[] { "GROUP_EVERYONE", "Consumer" } });

         talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() { {
            put("NAME", "cm:name");
            put("TYPE", "d:text");
            } }, "my_file_writer.pdf");
         talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() { {
            put("NAME", "cm:content");
            put("TYPE", "d:content");
            } }, "classpath:alfresco/bootstrap/Alfresco-Tutorial.pdf");

         talendAcpXmlWriter.writeEndDocument();
         
         
         talendAcpXmlWriter.writeStartDocument("test2/test1/test1", new String[][] { new String[] { "GROUP_EVERYONE", "Consumer" } });

         talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() { {
            put("NAME", "cm:name");
            put("TYPE", "d:text");
            } }, "my_file_writer.pdf");
         talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() { {
            put("NAME", "cm:content");
            put("TYPE", "d:content");
            } }, "classpath:alfresco/bootstrap/Alfresco-Tutorial.pdf");

         talendAcpXmlWriter.writeEndDocument();
         
         
         talendAcpXmlWriter.writeStartDocument("test2/test2", new String[][] { new String[] { "GROUP_EVERYONE", "Consumer" } });

         talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() { {
            put("NAME", "cm:name");
            put("TYPE", "d:text");
            } }, "my_file_writer.pdf");
         talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() { {
            put("NAME", "cm:content");
            put("TYPE", "d:content");
            } }, "classpath:alfresco/bootstrap/Alfresco-Tutorial.pdf");

         talendAcpXmlWriter.writeEndDocument();
         
         talendAcpXmlWriter.close();
      } catch (AcpXmlException e) {
         throw new RestClientException("Error creating XML result", e);
      }
      
      String content = talendAcpXmlWriter.toString();
      
      ByteArrayInputStream acpXmlIs = new ByteArrayInputStream(content.getBytes());
      
      ClientImportCommand cmd = new ClientImportCommand("/test1", acpXmlIs);
      cmd.setDocumentMode(ContentImporterConfiguration.DOCUMENT_MODE_CREATE_OR_UPDATE);
   
      // Execute the command.
      alfrescoRestClient.execute(cmd);
      
      assertTrue(RestConstants.CODE_OK.equals(cmd.getResultCode()));
      System.out.println(cmd.toString() + " " + cmd.getResultMessage() + " " + cmd.getResultError());
      
      ArrayList<String[]> resultLogs = cmd.getResultLogs();
      assertTrue(resultLogs != null);
      //assertTrue(resultLogs.size() == 1);
      //assertTrue(cmd.getErrorLogs().isEmpty());
      System.out.println("Result :");
      for (String[] resultLog : cmd.getResultLogs()) {
         System.out.println("   " + Arrays.asList(resultLog));
      }
      System.out.println("\n");
   }
   
}
