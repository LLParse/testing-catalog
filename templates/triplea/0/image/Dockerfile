FROM alpine:3.3

RUN \
  apk update && \
  apk add curl unzip openjdk8

RUN \
  curl -L http://tenet.dl.sourceforge.net/project/triplea/TripleA/1_8_0_9/triplea_1_8_0_9_all_platforms.zip -o /triplea.zip && \
  unzip /triplea.zip && \
  rm -f /triplea.zip && \
  mv /triplea_1_8_0_9 /triplea

RUN \
  apk del unzip && \
  rm -rf /var/cache/apk/*

EXPOSE 3304
ADD run.sh /run.sh
ENTRYPOINT ["/run.sh"]
