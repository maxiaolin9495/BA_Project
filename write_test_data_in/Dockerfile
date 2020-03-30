FROM baproject:latest
WORKDIR /ba_project/write_test_data_in/
#RUN mvn package
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ba_project/write_test_data_in/target/write_test_data_in-0.0.1-SNAPSHOT.jar"]