FROM amazoncorretto:11-alpine3.17
LABEL org.opencontainers.image.source=https://github.com/dinukagtngroup/sample-aws-batch
COPY target/app.jar app.jar
CMD ["java","-jar","/app.jar"]