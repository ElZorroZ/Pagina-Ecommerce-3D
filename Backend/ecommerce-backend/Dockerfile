# Imagen base con Java (puede ser openjdk:17, 21, etc. según uses)
FROM openjdk:17-jdk-alpine

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el .jar generado al contenedor
COPY build/libs/mi-app.jar app.jar
# o si usás out/artifacts:
# COPY out/artifacts/mi_app/mi-app.jar app.jar

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
