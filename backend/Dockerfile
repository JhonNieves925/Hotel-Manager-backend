FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


SPRING_DATASOURCE_URL=jdbc:mysql://mysql-66bd769-termuxh245-38a8.k.aivencloud.com:25975/defaultdb?useSSL=true&requireSSL=true
SPRING_DATASOURCE_USERNAME=avnadmin
SPRING_DATASOURCE_PASSWORD=AVNS_eX4o_7GyV1kSRJWbcmd
SPRING_JPA_HIBERNATE_DDL_AUTO=validate