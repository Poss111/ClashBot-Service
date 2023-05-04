FROM openjdk:11

COPY ./build/libs/* clash-bot-spring-service.jar

ENTRYPOINT ["java","-jar","clash-bot-spring-service.jar"]