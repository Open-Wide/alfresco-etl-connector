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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
 * Tests the Talend content import driver. Requires a running alfresco with
 * talendalfresco ext.
 * 
 * @author Marc Dutoo - Open Wide SA
 * 
 */
public class RestClientTalendAcpXmlWriterImportTest extends TestCase {

	// file path is relative to project
	private static final String FILE_NAME = "readme.html";
	private static final String CLASSPATH_FILE_PATH = "classpath:alfresco/bootstrap/webscripts/" + FILE_NAME;
	private String serverFileSeparator = File.separator; // to change depending on the server
	
	private AlfrescoRestClient alfrescoRestClient;
	private TalendAcpXmlWriter talendAcpXmlWriter;

	public RestClientTalendAcpXmlWriterImportTest() {
		super(RestClientTalendAcpXmlWriterImportTest.class.getName());
	}

	@Override
	protected void setUp() throws Exception {
		alfrescoRestClient = new AlfrescoRestClient();
		alfrescoRestClient.setTimeout(5000);
		// default server : localhost
		alfrescoRestClient.login("admin", "admin");
		talendAcpXmlWriter = new TalendAcpXmlWriter();
	}

	@Override
	protected void tearDown() throws Exception {
		alfrescoRestClient.logout();
		alfrescoRestClient = null;
	}

	public void testWriterImport() throws RestClientException {
		talendAcpXmlWriter
				.setMappedContentNamespaces(new ArrayList<Map<String, String>>() {
					{
						add(new HashMap<String, String>() {
							{
								put("PREFIX", "cm");
								put("URI",
										"http://www.alfresco.org/model/content/1.0");
							}
						});
						add(new HashMap<String, String>() {
							{
								put("PREFIX", "app");
								put("URI",
										"http://www.alfresco.org/model/application/1.0");
							}
						});
					}
				});
		talendAcpXmlWriter.setAlfrescoType("cm:content");
		talendAcpXmlWriter
				.setMappedAlfrescoAspects(new ArrayList<Map<String, String>>() {
					{
						add(new HashMap<String, String>() {
							{
								put("NAME", "app:uifacets");
							}
						});
					}
				});
		talendAcpXmlWriter.setConfigurePermission(true);
		// talendAcpXmlWriter.setPermissionOnDocumentAndNotContainer(false); //
		// default
		talendAcpXmlWriter.setInheritPermissions(false);
		/*
		 * talendAcpXmlWriter.setPermissions(new ArrayList<Map<String,
		 * String>>() { { add(new HashMap<String, String>() { {
		 * put("USERORGROUP", "GROUP_EVERYONE"); put("PERMISSION", "Consumer");
		 * } }); }});
		 */// not required
		// talendAcpXmlWriter.setContainerType(containerType); // default
		// talendAcpXmlWriter.setContainerChildAssociationType(containerChildAssociationType);
		// // default

		try {

			// layout under base target location :
			// my_file_writer.pdf
			// test2/my_file_writer.pdf
			// test2/test1/my_file_writer.pdf
			// test2/test1/test1/my_file_writer.pdf
			// test2/test2/my_file_writer.pdf

			talendAcpXmlWriter.start();

			writeTestDocument1("");
			writeTestDocument1("test2");
			writeTestDocument1("test2/test21"); // subfolder
			writeTestDocument1("test2/test21/test211/test2111"); // deep subfolder
			writeTestDocumentError("test2/test21/test211/test2112"); // another deep subfolder, content path error
			writeTestDocument1("test2/test21/test212/test2121"); // back up
			writeTestDocument1("test2/test21/test212/test2121"); // duplicate child node error
			writeTestDocument1("test2/test22"); // back up several layers
			talendAcpXmlWriter.close();
		} catch (AcpXmlException e) {
			throw new RestClientException("Error creating XML result", e);
		}

		String content = talendAcpXmlWriter.toString();

		ByteArrayInputStream acpXmlIs = new ByteArrayInputStream(content
				.getBytes());

		ClientImportCommand cmd = new ClientImportCommand(serverFileSeparator
				+ "test1", acpXmlIs);
		cmd.setDocumentMode(ContentImporterConfiguration.DOCUMENT_MODE_CREATE_OR_UPDATE);

		// Execute the command.
		alfrescoRestClient.execute(cmd);

		assertTrue(RestConstants.CODE_OK.equals(cmd.getResultCode()));
		System.out.println(cmd.toString() + " " + cmd.getResultMessage() + " "
				+ cmd.getResultError());

		ArrayList<String[]> resultLogs = cmd.getResultLogs();
		assertTrue(resultLogs != null);
		//assertTrue(resultLogs.size() == 16); // only if not yet imported (update does not work because no uuid)
		//assertTrue(cmd.getErrorLogs().size() == 1); // because of writeTestDocumentError, only if not yet imported (update does not work because no uuid)
		assertTrue(cmd.getErrorLogs().size() >= 1); // because of writeTestDocumentError
		System.out.println("Result :");
		for (String[] resultLog : cmd.getResultLogs()) {
			System.out.println("   " + Arrays.asList(resultLog));
		}
		System.out.println("\n");
	}

	private void writeTestDocument1(String slashedPath) throws AcpXmlException {
		writeTestDocument1(slashedPath, FILE_NAME, CLASSPATH_FILE_PATH);
	}

	private void writeTestDocument1(String slashedPath,
			String fileName, String filePath) throws AcpXmlException {
		String serverOsSlashedPath = patchSlashedPathForServer(slashedPath);

		talendAcpXmlWriter
				.writeStartDocument(serverOsSlashedPath,
						new String[][] { new String[] { "GROUP_EVERYONE",
								"Consumer" } });

		talendAcpXmlWriter.writeStartProperties();
		talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() {
			{
				put("NAME", "cm:name");
				put("TYPE", "d:text");
			}
		}, fileName + "_" + slashedPath.substring(slashedPath.lastIndexOf("/") + 1));
		talendAcpXmlWriter.writeMappedProperty(new HashMap<String, String>() {
			{
				put("NAME", "cm:content");
				put("TYPE", "d:content");
			}
		}, filePath);
		talendAcpXmlWriter.writeEndProperties();
		
		talendAcpXmlWriter.writeStartAssociations();
		talendAcpXmlWriter.writeEndAssociations();

		talendAcpXmlWriter.writeEndDocument();
	}

	private void writeTestDocumentError(String slashedPath)
			throws AcpXmlException {
		writeTestDocument1(slashedPath, FILE_NAME,
				CLASSPATH_FILE_PATH + "T"); // error : does not exist
	}

	private String patchSlashedPathForServer(String slashedPath) {
		String[] pathNames = slashedPath.split("/");
		StringBuffer serverOsSlashedPathBuf = new StringBuffer();
		for (String pathName : pathNames) {
			serverOsSlashedPathBuf.append(pathName);
			serverOsSlashedPathBuf.append(serverFileSeparator);
		}
		serverOsSlashedPathBuf
				.deleteCharAt(serverOsSlashedPathBuf.length() - 1);
		String serverOsSlashedPath = serverOsSlashedPathBuf.toString();
		return serverOsSlashedPath;
	}

}
