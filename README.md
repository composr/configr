## Configuration Store

# Requirements
1. Java (OpenJDK 14+ recommended)
2. Maven
3. Lombok (https://projectlombok.org/)
   *Install for your IDE or you will not be able to compile the code
4. MongoDB (5+ recommended)
   *Default port + localhost expected

# Run Spring Boot application from IDE
Entrypoint for the Spring boot application is - src/main/java/com/citihub/configr/ConfigurationStoreApplication.java

# To run tests from command line:
`mvn clean package`

Jacoco coverage report will automatically generate and can be found in `/target/site/jacoco/index.html`

# To build executable jar, which will be located in target/, run:
mvn clean package spring-boot:repackage

# Contents
Simple API-driven Configuration Store, backed by MongoDB.

