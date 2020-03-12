FROM maven:3.6.3-jdk-13 AS builder
WORKDIR /source
# Optimise builds by caching dependencies
COPY pom.xml .
RUN ["mvn", "dependency:go-offline"]
# Do the actual build 
COPY . .
RUN ["mvn", "package"]

# Copy compiled files to final image
FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=builder /source/target/branch-deployer.jar .
EXPOSE 8080
CMD ["java", "-jar", "/app/branch-deployer.jar"]
