Preliminary note
----------------

The application is a Java application compiling to a single JAR file
containing the application and all its dependencies. Running the
application involves essentially copying the JAR file and running it. A
web server is integrated. It might be necessary to install some
additional utility programs, see below.

Prerequisites
-------------

### Hardware

-   800 GB disk space
    -   2GB Linux server system with libraries
    -   \<1 GB application data
    -   600 GB images
    -   \<1 GB XML files
    -   extra space for caching, temporary files etc.

-   1024 MB RAM

### Software

These installation instructions are based on a fresh minimal install of
[Ubuntu Linux Server 14.04 LTS](http://www.ubuntu.com/download/server).
For other systems, they might need to be modified accordingly.

These instructions assume you to have root privileges. 

The application relies on the following libraries/packages: Java 7
Runtime Environment

To install them, type

       apt-get install openjdk-7-jre

These instructions also assume that you have unzip installed, if that is
not the case, install it by typing

       apt-get install unzip

#### Possibly required software packages for extended functionality

Some software packages might further be required for extended
functionality such as generating PDF output and pre-calculating the
transcriptions. All of these are mature packages and readily available
in e.g. the Ubuntu repositories.

-   [Python2.6](https://www.python.org/)
-   [Imagemagick](http://www.imagemagick.org/)
-   [Graphviz](http://www.graphviz.org/)
-   [LaTeX](http://www.latex-project.org/)
-   [PhantomJS](http://phantomjs.org/)

It is not yet decided whether these will be required. They don't need to
be installed for now.

Quick Start using Pre-Built Binaries
------------------------------------

The easiest way to use the edition framework is to use the pre-built
binaries. If you want to build them yourself, please see section
[Building from Source](#Building_from_Source "wikilink")

### Creating Directories

By default, the application is looking for its installation files in
`/opt/faustedition` and for its data files (xml, images) in
`/opt/faustedition/data`. To change the location of these files, please
see section [Changing the
Configuration](#Changing_the_Configuration "wikilink") or work with
symbolic links.

To create the directory for the edition data and subdirectories for the
databases, type

       mkdir /opt/faustedition /var/opt/faustedition

### Downloading Example Data

A small set of example data is being maintaned at
<https://github.com/faustedition/faust-example-data>

To download it, type

       cd /var/opt/faustedition
       wget --content-disposition https://codeload.github.com/faustedition/faust-example-data/zip/master
       unzip faust-example-data-master.zip
       mv faust-example-data-master data

### Downloading the Binaries

       cd /opt/faustedition
       wget --no-check-certificate --content-disposition 'https://faustedition.uni-wuerzburg.de/nexus/service/local/artifact/maven/redirect?r=snapshots&g=de.faustedition&a=faust&c=app&p=zip&v=LATEST'
       unzip faust-*-app.zip

### Populating the Database from XML data

       rm -rf /var/opt/faustedition/db/*
       java -Xmx512m -Dfile.encoding=UTF-8 -cp /opt/faustedition/app/lib/faust-1.4-SNAPSHOT.jar de.faustedition.transcript.TranscriptBatchReader

### Starting the Server

       java -Xmx512m -server -Dfile.encoding=UTF-8 -jar /opt/faustedition/app/lib/faust-1.4-SNAPSHOT.jar

The web server runs on port 80 (HTTP) by default. If you want to change
this (for example for a reverse proxy setup), please see section
[Changing the Configuration](#Changing_the_Configuration "wikilink").
The server (and data import) can also be run as a non-root dedicated user, 
which has to have write access to `/var/opt/faustedition/db`

Editing the Edition Data
------------------------

The example XML and image files in `/var/opt/faustedition/data` can now
be replaced by the actual data files, using the same format.

Afterwards, it is necessary to stop the server and perform a database
update as described in section [Populating the Database from XML
data](#Populating_the_Database_from_XML_data "wikilink")

Editing the Website Content
---------------------------

To edit the website content, for example the "About the Project" page,
you can modify the FTL files in `/opt/faustedition/app/templates`

Changing the Configuration
--------------------------

General options such as data directories, server port, context path etc.
can be configured.

The edition contains a default config file that can be used as a
template. Extract it to the installation directory

       cd /opt/faustedition
       unzip app/lib/faust-1.4-SNAPSHOT.jar config-default.properties
       mv config-default.properties config-local.properties

After editing the `config-local.properties` file, start the server with
the config file path as its argument.

       java -Xmx512m -server -Dfile.encoding=UTF-8 -jar /opt/faustedition/app/lib/faust-1.4-SNAPSHOT.jar /opt/faustedition/config-local.properties

Building from Source
--------------------

### Dependencies

-   Git <http://git-scm.com/>
-   Maven 2 <https://maven.apache.org/>
-   Open JDK 7 <http://openjdk.java.net/>

### Installing the Dependencies

       apt-get install git maven2 openjdk-7-jdk

### Downloading the source

       mkdir -p /opt/faustedition/src
       cd /opt/faustedition/src
       git clone https://github.com/faustedition/app.git

### Compiling and Install the tei-odd-plugin for Maven

       cd app/tei-odd-plugin
       mvn install

### Compiling and Install the XML Schema

       cd ../faust-schema
       mvn install

### Building the Web Application

       cd ../faust
       mvn -Dmaven.test.skip=true install

### Starting the Web Application

The application looks for data in default directories. If you want to use your own directories, you need to specify them in a confing file.

       cp target/classes/config-default.properties config-local.properties

Edit the file config-local.properties, creating the appropriate directories.

Populate the database from XML data

       export MAVEN_OPTS="-server -Xmx1024m"

       mvn exec:java -Dexec.mainClass="de.faustedition.transcript.TranscriptBatchReader" -Dexec.args="config-local.properties"

Start the webapp

       mvn exec:java -Dexec.mainClass="de.faustedition.Server" -Dexec.args="config-local.properties"
