FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY backend/shared/ ./backend/shared/

RUN mvn -f backend/shared/pom.xml clean install -DskipTests

COPY backend/eureka-server/ ./backend/eureka-server/

RUN mvn -f backend/eureka-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/backend/eureka-server/target/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Xss256k"

EXPOSE 8761
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]