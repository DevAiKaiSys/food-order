# ===============================
# Stage 1: Build
# ===============================
FROM maven:3.9.11 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first
COPY my-api/pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY my-api/src ./src

# Build application
RUN mvn clean package -DskipTests

# ===============================
# Stage 2: Runtime
# ===============================
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/food-order-*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
# ENTRYPOINT ["java", "-jar", "app.jar"]
