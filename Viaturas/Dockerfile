# Etapa 1: Build do projeto
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

# Agente fixo para reproduzir o experimento; nenhuma credencial entra na imagem.
FROM alpine:3.21 AS apm-agent
WORKDIR /download
RUN wget -q https://download.newrelic.com/newrelic/java-agent/newrelic-agent/9.4.0/newrelic-java.zip \
    && echo 'c9aecbe18e90e7fff2385678144ce023cfd4dda91f89a2804bc4a6414078adbc  newrelic-java.zip' | sha256sum -c - \
    && unzip -q newrelic-java.zip \
    && test -s newrelic/newrelic.jar

# Etapa 2: Imagem final (mais leve)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY --from=apm-agent /download/newrelic/newrelic.jar /opt/newrelic/newrelic.jar
COPY observability/newrelic.yml /opt/newrelic/newrelic.yml
COPY docker/entrypoint.sh /app/entrypoint.sh
# Checkout no Windows pode converter os finais de linha do script.
RUN sed -i 's/\r$//' /app/entrypoint.sh && chmod 755 /app/entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
