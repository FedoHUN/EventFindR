# EventfindR - Analýza projektu

## Zadanie

Cieľom projektu je vytvoriť platformu, na ktorej môžu používatelia vyhľadávať a sledovať eventy, najmä hudobné, ale aj iné kultúrne či spoločenské podujatia. Organizátori môžu pridávať vlastné eventy s názvom, popisom, miestom konania, dátumom, cenou, fotografiami, odkazmi na kúpu lístkov a menami vystupujúcich. Používatelia si môžu eventy uložiť do svojho profilu buď ako „zúčastním sa" alebo „sledujem" a neskôr ich jednoducho nájsť na svojom profile cez prehľadné záložky. Systém bude podporovať vyhľadávanie a filtrovanie eventov podľa názvu, lokácie, vystupujúcich umelcov a dátumu.

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
