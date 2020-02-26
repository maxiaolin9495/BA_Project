FROM baproject:latest
WORKDIR /ba_project/long_term_ca_mock/
#RUN mvn package
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ba_project/long_term_ca_mock/target/long_term_ca_mock-0.0.1-SNAPSHOT.jar"]