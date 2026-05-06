# 🚀 ZAČNITE TU — GET STARTED

**Máš kompletný DevOps setup. Teraz len aplikuj.**

---

## 📖 3 Súbory, Ktoré Máš

1. **DEVOPS-INDEX.md** ← `START HERE` (tento dokument)
2. **QUICKSTART.md** ← Rýchly 5-minútový prehľad
3. **DEVOPS-SETUP.md** ← Kompletný detailný návod (700+ liniek)

---

## 🎯 3 Fázy Aplikovania

### FÁZA 1: Prečítaj si 5 Minút

```
Otvor: QUICKSTART.md ← TERAZ!
(Pojďme, je to len 5 minút)
```

---

### FÁZA 2: Personalizuj 5 Minút

**Find & Replace v editore:**

| Nájdi | Zameniaj na |
|------|------------|
| `<your-prefix>` | `tvoj-prefix` (bez pomlčiek!) |
| `your-email@example.com` | Tvoj email |
| `<IP>` | IP z ingress-nginx (neskôr) |
| `<REGION>` | Tvoj Azure region |

**Súbory na personalizáciu:**
- `kubernetes/workload/02-secrets.yaml`
- `kubernetes/helm/helm-values/ingress-nginx/override.yaml`
- `kubernetes/helm/helm-values/keycloak/override.yaml`
- `kubernetes/workload/03-app-backend/deployment.yaml`
- `kubernetes/workload/04-app-frontend/deployment.yaml`
- `kubernetes/helm/helm-values/cert-manager/letsencrypt-cluster-issuer.yaml`

*Podrobne viď: DEVOPS-CHECKLIST.md → PERSONALIZÁCIA*

---

### FÁZA 3: Aplicuj 30-45 Minút

```
Prejdi: DEVOPS-SETUP.md
(Postupuj podľa krokov — všetko je tam)
```

---

## ⚡ TL;DR (Veľmi Stručne)

```powershell
# 1. Prihlásenie
az login
az aks get-credentials --resource-group rg-fsa-<prefix> --name aks-fsa-<prefix> --admin

# 2. Aplikuj K8s
kubectl apply -f kubernetes/workload/01-namespace.yaml
kubectl apply -f kubernetes/workload/02-secrets.yaml

# 3. Helm instals (príkazy v DEVOPS-SETUP.md)
helm upgrade --install ingress-nginx ...
helm upgrade --install cert-manager ...
helm upgrade --install keycloak ...

# 4. Docker images
docker build -t acrfsa<prefix>.azurecr.io/fsa-eventfindr-backend:latest .
docker build -t acrfsa<prefix>.azurecr.io/fsa-eventfindr-frontend:latest .

# 5. Workload
kubectl apply -f kubernetes/workload/03-app-backend/
kubectl apply -f kubernetes/workload/04-app-frontend/
kubectl apply -f kubernetes/workload/05-ingress/

# 6. Overenie
curl https://app.<IP>.nip.io
```

**VŠETKY PRÍKAZY (S DETAILMI) sú v DEVOPS-SETUP.md**

---

## 📋 Mapa Dokumentácie

```
START (TŤA) ↓
  │
  ├→ QUICKSTART.md (5-min cliff notes)
  │
  ├→ DEVOPS-SETUP.md (kompletný návod) ←-- MAIN
  │   ├─ Prihlásenie
  │   ├─ K8s Setup (7 krokov)
  │   ├─ Docker builds
  │   ├─ GitLab setup
  │   ├─ Nasadenie
  │   ├─ nip.io domain (ÚLOHA 1!)
  │   └─ Troubleshooting
  │
  ├→ DEVOPS-CHECKLIST.md (Personalizácie + checklist)
  │   ├─ Čo som vykonal
  │   ├─ Kľúčové zmeny
  │   └─ Finálny checklist
  │
  ├→ kubernetes/README.md (Technical)
  │   ├─ Štruktúra
  │   ├─ Príkazy
  │   └─ Troubleshooting
  │
  └→ DEVOPS-INDEX.md (Tohto súboru)
      └─ This Overview
```

---

## ✅ Pred Zatvorením Tohoto Súboru

- [ ] Máš nainštalované: `az`, `kubectl`, `helm`, `docker`, `git`
- [ ] Máš prístup na Azure a GitLab
- [ ] Si si prečítala QUICKSTART.md
- [ ] Si si prečítala minimálne intro DEVOPS-SETUP.md

---

## 🆘 Máš Otázku?

| Situácia | Čo Robiť |
|----------|----------|
| "Neviem o čom je reč" | DEVOPS-SETUP.md (Intro) |
| "Rýchlo, potrebujem len príkazy" | QUICKSTART.md |
| "Niečo nefunguje" | DEVOPS-SETUP.md (Troubleshooting) |
| "Ako personalizovať sutbory?" | DEVOPS-CHECKLIST.md (PERSONALIZÁCIA) |
| "Čo je Kubernetes/Helm/...?" | DEVOPS-SETUP.md (Concepts) |

---

## 🎬 Ďalší Krok

👉 **Otvor QUICKSTART.md**

Alebo ak chceš detail: **DEVOPS-SETUP.md** → Krok 1

---

**Trvá to ~45 minút. Dá sa to.** 🚀

---

*Vytvorené: 2026-05-06*  
*Verzia: 1.0*  
*Autom: GitHub Copilot*

