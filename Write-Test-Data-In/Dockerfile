FROM baproject:latest
WORKDIR /ba_project/Write-Test-Data-In/
#RUN mvn package
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ba_project/Write-Test-Data-In/target/Write-Test-Data-In-0.0.1-SNAPSHOT.jar"]