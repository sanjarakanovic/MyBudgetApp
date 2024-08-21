FROM openjdk:22

WORKDIR /app
COPY target/my_budget-0.0.1-SNAPSHOT.jar my_budget-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "my_budget-0.0.1-SNAPSHOT.jar"]

