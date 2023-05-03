FROM openjdk:11

ARG JAR
COPY ./build/libs/* clash-bot-spring-service.jar

RUN ls -lha

ENTRYPOINT ["java","-jar","clash-bot-spring-service.jar"]