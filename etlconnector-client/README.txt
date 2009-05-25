======================================================
ETL Connector for Alfresco - ETL client library
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
http://forge.alfresco.com/frs/?group_id=206
   * put the etlconnector-alfresco*.jar file in the WEB-INF/lib of your
Alfresco installation, ex. $ALF_HOME/tomcat/webapps/alfresco/WEB-INF/lib
   * restart alfresco
   
Test
   * you can test it by using the samples provided in the companion project
etlconnector-samples , and a compatible ETL like Talend 3.1 on the client side.
   * for the Quitus sample, using Talend :
   * start the Alfresco server (after having installed the ETL Connector extension)
   * import the etlconnector-samples/quitus/GED_TECHNIQUE.acp document package
in a new "GED TECHNIQUE" folder within the company home folder, using the
custom action wizard in the Alfresco web interface
   * start Talend
   * import the etlconnector-samples/quitus/talend/ALFRESCO_ETLCONNECTOR_QUITUS
as a Talend workspace project
   * open the single Talend document import job ("ALFRESCO IMPORT_QUITUS 0.1")
   * set the PATH_SOURCE global context variable of the job to the location of the
etlconnector-samples/quitus/talend/ALFRESCO_ETLCONNECTOR_QUITUS folder
   * run it in Talend : the complex document tree has been imported in Alfresco,
including custom metadata and associations


Documentation
--------------
ETL Connector
   * Project page, news and forums at http://forge.alfresco.com/projects/etlconnector/
   * Website and documentation at http://knowledge.openwide.fr/bin/view/Main/AlfrescoETLConnector
   * Download page at http://forge.alfresco.com/frs/?group_id=206
   
Using ETL Connector with Talend
   * Talend documentation at Talend 3.1 manual at http://www.talend.com/resources/documentation.php
   * Tutorial by Nicolas Feray at http://nicolas.feray.free.fr/blog/?cat=5
   * Talend use case at http://www.talend.com/open-source-provider/casestudy/CaseStudy_Habitat76_FR.php
   * Presentation by Open Wide (French) at http://www.openwide.fr/index.php/Open-Wide/Lab/Contributions/Alfresco-Meetup-ETL-Connector-Talend


For developers
--------------
Alfresco Server Extension architecture
   * builds on the existing Alfresco Content Package (ACP) import
   * enriches it with : import of each node in its own transaction,better
name path addressing, full error logs, custom import strategies allowing
creation vs update import modes
   * XML REST / HTTP server implemented as Alfresco web Commands (though
a Java webscript would be a viable alternative today)

Building the Alfresco Server Extension
   * provide the etlconnector-alfresco Eclipse project with the Alfresco SDK
and java 1.5 dependencies
   * run Ant on the given build.xml
   * the ETL Connector Server release is in build/export/ , ready to be
added to an Alfresco installation

Building the ETL client library
   * provide the etlconnector-client Eclipse project with the java 1.5 dependencies
   * run Ant on the given build.xml
   * the ETL Connector Client release is in build/export/
   * you want to integrate it in an ETL ? Ask questions on the forums
at http://forge.alfresco.com/projects/etlconnector/