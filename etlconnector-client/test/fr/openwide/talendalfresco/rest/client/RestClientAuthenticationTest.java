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

package fr.openwide.talendalfresco.rest.client;

import junit.framework.TestCase;
import fr.openwide.talendalfresco.rest.RestConstants;


/**
 * Requires a running alfresco with talendalfresco ext.
 * 
 * @author Marc Dutoo - Open Wide SA
 *
 */
public class RestClientAuthenticationTest extends TestCase {
   
   private AlfrescoRestClient alfrescoRestClient;


   public RestClientAuthenticationTest() {
      super(RestClientAuthenticationTest.class.getName());
   }

   @Override
   protected void setUp() throws Exception {
      alfrescoRestClient = new AlfrescoRestClient();
      alfrescoRestClient.setTimeout(5000);
      // default server : localhost
   }

   @Override
   protected void tearDown() throws Exception {
      alfrescoRestClient = null;
   }
   
   public void testRestLogin() throws RestClientException {
      ClientLoginCommand cmd = new ClientLoginCommand("admin", "admin");
   
      // Execute the command.
      alfrescoRestClient.execute(cmd);
      
      assertTrue(RestConstants.CODE_OK.equals(cmd.getResultCode()));
      assertTrue(cmd.getTicket() != null && cmd.getTicket().length() != 0);
      assertTrue(!cmd.getTicket().equals(alfrescoRestClient.getTicket())); // because not set
      System.out.println("got ticket " + cmd.getTicket());
   }
   
   public void testRestClientLogin() throws RestClientException {
      // login
      alfrescoRestClient.login("admin", "admin");
      
      assertTrue(alfrescoRestClient.getTicket() != null && alfrescoRestClient.getTicket().length() != 0);
      assertTrue("admin".equals(alfrescoRestClient.getUsername()));
      System.out.println("got ticket " + alfrescoRestClient.getTicket());
   }
   
   public void testRestClientLogout() throws RestClientException {
      // first login
      alfrescoRestClient.login("admin", "admin");
   
      // Execute the command.
      alfrescoRestClient.logout();
      
      assertTrue(alfrescoRestClient.getTicket() == null);
      assertTrue(alfrescoRestClient.getUsername() == null);
   }
   
   public void testRestLoginBadPassword() throws RestClientException {
      try {
         // Execute the command.
         alfrescoRestClient.login("admin", "badpass");
         
         fail("Should have exploded");

      } catch (RestClientException e) {
         assertTrue(alfrescoRestClient.getUsername() == null);
      }
    }
   
}
