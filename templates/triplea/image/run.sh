#!/bin/sh

# may need adjustment
MAX_HEAP=256
PORT=3304

# we shouldn't have to to change this very often
LOBBY_HOST=173.255.229.134
LOBBY_PORT=3303

java \
  -server \
  -Xmx${MAX_HEAP}m \
  -Djava.awt.headless=true \
  -classpath /triplea/bin/triplea.jar \
  games.strategy.engine.framework.headlessGameServer.HeadlessGameServer \
    triplea.game.host.console=true \
    triplea.game.host.ui=false \
    triplea.game= \
    triplea.server=true \
    triplea.port=${PORT} \
    triplea.lobby.host=${LOBBY_HOST} \
    triplea.lobby.port=${LOBBY_PORT} \
    triplea.name=Bot${INDEX}_${NAME} \
    triplea.lobby.game.hostedBy=Bot${INDEX}_${NAME} \
    triplea.lobby.game.supportEmail=${SUPPORT_EMAIL} \
    triplea.lobby.game.comments="automated_host"
