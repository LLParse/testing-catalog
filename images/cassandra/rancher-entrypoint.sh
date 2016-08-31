#!/bin/bash -e
if [ "$RANCHER_DEBUG" == "true" ]; then set -x; fi

META_URL=http://rancher-metadata.rancher.internal/2015-12-19
IP=$(curl -s ${META_URL}/self/container/primary_ip)
STACK_NAME=$(curl -s ${META_URL}/self/stack/name)
SERVICE_NAME=$(curl -s ${META_URL}/self/service/name)

export CASSANDRA_LISTEN_ADDRESS=$IP

if [ "$SERVICE_NAME" == "cassandra" ]; then
  for container in $(curl -s ${META_URL}/stacks/${STACK_NAME}/services/seed/containers); do
    meta_index=$(echo $container | tr '=' '\n' | head -n1)
    container_ip=$(wget -q -O - ${META_URL}/stacks/${STACK_NAME}/services/seed/containers/${meta_index}/primary_ip)
    if [ "$CASSANDRA_SEEDS" == "" ]; then
      export CASSANDRA_SEEDS=$container_ip
    else
      export CASSANDRA_SEEDS="${CASSANDRA_SEEDS},${container_ip}"
    fi
  done
fi

# first arg is `-f` or `--some-option`
if [ "${1:0:1}" = '-' ]; then
  set -- cassandra -f "$@"
fi

# allow the container to be started with `--user`
if [ "$1" = 'cassandra' -a "$(id -u)" = '0' ]; then
  chown -R cassandra /var/lib/cassandra /var/log/cassandra "$CASSANDRA_CONFIG"
  exec gosu cassandra "$BASH_SOURCE" "$@"
fi

if [ "$1" = 'cassandra' ]; then
  : ${CASSANDRA_RPC_ADDRESS='0.0.0.0'}

  : ${CASSANDRA_LISTEN_ADDRESS='auto'}
  if [ "$CASSANDRA_LISTEN_ADDRESS" = 'auto' ]; then
    CASSANDRA_LISTEN_ADDRESS="$(hostname --ip-address)"
  fi

  : ${CASSANDRA_BROADCAST_ADDRESS="$CASSANDRA_LISTEN_ADDRESS"}

  if [ "$CASSANDRA_BROADCAST_ADDRESS" = 'auto' ]; then
    CASSANDRA_BROADCAST_ADDRESS="$(hostname --ip-address)"
  fi
  : ${CASSANDRA_BROADCAST_RPC_ADDRESS:=$CASSANDRA_BROADCAST_ADDRESS}

  if [ -n "${CASSANDRA_NAME:+1}" ]; then
    : ${CASSANDRA_SEEDS:="cassandra"}
  fi
  : ${CASSANDRA_SEEDS:="$CASSANDRA_BROADCAST_ADDRESS"}
  
  sed -ri 's/(- seeds:).*/\1 "'"$CASSANDRA_SEEDS"'"/' "$CASSANDRA_CONFIG/cassandra.yaml"

  for yaml in \
    broadcast_address \
    broadcast_rpc_address \
    cluster_name \
    endpoint_snitch \
    listen_address \
    num_tokens \
    rpc_address \
    start_rpc \
  ; do
    var="CASSANDRA_${yaml^^}"
    val="${!var}"
    if [ "$val" ]; then
      sed -ri 's/^(# )?('"$yaml"':).*/\2 '"$val"'/' "$CASSANDRA_CONFIG/cassandra.yaml"
    fi
  done

  for rackdc in dc rack; do
    var="CASSANDRA_${rackdc^^}"
    val="${!var}"
    if [ "$val" ]; then
      sed -ri 's/^('"$rackdc"'=).*/\1 '"$val"'/' "$CASSANDRA_CONFIG/cassandra-rackdc.properties"
    fi
  done
fi

exec "$@"