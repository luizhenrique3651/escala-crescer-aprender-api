FROM maven:3.9.7-eclipse-temurin-21-alpine as build


COPY src /app/src
COPY pom.xml /app

WORKDIR /app
RUN mvn clean install -DskipTests

FROM openjdk:21-ea-21-oracle

COPY --from=build /app/target/escala-0.0.1-SNAPSHOT.jar /app/app.jar

WORKDIR /app

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]