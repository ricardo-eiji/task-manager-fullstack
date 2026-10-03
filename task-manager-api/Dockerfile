FROM eclipse-temurin:17-jdk-alpine AS build 
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw package -DskipTests

# first stage compiles the app (needs the full JDK + Maven)

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# second stage only copies the final .jar into a lightweight runtime 
    # image (just JRE). Keeps the final image small