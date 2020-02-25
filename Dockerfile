FROM maven:3.5-jdk-8
COPY . / /ba_project/
COPY /lib/rt.jar /
WORKDIR /ba_project
RUN mvn clean install