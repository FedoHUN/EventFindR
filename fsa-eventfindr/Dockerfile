FROM maven:3.9.6-eclipse-temurin-21 as builder

WORKDIR /application

COPY pom.xml .
COPY application application

RUN mvn package -DskipTests
RUN mkdir build && cd build && java -Djarmode=layertools -jar ../application/springboot/target/*.jar extract

FROM eclipse-temurin:21.0.1_12-jdk-alpine
WORKDIR /application

COPY --from=builder application/build/dependencies/ ./
COPY --from=builder application/build/spring-boot-loader/ ./
COPY --from=builder application/build/snapshot-dependencies/ ./
COPY --from=builder application/build/application/ ./

RUN addgroup -S boot && adduser -S boot -G boot
USER boot

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher"]
