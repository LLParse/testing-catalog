FROM maven:3.3.9-jdk-8

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD src/ pom.xml /usr/src/app/
RUN mvn install

