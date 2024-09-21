# https://hub.docker.com/layers/library/openjdk/17/images/sha256-98f0304b3a3b7c12ce641177a99d1f3be56f532473a528fda38d53d519cafb13?context=explore
FROM openjdk:17

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

CMD ["./mvnw", "spring-boot:run"]