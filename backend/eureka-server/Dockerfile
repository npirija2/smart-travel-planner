FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY eureka-server/pom.xml /app/eureka-server/
COPY eureka-server/src /app/eureka-server/src

RUN cd /app/eureka-server && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/eureka-server/target/*.jar app.jar

EXPOSE 8761

ENV JAVA_OPTS="-XX:TieredStopAtLevel=1 -Xss256k -XX:+UseSerialGC -Dspring.profiles.active=docker"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]