# Zwiebelschalen Architektur

## Application Services

Anwendungsdienste stellen die Funktionalität nach außen und zwischen den 
Anwendungsteilen bereit. Sie verbinden die technische Schicht mit dem Modell. 
Technische Elemente sind zum Beispiel:

* ReST-Endpoints für die Benutzerschnittstelle
* Ereignis-Handler für die Kommunikation zwischen den Anwendungsteilen.
* Datenbank für die Persistenz der Aggregate und Sichten
* Web Client für den Zugriff auf externe Systeme
* Codecs

## Domain Services

Domaindienste besitzen im Gegensatz zu Anwendungsdiensten keine Kenntnisse 
über die Umgebung, in der sie eingesetzt werden und sind daher frei von 
jeglichen technischen Bezügen. Event Bus, Web Client und Datenbank sind 
Beispiele für technische Komponenten, die nicht in Domaindiensten benutzt 
werden dürfen.

Um die Brücke zwischen Anwendungsdiensten und Domaindiensten herzustellen 
werden folgende Muster verwendet:

* Abhängigkeitsumkehrung (Inversion of Dependency): Der Domaindienst schlägt 
  eine Schnittstelle aus fachlicher Sicht vor, die von der Anwendungsschicht 
  implementiert wird. Die Implementierung wird dem Domaindienst übergeben.
* Ereignisse, sind Bestandteile des Domänenmodells. Das Modell muss aber 
  frei von technischen Abhängigkeiten zu konkreten Implementierungen sein. 
  Daher geben Methoden, die Ereignisse auslösen sollen, das Ereignis als 
  Ergebnis zurück. Soll eine Methode aus ein Ereignis reagieren, so erhält 
  diese Methode das Ereignis als Parameter. Es handelt sich dabei um eine 
  Konvention und nicht um eine technische Lösung. Event Bus und Event 
  Handler bleiben in der Zuständigkeit der Anwendungsschicht.
* Reaktive Programmierung liegt in der Zuständigkeit der Anwendungsschicht. 
  Das Modell bleibt in völliger Unkenntnis eines solchen Programmierstils.

Ein Beispiel für die Anwendung von Ereignissen in einem Modell:

1. Ereignis JobReceived tritt ein. Ein Ereignis Handler der 
   Anwendungsschicht nimmt das Ereignis entgegen.
2. Die Anwendungsschicht ruft eine den Domaindienst auf, der das Ereignis 
   verarbeiten kann. Der Domaindienst erzeugt durch seine Operation zwei 
   neue Modell-Elemente. Ein Recipe Entity und ein Ereignis JobCompleted.
3. Die Anwendungsschicht persistiert die Recipe Entität.
4. Die Anwendungsschicht sendet das JobCompleted Ereignis über den Event Bus.

Die unten aufgeführte Beispielimplementierung zeigt die Entkopplung von 
Domänenmodell und Anwendungsschicht: Das Modell definiert eine Schnittstelle 
`Grabber`. Über diese Schnittstelle teilt der RecipeImportService mit, wie 
aus der Datenquelle ein Recipe Entity transformiert wird. Diese 
Transformation ist Teil der Domäne. Der Grabber kann nun synchron, asynchron 
oder reaktiv die Daten von der Quelle lesen und der Transformation übergeben.  

Der Anwendungsdienst ist nun zuständig, das Ergebnis zu verarbeiten. Die 
Entität wird persistiert und danach das Ereignis gesendet. Dem 
Anwendungsdienst obliegt die Verantwortung für die verwendete Technologie. 
In diesem Fall wird die Verarbeitung in einem reaktiven Modell ausgeführt.

```java
import family.haschka.wolkenschloss.cookbook.job.JobCompletedEvent;
import family.haschka.wolkenschloss.cookbook.recipe.download.RecipeImport;
import io.quarkus.vertx.ConsumeEvent;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Function;

class RecipeApplication {
    @Inject
    Grabber grabber;

    @Inject
    Repository repository;

    @ConsumeEvent("job.received")
    public void onJobReceived(JobReceivedEvent event) {
        var service = new RecipeImportService();
        service.grab(grabber, event);
        grabber.then(result ->
                repository.persist(result.entity)
                        .subscribe()
                        .with(entity -> bus.publish("completed", result.event)));
    }
}

interface Grabber {
    extract(URI source, Function<String, RecipeImport.GrabResult> fn);

    then(Consumer<RecipeImport.GrabResult> fn);
}

class RecipeImportService {

    public void grab(Grabber grabber, JobReceivedEvent event) {
        return grabber.extract(event.source(), content -> {
            var entity = parse(content);
            var event = new JobCompletedEvent(entity);
            return new GrabResult(entity, event);
        });
    }

    public static class GrabResult {
        public void GrabResult(Recipe entity, JobCompleted event) {
            this.entity = entity;
            this.event = event;
        }
    }
}
```

