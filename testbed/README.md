# Testbed

Ein Prüfstand für Wolkenschloss.

Der Prüfstand stellt eine Cloud Umgebung für den automatisierten Test der 
Microservices in der Entwicklungsumgebung bereit, die ähnliche Eigenschaften 
wie die Zielplattform aufweist.

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
In diesem Container sind die Client Programme für den Zugriff auf den 
Prüfstand installiert und konfiguriert:

* kubectl
* cmctl
* ssh
* openssl


[k8s]: https://kubernetes.io/
[microk8s]: https://microk8s.io/docs