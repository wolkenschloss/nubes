# Testbed

Ein Prüfstand für Wolkenschloss zum Testen und Entwickeln.

## TL;DR

It works on my machine:

* Intel Core i7 
* Ubuntu 20.04, 
* 16 GB RAM
* 1 TB SSD.

```bash
sudo snap install multipass

# testbed Instanz starten
# Run Configuration: testbed start
./gradlew :testbed:start

# DNS überlisten:
cat testbed/build/run/hosts | sudo tee -a /etc/hosts > /dev/null

# CA installieren (sollte auch irgendwie automatisiert werden):
sudo copy $HOME/.local/share/wolkenschloss/ca/ca.crt /usr/local/share/ca-certificates
sudo update-ca-certificates
sudo systemctl restart docker
# Zertifikat im Browser installieren
# Firefox: Einstellungen > Datenschutz & Sicherheit > Zertifikate anzeigen... > Importieren...

# Testbed Status abfragen:
# Run Configuration: testbed status
./gradlew :testbed:status

# Projekt für Staging Umgebung bauen
# Run Configuration: build staging
./gradlew build -Dquarkus.profile=staging

# Staging Umgebung starten oder aktualisieren
# Das ist nicht die beste Lösung. Der oben genannte Befehl sollte bereits
# die Kubernetes Manifest aktualisieren.
# Run Configuration: testbed staging
./gradlew :testbed:staging

# testbed anhalten - Gibt Speicher und Prozessoren frei
multipass stop testbed

# testbed fortsetzen
multipass start testbed

# testbed endgültig löschen und alle Ressourcen freigeben:
# Run Configuration: testbed destroy
./gradlew :testbed:destroy


# Shell auf dem Prüfstand öffnen:
multipass shell testbed
```

## Features

### Root CA

Mit dem Task *:testbed:ca* erstellst Du eine selbst signierte Root CA 
für den Prüfstand. Das Root CA Zertifikat wird vom Prüfstand verwendet,
um TLS Zertifikate für die jeweiligen Dienste zu erstellen. Die TLS
Zertifikate werden vom cert-manager erstellt und verwaltet. 

Das Wurzelzertifikat *ca.crt* und der private Schlüssel *ca.key* werden im 
Verzeichnis *$XDG_DATA_HOME/wolkenschloss/ca* gespeichert. Ein erneuter
Aufruf des Tasks erzeugt kein neues Zertifikat und überschreibt die
Dateien nicht.

Die Voreinstellung für *$XDG_DATA_HOME* ist *$HOME/.local/share*

Um von Deinem Entwicklungsrechner auf die Dienste zugreifen zu können,
musst Du das Wurzelzertifikat importieren:

```bash
cp $HOME/.local/wolkenschloss/ca/ca.crt /usr/local/share/ca-certificates
sudo update-ca-certificates
```

Außerdem musst Du das Zertifikat in Deinem Browser importieren. Z.B. für
Firefox:

Einstellungen -> Datenschutz & Sicherheit -> Zertifikate -> Zertifikate anzeigen... -> Importieren

### Namensauflösung

Die TLS Zertifikate der Dienste des Prüfstandes sind auf deren jeweiligen
FQDN ausgestellt. Deshalb muss auf diese Dienste mit einer funktionierenden
Namensauflösung zugegriffen werden.

Durch den Start des Prüfstandes mit *./gradlew :testbed:start* erzeugst
Du die Datei *testbed/build/run/hosts*, deren Inhalt an Deine */etc/hosts*
angefügt werden muss, damit die Namensauflösung funktioniert.

```
sudo cat /etc/hosts testbed/build/run/hosts > /etc/hosts
```

Danach kannst Du die Dienste des Prüfstandes mit folgenden URLs
erreichen:

* https://dashboard.wolkenschloss.local
* https://registry.wolkenschloss.local/v2/_catalog
* ...

### Secure Container Registry

Damit die Container Registry auf deinem Entwicklungsrechner richtig
funktioniert, muss die Namensauflösung und Root CA eingerichtet sein.

Folgender Aufruf sollte ohne Fehlermeldung ausgeführt werden können:

```bash
curl https://registry.wolkenschloss.local/v2/_catalog
```

### Kubernetes Dashboard

Das Kubernetes Dashboard kannst Du über die URL https://dashboard.wolkenschloss.local erreichen.

Für die Anmeldung benötigst Du entweder ein Token oder die Kubernetes
Konfigurationsdatei.

Das Token erhältst Du mit dem Aufruf von:

```bash
token=$(multipass exec testbed  -- microk8s kubectl -n kube-system get secret | grep default-token | cut -d " " -f1)
multipass exec testbed -- microk8s kubectl -n kube-system describe secret $token
```

Du kannst auch die Konfigurationsdatei [kubeconfig](./build/run/kubeconfig)
benutzen.

Der Prüfstand besteht aus einer virtuellen Maschine mit der [microk8s]
[microk8s] Variante von [Kubernetes][k8s] und kann bei
Bedarf automatisch erstellt und gestartet werden.

## Zugriff auf den Prüfstand

Du hast Zugriff auf den Prüfstand mit dem Befehl `multipass shell testbed`.

Nach der Einrichtung der Namensauflösung erhälst du Zugriff auf das Kubernetes Dashboard
über dir URL https://dashboard.wolkenschloss.local

Für die Anmeldung am Dashboard benötigst Du eine Kubernetes
Konfigurationsdatei oder ein Token (Das Token befindet sich in der
Konfigurationsdatei). Die Konfigurationsdatei findest Du in
[build/run/kubeconfig](build/run/kubeconfig)

## IP-Adresse des Prüfstandes ermitteln

Die IP-Adresse des Prüfstandes kannst Du mit dem Befehl `multipass info testbed`
ermitteln. Die IP-Adresse benötigst Du zur Aktualisierung der Datei */etc/hosts*,
wenn sich die IP-Adresse des Prüfstandes geändert hat oder Du eine neue Instanz
erzeugt hast.

```bash
user@develop:~$ multipass info testbed
Name:           testbed
State:          Running
IPv4:           10.85.193.200
                10.0.1.1
                10.1.29.64
Release:        Ubuntu 20.04.4 LTS
Image hash:     f9b94982abcb (Ubuntu 20.04 LTS)
Load:           0.84 0.51 0.46
Disk usage:     3.8G out of 19.2G
Memory usage:   1.1G out of 3.8G
Mounts:         --
```

Alternativ kannst du auch den Befehl `./gradlew :testbed:status` verwenden:

```bash
user@develop:~/src/nubes$ ./gradlew :testbed:status
> Task :testbed:status
Status of testbed
✓ IP Address     : 10.85.193.200
✓ K8s config     : /home/user/nubes/testbed/build/run/kubeconfig
✓ Testbed        : OK
✓ Address        : registry.wolkenschloss.local
✓ Upload Image   : registry.wolkenschloss.local/hello-world:latest
✓ Catalogs       : hello-world
✓ Registry       : OK
```

## Internas

In diesem Abschnitt werden interne Details der Implementierung behandelt. 

### Namensauflösung

Einige Funktionen des Prüfstandes benötigen Zertifikate für TLS Verschlüsselung. Zertifikate
setzen wiederum funktionierende Namensauflösung voraus. Diese wird für den Prüfstand folgendermaßen
implementiert:

Mit `microk8s enable host-access` wird eine loopback Schnittstelle mit der IP-Adresse 10.0.1.1
erstellt. Sowohl der Host, wie auch Container können auf diese Schnittstelle zugreifen.

Für die Namensauflösung auf der Seite des Hosts werden in der Datei `/etc/hosts` die entsprechenden
Einträge für die IP-Adresse 10.0.1.1 gesetzt. Die Datei `/etc/hosts` wird durch `cloud-init` erzeugt.
Dazu verwendet `cloud-init` die Vorlagendatei `/etc/cloud/templates/hosts.debian.tmpl`. Diese Vorlage
wird vom testbed Projekt angepasst:

      ## template:jinja
      127.0.1.1 {{fqdn}} {{hostname}}
      127.0.0.1 localhost
      10.0.1.1 ${hosts.get()}
      ::1 localhost ip6-localhost ip6-loopback
      ff02::1 ip6-allnodes
      ff02::2 ip6-allrouters

Dabei sind `{{fqdn}}` und `{{hostname}}` jinja Variablen, die durch cloud-init gesetzt werden. Bei
`{$hosts.get()}` handelt es sich um eine Variable, die durch den gradle Erstellungsprozess gesetzt
wird.

Weiterhin ist eine Anpassung der CoreDNS Konfiguration notwendig. Dazu wird von cloud-init die in
user-data stehende Datei `/etc/testbed/coredns-config.yaml` erstellt. Die Anpassung enthält einen
zusätzlichen `hosts` Konfigurationsblock mit den statischen DNS Einträgen.

Namensauflösung aus einem Container:

```bash
user@develop:~$ multipass shell testbed
Welcome to Ubuntu 20.04.4 LTS (GNU/Linux 5.4.0-1059-kvm x86_64)
...
ubuntu@testbed:~$ microk8s kubectl run xbox -i --tty --rm --image alpine -- sh
If you don't see a command prompt, try pressing enter.
/ # nslookup registry.wolkenschloss.local
Server:		10.152.183.10
Address:	10.152.183.10:53

Name:	registry.wolkenschloss.local
Address: 10.0.1.1

```

Namensauflösung im Host:

`multipass exec testbed -- nslookup registry.wolkenschloss.local`