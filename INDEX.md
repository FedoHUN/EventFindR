# 🎯 EventfindR — DevOps Cloud Deployment Complete Setup

**Kompletný production-ready DevOps setup pre nasadenie EventfindR na Azure AKS.**

---

## 🚀 START HERE — 3 MOŽNOSTI

### ✨ Pre Vydesení Začiatočníci
```
👉 Otvor: [GET-STARTED.md](./GET-STARTED.md)
   (Jednoduchá orientácia, 2 minúty)
```

### ⚡ Pre Tých, Ktorí Chcú Rýchlo
```
👉 Otvor: [QUICKSTART.md](./QUICKSTART.md)
   (5-minútový prehľad všetkého)
```

### 📖 Pre Tých, Ktorí Chcú Podrobnosti
```
👉 Otvor: [DEVOPS-SETUP.md](./DEVOPS-SETUP.md)
   (Kompletný step-by-step návod — 700+ liniek!)
```

### 📋 Pre Tých, Čo Vedia Čo Robia
```
👉 Otvor: [TLDR.md](./TLDR.md)
   (Iba príkazy, bez vysvetľovania)
```

---

## 📚 Všetky Dokumenty

| Dokument | Typ | Čas | Účel |
|----------|-----|-----|------|
| **[GET-STARTED.md](./GET-STARTED.md)** | Entry Point | 2 min | Orientácia |
| **[QUICKSTART.md](./QUICKSTART.md)** | Quick Ref | 5 min | Rýchly prehľad |
| **[DEVOPS-SETUP.md](./DEVOPS-SETUP.md)** | Tutorial | 45 min | Kompletný návod |
| **[DEVOPS-CHECKLIST.md](./DEVOPS-CHECKLIST.md)** | Checklist | 5 min | Čo som vykonal + personalizácie |
| **[DEVOPS-INDEX.md](./DEVOPS-INDEX.md)** | Overview | 10 min | Detailed obsah všetkého |
| **[TLDR.md](./TLDR.md)** | Commands | 1 min | Iba príkazy |
| **[READY.md](./READY.md)** | Summary | 5 min | Confirmation + next steps |
| **[kubernetes/README.md](./kubernetes/README.md)** | Technical | 15 min | K8s technical details |

---

## 🎯 Quick Navigation

```
Prvýkrát tu?
    ↓
Otvor: GET-STARTED.md
    ↓
Chceš rýchlo?
    ├─→ QUICKSTART.md (5 min setup)
    ├─→ TLDR.md (iba príkazy)
    └─→ READY.md (co dalej)
    ↓
Chceš detaily?
    └─→ DEVOPS-SETUP.md (ALL EVERYTHING!)
    └─→ kubernetes/README.md (K8s ref)
    └─→ DEVOPS-CHECKLIST.md (Personalizácie)
```

---

## 📦 Čo Máš

✅ **Dokumentácia** — 8 kompletných MD súborov  
✅ **Kubernetes** — 8 production-ready manifestov  
✅ **Helm Values** — 8 configurácií (ingress, cert-manager, Keycloak, runner)  
✅ **Docker & CI/CD** — Frontend Dockerfile + 2x .gitlab-ci.yml  
✅ **Tutoriály** — 3 úrovne detailov (quick, detailed, technical)  
✅ **Troubleshooting** — 20+ common problémov a riešení  

---

## 🚀 3 Teps na Start

### 1. Prečítaj (5 min)
```
→ [GET-STARTED.md](./GET-STARTED.md)
  alebo
→ [QUICKSTART.md](./QUICKSTART.md)
```

### 2. Personalizuj (5 min)
```
Find & Replace v editore:
  <your-prefix> → janko
  <REGION> → eastus
  <IP> → 52.138.207.76
```

### 3. Aplikuj (30-40 min)
```
Postupuj [DEVOPS-SETUP.md](./DEVOPS-SETUP.md)
alebo skopíruj príkazy z [TLDR.md](./TLDR.md)
```

---

## ⏱️ Typický Čas

| Fáza | Čas |
|------|-----|
| Príprava + čítanie | 10 minút |
| Personalizácia | 5 minút |
| Kubernetes setup | 10 minút |
| Helm instalácie | 30 minút |
| Docker builds | 10 minút |
| Workload deploy | 5 minút |
| **TOTAL** | **~60 minút** |

---

## 🎯 Čo Si Možeš Dosiahnuť

Po~60 minútach budú:

✅ **Frontend** dostupný na: `https://app.<IP>.nip.io`  
✅ **Backend API** fungujúci na: `/events`, `/users`, `/actuator`  
✅ **Keycloak** dostupný na: `https://keycloak.<IP>.nip.io`  
✅ **SSL/TLS** automaticky nastavené (Let's Encrypt)  
✅ **nip.io doména** bez DNS registrácie  
✅ **ÚLOHA 1 z TASKS.md** — hotová! ✅  

---

## 🔐 Bezpečne

- ✅ Kubernetes secrets (nie hardkoded)
- ✅ RBAC permissions
- ✅ TLS encryption (Let's Encrypt)
- ✅ Namespace isolation
- ✅ Resource limits

---

## 🆘 Niečo Nicht Funguje?

```
→ DEVOPS-SETUP.md → Troubleshooting
  (20+ common problémov a riešení)
```

---

## 📊 Štatistika

- **Dokumentácie**: 8 súborov, ~2500 riadkov
- **Kubernetes**: 8 manifestov, ~550 riadkov
- **Helm values**: 8 súborov, ~400 riadkov
- **Docker + CI/CD**: 3 súbory, ~100 riadkov
- **TOTAL**: 27 súborov, ~3500 riadkov

---

## ✨ Special Features

🎯 **nip.io Domain** — Bez DNS registrácie  
🔐 **Let's Encrypt** — Automatické SSL/TLS  
🔄 **CI/CD Ready** — GitLab pipelines (build + deploy)  
📊 **Monitoring Ready** — Prometheus metrics builtin  
🛡️ **Secure** — K8s secrets, RBAC, namespaces  
🚀 **Production Ready** — Nie toys, real setup  

---

## 📖 Dokumentácia Structure

```
EventfindR/
├── GET-STARTED.md                      ← START HERE!
├── QUICKSTART.md                       (5-min cliff notes)
├── DEVOPS-SETUP.md                     (kompletný návod)
├── DEVOPS-CHECKLIST.md                 (checklist + personalizácie)
├── DEVOPS-INDEX.md                     (detailed overview)
├── READY.md                            (summary + next)
├── TLDR.md                             (iba príkazy)
├── DEVOPS-MANIFEST.md                  (manifest súborov)
│
├── kubernetes/
│   ├── README.md                       (K8s technical)
│   ├── workload/
│   │   ├── 01-namespace.yaml           ✅ NOVÝ
│   │   ├── 02-secrets.yaml             ✅ NOVÝ (⚠️ personalizácia!)
│   │   ├── 03-app-backend/             ✅ NOVÝ
│   │   ├── 04-app-frontend/            ✅ NOVÝ
│   │   └── 05-ingress/                 ✅ NOVÝ
│   └── helm/helm-values/
│       ├── ingress-nginx/              ✅ NOVÝ
│       ├── cert-manager/               ✅ NOVÝ
│       ├── keycloak/                   ✅ NOVÝ
│       └── gitlab-runner/              ✅ NOVÝ
│
├── fsa-eventfindr-backend/
│   ├── .gitlab-ci.yml                  ✅ NOVÝ
│   └── Dockerfile                      ✅ Existuje
│
└── fsa-eventfindr-frontend/
    ├── .gitlab-ci.yml                  ✅ NOVÝ
    └── Dockerfile                      ✅ NOVÝ
```

---

## 🎓 Zápas

Po tomto setupu budeš vedieť:

✅ Kubernetes (AKS, Deployments, Services, Ingress, Namespaces)  
✅ Helm (package management, values overrides)  
✅ cert-manager (ACME, Let's Encrypt automation)  
✅ Keycloak (OAuth2/OIDC setup)  
✅ ingress-nginx (Load Balancing, TLS)  
✅ Docker (Multi-stage builds)  
✅ GitLab CI/CD (Automated pipelines)  
✅ Azure (AKS, ACR, PostgreSQL)  

---

## 🚀 ZAČNI TERAZ!

### OPTION 1: Ja Sum Nový (Rád Si Prečítam)
```
👉 [GET-STARTED.md](./GET-STARTED.md)
```

### OPTION 2: Som Skororý (Chcem Rýchló)
```
👉 [QUICKSTART.md](./QUICKSTART.md)
```

### OPTION 3: Som Expert (Len Príkazy!)
```
👉 [TLDR.md](./TLDR.md)
```

### OPTION 4: Chcem Všetko Vedieť
```
👉 [DEVOPS-SETUP.md](./DEVOPS-SETUP.md)
```

---

## 📞 Contact

- **Questions?** → Check dokumenty
- **Troubleshooting?** → DEVOPS-SETUP.md → Troubleshooting
- **Just give me commands!** → TLDR.md
- **What's next?** → READY.md

---

## ✅ Status

**⚪ Prepared** → Dokumenty sú hotové  
**⚪ Personalized** → Personalizuj si súbory (Find & Replace)  
**⚪ Applied** → Aplikuj kubectl a helm príkazy  
**⚪ Verified** → Overenie s curl/prehliadačom  
**⚪ Done!** → Aplikácia beží! 🎉  

---

**Verzia**: 1.0  
**Dátum**: 2026-05-06  
**Status**: ✅ **KOMPLETNE HOTOVO A PRIPRAVENÉ!**

---

**Šťastný deployment! 🚀**

