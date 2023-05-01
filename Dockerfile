FROM openjdk:11
EXPOSE 9090
ADD target/auth2factor-0.0.1-SNAPSHOT.jar auth2factor.jar
ENTRYPOINT ["java","-jar","/auth2factor.jar"]