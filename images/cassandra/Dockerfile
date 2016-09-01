FROM cassandra:3.7

RUN \
  apt-get update && \
  apt-get install -y curl && \
  rm -rf /var/lib/apt/lists/*

ADD rancher-entrypoint.sh /
ADD java/target/*.jar /usr/share/cassandra/lib/
ADD cassandra.yaml /etc/cassandra
ENTRYPOINT ["/rancher-entrypoint.sh"]
CMD ["cassandra", "-f"]