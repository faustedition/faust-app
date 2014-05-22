==Preliminary note==

<strong>The Faust Edition application is still under active development. These instructions reflect the current state of development, but they are not final and are expected to change!</strong>

The application is a Java application compiling to a single JAR file containing the application and all its dependencies. Running the application involves essentially copying the JAR file and running it. A web server is integrated. It might be necessary to install some additional utility programs, see below.

==Prerequisites==

===Hardware===

* 800 GB disk space
** 2GB Linux server system with libraries
** <1 GB application data
** 600 GB images
** <1 GB XML files
** extra space for caching, temporary files etc.
* 1024 MB RAM

===Software===
These installation instructions are based on a fresh minimal install of [http://www.ubuntu.com/download/server Ubuntu Linux Server 14.04 LTS]. For other systems, they might need to be modified accordingly.

These instructions assume you to have root priviledges.

The application relies on the following libraries/packages: Java 7 Runtime Environment

To install them, type
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   apt-get install openjdk-7-jre
</pre>
These instructions also assume that you have unzip installed, if that is not the case, install it by typing
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   apt-get install unzip
</pre>

====Possibly required software packages for extended functionality====

Some software packages might further be required for extended functionality such as generating PDF output and pre-calculating the transcriptions. All of these are mature packages and readily available in e.g. the Ubuntu repositories.

* [https://www.python.org/ Python2.6]
* [http://www.imagemagick.org/ Imagemagick]
* [http://www.graphviz.org/ Graphviz]
* [http://www.latex-project.org/ LaTeX]
* [http://phantomjs.org/ PhantomJS]

It is not yet decided whether these will be required. They don't need to be installed for now.

==Quick Start using Pre-Built Binaries==
The easiest way to use the edition framework is to use the pre-built binaries. If you want to build them yourself, please see section [[#Building_from_Source|Building from Source]]

===Creating Directories===

By default, the application is looking for its installation files in <code>/opt/faustedition</code> and for its data files (xml, images) in <code>/opt/faustedition/data</code>. To change the location of these files, please see section [[#Changing_the_Configuration|Changing the Configuration]] or work with symbolic links.

To create the directory for the edition data and subdirectories for the databases, type
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   mkdir /opt/faustedition /var/opt/faustedition
</pre>
===Downloading Example Data===

A small set of example data is being maintaned at https://github.com/faustedition/data

To download it, type 
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   cd /var/opt/faustedition
   wget --content-disposition https://codeload.github.com/faustedition/data/zip/master
   unzip data-master.zip
   mv data-master data
</pre>
===Downloading the Binaries===
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   cd /opt/faustedition
   wget --no-check-certificate --content-disposition 'https://faustedition.uni-wuerzburg.de/nexus/service/local/artifact/maven/redirect?r=snapshots&g=de.faustedition&a=faust&c=app&p=zip&v=LATEST'
   unzip faust-*-app.zip
</pre>

===Populating the Database from XML data===

<pre style="white-space: pre-wrap; word-wrap: break-word;">
   rm -rf /var/opt/faustedition/db/*
   java -Xmx512m -Dfile.encoding=UTF-8 -cp /opt/faustedition/app/lib/faust-1.3-SNAPSHOT.jar de.faustedition.transcript.TranscriptBatchReader
</pre>

===Starting the Server===
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   java -Xmx512m -server -Dfile.encoding=UTF-8 -jar /opt/faustedition/app/lib/faust-1.3-SNAPSHOT.jar
</pre>

The web server runs on port 80 (HTTP) by default. If you want to change this (for example for a reverse proxy setup), please see section [[#Changing_the_Configuration|Changing the Configuration]]

==Editing the Edition Data==

The example XML and image files in <code>/var/opt/faustedition/data</code> can now be replaced by the actual data files, using the same format.

Afterwards, it is necessary to stop the server and perform a database update as described in section [[#Populating_the_Database_from_XML_data|Populating the Database from XML data]]

==Editing the Website Content==

To edit the website content, for example the "About the Project" page, you can modify the FTL files in <code>/opt/faustedition/app/templates</code>

==Changing the Configuration==

General options such as data directories, server port, context path etc. can be configured.

The edition contains a default config file that can be used as a template. Extract it to the installation directory

<pre style="white-space: pre-wrap; word-wrap: break-word;">
   cd /opt/faustedition
   unzip app/lib/faust-1.3-SNAPSHOT.jar config-default.properties
   mv config-default.properties config-local.properties
</pre>

After editing the <code>config-local.properties</code> file, start the server with the config file path as its argument.

<pre style="white-space: pre-wrap; word-wrap: break-word;">
   java -Xmx512m -server -Dfile.encoding=UTF-8 -jar app/lib/faust-1.3-SNAPSHOT.jar /opt/faustedition/config-local.properties
</pre>

==Building from Source==
===Dependencies===

* Git http://git-scm.com/
* Maven 2 https://maven.apache.org/
* Open JDK 7 http://openjdk.java.net/

=== Installing the Dependencies ===
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   apt-get install git maven2 openjdk-7-jdk
</pre>

===Downloading the source===

<pre style="white-space: pre-wrap; word-wrap: break-word;">
   mkdir -p /opt/faustedition/src
   cd /opt/faustedition/src
   git clone https://github.com/faustedition/app.git
</pre>

===Compiling and Install the tei-odd-plugin for Maven===
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   cd app/tei-odd-plugin
   mvn install
</pre>

===Compiling and Install the XML Schema===
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   cd ../faust-schema
   mvn install
</pre>
===Building the Web Application===
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   cd ../faust
   mvn -Dmaven.test.skip=true install
</pre>
===Starting the Web Application===
<pre style="white-space: pre-wrap; word-wrap: break-word;">
   export MAVEN_OPTS="-server -Xmx1024m"
   mvn exec:java -Dexec.mainClass="de.faustedition.Server"
</pre>

This relies on pre-build Maven artifacts from other servers. TODO: How to build those.
