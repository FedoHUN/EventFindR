# EventfindR - Analýza projektu

## Zadanie

Cieľom projektu je vytvoriť platformu, na ktorej môžu používatelia vyhľadávať a sledovať eventy, najmä hudobné, ale aj iné kultúrne či spoločenské podujatia. Organizátori môžu pridávať vlastné eventy s názvom, popisom, miestom konania, dátumom, cenou, fotografiami, odkazmi na kúpu lístkov a menami vystupujúcich. Používatelia si môžu eventy uložiť do svojho profilu buď ako „zúčastním sa" alebo „sledujem" a neskôr ich jednoducho nájsť na svojom profile cez prehľadné záložky. Systém bude podporovať vyhľadávanie a filtrovanie eventov podľa názvu, lokácie, vystupujúcich umelcov a dátumu.

Aktuálna verzia projektu rozširuje pôvodné zadanie o verejné profily organizátorov a artistov, profilové príspevky s médiami, komentáre a hodnotenia eventov, notifikácie, odporúčané a trendové eventy, správu konceptov eventov, publikovanie eventov a bezpečnejšiu autentifikáciu cez Keycloak. Backend je navrhnutý podľa hexagonálnej architektúry a frontend používa modernú Angular standalone architektúru.

---

## Zber požiadaviek

### Funkčné požiadavky

* **RQ01** Systém umožní organizátorovi vytvoriť nový event.
* **RQ02** Systém umožní organizátorovi zadať k eventu názov, popis, miesto konania, dátum a čas, cenu vstupného a odkaz na kúpu lístkov.
* **RQ03** Systém umožní organizátorovi pridať k eventu jednu alebo viac fotografií.
* **RQ04** Systém umožní organizátorovi zadať zoznam vystupujúcich umelcov/účinkujúcich.
* **RQ05** Systém umožní organizátorovi upraviť existujúci event.
* **RQ06** Systém umožní organizátorovi zmazať existujúci event.
* **RQ07** Systém umožní používateľovi prezerať zoznam všetkých dostupných eventov.
* **RQ08** Systém umožní používateľovi vyhľadávať eventy podľa názvu.
* **RQ09** Systém umožní používateľovi filtrovať eventy podľa lokácie.
* **RQ10** Systém umožní používateľovi filtrovať eventy podľa dátumu (rozsah dátumov).
* **RQ11** Systém umožní používateľovi filtrovať eventy podľa vystupujúcich umelcov.
* **RQ12** Systém umožní používateľovi označiť event ako "zúčastním sa".
* **RQ13** Systém umožní používateľovi označiť event ako "sledujem".
* **RQ14** Systém umožní používateľovi zobraziť zoznam eventov označených ako "zúčastním sa" vo svojom profile.
* **RQ15** Systém umožní používateľovi zobraziť zoznam eventov označených ako "sledujem" vo svojom profile.
* **RQ16** Systém umožní používateľovi odobrať event zo svojho profilu.
* **RQ17** Systém umožní zobraziť detailné informácie o evente (všetky údaje vrátane fotografií).
* **RQ18** Systém umožní registráciu nového používateľa (organizátora alebo bežného používateľa).
* **RQ19** Systém umožní prihlásenie existujúceho používateľa.
* **RQ20** Systém automaticky zruší eventy, ktorých dátum konania už uplynul (archivovanie).
* **RQ21** Systém umožní kombinovať viacero filtrov naraz pri vyhľadávaní.
* **RQ22** Systém zobrazí pri evente počet používateľov, ktorí sa ho zúčastnia.
* **RQ23** Systém zobrazí pri evente počet používateľov, ktorí event sledujú.
* **RQ24** Systém umožní používateľovi upraviť svoje osobné informácie v profile (meno, priezvisko, kontaktné údaje).
* **RQ25** Systém umožní organizátorovi uložiť event ako koncept a neskôr ho publikovať.
* **RQ26** Systém umožní organizátorovi zrušiť a obnoviť event bez fyzického odstránenia historických údajov.
* **RQ27** Systém umožní používateľom pridávať komentáre a hodnotenia k eventom.
* **RQ28** Systém zobrazí pri evente priemerné hodnotenie a počet komentárov.
* **RQ29** Systém umožní používateľom sledovať verejné profily organizátorov a artistov.
* **RQ30** Systém umožní organizátorom a artistom vytvárať profilové príspevky s textom a médiami.
* **RQ31** Systém zobrazí používateľovi notifikácie o dôležitých zmenách, nových komentároch, followeroch a pripomienkach.
* **RQ32** Systém zobrazí trendové eventy podľa popularity, účasti, hodnotení a aktivity.
* **RQ33** Systém zobrazí podobné eventy podľa žánru, lokality a vystupujúcich.
* **RQ34** Systém rozlišuje roly USER, ORGANIZER, ARTIST a ADMIN a podľa nich povoľuje jednotlivé operácie.

---

## Slovník pojmov

| Pojem | Anglický názov | Definícia |
|-------|----------------|-----------|
| Event | Event | Podujatie (hudobné, kultúrne, spoločenské), ktoré má definovaný názov, popis, miesto konania, dátum, čas a cenu. Event môže mať pridané fotografie a zoznam vystupujúcich. |
| Organizátor | Organizer | Používateľ systému, ktorý má oprávnenie vytvárať, upravovať a mazať eventy. Jeden organizátor môže spravovať viac eventov. |
| Používateľ | User | Osoba registrovaná v systéme, ktorá môže vyhľadávať eventy a označovať ich ako "zúčastním sa" alebo "sledujem". Používateľ môže byť aj organizátorom. |
| Miesto konania | Venue | Fyzická lokácia, kde sa event koná. Obsahuje názov miesta a adresu (mesto, ulica). |
| Vystupujúci | Performer | Umelec, hudobník alebo iný účinkujúci, ktorý vystupuje na evente. Jeden event môže mať viacerých vystupujúcich a jeden vystupujúci môže účinkovať na viacerých eventoch. |
| Fotografia | Photo | Obrázok pridaný k eventu, ktorý vizuálne reprezentuje podujatie. Event môže mať viac fotografií. |
| Účasť | Attendance | Vzťah medzi používateľom a eventom vyjadrujúci zámer používateľa zúčastniť sa eventu. Používateľ sa môže zúčastniť viacerých eventov. |
| Sledovanie | Following | Vzťah medzi používateľom a eventom vyjadrujúci záujem používateľa o event bez priameho závazku účasti. Používateľ môže sledovať viacero eventov. |
| Profil | Profile | Osobný priestor používateľa v systéme, kde môže upravovať svoje osobné informácie (meno, priezvisko, kontaktné údaje). Profil obsahuje záložky na zobrazenie eventov označených ako "zúčastním sa", "sledujem" a pre organizátorov aj záložku so zoznamom eventov, ktoré vytvoril. |
| Konto | Account | Identita používateľa v systéme umožňujúca autentifikáciu (e-mail a heslo) a prístup k funkcionalitám platformy. |
| Filter | Filter | Mechanizmus na zúženie výsledkov vyhľadávania eventov podľa kritérií ako lokácia, dátum, názov alebo vystupujúci. |
| Artist | Artist | Používateľ reprezentujúci interpreta alebo vystupujúceho, ktorý môže mať verejný profil, popis a zoznam eventov, na ktorých vystupuje. |
| Príspevok | Post | Textový alebo multimediálny obsah na verejnom profile organizátora alebo artista. |
| Komentár | Comment | Spätná väzba používateľa k eventu, voliteľne doplnená hodnotením. |
| Hodnotenie | Rating | Číselné vyjadrenie spokojnosti používateľa s eventom. |
| Notifikácia | Notification | Systémová správa používateľovi o udalosti, ktorá sa ho týka. |
| Koncept | Draft | Event, ktorý organizátor pripravil, ale ešte nepublikoval verejne. |
| Publikovaný event | Published event | Event dostupný verejnosti v zozname a detaile eventov. |

---

## Prípady použitia (Use Cases)

### UC01: Vytvorenie eventu

**Účel**

Vytvoriť nový event v systéme.

**Používateľ**

Organizátor

**Vstupné podmienky**

Používateľ je prihlásený v systéme a má oprávnenie organizátora.

**Výstup**

V systéme pribudne nový event. Používateľ vidí potvrdenie o vytvorení eventu a event sa zobrazí v zozname všetkých eventov.

**Postup**

1. Používateľ vybere možnosť "Vytvoriť nový event".
2. Systém zobrazí formulár na zadanie údajov eventu.
3. Používateľ vyplní povinné údaje: názov, dátum a čas, miesto konania.
4. Systém validuje zadané údaje.
5. Používateľ voliteľne vyplní popis, cenu, odkaz na lístky, pridá fotografie a vystupujúcich.
6. Systém zobrazí náhľad eventu.
7. Používateľ potvrdí vytvorenie eventu.
8. Systém uloží event do databázy.
9. Systém zobrazí potvrdenie a presmeruje používateľa na detail eventu.

**Alternatívy**

3a. Používateľ nevyplní všetky povinné polia - systém zobrazí chybové hlásenie a zvýrazní chýbajúce polia.

7a. Používateľ zruší vytvorenie eventu - systém sa vráti na predchádzajúcu stránku bez uloženia údajov.

---

### UC02: Vyhľadávanie a filtrovanie eventov

**Účel**

Nájsť konkrétne eventy podľa zadaných kritérií.

**Používateľ**

Používateľ (prihlásený alebo neprihlásený)

**Vstupné podmienky**

Používateľ má prístup k systému.

**Výstup**

Systém zobrazí zoznam eventov vyhovujúcich zadaným kritériám. Používateľ môže otvoriť detail vybraného eventu.

**Postup**

1. Používateľ otvorí stránku s eventmi.
2. Systém zobrazí zoznam všetkých aktuálnych eventov s možnosťou vyhľadávania a filtrovania.
3. Používateľ zadá vyhľadávací výraz alebo vybere filtre (lokácia, dátum, vystupujúci).
4. Systém spracuje vyhľadávanie a zobrazí filtrované výsledky.
5. Používateľ vyberie konkrétny event zo zoznamu.
6. Systém zobrazí detail vybraného eventu.

**Alternatívy**

3a. Používateľ kombinuje viacero filtrov naraz - systém aplikuje všetky filtre súčasne a zobrazí výsledky.

4a. Žiadne eventy nevyhovujú zadaným kritériám - systém zobrazí správu "Nenašli sa žiadne eventy" a ponúkne resetovať filtre.

---

### UC03: Označenie eventu ako "zúčastním sa"

**Účel**

Označiť event ako podujatie, ktorého sa používateľ zúčastní.

**Používateľ**

Používateľ

**Vstupné podmienky**

Používateľ je prihlásený v systéme.

**Výstup**

Event je pridaný do záložky "Zúčastním sa" v profile používateľa. Systém zobrazí potvrdenie a aktualizuje počet účastníkov eventu.

**Postup**

1. Používateľ prezerá detail eventu.
2. Systém zobrazí tlačidlo "Zúčastním sa" a aktuálny počet účastníkov.
3. Používateľ klikne na tlačidlo "Zúčastním sa".
4. Systém zaznamená účasť používateľa na evente, pridá event do záložky "Zúčastním sa" v profile používateľa a aktualizuje počet účastníkov eventu.

**Alternatívy**

3a. Používateľ už má event označený ako "Zúčastním sa" - systém zobrazí tlačidlo "Zrušiť účasť" a umožní odobrať označenie.

3b. Používateľ má event označený ako "Sledujem" - systém automaticky odstráni označenie "Sledujem" a nastaví "Zúčastním sa".

---

### UC04: Pridanie komentára a hodnotenia k eventu

**Účel**

Umožniť prihlásenému používateľovi reagovať na event komentárom a voliteľným hodnotením.

**Používateľ**

Používateľ

**Vstupné podmienky**

Používateľ je prihlásený a otvoril detail publikovaného eventu.

**Výstup**

Komentár sa zobrazí v detaile eventu a systém prepočíta počet komentárov, počet hodnotení a priemerné hodnotenie.

**Postup**

1. Používateľ otvorí detail eventu.
2. Používateľ vyplní text komentára a prípadne zvolí hodnotenie.
3. Systém validuje obsah komentára.
4. Systém uloží komentár a aktualizuje súhrn hodnotení.
5. Systém zobrazí nový komentár v zozname komentárov.

**Alternatívy**

3a. Používateľ odošle prázdny komentár - systém zobrazí validačnú chybu.

---

### UC05: Vytvorenie profilového príspevku

**Účel**

Umožniť organizátorovi alebo artistovi publikovať novinku na verejnom profile.

**Používateľ**

Organizátor alebo artist

**Vstupné podmienky**

Používateľ je prihlásený a má rolu ORGANIZER, ARTIST alebo ADMIN.

**Výstup**

Na verejnom profile používateľa pribudne nový príspevok s textom a voliteľným médiom.

**Postup**

1. Používateľ otvorí svoj verejný profil.
2. Používateľ napíše obsah príspevku.
3. Používateľ voliteľne priloží obrázok alebo video.
4. Systém validuje oprávnenie, text, veľkosť súboru a typ média.
5. Systém uloží príspevok a médium.
6. Systém zobrazí príspevok na profile.

**Alternatívy**

4a. Používateľ nemá povolenú rolu - systém vráti chybu oprávnenia.

4b. Médium má nepovolený typ alebo veľkosť - systém odmietne upload a zobrazí chybu.

---

### UC06: Sledovanie verejného profilu

**Účel**

Umožniť používateľovi sledovať organizátora alebo artista a neskôr si zobraziť sledované profily.

**Používateľ**

Používateľ

**Vstupné podmienky**

Používateľ je prihlásený a otvoril verejný profil iného používateľa.

**Výstup**

Profil je pridaný medzi sledované profily a vlastník profilu môže dostať notifikáciu o novom followerovi.

**Postup**

1. Používateľ otvorí verejný profil organizátora alebo artista.
2. Používateľ klikne na tlačidlo "Follow".
3. Systém skontroluje, že používateľ nesleduje sám seba.
4. Systém uloží vzťah sledovania.
5. Systém aktualizuje počet followerov.

---

### UC07: Publikovanie konceptu eventu

**Účel**

Umožniť organizátorovi pripraviť event ako koncept a publikovať ho až po dokončení údajov.

**Používateľ**

Organizátor

**Vstupné podmienky**

Používateľ je prihlásený, má oprávnenie organizátora a vlastní daný koncept eventu.

**Výstup**

Event zmení stav z DRAFT na PUBLISHED a začne sa zobrazovať verejne.

**Postup**

1. Organizátor otvorí zoznam svojich konceptov.
2. Organizátor vyberie event pripravený na publikovanie.
3. Systém overí vlastníctvo eventu a validitu údajov.
4. Organizátor potvrdí publikovanie.
5. Systém zmení stav eventu na publikovaný.

---
### Ďalšie prípady použitia (zoznam):

* **UC13:** Odhlásenie používateľa
* **UC14:** Zmena hesla
* **UC15:** Obnovenie zabudnutého hesla
* **UC16:** Pridanie fotografie k eventu
* **UC17:** Odstránenie fotografie z eventu
* **UC18:** Pridanie vystupujúceho k eventu
* **UC19:** Odstránenie vystupujúceho z eventu
* **UC20:** Automatické archivovanie uplynulých eventov
* **UC21:** Zobrazenie archivovaných eventov
* **UC22:** Vyhľadávanie podľa vystupujúceho
* **UC23:** Filtrovanie podľa viacerých kritérií naraz
* **UC24:** Zoradenie výsledkov vyhľadávania (podľa dátumu, popularity)
* **UC25:** Zobrazenie eventov na mape (podľa lokácie)
* **UC26:** Zdieľanie eventu na sociálnych sieťach
* **UC27:** Export eventu do kalendára (iCal)
* **UC28:** Nahlásenie nevhodného eventu
* **UC29:** Zmena stavu organizátora (aktivácia/deaktivácia)
* **UC30:** Zobrazenie verejného profilu organizátora

---

## Aktuálna technická architektúra

### Backend

Backend je rozdelený na Maven moduly podľa hexagonálnej architektúry:

* **domain** - doménové entity, porty, business pravidlá a use-case služby bez závislosti na Spring frameworku.
* **outbound-repository-jpa** - JPA adaptéry, Spring Data repository implementácie, ORM XML mapovanie, Liquibase migrácie a filesystem media storage.
* **inbound-controller-rest** - REST kontrolery, bezpečnostná konfigurácia, mapovanie DTO objektov a jednotné spracovanie chýb.
* **api-spec** - OpenAPI špecifikácia a generované API/DTO kontrakty.
* **springboot** - spustenie aplikácie, dependency wiring, transakčné wrappery a infraštruktúrna konfigurácia.

Dôležité architektonické rozhodnutia:

* Doména neobsahuje Spring anotácie a komunikuje cez porty.
* Oprávnenia sú kontrolované vo viacerých vrstvách: Spring Security rieši autentifikáciu a hrubé pravidlá, doménové služby kontrolujú vlastníctvo a konkrétne business oprávnenia.
* Event funkcionalita je rozdelená na samostatné fasády pre event lifecycle, attendance, media a discovery.
* Upload médií je validovaný na backende podľa typu a veľkosti súboru a storage vrstva chráni pred path traversal útokmi.
* Databázové zmeny sú riadené cez Liquibase a Hibernate používa `ddl-auto: validate`.

### Frontend

Frontend je Angular aplikácia so standalone komponentmi a lazy-loaded feature routami:

* **core** - autentifikácia, HTTP integrácia, layout, notifikácie a zdieľané služby.
* **modules/events** - event listing, detail, tvorba, editácia, kalendár a media API.
* **modules/profile** - používateľské profily, follow systém a profilové príspevky.
* **modules/discover** - vyhľadávanie a objavovanie eventov.
* **modules/home/about** - prezentačné stránky aplikácie.

Frontend používa reactive forms, signals, typed API služby, centralizované mapovanie API chýb a OAuth2/OIDC integráciu s Keycloakom.

### Bezpečnosť a deployment

Projekt používa Keycloak ako identity provider, JWT validáciu na backende, role USER/ORGANIZER/ARTIST/ADMIN, externalizované CORS origins, obmedzený actuator exposure a Kubernetes deployment cez ingress-nginx, cert-manager a Helm values podľa referenčného projektu.

# DevOps & Cloud Deployment

**Verziona:** 1.0  
**Posledná aktualizácia:** 2026-05-06  
**Status:** ✅ Hotovo — Pripravené na aplikovanie

## 📚 DevOps Dokumentácia

Kompletný DevOps setup pre nasadenie EventfindR na Azure AKS. Všetky súbory sú pripravené a personalizovateľné.

### Dokumentácia (ČÍTAJ V TOMTO PORADÍ):

1. **[QUICKSTART.md](./QUICKSTART.md)** — ⚡ Rýchly start (5-10 minút)
2. **[DEVOPS-SETUP.md](./DEVOPS-SETUP.md)** — 📖 Kompletný step-by-step návod (DETAILY!)
3. **[DEVOPS-CHECKLIST.md](./DEVOPS-CHECKLIST.md)** — ✅ Co som vykonal + checklist
4. **[kubernetes/README.md](./kubernetes/README.md)** — 🔧 Kubernetes technical details

### Infrastructure (Automaticky vytvorené):

```
kubernetes/
├── workload/                     # K8s manifesty
│   ├── 01-namespace.yaml         # Namespaces: app, infra, ingress-nginx, cert-manager
│   ├── 02-secrets.yaml           # Secrets pre DB a Keycloak
│   ├── 03-app-backend/           # Backend deployment
│   ├── 04-app-frontend/          # Frontend deployment
│   └── 05-ingress/               # Ingress, TLS, routing
│
└── helm/helm-values/
    ├── ingress-nginx/            # Azure Load Balancer config
    ├── cert-manager/             # Let's Encrypt ACME
    ├── keycloak/                 # OAuth2/OIDC setup
    └── gitlab-runner/            # CI/CD automation
```

### CI/CD Pipelines (Ready-to-Use):

- `fsa-eventfindr-backend/.gitlab-ci.yml` — Backend build pipeline
- `fsa-eventfindr-frontend/.gitlab-ci.yml` — Frontend build pipeline
- `fsa-eventfindr-frontend/Dockerfile` — Frontend container image

## ⚡ Quick Start (30 sec Cliff Notes)

```powershell
# 1. Prihlásenie
az login
az aks get-credentials --resource-group rg-fsa-<prefix> --name aks-fsa-<prefix> --admin

# 2. Personalizuj súbory (Find & Replace <your-prefix>, <IP>, db_url...)
# ⚠️ MUSÍ! Inak niČ nebude fungovať!

# 3. Apply infraštruktúru
kubectl apply -f kubernetes/workload/

# 4. Nainštaluj Helm komponenty (ingress-nginx, cert-manager, keycloak, runner)
# Príkazy sú v DEVOPS-SETUP.md

# 5. Bubuild a push Docker images
docker build -t acrfsa<prefix>.azurecr.io/fsa-eventfindr-backend:latest .

# 6. Aplikuj workload a ingress
kubectl apply -f kubernetes/workload/05-ingress/

# 7. Visit: https://app.<IP>.nip.io
```

**Detaily v DEVOPS-SETUP.md alebo QUICKSTART.md!**

## 🎯 Čo si dostal

✅ **Kubernetes manifesty** — Production-ready  
✅ **Helm values** — Pre ingress-nginx, cert-manager, Keycloak, GitLab Runner  
✅ **Docker images** — Backend (existuje) + Frontend (nový)  
✅ **CI/CD pipelines** — GitLab CI pre automated build  
✅ **SSL/TLS** — Let's Encrypt certificate automation  
✅ **OAuth2/OIDC** — Keycloak realm ready-to-go  
✅ **nip.io domain** — Bez DNS registrácie  
✅ **Detailná dokumentácia** — Step-by-step tutorials  

## 🚀 Technológie

- **Kubernetes (AKS)** — Container orchestration na Azure
- **Helm** — Package manager pre K8s (ingress-nginx, cert-manager, Keycloak, GitLab Runner)
- **cert-manager** — Automatic SSL/TLS (Let's Encrypt)
- **Keycloak** — OAuth2/OIDC authentication
- **ingress-nginx** — Load Balancer + routing
- **GitLab CI/CD** — Automated docker build & K8s deploy
- **Docker** — Multi-stage builds (Backend + Frontend)
- **nip.io** — Wildcard DNS bez registrácie

## ☑️ Predpoklady

Nainštalované:
- Azure CLI (`az`)
- kubectl
- Helm
- Docker
- Git

## ⏱️ Typický Čas Nasadenia

| Fáza | Čas |
|---|---|
| Príprava + personalizácia | 5 minút |
| Kubectl + Helm setup | 25 minút |
| Docker builds | 10 minút |
| Workload deployment | 5 minút |
| **TOTAL** | **~45 minút** |

*(Vrátane čakania na Azure services)*

## 📖 Ako Začať

1. **Prečítaj si QUICKSTART.md** — Overview
2. **Personalizuj súbory** — Find & Replace (viď DEVOPS-CHECKLIST.md)
3. **Postupuj DEVOPS-SETUP.md** — Step-by-step
4. **Monitoruj** — kubectl commands z kubernetes/README.md

## 🆘 Niečo Nefunguje?

→ **DEVOPS-SETUP.md → Troubleshooting**

---

