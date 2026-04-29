# Usa una imagen base ligera de Linux con Java 21 preinstalado
FROM eclipse-temurin:21-jdk-alpine

# Crea un directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo compilado (.jar) al contenedor y lo renombra
COPY target/*.jar app.jar

# Expone el puerto 8082 para que el Frontend se conecte
EXPOSE 8082

# Comando para ejecutar la aplicación al encender el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]