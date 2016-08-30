#!/bin/bash

META_URL=http://rancher-metadata.rancher.internal/2015-12-19
IP=$(curl -s ${META_URL}/self/container/primary_ip)
export CASSANDRA_LISTEN_ADDRESS=$IP

exec /docker-entrypoint.sh "$@"