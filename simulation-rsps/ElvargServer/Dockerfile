FROM arm64v8/openjdk:17-jdk-slim

COPY ./build/libs/app.jar .

COPY ./data ./data

USER 1000

CMD ["java", "-XX:+UseParallelGC","-XX:OnOutOfMemoryError=\"shutdown -r\"", "-Xmx3440M", "-Xms2048M", "-jar","app.jar", "1"]
