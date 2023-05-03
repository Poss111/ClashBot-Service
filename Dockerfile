FROM openjdk:11

COPY ./build/libs/clash-bot-spring-service-*-SNAPSHOT.jar clash-bot-spring-service.jar

RUN ls -lha

ENTRYPOINT ["java","-jar","clash-bot-spring-service.jar"]