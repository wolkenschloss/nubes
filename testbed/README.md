# Testbed

Ein Prüfstand für Wolkenschloss.

Der Prüfstand stellt eine Cloud Umgebung für den automatisierten Test der 
Microservices in der Entwicklungsumgebung bereit, die ähnliche Eigenschaften 
wie die Zielplattform aufweist.

Der Prüfstand besteht aus einer virtuellen Maschine mit der [microk8s]
[microk8s] Variante von [Kubernetes](https://kubernetes.io/de/) und kann bei
Bedarf automatisch gestartet werden.

Der Prüfstand wird mit dem Werkzeug `testbed` verwaltet, das in den 
Erstellungsprozess eingebettet ist, aber auch einzeln ausgeführt werden kann. 

## Voraussetzungen

Um den Prüfstand nutzen zu können, müssen folgende Softwarepakete installiert 
sein:

  * Internetverbindung
  * libvirt
  * node.js
  * Rootless Docker
  * Weis der Henker, was noch

### Installation des Werkzeugs testbed

In einer Kommandozeile im Verzeichnis mycloud/testbed sind folgende Befehle
auszuführen:

```shell
npm install
npm link
```

Dies installiert das `testbed` Werkzeug im Ausführungspfad des Anwenders. 
Solltest Du `mycloud` in mehreren Verzeichnissen ausgecheckt haben, 
überschreibst Du mit `npm link` ggf. eine bereits durchgeführt Installation 
eines anderen Verzeichnisses.

## Anwendung Standalone

Die Festplatten-Abbilder, die für den Start der virtuellen Maschine erforderlich
sind, können mit dem Befehl

```
testbed build
```

erstellt werden. 

Nachdem Erstellen der Festplatten Abbilder, kann die virtuelle Maschine
erstellt werden. Der folgende Befehl erstellt die virtuelle Maschine, aber
sie wird noch nicht gestartet:

```
testbed deploy
```


Wenn die virtuelle Maschine erstellt wurde, kann sie mit folgendem Befehl 
gestartet werden. Der erste Starte der Maschine dauer ein paar Minuten. 

```
testbed start
```

Wenn der Startvorgang unterbrochen wird, zum Beispiel durch das Drücken von 
`Strg+C`, bleibt die Testumgebung in einem nicht definiert Zustand. Der Abbruch
des Startvorgangs führt nicht dazu, dass eine bereits gestartete virtuelle 
Maschine angehalten oder gelöscht wird, unabhängig vom Zustand der Maschine.

Nach dem Start der virtuellen Maschine ist das Kubernetes Dashboard erreichbar
mit der Adresse `https://192.168.122.152/dashboard`.

Zur Anmeldung am Dashboard kann entweder die Kubernetes Konfigurationsdatei 
oder ein Token verwendet werden. Die Konfigurationsdatei befindet sich nach der
Installation in der Datei `build/run/config`. Das Token wird mit dem Befehl 
`testbed token` auf der Kommandozeile ausgegeben.

## Anwendung im Erstellungsprozess mit `gradle` [TBD]

```shell
cd mycloud
./gradlew :testbed:start
```

## Nutzung des Prüfstandes 

[TBD]

[microk8s]: https://microk8s.io/docs

## To Do


### Ingress für Dashboard

Ingress für das Dashboard installieren, damit von
außen auf die Weboberfläche zugegriffen werden kann


### Image Registry

[Using the built-in registry](https://microk8s.io/docs/registry-built-in)

1. Registry aktivieren mit: `microk8s enable registry`. Registry läuft jetzt
   unter `<VM Host IP Adress>:32000`
2. Der Client (Entwicklungsrechner) muss Rootless Docker installiert haben
3. Der Client muss der unsicheren privaten Registry vertrauen, die im Prüfstand
integriert ist. Dazu muss die Datei `~/.config/docker/daemon.json` folgenden
   Inhalt haben:
   ```json
    {
        "insecure-registries" : ["<VM Host IP Address>:32000"]
    } 
    ```
4.  Änderung der Konfiguration wird nach dem Neustart des Docker Daemons
    wirksam. Der Neustart wird mit folgendem Befehl durchgeführt:
    ```sh
    $ systemctl --user restart docker
    ```
5. Image auf dem Client taggen. Z. B. *alpine*:
    ```sh
    $ docker tag alpine:latest $TESTBED_IP:32000/alpine:latest
    ```
6. Image in die private Registry schieben:
    ```sh
    $ docker push $TESTBED_IP:32000/alpine:latest
    ```
 
Was ist für die automatisierung notwendig?

Die IP-Adresse der VM soll konfigurierbar oder dynamisch sein. Also muss
die Insecure Registry in der Datei `/etc/docker/daemon.json` entweder 
geprüft oder besser automatisch angepasst werden.

Prüfen, ob $USER in der Gruppe docker ist oder prüfen, ob `$DOCKER_HOME` 
gesetzt ist für Rootless Docker.

### Rootless Docker
Rootless Docker: Docker Engine >= 20.10, also Version prüfen!
[Run the Docker daemon as a non-root user](https://docs.docker.com/engine/security/rootless/)

```shell
sudo apt install uidmap
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo   "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install docker-ce-rootless-extras  
```

```shell

+ systemctl --user enable docker.service

...

[INFO] Make sure the following environment variables are set (or add them to ~/.bashrc):

export PATH=/usr/bin:$PATH
export DOCKER_HOST=unix:///run/user/1000/docker.sock

```


```shell
dockerd-rootless-setuptool.sh install --force
echo "export DOCKER_HOST=unix:///run/user/1000/docker.sock" >> ~/.bashrc
```

**Wichtig: Hier noch einmal nachsehen**

```shell
$ docker info

WARNING: Running in rootless-mode without cgroups. To enable cgroups in rootless-mode, you need to boot the system in cgroup v2 mode.
```

Konfiguration des Docker Daemon für Rootless Installation:
`~/.config/docker/daemon.json`

Testen, ob die Rootless Konfiguration läuft:

```shell
docker info
docker run hello-world
```

### SSH Key aus known_hosts entfernen, falls bereits enthalten ist.

```shell
ssh-keygen -H $TESTBED_VM_ADDRESS
```

### Callbacks aus der VM an den Host

IP Adresse der VM ermitteln (innerhalb der VM)

```shell
hostname -I | cut -d" " -f1
```


Fingerprint eines eventuell bereits vorhandenen
Host entfernen

```shell
ssh-keygen -R $TESTBED_IP
```

SSH Fingerprint der VM ermitteln und installieren:

```shell
ssh-keyscan -H $TESTBED_IP >> ~/.ssh/known_hosts
```


### Troubleshooting

*Diesen Absatz nochmal überarbeiten!*

Es gibt zwei Möglichkeiten auf die Schnauze zu fallen. Entweder 
man verwendet den Verbindungstyp `qemu:///system`, dann sind die 
Dateiberechtigungen der Builderzeugnisse für libvirt zugänglich
gemacht werden, indem `chmod 777` auf die Festplattenabbilder
angewendet werden oder sie in das /tmp Verzeichnis wandern; oder
man verwendet den Verbindungstyp qemu:///session und hat dann 
kein Netzwerk. Einen Tod muss man halt sterben.