#!/bin/bash

ORIG=$(sha256sum target/xmppbot-1.0-1-jar-with-dependencies.jar)

# Make sure the bot is running before we rebuild it
screen -XS wfabot true || bash ./run.sh

git fetch origin
reslog=$(git log HEAD..origin/master --oneline)
if [[ "${reslog}" != "" ]] ; then
  git pull

  mvn clean verify
  NEW=$(sha256sum target/xmppbot-1.0-1-jar-with-dependencies.jar)

  if [[ "$ORIG" != "$NEW" ]]; then
    bash ./run.sh
  fi
fi
