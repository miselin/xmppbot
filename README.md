# WikiForAll Jabber Bot

This repository contains the source for the jabber bot "skynet" which lives on the WikiForAll
Jabber server.

## Building & Running

Apache Maven is used to build; it isn't too complex to set up, and it automatically solves
dependencies which saves a lot of time hunting down random .jar files.

    $ mvn package
	$ java -jar target/wfabot-1.0-1-jar-with-dependencies.jar

## JDK/JRE Dependency

This package depends on a JDK/JRE based on Java 7.

## Certificates

The wikiforall.net Jabber server uses a certificate from Let's Encrypt. You will need to use
a tool like [Portecle](https://sourceforge.net/projects/portecle/files/latest/download) to
manage the `$JAVA_HOME/lib/security/cacerts` file (it is worth doing so for both the JDK
and the JRE cacerts path).

The three certificates, in `.pem` format, at https://letsencrypt.org/certificates/ should be
installed as trusted CAs. This will allow the bot to connect using TLS without error.