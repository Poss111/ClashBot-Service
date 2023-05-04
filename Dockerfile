FROM openjdk:11

ARG JAR
COPY ./$JAR clash-bot-spring-service.jar

ENTRYPOINT ["java","-jar","clash-bot-spring-service.jar"]