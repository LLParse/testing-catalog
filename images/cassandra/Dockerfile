FROM cassandra:3.7

RUN \
  apt-get update && \
  apt-get install -y curl && \
  rm -rf /var/lib/apt/lists/*

ADD cassandra.yaml /etc/cassandra
ADD java/target/rancher-cassandra.jar rancher-entrypoint.sh /
ENTRYPOINT ["/rancher-entrypoint.sh"]
CMD ["cassandra", "-f"]