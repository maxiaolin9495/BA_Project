FROM baproject:0.0.1SNAPSHOT
WORKDIR /ba_project/Vehicle-Mock/
RUN mvn package
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ba_project/Vehicle-Mock/target/Vehicle-Mock-0.0.1-SNAPSHOT.jar"]