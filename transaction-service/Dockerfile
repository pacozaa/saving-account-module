FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Copy maven wrapper and parent pom
COPY ../pom.xml ../pom.xml
COPY ../mvnw ../mvnw
COPY ../.mvn ../.mvn

# Copy this service
COPY pom.xml .
COPY src src

# Build
RUN cd .. && ./mvnw clean package -pl transaction-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/app/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app.jar"]
