# Skeleton only - to be completed once the Spring Boot backend produces a
# runnable jar (Federica's task). Do not treat as final.

# TODO: build stage
# FROM maven:3.9-eclipse-temurin-21 AS build
# WORKDIR /app
# COPY pom.xml .
# COPY src ./src
# RUN mvn -q clean package -DskipTests

# TODO: runtime stage
# FROM eclipse-temurin:21-jre
# WORKDIR /app
# COPY --from=build /app/target/*.jar app.jar
# EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "app.jar"]
