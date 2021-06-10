# Testbed

Ein Prüfstand für Wolkenschloss.

Der Prüfstand umfasst eine virtuelle Maschine mit
[microk8s][microk8s]. Die Services des Wolkenschlossprojekts
können in der Kubernetesumgebung der virtuellen
Maschine getestet werden.

## Voraussetzungen

Um den Prüfstand nutzen zu können, müssen folgende
Softwarepakete installiert sein:

* Internetverbindung
* libvirt
* node.js
* Weis der Henker, was noch


## Anwendung

Der Prüfstand, also die virtuelle Maschine mit 
dem Kubernetes kann mit folgendem Befehl hergestellt
werden: *(Wunschtram)*

```
testbed create
```

Nachdem die virtuelle Maschine erstellt wurde, kann
sie mit einem weiteren Befehl gestartet werden:

```
testbed start
```

Der erste Start des Prüfstandes dauer sehr lange.
Die virtuelle Maschine muss zunächst Pakete aktualisieren
und Kubernetes einrichten. Es ist nicht empfohlen
den Prüfstand für jeden Test neu aufzusetzen. Statt
dessen, sollten die Namespaces im Prüfstand gelöscht
werden, um eine saubere Neuinstallation durchzuführen.

Wenn der Prüfstand vorübergehend nicht benutzt wird,
können die belegten Systemressourcen freigegeben
werden, in dem die virtuelle Maschine mit folgendem
Befehl angehalten wird:

```
testbed stop
```

Der Prüfstand wird nach einem Neustart des
Entwicklungsrechners nicht automatisch neu gestartet.
Dazu muss der Befehl `testbed start` erneut
ausgeführt werden.

Um einen Prüfstand vollständige zu löschen und
auch die Festplatten Abbilder zu entfernen gibt
es den Befehl `delete`:

```
testbed delete
```



## Nutzung des Prüfstandes

[TBD]

[microk8s]: https://microk8s.io/docs

## To Do

### Add Ons

Folgende Add Ons müssen noch installiert werden:

```shell
dashboard            # The Kubernetes dashboard
dns                  # CoreDNS
ha-cluster           # Configure high availability on the current node
ingress              # Ingress controller for external access
metrics-server       # K8s Metrics Server for API access to service metrics
storage              # Storage class; allocates storage from host directory
```


### Ingress für Dashboard

Ingress für das Dashboard installieren, damit von
außen auf die Weboberfläche zugegriffen werden kann


```yaml
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: dashboard
  namespace: kube-system
  annotations:
    # use the shared ingress-nginx
    kubernetes.io/ingress.class: public
    nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
    nginx.ingress.kubernetes.io/rewrite-target: /$2
    nginx.ingress.kubernetes.io/configuration-snippet: |
      rewrite ^(/dashboard)$ $1/ redirect;
spec:
  # https://kubernetes.io/docs/concepts/services-networking/ingress/
  # https://kubernetes.github.io/ingress-nginx/user-guide/tls/
  rules:
  - http:
      paths:
      - path: /dashboard(/|$)(.*)
        pathType: ImplementationSpecific
        backend:
          service:
            name: kubernetes-dashboard
            port:
              number: 443
```

Das Dashboard ist dann unter https://192.168.122.152/dashboard
erreichbar.

### Token für Dashboard

Token für das Dashboard ermitteln:

```
token=$(microk8s kubectl -n kube-system get secret | grep default-token | cut -d " " -f1)
microk8s kubectl -n kube-system describe secret $token
```

### Image Registry

