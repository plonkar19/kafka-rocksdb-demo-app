FROM openjdk:11
ARG JAR_FILE=build/libs/kafka-rocksdb-demo-app-1.0.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
