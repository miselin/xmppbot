# WikiForAll Jabber Bot

This repository contains the source for the jabber bot "skynet" which lives on the WikiForAll
Jabber server.

## Building & Running

Apache Maven is used to build; it isn't too complex to set up, and it automatically solves
dependencies which saves a lot of time hunting down random .jar files.

    $ mvn package
	$ java -jar target/wfabot-1.0-1-jar-with-dependencies.jar

## JDK/JRE Dependency

This package depends on a JDK/JRE based on Java 8.
