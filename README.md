## Configuration Store

The Configuration store is an API-driven, hierarchical configuration storage platform supporting JSON and YAML markup.

<p align="center">

![build-test](https://github.com/composr/configr/workflows/build-test/badge.svg)
[![Apache 2.0](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/composr/configr/blob/master/LICENSE)
[![codecov](https://codecov.io/gh/composr/configr/branch/develop/graph/badge.svg)](https://codecov.io/gh/composr/configr)
[![Snyk Vulnerabilities](https://snyk.io/test/github/composr/configr/badge.svg)](https://snyk.io/test/github/composr/configr)
[![Sonarqube Quality Gate](http://sc-devsecops-sonar.eastus.cloudapp.azure.com:9000/api/project_badges/measure?project=configr&metric=alert_status&token=0f73df41ad15b538cdf8ddd6d0099031a4cd345c)](http://sc-devsecops-sonar.eastus.cloudapp.azure.com:9000/dashboard?id=configr)


<u>Planned Features</u>
<ul>
<li>Versioning</li>
<li>Tagging</li>
<li>Schema-based validation</li>
<li>Inheritance (Polymorphic)</li>
<li>Derivation</li>
<li>Pluggable Storage Model</li>
<li>Attribute-level access control</li>
<li>Querying engine</li>
<li>Event telemetry</li>
</ul>


## Code
Java Spring Boot application with MongoDB storage.

# Contribution
- Use the Google Java formatting style.
- Unit, integration tests written with JUnit 5
- Jacoco coverage report stored to default path: `/target/site/jacoco/index.html`

# Requirements
1. Java (OpenJDK 14+)
2. Maven
3. Lombok (https://projectlombok.org/)
   *Install for your IDE or you will not be able to compile the code
4. MongoDB (5+ recommended)


To build executable jar, use: `mvn clean package spring-boot:repackage`
