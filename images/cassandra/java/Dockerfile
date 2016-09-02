FROM maven:3.3.9-jdk-8

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD pom.xml /usr/src/app/
RUN mvn dependency:resolve

ADD src /usr/src/app/src
RUN mvn install

