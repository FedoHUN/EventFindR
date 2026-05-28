FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /application

COPY pom.xml .
COPY application application

RUN mvn package -DskipTests
RUN mkdir build && cd build && java -Djarmode=layertools -jar ../application/springboot/target/*.jar extract

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /application

COPY --from=builder application/build/dependencies/ ./
COPY --from=builder application/build/spring-boot-loader/ ./
COPY --from=builder application/build/snapshot-dependencies/ ./
COPY --from=builder application/build/application/ ./

RUN addgroup -S -g 10001 boot && adduser -S -D -H -u 10001 -G boot boot
USER boot

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher"]
