## Architekturziele

- Erfahrung mit Microservice Technologie sammeln.
- Erarbeitung einer CI/CD Pipeline mit einfachen Mitteln.
- Lernen der Zusammenarbeit in einem Projekt.

### Randbedingungen

1. Das Projekt ist nicht gewinnorientiert
2. Es ist ein Lernprojekt für
    - Zusammenarbeit
    - Beispiel für professionelle Softwareentwicklung
    - Open Source Entwicklung
3. Es darf kein Geld kosten, außer dem was eh schon vorhanden ist.
    - Keine Lizenzgebühren
    - Keine gemieteten Server
    - ...
4. Das Projekt ist zunächst nur für die teilnehmenden Mitglieder bestimmt.
    - Keine öffentlich sichtbaren Code Repositories und Artefakte.

### Architekturentscheidungen

#### Getroffene Entscheidungen

##### Für die Quellcode- und Projektverwaltung wird [GitHub] verwendet

Alternativen:

- JetBrains [Space](https://www.jetbrains.com/space/).
  Keine Erfahrung. Relativ neues Produkt. Wahrscheinlich gute Alternative zu
  Github.
- Selbst gehostet (z. B. Gitlab und Jenkins).
  Hoher Aufwand für Installation und Betrieb.

Für die Zusammenarbeit ist die Organisation [Wolkenschloss]
auf [GitHub] angelegt. Die Organisation verfügt über einen kostenlosen Zugang
(Free Plan). Für einen Vergleich der Organisationskonten siehe auch
[Compare Plans](https://github.com/organizations/wolkenschloss/billing/plans).

Im [Wolkenschloss] können private Projekte angelegt werden, die nur für
Teammitgliedern sichtbar sind. Allerdings stehen nicht alle GitHub Funktionen
im *Free Plan* zur Verfügung.

##### Die Anzahl der Abhängigkeiten ist so gering wie möglich zu halten

Die Zeit für Builds auf öffentlichen Servern ist für kostenlose Zugänge in
der Regel beschränkt. Abhängigkeiten werden bei jedem Build aus den öffentlichen
Repositories geladen. Der Download von Drittkomponenten verlängert die Dauer
des Builds.

##### Konventionen

Die im Projekt verwendeten Konventionen sind in einem eigenen Dokument
beschrieben.

##### Konventionelle Commits

[Konventionelle Commits] werden für Commit Messages verwendet.

Eine Systematik für Commit Messages gehört zum professionellen Arbeiten und
verbessert die Team-Zusammenarbeit.

Aus den *konventionellen Commits* können automatisch verarbeitet werden und
sind von Menschen gut lesbar.

Mehr:

- **[Keeping Git Commit Messages Consistent with a Custom Template ](https://dev.to/timmybytes/keeping-git-commit-messages-consistent-with-a-custom-template-1jkm)**
- [Conventional Commit Messages](https://gist.github.com/qoomon/5dfcdf8eec66a051ecd85625518cfd13)
- [Using Git Commit Message Templates to Write Better Commit Messages](https://gist.github.com/lisawolderiksen/a7b99d94c92c6671181611be1641c733)
- [commitlint](https://github.com/conventional-changelog/commitlint)
- [Conventional Commits](https://www.conventionalcommits.org/de/v1.0.0/)
- [Git commit messages for the bold and the daring](https://backlog.com/blog/git-commit-messages-bold-daring/)

##### Semantic Versioning

[Semantic Versioning] ist ein weit verbreitetes Verfahren wie Versionsnummern
gewählt und erhöht werden. Mit einer von Anfang an einheitlichen Vergabe der
Versionsnummern wird das professionelle Arbeiten im Team unterstützt. Es
gibt keine Auseinandersetzung bezüglich der Versionsnummern.

##### Git Branches

[Wolkenschloss] verwendet [GitFlow].

* Verringert den Aufwand, ein eigenes Branch Modell zu entwickeln.
* Ist ein bekanntes und gut dokumentiertes Vorgehen.

##### Lizenz

Die Software wird unter der Apache 2.0 Lizenz bereitgestellt.

- [Apache License, Version 2.0](https://opensource.org/licenses/Apache-2.0)
  auf [opensource.org](https://opensource.org/licenses/Apache-2.0)
- [Copyright notices for open source projects](https://ben.balter.com/2015/06/03/copyright-notices-for-websites-and-open-source-projects/)
- [SPDX Tutorial](https://github.com/david-a-wheeler/spdx-tutorial#spdx-tutorial)

##### Vue.js für Frontend

Du brauchst:

- nvm: Node Version Manager
- npm: Node Package Manager
- node.js: Node selbst
- vue.js

##### Git als Quellcode Verwaltung

- [Git] Mono-Repository
- [GitHub] Remote Repository

[Git]: https://git-scm.com/


#### Offene Entscheidung

- [x] Unter welcher Lizenz soll das Projekt veröffentlicht werden?
- [ ] Wie wird die CI/CD Pipeline realisiert?
- [ ] Konventionen:
    - Semantic Versioning
    - Conventional Commits
    - Copyright Hinweis
    - Lizenz (SPDX)
    - Git Naming Conventions -> [Pull Request Naming](https://namingconvention.org/git/pull-request-naming.html)
    - Git Branch Name [Branching](https://gist.github.com/digitaljhelms/4287848)
    - weitere

[Wolkenschloss]: https://github.com/wolkenschloss
[GitHub]: https://github.com/
[Konventionelle Commits]: https://www.conventionalcommits.org/de/v1.0.0/
[Semantic Versioning]: https://semver.org/lang/de/
[GitFlow]: https://nvie.com/posts/a-successful-git-branching-model/