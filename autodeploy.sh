#!/bin/bash

ORIG=$(sha256sum target/wfabot-1.0-1-jar-with-dependencies.jar)

git fetch origin
reslog=$(git log HEAD..origin/master --oneline)
if [[ "${reslog}" != "" ]] ; then
  git pull

  mvn package
  NEW=$(sha256sum target/wfabot-1.0-1-jar-with-dependencies.jar)

  if [[ "$ORIG" != "$NEW" ]]; then
    # restart daemon
    screen -XS wfabot quit || echo "doesn't exist yet"
    screen -dmS wfabot java -jar $PWD/target/wfabot-1.0-1-jar-with-dependencies.jar
  fi
fi
