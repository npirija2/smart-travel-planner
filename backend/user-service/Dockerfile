FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY backend/shared/ ./backend/shared/

RUN mvn -f backend/shared/pom.xml clean install -DskipTests

COPY backend/user-service/ ./backend/user-service/

RUN mvn -f backend/user-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/backend/user-service/target/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Xss256k"

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]