FROM gradle:8 AS build

WORKDIR /code
COPY . /code/
RUN gradle bootJar --console=plain

FROM openjdk:21

COPY --from=build /code/build/libs/musick-0.0.1-SNAPSHOT.jar /app.jar
CMD ["java", "-jar", "/app.jar"]
