FROM baproject:latest
WORKDIR /ba_project/vehicle_mock/
#RUN mvn package
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ba_project/vehicle_mock/target/vehicle_mock-0.0.1-SNAPSHOT.jar"]