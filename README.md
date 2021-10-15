## Configuration Store

The Configuration store is an API-driven, hierarchical configuration storage platform supporting JSON and YAML markup.

<p align="center">

![build-test](https://github.com/citihub/configr/workflows/build-test/badge.svg)
[![Apache 2.0](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/citihub/configr/blob/master/LICENSE)
[![codecov](https://codecov.io/gh/citihub/configr/branch/develop/graph/badge.svg)](https://codecov.io/gh/citihub/configr)

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
