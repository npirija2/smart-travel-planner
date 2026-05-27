FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY shared /app/shared
RUN cd /app/shared && mvn clean install -DskipTests

COPY finance-reservation-service/pom.xml /app/finance-reservation-service/
COPY finance-reservation-service/src /app/finance-reservation-service/src

RUN cd /app/finance-reservation-service && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/finance-reservation-service/target/*.jar app.jar

EXPOSE 8083

ENV JAVA_OPTS="-XX:TieredStopAtLevel=1 -Xss256k -XX:+UseSerialGC -Dspring.profiles.active=docker"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]