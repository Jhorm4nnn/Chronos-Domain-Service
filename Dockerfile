# Etapa 1: compila con el Maven Wrapper. Las pruebas se corren antes, con ./mvnw verify.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn ./.mvn
RUN ./mvnw -B -q dependency:go-offline
COPY src ./src
RUN ./mvnw -B -q -DskipTests package

# Etapa 2: solo el JRE y el jar, con un usuario sin privilegios y el perfil prod (MySQL).
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 turnos
COPY --from=build /app/target/domain-service-turnos-*.jar app.jar
USER turnos
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8082
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
