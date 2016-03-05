# XMPP Bot

[![Build Status](https://travis-ci.org/miselin/xmppbot.svg?branch=master)](https://travis-ci.org/miselin/xmppbot)

This repository contains the source for an XMPP bot which can connect to an XMPP server
and present useful features to members. In particular, the bot aims to add value to
multi-user chats.

## Building & Running

Apache Maven is used to build; it isn't too complex to set up, and it automatically solves
dependencies which saves a lot of time hunting down random .jar files.

    $ mvn package
	$ java -jar target/xmppbot-1.0-1-jar-with-dependencies.jar

On Windows, you will need to install the latest JDK and Apache Maven.

On Debian-based systems (including Ubuntu), you can run:

    $ sudo apt-get install openjdk-7-jdk openjdk-7-jre apache-maven
	$ sudo update-alternatives --config java  # Pick the 'java' from openjdk-7-jre

## Configuration

The `config.properties.example` file provides an example for configuring the bot. To use
a properties file to adjust the behaviour of the bot, pass it as the first argument to the
invocation:

    $ java -jar xmppbot.jar config.properties
	
`logging.properties` can also be used to manage the logging configuration; its usage is
straightforward:

    $ java -jar xmppbot.jar -Djava.util.logging.config.file="logging.properties"
	
The default `logging.properties` file enables logging at the `FINEST` level to ease
debugging. If a logging properties file is not specified, the default log level is used
(typically, `INFO`).

## Running Tests

A test suite is available and can be used to verify the behavior of commands hasn't changed.

To run the full test suite, just run:

    $ mvn test

## Deployment

At least one instance of the bot is using the `autodeploy.sh` script.

Commits pushed to this repository will be automatically pulled and built. If the new JAR is
different to the running one, the bot will be restarted automatically.

## JDK/JRE Dependency

This package depends on a JDK/JRE based on Java 7 or above.
