/*
 * Copyright (C) 2008-2012 Open Wide SA
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
 * More information at http://knowledge.openwide.fr/bin/view/Main/AlfrescoETLConnector/
 */

package fr.openwide.talendalfresco.rest.client.importer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;
import fr.openwide.talendalfresco.rest.RestConstants;
import fr.openwide.talendalfresco.rest.XmlHelper;
import fr.openwide.talendalfresco.rest.client.AlfrescoRestClient;
import fr.openwide.talendalfresco.rest.client.ClientImportCommand;
import fr.openwide.talendalfresco.rest.client.RestClientException;


/**
 * Requires a running alfresco with talendalfresco ext.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestClientWriterImportTest extends TestCase {
   
   private AlfrescoRestClient alfrescoRestClient;
	private String serverFileSeparator = File.separator; // to change depending on the server


   public RestClientWriterImportTest() {
      super(RestClientWriterImportTest.class.getName());
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
         XMLStreamWriter xmlWriter = XmlHelper.getXMLOutputFactory()
            .createXMLStreamWriter(acpXmlBos, "UTF-8");
         
         //xmlWriter.setNamespaceContext(nsCtx); // not useful
         xmlWriter.writeStartDocument(); // possibly encoding, version
         
         xmlWriter.writeStartElement("view:view");
         xmlWriter.writeNamespace("view", "http://www.alfresco.org/view/repository/1.0");
         xmlWriter.writeNamespace("cm", "http://www.alfresco.org/model/content/1.0");
         xmlWriter.writeNamespace("app", "http://www.alfresco.org/model/application/1.0");

         xmlWriter.writeStartElement("cm:content");

         xmlWriter.writeStartElement("view:acl");
         xmlWriter.writeAttribute("view:inherit", "false");
         xmlWriter.writeStartElement("view:ace");
         xmlWriter.writeAttribute("view:access", "ALLOWED");
         xmlWriter.writeStartElement("view:authority");
         xmlWriter.writeCharacters("GROUP_EVERYONE");
         xmlWriter.writeEndElement();
         xmlWriter.writeStartElement("view:permission");
         xmlWriter.writeCharacters("Consumer");
         xmlWriter.writeEndElement();
         xmlWriter.writeEndElement(); // end ace
         xmlWriter.writeEndElement(); // end acl

         xmlWriter.writeEmptyElement("app:uifacets");
         xmlWriter.writeStartElement("cm:name");
         xmlWriter.writeCharacters("my_file_writer.pdf");// NB. CDATA works, but rather using escaping
         xmlWriter.writeEndElement();
         xmlWriter.writeStartElement("cm:content");
         xmlWriter.writeCharacters("contentUrl=classpath:alfresco/bootstrap/Alfresco-Tutorial.pdf|mimetype=application/pdf|size=|encoding=");
         xmlWriter.writeEndElement();
         
         xmlWriter.writeEndElement(); // end content
         
         xmlWriter.writeEndElement(); // end view
         
         xmlWriter.writeEndDocument();
         
         xmlWriter.close();
      } catch (XMLStreamException e) {
         throw new RestClientException("Error creating XML result", e);
      }
      
      String content = acpXmlBos.toString();
      
      ByteArrayInputStream acpXmlIs = new ByteArrayInputStream(content.getBytes());
      
      ClientImportCommand cmd = new ClientImportCommand(serverFileSeparator, acpXmlIs);
   
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
