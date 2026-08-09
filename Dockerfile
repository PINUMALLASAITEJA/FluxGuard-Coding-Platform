# Use lightweight Java image
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build project
RUN ./mvnw clean install -DskipTests

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "target/coding-platform-0.0.1-SNAPSHOT.jar"]