FROM cassandra:3.7

RUN \
  apt-get update && \
  apt-get install -y curl && \
  rm -rf /var/lib/apt/lists/*

ADD rancher-entrypoint.sh /
ENTRYPOINT ["/rancher-entrypoint.sh"]
CMD ["cassandra", "-f"]