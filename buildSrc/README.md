# Developing Plugins and Conventions in buildSrc

> **WARNING:** Open this directory as a root project in your IDE. Otherwise, 
> it won't work.

*...but then git integration and run configurations does not work anymore.*

This project uses additional SourceSets to separate integration and functional
tests from unit tests. This prevents long loading times when updating build
scripts and plugins.

The additional SourceSets in [buildSrc] cause difficulties in the IDE. The
current solution is to load [buildSrc] as the root project in the IDE. Then
most functions are available for testing. However, the VCS integration
and the Run Configuration no longer work.

## Q: How do I become root in an innocent buildscript?

Answer: Start a docker container :smile:

## Testpyramide

### Funktionstests

Tests, die mit einem vollständigen Testprojekt ausgeführt werden und damit
am besten prüfen können, ob ein Plugin oder Konvention funktioniert. 
Funktionstests benutzen den `GradleRunner` des TestKids, um Testprojekte im
[fixtures] Ordner auszuführen. Allerdings dauert die Ausführung der Tests 
relativ lange.

| Task                   | Source Set | Path                                   |
|:-----------------------|:-----------|:---------------------------------------|
 | `./gradlew functional` | functional | [test/functional](src/test/functional) |


### Integrationstests

Tests, die nur einen Teil der Gradle Ausführungsumgebung benötigen, insbesondere
aber nicht den `GradleRunner` starten. Stattdessen werden einzelne Komponenten von
Gradle, zum Beispiel der `ProjectBuilder` verwendet, um Objekte aus der Gradle 
Umgebung als Kollaborateure im Test zu verwenden.


| Task                    | Source Set  | Path                                     |
|:------------------------|:------------|:-----------------------------------------|
| `./gradlew integration` | integration | [test/integration](src/test/integration) |


### Komponententests

Klassische Komponententests, bei denen die Komponenten einzeln getestet werden, ohne
Kollaborateure zu verwenden. Abhängigkeiten zu anderen Komponenten, insbesondere
zu Gradle, sollten durch Mock Objekte ersetzt werden. 


| Task             | Source Set | Path                       |
|:-----------------|:-----------|:---------------------------|
| `./gradlew test` | test       | [test/unit](src/test/unit) |

## Conventions

### Webapp Convention Plugin

Mit dem *Webapp Convention Plugin* werden die auf [npm] basierenden 
Erstellungsprozesse von Unterprojekten mit webbasierten Benutzeroberflächen, 
die as [Vue.js] Framework verwenden, den Gradle Multi-Projekt 
Erstellungsprozess integriert. 

Das *Webapp Convention Plugin* ergänzt die Projekte, in denen es verwendet 
wird um folgende Aufgaben:

| Gradle Task | vue-cli-service Kommando              | Gradle Lifecycle Task |
|-------------|---------------------------------------|-----------------------|
| `vue`       | `vue-cli-service build --dest ...`    | `build`               |
| `unit`      | `vue-cli-service test:unit`           | `check`               |
| `e2e`       | `vue-cli-service test:e2e --headless` | `check`               |

Um eine neue Webanwendung als Unterprojekt hinzuzufügen, gehe folgendermaßen
vor:

1. Wechsle in das Microservice Verzeichnis. In diesem Verzeichnis befinden 
   sich alle Unterprojekte, die zu dem Microservice gehören. Um ein Webapp 
   Projekt für den Microservice *cookbook* zu erstellen, wechsle also in das 
   Verzeichnis `services/cookbook`.
2. Erstelle das Webprojekt mit [Vue CLI]: `vue create --no-git webapp`. Das 
   erstellt im Ordner *webapp* eine [Vue.js] Anwendung. Für die Integration in 
   den Gradle Erstellungsprozess muss das *webapp* Projekt *Unit Testing* und 
   *E2E Testing* Features unterstützen und die entsprechenden Erweiterungen 
   installiert sein. Alle angebotenen Unit Testing und E2E Testing Features 
   kannst du verwenden. 
   ```
   Vue CLI v4.5.13
   ? Please pick a preset:
   Default ([Vue 2] babel, eslint)
   Default (Vue 3) ([Vue 3] babel, eslint)
   ❯ Manually select features
   ```
   ```
    Vue CLI v4.5.13
    ? Please pick a preset: Manually select features
    ? Check the features needed for your project:
    ◉ Choose Vue version
    ◯ Babel
    ◯ TypeScript
    ◯ Progressive Web App (PWA) Support
    ◯ Router
    ◯ Vuex
    ◯ CSS Pre-processors
    ◯ Linter / Formatter
    ◉ Unit Testing
    ❯◉ E2E Testing   
   ```
3. Wechsle in das Verzeichnis webapp und erstelle eine Gradle Build Datei 
   `gradle.build.kts`:
   ```kotlin
   plugin {
        id ("wolkenschloss.conventions.webapp")
   }

    tasks {
        named<NpxTask>("vue") {
            inputs.files("babel.config.js")
        }
    }
   ```

Die Dateien `package.json`, `package.json.lock` und `vue.config.js` sind 
bereits als Abhängigkeiten vom Plugin voreingestellt. Weitere Abhängigkeiten 
können bei Bedarf, wie oben gezeigt, hinzugefügt werden.

Die Ausgabe der `vue` Aufgabe erfolgt in das Verzeichnis 
`build/classes/java/main/META-INF/resources` damit Quarkus Services die 
Artefakte als statische Ressourcen über ihr Webinterface verteilen können. 
Dazu muss im Quarkus Projekt lediglich eine Projektabhängigkeit zum `webapp` 
Projekt hinzugefügt werden: 

```kotlin
dependencies {
    implementation(project(":webapp"))
}
```

### Core Convention Plugin

Das Core Convention Plugin enthält die gemeinsamen Voreinstellungen für 
Projekte, die ein Domänen-Modell entsprechend der Zwiebelschalen Architektur 
implementieren.

Gradle Projekte, die dieses Plugin anwenden erhalten folgende Eigenschaften:

* Java 11
* JUnit 5

Verwendung des Plugins in `build.gradle.kts`

```kotlin
plugins {
    id("wolkenschloss.conventions.core")
}
```

## Plugins

### [Testbed]

Das Plugin dient dazu ein Gradle Projekt zu unterstützen, mit dem ein Prüfstand
aufgebaut werden kann. Es muss dringend überarbeitet werden. Die Notwendigkeit
den Prüfstand in einer VM zu starten ist fragwürdig. Es kann auch eine [microk8s]
Installation auf dem Entwicklerrechner genügen. Dies erfordert weniger Ressourcen.

Der Prüfstand besteht aus einer virtuellen Maschine mit der [microk8s]
[microk8s] Variante von [Kubernetes][k8s] und kann bei
Bedarf automatisch erstellt und gestartet werden. 

#### Anwendung des Plugins

Das Plugin sollte in einem eigenen Gradle (Unter-)Projekt angewendet werden. 
Erstelle dazu in Deinem Projekt ein Gradle Build Skript, in dem Du das 
Plugin einbindest:

```kotlin
plugins {
    id("com.github.wolkenschloss.testbed")
}
```


#### Extension

Im Buildscript `build.gradle` Deines Projektes kannst Du Parameter zur 
Erstellung des Prüfstandes anpassen. Im folgenden Beispiel werden die 
voreingestellten Werte aufgelistet.

```groovy
testbed {

    host {
        hostAddress = ""
        callbackPort = 9191
    }

    // TODO: ssh key exklusiv für den Prüfstand, um möglichst
    // keinen Zugriff auf die Geheimnisse (private key) des
    // Entwicklers nehmen zu müssen
    user {
        name = "$System.env.USER"
        sshKey = "ssh-rsa AAA...= user@host"
        sshKeyFile = "$System.env.HOME/.ssh/id_rsa.pub"
    }

    domain {
        name = "testbed"
        fqdn = "testbed.wolkenschloss.test"
        locale = "$System.env.LANG"
        knownHostsFile = "$buildDir/run/known_hosts"
        kubeConfigFile = "$buildDir/run/kubeconfig"
    }

    pool {
        rootImageName = "root.qcow2"
        cidataImageName = "cidata.img"
        name = "testbed"
        poolDirectory = "$buildDir/pool"
        rootImageMd5File = "$buildDir/run/root.md5"
        poolRunFile = "$buildDir/run/pool.run"
    }

    baseImage {
        url = "https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img"
        name = "ubuntu-20.04"
        downloadDir = "$System.env.XDG_DATA_HOME/testbed"
        distributionDir = "$System.env.XDG_DATA_HOME/testbed/ubuntu-20.04"
        baseImageFile = "$System.env.XDG_DATA_HOME/testbed/ubuntu-20.04/focal-server-cloudimg-amd64-disk-kvm.img"
    }

    transformation {
        generatedCloudInitDirectory = "$buildDir/cloud-init"
        generatedVirshConfigDirectory = "$buildDir/config"
        sourceDirectory = "src"
    }

    runDirectory = "$buildDir/run"
}
```

Die IP-Adresse des Wirt-Rechners in `host.hostAddress` wird automatisch 
ermittelt und sollte nur gesetzt werden, wenn die Automatik versagt.

Der in `host.callbackPort` angegebene Port kann überschrieben werden, sollte 
es zu einem Konflikt mit bereits verwendeten Port kommen. Das `wolkenschloss.
testbed` Plugin startet einen Webserver auf diesem Port, um auf eine Antwort 
der erzeugten virtuellen Maschine zu warten, die deren SSH Fingerprint enthält. 

Die Umgebungsvariable `$XDG_DATA_HOME` ist normalerweise nicht gesetzt. Die 
Voreinstellung wäre `$HOME/.local/share`. Siehe [XDG Base Directory 
Specification][xdg].

Das einzige, was lohnt daran herumzuspielen, wäre das Basisimage. Mit `url` 
wird die URL des Basisimage festgelegt, das heruntergeladen und als 
Grundlage für das Root Image verwendet wird.

Das Plugin lädt automatisch die dazugehörenden `SHA256SUMS` und 
`SHA256SUMS.gpg` Dateien herunter, um die Signatur der Prüfsummendatei zu 
und die Prüfsumme des Basisimages zu verifizieren.

#### Gradle Tasks

Das Plugin benutzt absichtlich nicht die Standard-Lebenszyklen von Gradle, um
den Unterschied zu diesen hervorzuheben. Die Erstellung des Prüfstandes ist eine
umfangreiche Aufgabe, die mit einem üblichen Erstellungsvorgang nicht zu
vergleichen ist.

* Domain tasks
    * *buildDomain* - Starts the libvirt domain and waits for the callback.
    * *readKubeConfig* - Copies the Kubernetes client configuration to the 
        localhost for further use by kubectl.
    * **start** - The all-in-one lifecycle start task. Have a cup of coffee.
* Download tasks
    * *download* - Downloads the base image to a cache for later use.
* Pool tasks
    * *buildDataSourceImage* - Generates a cloud-init data source volume 
        containing the transformed network-config and user-data files.
    * *buildPool* - Defines a virtlib storage pool based on the transformed 
        description file containing a root image, and a cloud-init data source volume.
    * *buildRootImage* - Creates the root image for the later domain from a 
        downloaded base image.

* Transformation tasks
    * *transformDomainDescription* - Transforms domain.xml
    * *transformNetworkConfig* - Transforms network-config template
    * *transformPoolDescription* - Transforms pool.xml template
    * *transformUserData* - Transforms user-data template
* Other tasks
    * **destroy** - Destroy testbed and delete all files.
    * **status** - Performs tests to ensure the function of the test bench.

Die wichtigsten Lebenszyklus-Aufgaben sind:

* **start** erzeugt und startet den Prüfstand. Sollte der Prüfstand bereits
  existieren, kommt es zu einem Fehler. Der Prüfstand sollte im weiteren Verlauf
  mit `virsh` verwaltet werden.
* **destroy** löscht den Prüfstand mitsamt allen Ressourcen.

Das Plugin soll keinen Ersatz für die Schnittstelle zu *libvirt* sein und
weder `virsh` noch `Virtuelle Maschinenverwaltung` ersetzen. Die Aufgabe des
Plugins besteht primär darin, die erforderlichen Artefakte, wie angepasste
Konfigurationsdateien und Festplatten-Abbilder zu erstellen. Ebenso die
Entwicklungsumgebung für den Zugriff auf den Prüfstand vorzubereiten.

Zum Erstellen von Snapshots oder Autostarteinstellungen kannst Du weiterhin die
bekannten Werkzeuge wie zum Beispiel `virsh` benutzen.

##### Konfigurationsdateien

Die folgenden Dateien müssen im `src` Verzeichnis des Projekts liegen. Sie 
enthalten Platzhalter, die vom `wolkenschloss.testbed` Plugin durch Werte 
ersetzt werden, die in den Extensions angegeben sind.

Die transformierten Dateien werden für die Erstellung der virtuellen 
Maschine verwendet.

###### user-data

Die `user-data` Datei beschreibt die bei Installation der virtuellen Maschine
durchzuführenden Arbeitsschritte und ist Bestandteil der 
[Cloud-Init](https://cloud-init.io/) Infrastruktur.

Weitere Hinweise findest du in der Cloud-Init 
[Dokumentation](https://cloudinit.readthedocs.io)

###### network-config

Diese Datei beschreibt die Netzwerkkonfiguration der virtuellen Maschine und 
ist Bestandteil der Cloud-Init Infrastruktur

###### domain.xml

Hierbei handelt es sich um die XML Beschreibung der virtuellen Maschine 
(Domain). Anhand dieser Datei wird die virtuelle Maschine erzeugt. Die Datei 
enthält Platzhalt, die vom `wolkenschloss.testbed` Plugin aus den Werten der 
Extension gefüllt werden.

Hinweis: Die XML Beschreibung einer virtuellen Maschine kannst du mit dem Befehl 
`virsh dumpxml testbed` erstellen.

Weitere Hinweise zum Umgang mit `libvirt` findest du unter https://libvirt.org/

###### pool.xml

Diese Datei beschreibt den Storage Pool der virtuellen Maschine. Der Storage 
Pool enthält das Root-Image mit dem Betriebssystem und das Cloud-Init 
Konfigurationsvolumen `cidata.img`.

Die Platzhalter in der Datei werden durch das `wolkenschloss.testbed` Plugin 
mit den Werten aus der Extension ersetzt.

#### Anforderungen

Die Beschreibung der Anforderungen geht von einem Ubuntu 20.04 Desktop System
aus, das standardmäßig installiert ist. Mit ssh und gpg.

##### SSH Schlüsselpaar

Du benötigst ein SSH Schlüsselpaar. Wenn du keinen hast, erzeuge einen mit dem
Befehl:

```bash
ssh-keygen
```

Weitere Hinweise dazu findest du unter [Authentifizierung über Public-Keys][ssh]

##### GPG Schlüssel importieren

Zur Überprüfung der Signatur der heruntergeladenen Checksum-Datei musst du
den GPG Schlüssel des Herausgebers des Basisimages in dein Schlüsselbund
importieren. Das erfolgt mit diesem Befehl:

```bash
gpg --keyid-format long --keyserver hkp://keyserver.ubuntu.com \
  --recv-keys 0x1A5D6C4C7DB87C81
```

Weitere Informationen dazu findest du im
Artikel [Retrieve the correct signature key][gpg]

##### Benötigte Pakete

Damit der Prüfstand automatisch erstellt werden kann, müssen folgende
Softwarepakete installiert sein (Ubuntu 20.04).

* qemu-kvm
* libvirt-daemon-system
* cloud-image-utils

```bash
sudo apt update && sudo apt -y upgrade
sudo apt install -y qemu-kvm libvirt-daemon-system cloud-image-utils
```

#### Tests ausführen

Um die Tests auszuführen, musst Du das Verzeichnis des Projektes ausdrücklich
mit der Option `-p` angegeben. Das gilt auch für die
*Run Configuration* von *IntelliJ*. Das Verzeichnis wird nicht automatisch
angefügt.

```shell
./gradlew ':test -p buildSrc
```

### [Docker]

Erzeugt Docker Images und führt Container aus. Es handelt sich eine minimale 
Implementierung, die speziell auf die Anforderungen des Build Prozesses von 
*Wolkenschloss* zugeschnitten ist. Die Verwendung von Containern verringert
die Anforderungen an zu installierenden Paketen auf dem Entwicklungsrechner
(und ermöglicht ggf. die Ausführung von Aufgaben mit privilegierten Rechten).

Beispiele für die Verwendung des Plugins findest Du im [fixtures](fixtures)
Verzeichnis.

| Task               | Zweck                                                                  | Beispiel                |
|--------------------|------------------------------------------------------------------------|-------------------------|
| [RunContainerTask] | Führt ein einzelnes Kommando in einem Container aus und beenden diesen | [mount](fixtures/mount) |
| [BuildImageTask]   | Erstellt ein Docker Image.                                             | [image](fixtures/image) |

[k8s]: https://kubernetes.io/
[microk8s]: https://microk8s.io/docs
[xdg]: https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html
[ssh]: https://wiki.ubuntuusers.de/SSH/#Authentifizierung-ueber-Public-Keys
[gpg]: https://ubuntu.com/tutorials/how-to-verify-ubuntu#4-retrieve-the-correct-signature-key
[Vue.js]: https://vuejs.org/
[npm]: https://www.npmjs.com/
[Vue CLI]: https://cli.vuejs.org/
[buildSrc]: .
[fixtures]: fixtures
[RunContainerTask]: src/main/kotlin/wolkenschloss/gradle/docker/RunContainerTask.kt
[BuildImageTask]: src/main/kotlin/wolkenschloss/gradle/docker/RunContainerTask.kt
[Docker]: src/main/kotlin/wolkenschloss/gradle/docker
[Testbed]: src/main/kotlin/wolkenschloss/gradle/testbed