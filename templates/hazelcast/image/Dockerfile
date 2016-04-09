FROM centos

RUN \
  yum install -y java-1.8.0-openjdk python-jinja2

ENV HZ_VERSION 3.6.2
RUN \
  cd / && \
  curl -L "http://download.hazelcast.com/download.jsp?version=hazelcast-$HZ_VERSION&type=tar&p=rancher" -o hazelcast-$HZ_VERSION.tar.gz && \
  tar xzf hazelcast-$HZ_VERSION.tar.gz && \
  rm -f hazelcast-$HZ_VERSION.tar.gz && \
  mv hazelcast-$HZ_VERSION/ hazelcast/

EXPOSE 5701
ADD config.xml.j2 /config.xml.j2
ADD run.py /run.py
ENTRYPOINT ["/run.py"]
