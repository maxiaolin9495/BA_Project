FROM baproject:0.0.1SNAPSHOT
WORKDIR /ba_project/Long-Term-CA-Mock/
RUN mvn package
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ba_project/Long-Term-CA-Mock/target/Long-Term-CA-Mock-0.0.1-SNAPSHOT.jar"]