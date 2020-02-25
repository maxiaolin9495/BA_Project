FROM baproject:0.0.1SNAPSHOT
WORKDIR /ba_project/Write-Test-Data-In/
RUN mvn package
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/tmp/target/Write-Test-Data-In-B-0.0.1-SNAPSHOT.jar"]