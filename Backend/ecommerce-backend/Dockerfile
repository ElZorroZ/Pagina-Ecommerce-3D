# Imagen base con Java (puede ser openjdk:17, 21, etc. según uses)
FROM openjdk:17-jdk-alpine

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el .jar generado al contenedor
# Imagen base con Java 17 (podés usar 21 si tu proyecto lo soporta)
FROM openjdk:17-jdk-alpine

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el jar generado (ajustá la ruta según uses Maven o Gradle)
# Para Maven:
COPY target/ecommerce-backend-0.0.1-SNAPSHOT.jar app.jar

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]


# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
