FROM maven:3.8.2-adoptopenjdk-16 as builder
WORKDIR application
COPY src src
COPY pom.xml .
RUN mvn spring-boot:build-image -Pproduction
RUN cp target/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM adoptopenjdk/openjdk16
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
