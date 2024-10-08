FROM openjdk:17-jdk-alpine
COPY ./mmo_server-0.8.32-all.jar /app.jar
ENTRYPOINT ["java", "-Djava.util.concurrent.ForkJoinPool.common.parallelism=12", "-jar", "/app.jar"]
