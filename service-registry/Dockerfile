# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
ARG MAVEN_CLI_OPTS="-B -ntp -Dhttp.keepAlive=true -Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 -Dmaven.wagon.rto=120000"
COPY pom.xml .
RUN mvn $MAVEN_CLI_OPTS dependency:go-offline
COPY src ./src
RUN mvn $MAVEN_CLI_OPTS clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
