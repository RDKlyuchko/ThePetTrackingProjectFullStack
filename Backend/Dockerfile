# Dockerfile

# Stage 1: Build the application using Gradle
FROM gradle:8.2.1-jdk17 AS build
WORKDIR /app

# Copy build files and source code
COPY build.gradle.kts settings.gradle.kts gradle /app/
COPY src /app/src

# Build the application
RUN gradle build --no-daemon

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application's port
EXPOSE 8080

# Set the entry point to run the application
CMD ["java", "-jar", "app.jar"]