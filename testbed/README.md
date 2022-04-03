# Testbed

Ein Prüfstand für Wolkenschloss zum Testen und Entwickeln.

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

## TL;DR

Das läuft so auf einem Intel Core i7 mit Ubuntu 20.04, 16 GB 
RAM und 1 TB SSD.

```bash
# Falls du keinen SSH Schlüssel besitzt, musst du einen anlegen
ssh-keygen

# GPG Schlüssel zur Prüfung der Signatur installieren
gpg --keyid-format long --keyserver hkp://keyserver.ubuntu.com \
  --recv-keys 0x1A5D6C4C7DB87C81

# System aktualisieren und benötigte Pakete installieren  
sudo apt update && sudo apt -y upgrade
sudo apt install -y qemu-kvm libvirt-daemon-system cloud-image-utils

# Kubernetes Client installieren und konfigurieren
sudo snap install kubectl --classic
echo 'source <(kubectl completion bash)' >>~/.bashrc

###############
# NEU ANMELDEN
###############

# testbed erstellen
./gradlew :testbed:start
export KUBECONFIG=$(realpath testbed/build/run/kubeconfig)

# testbed anhalten
virsh shutdown testbed

# testbed fortsetzen
virsh start testbed

# testbed endgültig löschen und alle Ressourcen freigeben:
./gradlew :testbed:destroy

# testbed ingress Konfiguration. Sollte automatisiert werden
./gradlew :testbed:applyCommonServices

# DNS überlisten:
sudo cat testbed/build/run/hosts >> /etc/hosts
sudo echo >> /etc/hosts

# CA installieren (sollte auch irgendwie automatisiert werden):
sudo copy $HOME/.local/share/wolkenschloss/ca/ca.crt /usr/local/share/ca-certificates
sudo update-ca-certificates
sudo systemctl restart docker
# Zertifikat im Browser installieren
# Firefox: Einstellungen > Datenschutz & Sicherheit > Zertifikate anzeigen... > Importieren...
```

## IP-Adresse des Prüfstandes ermitteln

Um mit ssh auf den Prüfstand zugreifen zu können oder die Oberfläche
des Kubernetes Dashboards zu erreichen benötigst du die IP-Adresse des 
Prüfstandes. Mit `virsh` kannst du die IP-Adresse ermitteln:

```bash
$ virsh domifaddr testbed
 Name       MAC address          Protocol     Address
-------------------------------------------------------------------------------
 vnet0      52:54:00:8a:ba:90    ipv4         192.168.123.15/24
```

## Kubernetes Dashboard

Der Prüfstand besitzt ein Dashboard mit Weboberfläche:

<https://dashboard.wolkenschloss.local/>

Für die Anmeldung am Dashboard benötigst Du eine Kubernetes 
Konfigurationsdatei oder ein Token (Das Token befindet sich in der 
Konfigurationsdatei). Die Konfigurationsdatei findest Du in 
[build/run/kubeconfig](build/run/kubeconfig) 

## SSH Verbindung

Sofern Du die Datei `/etc/hosts` so wie oben beschrieben angepasst hast,
erreichst Du den Prüfstand mit: `ssh testbed.wolkenschloss.de`.

Alternativ kannst Du den Testbed-Client `tbc` verwenden, wenn Du diesen
installiert hast: 

```
ln -s $(realpath $HOME/.local/bin/tbc) $(realpath testbed/src/client.bash)
```

Mit dem Befehl `tbc` gelangst Du zu einer Shell in einem Docker Container.
In diesem Container sind die Client-Programme für den Zugriff auf den 
Prüfstand installiert und konfiguriert:

* kubectl
* cmctl
* ssh
* openssl


[k8s]: https://kubernetes.io/
[microk8s]: https://microk8s.io/docs

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