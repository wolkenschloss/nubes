# Integration Testing

Die Tests können nur ausgeführt werden, wenn
vorher das Artefakt gebaut wurde. Eigentlich
geschieht das mit dem `./gradlew build` Task.

Integrationstests testen das fertige Artefakt
gegebenenfalls zusammen mit der Infrastruktur
und anderen Diensten.

In diesem Verzeichnis dürfen nur Tests, die
mit @QuarkusIntegrationTest gekennzeichnet
sind, gemacht werden. Tests, die mit 
@QuarkusTest gekennzeichnet sind, befinden sich
im `test` Verzeichnis und sind Unittests.
