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

Der Prüfstand besitzt ein Dashboard mit Weboberfläche. Für den Zugriff 
solltest du den Ingress für das Dashboard installieren, wenn du nicht den 
`kubectl proxy` verwenden willst.

```bash
kubectl apply -f dashboard-ingress.yaml
```

Das Dashboard ist danach mit der Adresse `https://$TESTBED_ADDRESS/dashboard` 
erreichbar, wobei Du `$TESTBED_ADDRESS` durch die zuvor ermittelte IP-Adresse 
des Prüfstandes ersetzen musst.

Für die Anmeldung am Dashboard benötigst Du eine Kubernetes 
Konfigurationsdatei oder ein Token (Das Token befindet sich in der 
Konfigurationsdatei). Die Konfigurationsdatei findest Du in 
`testbed/build/run/kubeconfig`. 

## SSH Verbindung

Nachdem Du die IP-Adresse des Prüfstandes ermittelt hast, kannst Du Dich 
einfach durch `ssh $TESTBED_ADDRESS` mit dem Prüfstand verbinden. Ein 
Benutzer mit dem gleichen Namen wurde bereits angelegt und Dein öffentlicher 
Schlüssel kopiert. Eine Anmeldung mit Benutzernamen und Kennwort ist weder 
erforderlich noch möglich.

[k8s]: https://kubernetes.io/
[microk8s]: https://microk8s.io/docs