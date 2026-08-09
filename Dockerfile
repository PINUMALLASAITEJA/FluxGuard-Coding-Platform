FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

# 🔥 give execute permission
RUN chmod +x mvnw

# build
RUN ./mvnw clean install -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/coding-platform-0.0.1-SNAPSHOT.jar"]