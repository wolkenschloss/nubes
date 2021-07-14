# Testbed Gradle Plugin

Das Plugin dient dazu ein Gradle Projekt zu unterstützen, mit dem ein Prüfstand
aufgebaut werden kann.

Der Prüfstand besteht aus einer virtuellen Maschine mit der [microk8s]
[microk8s] Variante von [Kubernetes][k8s] und kann bei
Bedarf automatisch erstellt und gestartet werden. 

## Anwendung des Plugins

Das Plugin sollte in einem eigenen Gradle (Unter-)Projekt angewendet werden. 
Erstelle dazu in Deinem Projekt ein Gradle Build Skript, in dem Du das 
Plugin einbindest:

```kotlin
plugins {
    id("wolkenschloss.testbed")
}
```

## Extension

Im Buildscript `build.gradle` Deines Projektes kannst Du Parameter zur 
Erstellung des Prüfstandes anpassen. Im folgenden Beispiel werden die 
voreingestellten Werte aufgelistet.

```groovy
testbed {

    host {
        hostAddress = ""
        callbackPort = 9191
    }

    user {
        name = "$System.env.USER"
        sshKey = "ssh-rsa AAA...= user@host"
        sshKeyFile = "$System.env.HOME/.ssh/id_rsa.pub"
    }

    domain {
        name = "testbed"
        fqdn = "testbed.wolkenschloss.local"
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

## Gradle Tasks

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

### Konfigurationsdateien

Die folgenden Dateien müssen im `src` Verzeichnis des Projekts liegen. Sie 
enthalten Platzhalter, die vom `wolkenschloss.testbed` Plugin durch Werte 
ersetzt werden, die in den Extensions angegeben sind.

Die transformierten Dateien werden für die Erstellung der virtuellen 
Maschine verwendet.

#### user-data

Die `user-data` Datei beschreibt die bei Installation der virtuellen Maschine
durchzuführenden Arbeitsschritte und ist Bestandteil der 
[Cloud-Init](https://cloud-init.io/) Infrastruktur.

Weitere Hinweise findest du in der Cloud-Init 
[Dokumentation](https://cloudinit.readthedocs.io)

#### network-config

Diese Datei beschreibt die Netzwerkkonfiguration der virtuellen Maschine und 
ist Bestandteil der Cloud-Init Infrastruktur

#### domain.xml

Hierbei handelt es sich um die XML Beschreibung der virtuellen Maschine 
(Domain). Anhand dieser Datei wird die virtuelle Maschine erzeugt. Die Datei 
enthält Platzhalt, die vom `wolkenschloss.testbed` Plugin aus den Werten der 
Extension gefüllt werden.

Hinweis: Die XML Beschreibung einer virtuellen Maschine kannst du mit dem Befehl 
`virsh dumpxml testbed` erstellen.

Weitere Hinweise zum Umgang mit `libvirt` findest du unter https://libvirt.org/

#### pool.xml

Diese Datei beschreibt den Storage Pool der virtuellen Maschine. Der Storage 
Pool enthält das Root-Image mit dem Betriebssystem und das Cloud-Init 
Konfigurationsvolumen `cidata.img`.

Die Platzhalter in der Datei werden durch das `wolkenschloss.testbed` Plugin 
mit den Werten aus der Extension ersetzt.

## Anforderungen

Die Beschreibung der Anforderungen geht von einem Ubuntu 20.04 Desktop System
aus, das standardmäßig installiert ist. Mit ssh und gpg.

### SSH Schlüsselpar

Du benötigst ein SSH Schlüsselpaar. Wenn du keinen hast, erzeuge einen mit dem
Befehl:

```bash
ssh-keygen
```

Weitere Hinweise dazu findest du unter [Authentifizierung über Public-Keys][ssh]

### GPG Schlüssel importieren

Zur Überprüfung der Signatur der heruntergeladenen Checksummen-Datei musst du
den GPG Schlüssel des Herausgebers des Basisimages in dein Schlüsselbund
importieren. Das erfolgt mit diesem Befehl:

```bash
gpg --keyid-format long --keyserver hkp://keyserver.ubuntu.com \
  --recv-keys 0x1A5D6C4C7DB87C81
```

Weitere Informationen dazu findest du im
Artikel [Retrieve the correct signature key][gpg]

### Benötigte Pakete

Damit der Prüfstand automatisch erstellt werden kann, müssen folgende
Softwarepakete installiert sein (Ubuntu 20.04).

* qemu-kvm
* libvirt-daemon-system
* cloud-image-utils

```bash
sudo apt update && sudo apt -y upgrade
sudo apt install -y qemu-kvm libvirt-daemon-system cloud-image-utils
```

## Tests ausführen

Um die Tests auszuführen, musst Du das Verzeichnis des Projektes ausdrücklich
mit der Option `-p` angegeben. Das gilt auch für die
*Run Configuration* von *IntelliJ*. Das Verzeichnis wird nicht automatisch
angefügt.

```shell
./gradlew ':test -p buildSrc
```

[k8s]: https://kubernetes.io/
[microk8s]: https://microk8s.io/docs
[xdg]: https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html
[ssh]: https://wiki.ubuntuusers.de/SSH/#Authentifizierung-ueber-Public-Keys
[gpg]: https://ubuntu.com/tutorials/how-to-verify-ubuntu#4-retrieve-the-correct-signature-key