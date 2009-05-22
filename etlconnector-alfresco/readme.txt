======================================================
ETL Connector for Alfresco - Alfresco Server Extension
http://forge.alfresco.com/projects/etlconnector/
Copyright (C) 2008 Open Wide SA
======================================================


About ETL Connector
-------------------
The ETL Connector extension for Alfresco allows to import documents in an
Alfresco repository by using compatible ETL Tools (for now Talend).
It also provides an ETL client library that makes it easy to integrate in any ETL tool.

Features
   * works by simple REST HTTP interactions with Alfresco, content provided as
fully compliant ACP (Alfresco Content Package) XML
   * imports to any kind of Alfresco content (not only file and folder but also
custom types or aspects, any properties and associations, any document tree)
   * configure permissions on imported content
   * create vs update modes on document and containers
   * provides import result logs

Team
   * Sponsored by Habitat 76 - http://www.habitat76.fr/
   * Design & Development : Marc Dutoo, Open Wide SA - http://www.openwide.fr
   * Samples & Testing : Nicolas Feray, Habitat 76 - http://nicolas.feray.free.fr/blog/?cat=5
   * Management & Support : Cedric Carbone, Talend - http://www.talend.com
   
License
   * Alfresco Server Extension : GPL
   * ETL client library : LGPL
   

Installation
------------
Compatible Alfresco releases
   * validated with 2.1 Entreprise for Tomcat
   * should work with all 2.x Alfresco releases
   * reported to work on Labs 2.9b
   
Compatible ETLs
   * Talend 3.1, using the tAlfrescoOutput component - http://www.talend.fr

Installation
   * get ETL Connector from a compatible ETL release, or from
http://forge.alfresco.com/projects/etlconnector/ 
   * put the etlconnector-alfresco*.jar file in the WEB-INF/lib of your
Alfresco installation, ex. $ALF_HOME/tomcat/webapps/alfresco/WEB-INF/lib
   * restart alfresco
   
Test
   * you can test it by using the samples provided in the companion project
etlconnector-samples , and a compatible ETL like Talend 3.1 on the client side.


Documentation
--------------
ETL Connector
   * Project page and forums at http://forge.alfresco.com/projects/etlconnector/
   * Download page at http://forge.alfresco.com/frs/?group_id=206
   * Website at http://knowledge.openwide.fr/bin/view/Main/AlfrescoETLConnector
   
Using ETL Connector with Talend
   * Talend documentation at Talend 3.1 manual at http://www.talend.com/resources/documentation.php
   * Tutorial by Nicolas Feray at http://nicolas.feray.free.fr/blog/?cat=5
   * Talend use case at http://www.talend.com/open-source-provider/casestudy/CaseStudy_Habitat76_FR.php
   * Presentation by Open Wide (French) at http://www.openwide.fr/index.php/Open-Wide/Lab/Contributions/Alfresco-Meetup-ETL-Connector-Talend


Build - Alfresco Server Extension
---------------------------------
   * provide the project with the Alfresco SDK and java 1.5 dependencies
   * run Ant on the given build.xml
   * the ETL Connector Server release is in build/export/ , ready to be
added to an Alfresco installation