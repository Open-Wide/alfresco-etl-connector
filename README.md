======================================================
ETL Connector for Alfresco - Alfresco Server Extension
http://knowledge.openwide.fr/bin/view/Main/AlfrescoETLConnector
Copyright (C) 2008-2012 Open Wide SA
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
   

Getting Started
---------------
Client side (ETL tool) : get a *compatible* ETL tool release
   * *Talend 3.1*, using the tAlfrescoOutput component - http://www.talend.fr

Server-side (Alfresco repository) : get the release *compatible* with your
alfresco server at https://github.com/OpenWide-SI/alfresco-etl-connector/downloads .
   * etlconnector-alfresco2.1 : *validated with 2.1 Entreprise for Tomcat*, should
work with all 2.x Alfresco releases, reported to work on Labs 2.9b
   * etlconnector-alfresco3.1 : *tested with 3.1 Entreprise for Tomcat*
   * etlconnector-alfresco3.2 : *tested with 3.2 Community for Tomcat*
   * Alternatively, it may be provided in compatible ETL release bundles.

Server-side installation
   * extract the WEB-INF subdirectory from it ant put it in your alfresco webapp,
ex. $ALF_HOME/tomcat/webapps/alfresco/WEB-INF/lib
   * restart alfresco. If it's been correctly installed, there should be in the
startup logs (alfresco.log) a line like this one :

19:20:49,635 INFO [org.alfresco.config.source.UrlConfigSource] Found META-INF/web-client-config-custom.xml in file:/C:/dev/workspace/etlconnector-alfresco-deploy/tomcat/webapps/alfresco/WEB-INF/lib/etlconnector-alfresco_1.0.jar
   
Test
   * You can test it by using the samples provided in the companion project
etlconnector-samples , and a compatible ETL like Talend 3.1 on the client side.

*For the Quitus sample, using Talend* :
   * put the etlconnector-samples*jar in WEB-INF/lib in your alfresco web
application
   * start the Alfresco server (after having installed the ETL Connector
extension)
   * import the etlconnector-samples/quitus/GED_TECHNIQUE.acp document package
in a new "GED TECHNIQUE" folder within the company home folder, using the
custom action wizard in the Alfresco web interface
   * start Talend
   * import the etlconnector-samples/quitus/talend/ALFRESCO_ETLCONNECTOR_QUITUS
as a Talend workspace project
   * open the single Talend document import job ("ALFRESCO IMPORT_QUITUS 0.1")
   * click in the left panel on Context > PATHS 0.1 to open the configuration
dialog and there set the PATH_SOURCE variable of the job to the location of the
etlconnector-samples/quitus folder
   * run it in Talend : the complex document tree has been imported in Alfresco,
including custom metadata and associations


Documentation
--------------
ETL Connector
   * Downloads at https://github.com/OpenWide-SI/alfresco-etl-connector/downloads
   * Website at http://knowledge.openwide.fr/bin/view/Main/AlfrescoETLConnector
   * Alfresco Add-Ons page at https://addons.alfresco.com/addons/alfresco-etl-connector
   * Mailing list at 
   * Source code at https://github.com/OpenWide-SI/alfresco-etl-connector
   * Ohloh project page at http://www.ohloh.net/p/alfrescoetlconnector
   
Using ETL Connector with Talend
   * Talend documentation in the Talend 3.1 manual at http://www.talend.com/resources/documentation.php
   * Tutorials by Nicolas Feray about using a CSV filing plan, error log handling, rights management at http://nicolas.feray.free.fr/blog/?cat=5
   * Talend use case at http://www.talend.com/open-source-provider/casestudy/CaseStudy_Habitat76_FR.php
   * Presentation by Open Wide (French) at http://www.openwide.fr/index.php/Open-Wide/Lab/Contributions/Alfresco-Meetup-ETL-Connector-Talend
   * Tutorial courtesy of Landry Louam (French) about using an XML filing plan at http://landry-kouam.developpez.com/tutoriels/solutions-entreprise/ecm/integration-massive-document-dans-alfresco-suivant-plan-classement-avec-talend-open-studio


FAQ
---
What is ETL Connector interesting for ?
   * ETL Connector's main benefits stem from the productivity gains
inherent to ETL tools : allowing to design graphically how existing
information maps to Alfresco metadata, in an easy manner and using an
ETL's raw power when it comes to accessing data sources in the Information
System. Moreover, an ETL provides all kind of tools to first partition
data in smaller batches, and afterwards handle errors.

Performances
   * successfully tested on an import job creating 4000 nodes in 30
minutes, using the Talend ETL to target an Alfresco 2.1 Entreprise server sitting on Oracle.
   * in some deployment environments, a sustained speed of 12 nodes per
second has even been experienced. 


For developers
--------------
Alfresco Server Extension architecture
   * builds on the existing Alfresco Content Package (ACP) import code
   * enriches it with : import of each node in its own transaction,better
name path addressing, full error logs, custom import strategies allowing
creation vs update import modes
   * XML REST / HTTP server implemented as Alfresco web Commands (though
a Java webscript would be a viable alternative today)

Building the Alfresco Server Extension
   * provide the etlconnector-alfresco Eclipse project with the Alfresco SDK
(see which one in .classpath) and java 1.5 dependencies
   * run Ant on the given build.xml
   * the ETL Connector Server release is in build/export/ , ready to be
added to an Alfresco installation
   * to support a newer version of Alfresco : update source overrides to the latest
alfresco source code (ImporterComponent -> ContentImporterComponentBase, ViewParser
-> ViewParserBase, CommandServlet > ContentImporterCommandServlet) and reapply
changes on them (see javadoc) ; also update their Spring bean definitions
(contentImporterComponent & contentViewParser) in talendalfresco-services-context.xml
according to their newer versions ; update other alfresco configuration (web.xml,
web-client-config-custom.xml and in samples).

Building the ETL client library
   * provide the etlconnector-client Eclipse project with the java 1.5 dependencies
   * run Ant on the given build.xml
   * the ETL Connector Client release is in build/export/
   * you want to integrate it in another ETL ? Contact us
at http://knowledge.openwide.fr/bin/view/Main/AlfrescoETLConnector/

Alfresco ETL connector plugin for Talend
   * see plugin definition at http://talendforge.org/trac/tos/browser/trunk/org.talend.designer.components.localprovider/components/tAlfrescoOutput/tAlfrescoOutput_java.xml
   * custom Talend Studio dialog box to choose Alfresco document type
definition files, see source at http://talendforge.org/trac/tos/browser/trunk/org.talend.designer.alfrescooutput
   * plugin runtime : see libraries (including Alfresco ETL Connector client
library) and JET Java templates in source at http://talendforge.org/trac/tos/browser/trunk/org.talend.designer.components.localprovider/components/tAlfrescoOutput
   * how to build : see Talend Studio developer documentation and especially
http://www.talendforge.org/wiki/doku.php?id=dev:run_from_svn


Release Notes - 1.3
-------------------
server
   * migrated to and tested with Alfresco 3.2 Community
   * disabled command servlet request-wide transactions for ImportCommand, allowing
to use propagating transactions and fully transactionalized repository services. Done
by also overriding CommandServlet code and replacing it in web.xml (though it could
exist along the original one but would require changing client ETL code)
   * reapplying rules & behaviours is now done in its own transaction. However a
separate result line is returned only in error case.
   
client
   * now custom CommandServlet URL path can be specified (typically when deploying
etlconnector-alfresco3.1.3+'s overriden ContentImporterCommandServlet along the
original one).
   * patched cm:folder / view:associations / cm:container hierarchy which was missing
view:associations (but to no harm in most cases)
   * improved test framework, added test of duplicate child name error case


Release Notes - 1.2
-------------------
server
   * migrated to and tested with Alfresco 3.1 Entreprise


Release Notes - 1.1
-------------------
server
   * namePath now resolved using db rather than lucene (since custom lucene analyzers
may make it fail,  e.g. like those for French locale in 3.1 which remove ending "s")
   * tested with Alfresco 2.1.1 Entreprise
   * build script now outputs alfresco server version, the right build time & user

client
   * fully compatible with 1.0
   * ACP XML : more robust writing of properties and associations. Now null is allowed
as "no value" and String is allowed for single-valued associations, instead of
outputting wrongly formed XML because of exception.
   * removed warning from org.apache.commons.httpclient.HttpMethodBase getResponseBody
   
overall
   * improved README (Talend plugin developer doc), build, copyright, moved links
from forge to github & addons


Release Notes - 1.0
-------------------
First release. Tested with Alfresco 2.1 Entreprise and Talend 3.1 .
