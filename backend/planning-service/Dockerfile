FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY shared /app/shared
RUN cd /app/shared && mvn clean install -DskipTests

COPY planning-service/pom.xml /app/planning-service/
COPY planning-service/src /app/planning-service/src

RUN cd /app/planning-service && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/planning-service/target/*.jar app.jar

EXPOSE 8082

ENV JAVA_OPTS="-XX:TieredStopAtLevel=1 -Xss256k -XX:+UseSerialGC -Dspring.profiles.active=docker"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]