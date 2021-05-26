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

**Das Projekt muss gleichermaßen mit [Gradle] in der Kommandozeile und 
[IntelliJ] gebaut und getestet werden können. Dazu ist es erforderlich, dass 
in beiden Fällen exakt die gleiche JDK Version verwendet wird.**

Empfehlung ist: **Adopt OpenJDK 11 (HotSpot)** `(jdk-11.0.11+9)`

## Kommandozeile

Wenn der Build von der Kommandozeile gestartet wird, verwendet Gradle die 
durch `$JAVA_HOME` spezifizierte JVM oder, falls diese vorhanden ist, oder 
die JVM die auf im aktuellen Pfad der Konsole gefunden werden kann (`which 
java`). Gradle begnügt sich dabei mit einem JRE.

Bei einer Standard-Ubuntu Installation muss das OpenJDK 11 installiert sein. 
Dazu ist folgendes Paket zu installieren:

    sudo apt install openjdk-11-jre

## IntelliJ

Im Menü `File -> New Project Settings -> Structure for New Projects` den 
Dialog `Platform Settings -> SDKs` öffenen. Falls das Ubuntu OpenJDK 11 
nicht installiert ist, mit dem Button `(+) Add JDK ` den Pfad zum Ubuntu 
OpenJDK 11 auswählen: `/usr/lib/jvm/java-11-openjdk-amd64` und einen 
sprechenden Namen vergeben. 

Alternativ kann im Dialog `Platform Settings -> SDKs` mit `[+] Download JDK` 
das `AdoptOpenJDK (HotSpot) 11.0.11` heruntergeladen werden.

Nachder Installation von Ubuntu OpenJDK 11 oder AdoptOpenJDK (HotSpot) 11.0.
11 kann im Dialog `Project Structure for New Projects` unter `Project 
Settings -> Project` im Feld `Project SDK` eines der beiden JDKs ausgewählt 
werden. Alle Projekte, die danach neu erstellt werden, erhalten dieses JDK 
als Voreinstellung.

Weil das Projekt `mycloud` ohne IntelliJ Projekteinstellungen in der 
Quellcodeverwaltung abgelegt ist, verwendet IntelliJ künftig das zuvor 
festgelegte JDK als Vorgabe.

Bei der Erstellung mit Gradle ist zu berücksichtigen, dass das JDK, mit dem 
Gradle gestartet wird und das JDK, mit dem Gradle den Build durchführt, 
unterschiedlich sein können (und in der Regel auch sind)

### Adopt OpenJ9-16
Die Integration von Gradle mit IntelliJ scheint für adopt-openj9-16 **nicht 
richtig zu funktionieren**. Bei der Ausführung der Tests erscheint die 
Fehlermeldung:

    Cause: cannot assign instance of java.util.Collections$EmptyList to field 
    java.lang.StackTraceElement.moduleVersion of type java.lang.String in 
    instance of java.lang.StackTraceElement

### Gradle Daemons

Bei der ganzen Umstellung der JDK, sollte man darauf achten, dass die von 
Gradle gestarteten Daemons auch mal getötet werden sollten:

Den Status der Gradle Daemons gibt folgende Kommano aus:

``sh
administrator@upc14-bmws:~/.local/src/mycloud$ ./gradlew --status
Initialized native services in: /home/administrator/.gradle/native
PID STATUS   INFO
68566 IDLE     7.0.2
108768 IDLE     7.0.2
109081 IDLE     7.0.2
100651 STOPPED  (stop command received)
``

### gradle.properties

Die Datei `/gradle.properties` wird von IntelliJ nur teilweise berücksichtigt.
Alle Properties, die einen Punkt enthalten scheinen von IntelliJ ignoriert 
zu werden. Eigene Properties ohne Punkt scheinen zu funktionieren.

Um den Debug Level bei Builds durch IntelliJ zu beeinflussen, muss Gradle eine
der Optionen `--quiet`, `--warn`, `--info` oder `--debug` als Argument in 
der Run Konfiguration übergeben werden.  

[Gradle]: https://gradle.org/
[IntelliJ]: https://www.jetbrains.com/de-de/idea/