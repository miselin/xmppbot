#!/bin/bash

ORIG=$(sha256sum target/xmppbot-1.0-1-jar-with-dependencies.jar)

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
