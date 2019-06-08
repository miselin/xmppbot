#!/bin/bash

screen -XS wfabot quit || echo "doesn't exist yet"
screen -dmS wfabot java -jar $PWD/target/xmppbot-1.1-1-jar-with-dependencies.jar $PWD/config.properties
