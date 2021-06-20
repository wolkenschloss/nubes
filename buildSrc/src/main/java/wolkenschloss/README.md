# Testbed Gradle Plugin

Das Plugin dient dazu ein Gradle Projekt zu unterstützen, mit dem
ein Prüfstand aufgebaut werden kann.

Das Plugin benutzt absichtlich nicht die Standard-Lebenszyklen von Gradle,
um den Unterschied zu diesen hervorzuheben. Die Erstellung des Prüfstandes
ist eine umfangreiche Aufgabe, die mit einem üblichen Erstellungsvorgang
nicht zu vergleichen ist.

Die angedachten Lebenszyklus-Aufgaben wären folgende:

* **start** erzeugt und startet den Prüfstand. Wenn bereits ein
Prüfstand existiert, wird dieser wiederaufgenommen, sollte er pausiert sein.
  Sollten bei der Wiederaufnahme eines bestehenden Prüfstandes Änderungen an 
  der Konfiguration erkannt werden, welche eine Neuerstellung des 
  Prüfstandes zur Folge hätte, wird der Startvorgang abgebrochen. Der alte 
  Prüfstand muss dann erst mit `gradle destroy` zerstört werden.
  Die Aufgabe *start* ist die Voreingestellte Aufgabe des Projekts. Um den 
  Prüfstand zu starten, reicht also die Eingabe von `gradle :testbed`.
* **stop** hält einen vorhandenen Prüfstand an. Das sollte immer gemacht 
  werden, wenn der Prüfstand nicht benötigt wird. Der Prüfstand verbraucht 
  verhältnismäßig viele Systemressourcen.
* **destroy** löscht den Prüfstand mitsamt allen Ressourcen.

Das Plugin soll keinen Ersatz für die Schnittstelle zu *libvirt* sein und 
weder `virsh` noch `Virtuelle Maschinenverwaltung` ersetzen. Die Aufgabe des 
Plugins besteht primär darin, die erforderlichen Artefakte, wie angepasste 
Konfigurationsdateien und Festplatten-Abbilder zu erstellen. Ebenso die 
Entwicklungsumgebung für den Zugriff auf den Prüfstand vorzubereiten.

Zum Erstellen von Snapshots oder Autostarteinstellungen kannst Du weiterhin 
die bekannten Werkzeuge wie zum Beispiel `virsh` benutzen.

## Anforderungen

Damit der Prüfstand automatisch erstellt werden kann, müssen folgende 
Softwarepakete installiert sein (Ubuntu 20.04).

*TBD*

## Zu Erledigen

  * Die IP-Adresse des Prüfstandes ermitteln. Die virtuelle Maschine 
    bezieht ihre IP-Adresse vom libvirt DHCP Server.
  * Auf den Phone-Home Rückruf der virtuellen Maschine warten. Dazu muss ein 
    Webserver gestartet werden. Die Prüfstand VM sendet den Server-Key
    an den Webserver. (Ein Gradle Task startet einen Webserver? Das ist ein 
    Hack!) https://stackoverflow.com/a/3732328/168944
  * Den Server-Key in die known_hosts Datei eintragen und ggf. einen bereits 
    vorhandenen Server-Key von einer vorangegangenen Instanz entfernen.
  * Die Kubernetes Konfiguration aus dem Prüfstand auslesen und in das 
    Verzeichnis build/run kopieren.
  * Bessere Gruppierung der Aufgaben. Derzeit ist noch alles in der Gruppe 
    *other*.
  * Die Aufgaben `defineDomain` und `definePool` umstellen, so dass die 
    libvirt API verwendet wird statt virsh.#
  * Abhängigkeiten und Inkrementellen Build prüfen. Es sollte nur das gebaut 
    werden, dass sich auch geändert hat. Nach dem ersten Start des 
    Prüfstandes dürfen keine Änderungen mehr vorgenommen werden. Der 
    Prüfstand muss zunächst zerstört werden.