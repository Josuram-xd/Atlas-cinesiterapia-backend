# syntax=docker/dockerfile:1

# ---- Build: compila el jar con el wrapper de Maven ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Primero el wrapper y el pom: la capa de dependencias se reutiliza mientras no cambien.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src src
RUN ./mvnw -q -B -DskipTests package \
 && cp target/*.jar /workspace/app.jar

# ---- Runtime: solo JRE, usuario no root ----
FROM eclipse-temurin:21-jre AS runtime

RUN groupadd --system atlas \
 && useradd --system --gid atlas --home-dir /app --shell /usr/sbin/nologin atlas

WORKDIR /app
COPY --from=build --chown=atlas:atlas /workspace/app.jar /app/app.jar

USER atlas

# Plan free de Render (512 MB): heap acotado al porcentaje de RAM del contenedor,
# GC serie (menor huella) y sin hilos de compilacion extra. Sobrescribible desde el entorno.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -XX:MaxMetaspaceSize=160m -Xss512k -XX:+ExitOnOutOfMemoryError"
# Perfil de produccion: exige DATABASE_URL, DB_USER, DB_PASSWORD, JWT_SECRET, CORS_ORIGINS, FRONTEND_URL.
ENV SPRING_PROFILES_ACTIVE=prod

# PORT lo inyecta la plataforma (Render); application.yml lo lee como server.port.
EXPOSE 8080

# exec: la JVM recibe las senales de parada (SIGTERM) directamente.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
