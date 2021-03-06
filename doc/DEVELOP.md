# Develop

Dieser Artikel beschreibt die Voraussetzungen zur Mitarbeit an
*Wolkenschloss/nubes*. Diese Beschreibung setzt voraus, dass Du Ubuntu 20.04
Desktop verwendest. Benutzt Du etwas anderes, bist Du auf Dich allein gestellt.

## Entwicklungsumgebung
### Testumgebung
* E2E Tests (cypress): sudo apt install -y xvfb libxss1
* QuarkusTest (): sudo apt install 

```bash
# Zunächst das übliche Procedere: System aktualisieren
sudo apt update
sudo apt upgrade

# Zeugs für die Testumgebung
sudo apt install -y xvfb libxss1

wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo apt install ./google-chrome-stable_current_amd64.deb

# ggf. alte Docker Version entfernen
sudo apt-get remove docker docker-engine docker.io containerd runc

# Docker installieren
sudo apt-get install     apt-transport-https     ca-certificates     curl     gnupg     lsb-release
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo   "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
   
sudo apt update
sudo apt-get install docker-ce docker-ce-cli containerd.io
sudo usermod -aG docker $USER

sudo reboot
docker info
```

### SSH Schlüsselpaar

Du benötigst ein SSH Schlüsselpaar für deinen Github Account und den
[Prüfstand][testbed]. Wenn du noch kein SSH Schlüsselpaar besitzt, erzeuge
einen:

```bash
ssh-keygen
```

Weitere Hinweise dazu findest du unter [Authentifizierung über Public-Keys][ssh]

### GitHub Account

Du benötigst einen [GitHub] Account.

#### Öffentlichen SSH Schlüssel hinterlegen

Hinterlege Deinen öffentlichen SSH Schlüssel in den
[SSH and GPG keys][github-keys] Einstellungen Deines Github Profils. Deinen
öffentlichen SSH Schlüssel findest Du in der Datei `$HOME/.ssh/id_rsa.pub`.

Weitere Informationen findest Du im Artikel
[Connecting to GitHub with SSH][github-ssh].

#### E-Mail Einstellungen

In den [Emails][github-email] Einstellungen solltest Du die Optionen `Keep my
email addresses private` und `Block command line pushes that expose my email`
einschalten. Du findest hier auch eine E-Mail-Adresse, die Du für Git
commits verwenden solltest.

Weitere Informationen findest du im Artikel
[Setting your commit email address][github-commit-email]

Wolkenschloss verwendet [Gradle] als Build-Tool. Um Gradle starten zu können 
brauchst du eine Java Runtime (JRE) oder Development Kit (JDK). 

```bash
sudo apt install openjdk-11-jdk
```
Zur Einrichtung des Prüfstandes folge den Anweisungen in
[README.md][testbed]

### Git

Um Beiträge im Repository zu veröffentlichen, musst du die E-Mail-Adresse in 
Git konfigurieren. Folge dazu den Anweisungen auf 
[Setting your commit email address in Git][gea].

### Frontend

- [Node Version Manager (nvm)][nvm]
- [Node Package Manager (npm)][npm]
    - [Documentation](https://docs.npmjs.com/)
        - [Configuring your local environment](https://docs.npmjs.com/getting-started/configuring-your-local-environment/)
- [Vue CLI](https://cli.vuejs.org/)
    - [Getting Started](https://cli.vuejs.org/guide/)
    - [Installation](https://cli.vuejs.org/guide/installation.html)
- [Vue.js][vue]

Installation des Node Version Managers:

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.38.0/install.sh | bash
```

Installation von Node und npm:

```bash
nvm install --lts
```

Installation von @vue/cli

```
npm install -g @vue/cli
```

Vue.js Projekt anlegen:

```bash
vue create hello-world
```

Vue 2 auswählen! Bootstrap Vue ist nicht kompatibel zu Vue 3:
[Vue 3 support #5196](https://github.com/bootstrap-vue/bootstrap-vue/issues/5196)

Bootstrap Vue Plugin hinzufügen

```bash
cd hello-world
vue add bootstrap-vue
```

Sie dazu auch [Getting Started](https://bootstrap-vue.org/docs#vue-cli-3-plugin)


## Konventionen

### REST
* [Representational State Transfer](https://de.wikipedia.org/wiki/Representational_State_Transfer)
* [REST Resource Naming Guide](https://restfulapi.net/resource-naming/)
* [Best practices for REST API design](https://stackoverflow.blog/2020/03/02/best-practices-for-rest-api-design/)
 

### Microservice
Jeder Microservice besteht aus drei Unterprojekten, für die jeweils eine 
Gradle Build Konvention existiert.

| Verzeichnisname | Konvention          | Zweck                                |
| ----------------|---------------------|--------------------------------------|
| webapp          | wolkenschloss.conventions.webapp  | Webbasierte Benutzeroberfläche       |
| service         | wolkenschloss.conventions.service | Technische Infrastruktur für HTTP Endpunkt, Persistenz, Messaging und weiteres |
| core            | wolkenschloss.conventions.core | Implementierung der Domäne |

Die Projekte *service* und *core* folgen der 
[Zwiebelschalen-Architektur][onion].

Die Artefakte des *webapp* Projekts werden als statische Ressourcen in den 
Klassenpfad der *service* Anwendung gelegt. Damit wird die CORS Header 
Problematik umgangen und kein zusätzlicher Webserver für die Auslieferung 
der Webanwendung benötigt.

## Build Prozess

**Das Projekt muss gleichermaßen mit [Gradle] in der Kommandozeile und
[IntelliJ] gebaut und getestet werden können.**

## Organisatorische Konventionen

### Pull-Requests
* Es dürfen nur Pull-Requests angenommen werden, deren Tests erfolgreich 
  durchlaufen wurden. (Gradle Build Action ist grün)
* Es dürfen nur Pull-Requests angenommen werden, die durch Tests eine neue 
  Funktionalität spezifizieren.
  * Ausnahme: Pull Requests, in denen ausschließlich Dokumentationen 
    geändert oder hinzugefügt werden.
* Alle Pull-Requests durchlaufen einen Review-Prozess.

### Arbeitsabläufe

#### GitFlow und Pull Request

GitFlow berücksichtigt nicht die Arbeitsabläufe in Zusammenhang mit GitHub.
Mit Pull Requests können in GitHub Actions ausgelöst werden. Wolkenschloss
verwendet Actions für den Continuous Integration (CI) Build. Dazu werden
einige Schritte in GitFlow durch entsprechende Pull Requests ersetzt:

0. create new branch from develop
1. [Incorporating a finished feature on develop]
    * PR: origin/develop <- origin/feature-branch
    * Squash and merge
2. [Creating a release branch]
   ```bash
   # Remote Branches > origin/develop > New Branch from Selected...
   git checkout -b release-* origin/develop
   change build.gradle.kts
   # Commit > Commit and Push...
   git commit -a -m "bump: version *"
   ```
3. [Finishing a release branch]  
   * PR: origin/master <- origin/release-* 
   * Squash and merge
   * Create version tag on master:
   ```bash   
   # Local Branches > master > Checkout
   git checkout master
   # Update Project (Ctrl-T)
   git pull
   # New Tag
   git tag v*
   # Push (Ctrl-Shift-K)
   git push --tags
4. Github: [Create new release](https://github.com/wolkenschloss/nubes/releases/new)
   * Choose a tag
   * [ ] This is a pre-release
   * Publish release
5. PR: origin/develop <- origin/release-*
6. `git checkout -b nubes-* origin/develop` ?


#### Issues bearbeiten

1. Ein Issue im Projekt in den Status *in progress* setzen.
2. Auf dem Entwicklungsrechner einen neuen Feature-Branch anlegen. Der Name des 
   Branches soll der Konvention `nubes-#` folgen, wobei *#* 
   der Nummer des Issues aus Schritt 1 entspricht. In IntelliJ kannst Du 
   `Tools -> Tasks & Contexts -> Open Task` verwenden, wenn Du IntelliJ 
   entsprechend eingerichtet hast.
3. Änderungen mit `git commit` und `git push` regelmäßig sichern und 
   synchronisieren. Für Commit Nachrichten 
   [Conventional Commit Messages][conventional commits] verwenden.
4. Pull-Request erstellen und den Pull-Request mit dem bearbeiteten Issue 
   und Projekt verlinken. Der Pull-Request soll den Feature-Branch 
   *nubes-#* inden *master* Branch zusammenführen.
5. Review des Pull-Requests. Den Pull-Request in den Status *Review 
   in Progress* setzen. 
6. Anmerkungen ggf. Nacharbeiten und Änderungen einchecken.
7. Der Reviewer setzt den Pull-Request in den Status *Reviewer approved*.
8. Auf Github die Änderungen durch einen *Squash-Merge* in den *master* Branch 
   zusammenführen.
9. Auf Github den Feature-Branch löschen.
10. Im Github Projekt Issue und Pull-Request in den Status *Done* setzen
11. Auf dem Entwicklungsrechner den *master* Branch aktualisieren, auschecken 
   und den Feature-Branch löschen.

Die Bearbeitung eines Issues soll einen Tag nicht überschreiten. Wenn ein 
Issue in diesem Zeitraum nicht bearbeitet werden kann, sollen weitere 
Issues erstellt werden, in denen die Restarbeiten dokumentiert sind.

#### Planung

Der Zeitraum eines Meilensteins beträgt ein Monat und hat ein festgelegtes, 
dokumentiertes Ziel. Der Name des Meilensteins folgt der Konvention *M#*, 
wobei *#* eine fortlaufende Zahl ist.

Zum Ende des Zeitraums wird der nächste Meilenstein unter Beachtung der 
folgenden Punkte geplant:

1. Neuen Meilenstein in Github anlegen.
2. Das Ziel des Meilensteins benennen.
3. Issues auswählen, die relevant für die Zielerreichung sind und dem 
   Meilenstein zuordnen.
4. Issues priorisieren
5. Aufwand der einzelnen Issues abschätzen.

## Referenzen

### Badges

Dokumentation zu den Build Badges in GitHub:
<https://docs.github.com/en/actions/managing-workflow-runs/adding-a-workflow-status-badge>

[Gradle]: https://gradle.org/
[IntelliJ]: https://www.jetbrains.com/de-de/idea/
[GitHub]: https://github.com/
[conventional commits]: https://www.conventionalcommits.org/de/v1.0.0/
[ssh]: https://wiki.ubuntuusers.de/SSH/#Authentifizierung-ueber-Public-Keys
[testbed]: /testbed/README.md
[github-ssh]: https://docs.github.com/en/github/authenticating-to-github/connecting-to-github-with-ssh
[github-email]: https://github.com/settings/emails
[github-commit-email]: https://docs.github.com/en/github/setting-up-and-managing-your-github-user-account/managing-email-preferences/setting-your-commit-email-address
[github-keys]: https://github.com/settings/keys
[onion]: https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/
[nvm]: https://github.com/nvm-sh/nvm
[npm]: https://www.npmjs.com/
[vue]: https://v3.vuejs.org/
[gea]: https://docs.github.com/en/github/setting-up-and-managing-your-github-user-account/managing-email-preferences/setting-your-commit-email-address#setting-your-commit-email-address-in-git

[Incorporating a finished feature on develop]: https://nvie.com/posts/a-successful-git-branching-model/#incorporating-a-finished-feature-on-develop
[Creating a release branch]: https://nvie.com/posts/a-successful-git-branching-model/#creating-a-release-branch
[Finishing a release branch]: https://nvie.com/posts/a-successful-git-branching-model/#finishing-a-release-branch