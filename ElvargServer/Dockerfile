FROM arm64v8/openjdk:11-jre-slim

COPY ./build/libs/app.jar .

COPY ./data ./data

USER 1000

EXPOSE 49595

CMD ["java","-jar","app.jar", "1"]
