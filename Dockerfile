# ====== ESTÁGIO 1: BUILD ======
# Em vez de usar Ubuntu e instalar coisas, usamos uma imagem Maven pronta
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia o pom.xml e baixa dependências (Isso aproveita o cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests

# ====== ESTÁGIO 2: RUNTIME ======
# Aqui usamos o Alpine (super leve) apenas para rodar
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Expõe a porta
EXPOSE 8080

# Copia APENAS o .jar gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Roda a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]