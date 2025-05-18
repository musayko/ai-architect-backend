FROM eclipse-temurin:17-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml to leverage Docker cache
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download project dependencies
# This step is cached so it only runs if pom.xml changes
RUN ./mvnw dependency:go-offline -B

# Copy the project source code
COPY src ./src

# Package the application
# This will also run tests by default. To skip tests: RUN ./mvnw package -DskipTests
RUN ./mvnw package -DskipTests

EXPOSE 8080

# Define the command to run the application
# The JAR file will be in target/ai-architect-backend-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "target/ai-architect-backend-0.0.1-SNAPSHOT.jar"]