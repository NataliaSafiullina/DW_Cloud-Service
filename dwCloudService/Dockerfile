FROM openjdk:23

EXPOSE 8080

ADD target/dwCloudService-0.0.1-SNAPSHOT.jar cloudservice.jar

ENTRYPOINT ["java","-jar","cloudservice.jar"]