# Develop

Install (Ubuntu 20.04):

Eine Entwicklungsumgebung erstellen:

```bash
sudo apt update
sudo apt upgrade
sudo apt install qemu-guest-agent
sudo apt install openjdk-11-jdk
sudo apt remove docker docker.io containerd runc
sudo apt-get install apt-transport-https ca-certificates curl gnupg lsb-release
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo   "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt-get install docker-ce docker-ce-cli containerd.io

// Docker Rootless
sudo apt install uidmap
sudo systemctl disable --now docker.service docker.socket
dockerd-rootless-setuptool.sh install

systemctl --user start docker
systemctl --user enable docker

# Installation des Ubuntu GPG Schlüssels zur Überprüfung der Prüfsummen-Datei
gpg --keyid-format long --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0x1A5D6C4C7DB87C81

git clone https://github.com/wolkenschloss/mycloud.git
./gradlew build
```

Überprüfung des Schlüssels ist möglich auf der Seite
[FAQ des Ubuntu Security Team](https://wiki.ubuntu.com/SecurityTeam/FAQ#GPG_Keys_used_by_Ubuntu)

## GPG Bug

Bei einigen Ubuntu Versionen kommt es zu einem Fehler, wenn man einen
GPG Schlüssel zu seinem Schlüsselbund hinzufügen möchte:

```bash
sudo chown -R $(id -u):$(id -g) $HOME/.gnupg/crls.d
```

Links:

* [GnuPrivacyGuardHowto](https://help.ubuntu.com/community/GnuPrivacyGuardHowto)
* [How to verify your Ubuntu download](https://ubuntu.com/tutorials/how-to-verify-ubuntu#1-overview)
* [The GNU Privacy Handbook](https://www.gnupg.org/gph/en/manual.html)

Testbed zum Laufen kriegen:

```bash
ssh-keygen
sudo apt install cloud-image-utils
sudo apt install cpu-checker
kvm-ok
sudo apt install qemu-kvm libvirt-daemon-system
sudo adduser $USER libvirt
(relogin)
```

## Konvention

Jedes Teilprojekt besteht mindestens aus einem Backend und einem Frontend.
Die Bezeichnungen sind jeweils:

* Backend: service
* Frontend: webapp
* Business Core: core

In der Entwicklungsumgebung werden manchmal mehrere Services und Frontends
gestartet. Ein Service Discovery für die Entwicklungsumgebung ist nicht
vorgesehen. Damit es nicht zu Kollisionen bei der Verwendung der Ports
kommt, gibt es folgende Konvention:

Alle Ports sind vierstellig: z. B. 8080

Jedes Teilprojekt bekommt eine eigenes zweistelliges Projektpräfix. Z. B. 

* blog: 80
* cookbook: 81
* dashboard: 82

Frontend und Backend erhalten Standardports:

* Frontend: 81
* Backend: 80

Den Standardports ist der Präfix voranzustellen:

* blog/webapp: 8081
* blog/service: 8080
* cookbook/webapp: 8181
* cookbook/service: 8180
* dashboard/webapp: 8281
* dashboard/service: 8280

Erstmal schauen, ob das funktioniert :-)

## Frontend

- [Node Version Manager (nvm)](https://github.com/nvm-sh/nvm)
- [Node Package Manager (npm)](https://www.npmjs.com/)
    - [Documentation](https://docs.npmjs.com/)
        - [Configuring your local environment](https://docs.npmjs.com/getting-started/configuring-your-local-environment/)
- [Vue CLI](https://cli.vuejs.org/)
    - [Getting Started](https://cli.vuejs.org/guide/)
    - [Installation](https://cli.vuejs.org/guide/installation.html)
- [Vue.js](https://v3.vuejs.org/)

Installation des Node Version Managers:

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.38.0/install.sh | bash
```

Installation von Node und npm:

```bash
nvm install --lts
```

Installation von @vue/cli

```
npm install -g @vue/cli
```

Vue.js Projekt anlegen:

```bash
vue create hello-world
```

Vue 2 auswählen! Bootstrap Vue ist nicht kompatibel zu Vue 3:
[Vue 3 support #5196](https://github.com/bootstrap-vue/bootstrap-vue/issues/5196)

Bootstrap Vue Plugin hinzufügen

```bash
cd hello-world
vue add bootstrap-vue
```

Sie dazu auch [Getting Started](https://bootstrap-vue.org/docs#vue-cli-3-plugin)

## Build Prozess

- [Gradle] Multi-Projekt

**Das Projekt muss gleichermaßen mit [Gradle] in der Kommandozeile und
[IntelliJ] gebaut und getestet werden können. Dazu ist es erforderlich, dass
in beiden Fällen exakt die gleiche JDK Version verwendet wird.**

Empfehlung ist: **Adopt OpenJDK 11 (HotSpot)** `(jdk-11.0.11+9)`

### Kommandozeile

Wenn der Build von der Kommandozeile gestartet wird, verwendet Gradle die
durch `$JAVA_HOME` spezifizierte JVM oder, falls diese vorhanden ist, oder
die JVM die auf im aktuellen Pfad der Konsole gefunden werden kann (`which
java`). Gradle begnügt sich dabei mit einem JRE.

Bei einer Standard-Ubuntu Installation muss das OpenJDK 11 installiert sein.
Dazu ist folgendes Paket zu installieren:

    sudo apt install openjdk-11-jre

### IntelliJ

Im Menü `File -> New Project Settings -> Structure for New Projects` den
Dialog `Platform Settings -> SDKs` öffnen. Falls das Ubuntu OpenJDK 11
nicht installiert ist, mit dem Button `(+) Add JDK ` den Pfad zum Ubuntu
OpenJDK 11 auswählen: `/usr/lib/jvm/java-11-openjdk-amd64` und einen
sprechenden Namen vergeben.

Alternativ kann im Dialog `Platform Settings -> SDKs` mit `[+] Download JDK`
das `AdoptOpenJDK (HotSpot) 11.0.11` heruntergeladen werden.

Nach der Installation von Ubuntu OpenJDK 11 oder AdoptOpenJDK (HotSpot) 11.0.
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

Bei der ganzen Umstellung der JDK, sollte man darauf achten, dass die von Gradle
gestarteten Daemons auch mal getötet werden sollten:

Den Status der Gradle Daemons gibt folgende Kommando aus:

``sh 
./gradlew --status 
``

### gradle.properties

Die Datei `/gradle.properties` wird von IntelliJ nur teilweise berücksichtigt.
Alle Properties, die einen Punkt enthalten scheinen von IntelliJ ignoriert zu
werden. Eigene Properties ohne Punkt im Bezeichner funktionieren.

Um den Debug Level bei Builds durch IntelliJ zu beeinflussen, muss Gradle eine
der Optionen `--quiet`, `--warn`, `--info` oder `--debug` als Argument in der
Run Konfiguration übergeben werden.

Die Einstellungen in der Datei `idea.properties` vorzunehmen zeigt ebenfalls
keine Wirkung.

Dazu gibt es
bei [IDEs Support (IntelliJ Platform) | JetBrains](https://intellij-support.jetbrains.com/hc/en-us)
einen
Support-Eintrag [How to see debug logging when running gradle inside IntelliJ?](https://intellij-support.jetbrains.com/hc/en-us/community/posts/360000420140-How-to-see-debug-logging-when-running-gradle-inside-IntelliJ-)

### Badges

Dokumentation zu den Build Badges in GitHub:
https://docs.github.com/en/actions/managing-workflow-runs/adding-a-workflow-status-badge


[Gradle]: https://gradle.org/

[IntelliJ]: https://www.jetbrains.com/de-de/idea/