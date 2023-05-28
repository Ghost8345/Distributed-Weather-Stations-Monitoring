FROM openjdk:11
COPY out/artifacts/CentralStation_jar/CentralStation.jar .
COPY src/main/resources/avro.avsc .
ENTRYPOINT ["java","-jar","CentralStation.jar"]