FROM amazoncorretto:11-alpine3.17
COPY target/app.jar app.jar
CMD ["java","-jar","/app.jar"]