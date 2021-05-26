# MyCloud

Irgendwann wird das vielleicht ein Nextcloud-Nachfolger.
Jetzt kann man nur Rezepte damit verwalten.

## Architekturziele

- Erfahrung mit Microservice Technologie sammeln.
- Erarbeitung einer CI/CD Pipeline mit einfachen Mitteln.
- Lernen der Zusammenarbeit in einem Projekt.

### Randbedingungen

1. Das Projekt ist nicht gewinnorientiert
2. Es ist ein Lernprojekt für
    - Zusammenarbeit
    - Beispiel für professionelle Softwareentwicklung
    - Open Source Entwicklung
3. Es darf kein Geld kosten, außer dem was eh schon vorhanden ist.
    - Keine Lizenzgebühren
    - Keine gemieteten Server
    - ...
4. Das Projekt ist zunächst nur für die teilnehmenden Mitglieder bestimmt.    
    - Keine öffentlich sichtbaren Code Repositories und Artefakte.
    
### Architekturentscheidungen

#### Getroffene Entscheidungen

##### Für die Quellcode- und Projektverwaltung wird [GitHub] verwendet

Für die Zusammenarbeit ist die Organisation [Wolkenschloss]
auf [GitHub] angelegt. Die Organisation verfügt über einen kostenlosen 
Zugang (Free Plan). Für einen Vergleich der Organisationskonten siehe auch 
[Compare Plans](https://github.com/organizations/wolkenschloss/billing/plans).

Im [Wolkenschloss] können private Projekte angelegt werden, die nur für 
Teammitgliedern sichtbar sind. Allerdings stehen nicht alle GitHub 
Funktionen im *Free Plan* zur Verfügung.

##### Die Anzahl der Abhängigkeiten ist so gering wie möglich zu halten

Die Zeit für Builds auf öffentlichen Servern ist für kostenlose Zugänge in 
der Regel beschränkt. Abhängigkeiten werden bei jedem Build aus den öffentlichen 
Repositories geladen. Der Download von Drittkomponenten verlängert die Dauer 
des Builds.

##### Konventionelle Commits

[Konventionelle Commits] werden für Commit Messages verwendet.

Eine Systematik für Commit Messages gehört zum professionellen Arbeiten und
verbessert die Team-Zusammenarbeit.

Aus den *konventionellen Commits* können automatisch verarbeitet werden und 
sind von Menschen gut lesbar. 

##### Semantic Versioning

[Semantic Versioning] ist ein weit verbreitetes Verfahren wie Versionsnummern 
gewählt und erhöht werden. Mit einer von Anfang an einheitlichen Vergabe der 
Versionsnummern wird das professionelle Arbeiten im Team unterstützt. Es 
gibt keine Auseinandersetzung bezüglich der Versionsnummern.

#### Offene Entscheidung

- [ ] Unter welcher Lizenz soll das Projekt veröffentlicht werden?
- [ ] Wie wird die CI/CD Pipeline realisiert?

[Wolkenschloss]: https://github.com/wolkenschloss
[GitHub]: https://github.com/
[Konventionelle Commits]: https://www.conventionalcommits.org/de/v1.0.0/
[Semantic Versioning]: https://semver.org/lang/de/

## Quellcode Verwaltung

- [Git] Mono-Repository
- [GitHub] Remote Repository

[Git]: https://git-scm.com/

## Build Prozess

- [Gradle] Multi-Projekt

Das Projekt muss gleichermaßen mit [Gradle] in der Kommandozeile und 
[IntelliJ] gebaut und getestet werden können.

[Gradle]: https://gradle.org/
[IntelliJ]: https://www.jetbrains.com/de-de/idea/