FROM maven:3.9-eclipse-temurin-25-noble AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/financas-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=80
EXPOSE 80
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-80} -jar app.jar"]
