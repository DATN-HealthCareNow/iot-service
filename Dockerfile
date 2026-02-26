FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies (to cache them)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Run the application
# We use the jar that was just built. The version matches pom.xml
CMD ["java", "-jar", "target/iot-service-0.0.1-SNAPSHOT.jar"]
