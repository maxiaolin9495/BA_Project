FROM baproject:latest
WORKDIR /ba_project/authorization_server_mock/
#RUN mvn package
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ba_project/authorization_server_mock/target/authorization_server_mock-0.0.1-SNAPSHOT.jar"]