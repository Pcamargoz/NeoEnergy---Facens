# ---------- Estágio 1: build ----------
# Compila o projeto e gera o .jar. Usa o Maven Wrapper já versionado no repo.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia primeiro só o que define dependências, pra aproveitar cache de camadas.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Agora o código-fonte e o empacotamento.
COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

# ---------- Estágio 2: runtime ----------
# Imagem final só com JRE (menor e mais segura — sem o JDK/Maven).
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuário não-root por segurança.
RUN useradd -r -u 1001 spring
USER spring

# Copia apenas o jar gerado no estágio de build.
COPY --from=build /app/target/NEO-ENERGY-0.0.1-SNAPSHOT.jar app.jar

# O Render injeta a porta via env var PORT; a app já lê ${PORT:8080} no application.yaml.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
