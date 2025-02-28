FROM openjdk:21

ARG JAR
COPY build/libs/clash-bot-spring-service-1.0.0.jar clash-bot-spring-service.jar

ENTRYPOINT ["java","-jar","clash-bot-spring-service.jar"]