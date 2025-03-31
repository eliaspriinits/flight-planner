FROM openjdk:21
WORKDIR /app
COPY . /app
CMD ["java", "-jar", "target/your-app.jar"]
