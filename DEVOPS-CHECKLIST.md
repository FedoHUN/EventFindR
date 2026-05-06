# EventfindR DevOps Complete Checklist & Summary

**Vytvorené:** 2026-05-06  
**Status:** ✅ Hotovo — Pripravené na aplikovanie

---

## 📊 Čo Som Pre Teba Vytvoril

### 1. ✅ Dokumentácia

| Súbor | Popis | Čítaj |
|---|---|---|
| **DEVOPS-SETUP.md** | Kompletný step-by-step návod (VÄČŠÍ DETAIL) | Keď nerozumieš niečo |
| **QUICKSTART.md** | Rýchly start bez detailov | Keď chceš rýchlo začať |
| **kubernetes/README.md** | Technical details infraštruktúry | Pre DevOps engineering |
| **Tento súbor** | Checklist + Zhrnutie | Orientačný bod |

---

### 2. ✅ Docker & CI/CD

| Súbor | Účel |
|---|---|
| `fsa-eventfindr-backend/.gitlab-ci.yml` | Build backend pipeline |
| `fsa-eventfindr-frontend/.gitlab-ci.yml` | Build frontend pipeline |
| `fsa-eventfindr-frontend/Dockerfile` | Frontend Docker image |

**Backend Dockerfile** — Už existuje! ✅

---

### 3. ✅ Kubernetes Manifesty

| Cesta | Typ | Popis |
|---|---|---|
| `kubernetes/workload/01-namespace.yaml` | Namespaces | app, infra, ingress-nginx, cert-manager, monitoring |
| `kubernetes/workload/02-secrets.yaml` | Secrets | DB + Keycloak credentials |
| `kubernetes/workload/03-app-backend/deployment.yaml` | Deployment | Backend aplikácia |
| `kubernetes/workload/03-app-backend/service.yaml` | Service | Backend service |
| `kubernetes/workload/04-app-frontend/deployment.yaml` | Deployment | Frontend aplikácia |
| `kubernetes/workload/04-app-frontend/service.yaml` | Service | Frontend service |
| `kubernetes/workload/05-ingress/app-ingress-nip.yaml` | Ingress | App + Keycloak routing (nip.io) |
| `kubernetes/workload/05-ingress/keycloak-ingress.yaml` | Ingress | Keycloak specific |

---

### 4. ✅ Helm Values

| Cesta | Popis |
|---|---|
| `kubernetes/helm/helm-values/ingress-nginx/override.yaml` | Azure Load Balancer config |
| `kubernetes/helm/helm-values/cert-manager/override.yaml` | Certificate manager config |
| `kubernetes/helm/helm-values/cert-manager/letsencrypt-cluster-issuer.yaml` | Let's Encrypt ACME issuer |
| `kubernetes/helm/helm-values/keycloak/override.yaml` | Keycloak deployment config |
| `kubernetes/helm/helm-values/keycloak/keycloak-java-config.yaml` | Azure PostgreSQL fix |
| `kubernetes/helm/helm-values/keycloak/realm-fsa-configmap.yaml` | Keycloak realm config |
| `kubernetes/helm/helm-values/gitlab-runner/override.yaml` | GitLab Runner config |

---

## 🎯 Čo Je AK MUSÍ UROBIŤ

### Pred Aplikovaním Manifestov:

```powershell
# 1. Prihlásenie
az login
az account set --subscription "<SUBSCRIPTION_ID>"
az aks get-credentials --resource-group rg-fsa-<prefix> --name aks-fsa-<prefix> --admin

# 2. PERSONALIZÁCIA SÚBOROV!
```

---

## 🔧 PERSONALIZÁCIA — SÚ TO KRITICKÉ ZMENY!

**AK NEUROBÍŠ TIETO ZMENY, NIČ NEBUDE FUNGOVAŤ!**

### A) Database URL (base64 kódovať!)

**Súbor:** `kubernetes/workload/02-secrets.yaml`

```powershell
# Zisti svoju DB URL:
# Príklad: psql-fsa-janko.postgres.database.azure.com

# Zakóduj do base64:
[System.Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("psql-fsa-janko.postgres.database.azure.com"))
# Výsledok (príklad): cHNxbC1mc2EtamFua28ucG9zdGdyZXMuZGF0YWJhc2UuYXp1cmUuY29t

# Potom v 02-secrets.yaml zameniť:
# db_url: cHNxbC1mc2EteWFqb2gucG9zdGdyZXMuZGF0YWJhc2UuYXp1cmUuY29t
# na:
# db_url: <TVOJ_BASE64>
```

### B) Azure Resource Group Names

**Súbor:** `kubernetes/helm/helm-values/ingress-nginx/override.yaml`

```powershell
# Zisti Node Resource Group:
az aks show --resource-group rg-fsa-<prefix> --name aks-fsa-<prefix> --query nodeResourceGroup -o tsv
# Výsledok (príklad): MC_rg-fsa-janko_eastus

# Zisti region:
az aks show --resource-group rg-fsa-<prefix> --name aks-fsa-<prefix> --query location -o tsv
# Výsledok: eastus (alebo iný)

# V override.yaml zameniť:
# azure-load-balancer-resource-group: MC_rg-fsa-<your-prefix>_<region>
# azure-pip-name: pip-fsa-<your-prefix>
```

### C) Keycloak Database Hostname

**Súbor:** `kubernetes/helm/helm-values/keycloak/override.yaml`

```yaml
database:
  hostname: psql-fsa-<your-prefix>.postgres.database.azure.com
```

### D) ACR Registry Names (2 súbory!)

**Súbory:**
- `kubernetes/workload/03-app-backend/deployment.yaml`
- `kubernetes/workload/04-app-frontend/deployment.yaml`

```yaml
# Zameniť:
image: acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-backend:latest
# na:
image: acrfsajanko.azurecr.io/fsa-eventfindr-backend:latest
# (bez pomlčiek v "acrfsajanko"!)
```

### E) Email pre Let's Encrypt

**Súbor:** `kubernetes/helm/helm-values/cert-manager/letsencrypt-cluster-issuer.yaml`

```yaml
email: your-email@example.com
```

### F) Frontend Dockerfile Output Path

**Súbor:** `fsa-eventfindr-frontend/Dockerfile`

```dockerfile
# Zisti správnu output cestu z Angular build:
COPY --from=build /app/dist/fsa-eventfindr-frontend/browser /usr/share/nginx/html
```

(Mal by byť OK, ale skontroluj v `angular.json` čo je `"outputPath"`)

---

## 📋 KDY APLIKOVAŤ SÚBORY

### Fáza 1: Základná Infraštruktúra

```powershell
kubectl apply -f kubernetes/workload/01-namespace.yaml
kubectl apply -f kubernetes/workload/02-secrets.yaml       # ⚠️ MUSÍ BYŤ PERSONALIZOVANÝ!
```

### Fáza 2: Helm Instalácie (v tomto poradí!)

```powershell
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx && helm repo update
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx -n ingress-nginx --version 4.15.1 -f kubernetes/helm/helm-values/ingress-nginx/override.yaml

helm repo add jetstack https://charts.jetstack.io && helm repo update
helm upgrade --install cert-manager jetstack/cert-manager -n cert-manager --version 1.20.1 -f kubernetes/helm/helm-values/cert-manager/override.yaml
kubectl apply -f kubernetes/helm/helm-values/cert-manager/letsencrypt-cluster-issuer.yaml

helm repo add codecentric https://codecentric.github.io/helm-charts && helm repo update
kubectl apply -f kubernetes/helm/helm-values/keycloak/keycloak-java-config.yaml
kubectl apply -f kubernetes/helm/helm-values/keycloak/realm-fsa-configmap.yaml
helm upgrade --install keycloak -n app codecentric/keycloakx --version 7.1.9 -f kubernetes/helm/helm-values/keycloak/override.yaml

# Runner (keď máš token)
helm repo add gitlab https://charts.gitlab.io && helm repo update
kubectl create secret generic gitlab-runner-secret --from-literal=runner-token=<TOKEN> -n infra
helm upgrade --install gitlab-runner -n infra gitlab/gitlab-runner --version 0.87.0 -f kubernetes/helm/helm-values/gitlab-runner/override.yaml
```

### Fáza 3: Build Docker Images

```powershell
az acr login --name acrfsa<your-prefix>

# Backend
cd fsa-eventfindr-backend
docker build -t acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-backend:latest .
docker push acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-backend:latest

# Frontend
cd fsa-eventfindr-frontend
docker build -t acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-frontend:latest .
docker push acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-frontend:latest
```

### Fáza 4: Aplikuj Workload

```powershell
kubectl apply -f kubernetes/workload/03-app-backend/
kubectl apply -f kubernetes/workload/04-app-frontend/
# ⚠️ TOTO UROB AŽ KEĎKEĎ máš vygenerovanú IP z ingress-nginx!

# Zameniť <IP> v app-ingress-nip.yaml!
kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}'

kubectl apply -f kubernetes/workload/05-ingress/app-ingress-nip.yaml
```

---

## ✅ Finálny Checklist Pred Deploy

- [ ] `az aks get-credentials` — Funguje prihlásenie do K8s
- [ ] **02-secrets.yaml** — Personalizovaný s DB URL a hesly
- [ ] **ingress-nginx/override.yaml** — Personalizovaný s Azure RG + PIP
- [ ] **keycloak/override.yaml** — Personalizovaný s DB hostname
- [ ] **keycloak/letsencrypt-cluster-issuer.yaml** — Personalizovaný s email
- [ ] **03-app-backend/deployment.yaml** — ACR URL bez typo
- [ ] **04-app-frontend/deployment.yaml** — ACR URL bez typo
- [ ] **Dockerfile** — Frontend Dockerfile existuje a je validný
- [ ] **Docker images** — Backend a Frontend buildy pushunul do ACR
- [ ] **GitLab** — Group vytvorená, runner token pripravený (optional)
- [ ] **nip.io ingress** — <IP> zameniť za skutočnú IP

---

## 🎬 Spustenie Setup

**Rekomendovaný postup:**

1. **Prečítaj si QUICKSTART.md** — Rýchly prehľad
2. **Personalizuj všetky súbory** — Podľa sekcie "PERSONALIZÁCIA"
3. **Postupuj podľa DEVOPS-SETUP.md** — Krok po kroku
4. **Monitoruj s príkazmi z kubernetes/README.md** — Overenie

---

## 🆘 Keď Niečo Zlyhá

| Problém | Zistenie | Riešenie |
|---|---|---|
| Nemôžem sa pripojiť na K8s | `kubectl get nodes` → error | Skontroluj `az aks get-credentials` |
| ImagePullBackOff | `kubectl describe pod...` | Docker image neexistuje v ACR |
| Keycloak sa nespúšťa | `kubectl logs -f deployment/fsa-keycloak` | Skontroluj DB connectivitu |
| Ingress nemá IP | `kubectl get svc -n ingress-nginx` | Čakaj 5 minút, je to Azure |
| Certifikát sa nevyvracuje | `kubectl describe certificate app-tls-nip` | Skontroluj Let's Encrypt logy |
| Runner je offline | `kubectl logs -n infra deployment/gitlab-runner` | Token, connectivity alebo DNS |

**Všechny detaily v**: DEVOPS-SETUP.md → Troubleshooting sekcia

---

## 📚 Súbory v Projekte

```
EventfindR/
├── DEVOPS-SETUP.md              ← KOMPLETNÝ NÁVOD (ČÍTAJ!)
├── QUICKSTART.md                ← Rýchly start
├── DEVOPS-CHECKLIST.md          ← TENTO SÚBOR
├── README.md                    ← Project overview
│
├── kubernetes/                  ← Kubernetes infraštruktúra
│   ├── README.md
│   ├── workload/
│   │   ├── 01-namespace.yaml
│   │   ├── 02-secrets.yaml       ⚠️ PERSONALIZÁCIA!
│   │   ├── 03-app-backend/
│   │   ├── 04-app-frontend/
│   │   └── 05-ingress/
│   └── helm/helm-values/
│       ├── ingress-nginx/        ⚠️ PERSONALIZÁCIA!
│       ├── cert-manager/         ⚠️ PERSONALIZÁCIA!
│       ├── keycloak/             ⚠️ PERSONALIZÁCIA!
│       └── gitlab-runner/
│
├── fsa-eventfindr-backend/
│   ├── .gitlab-ci.yml           ← CI/CD pipeline
│   ├── Dockerfile               ✅ Existuje
│   └── ...
│
└── fsa-eventfindr-frontend/
    ├── .gitlab-ci.yml           ← CI/CD pipeline
    ├── Dockerfile               ✅ NOVONEW!
    └── ...
```

---

## ⏱️ Časový Rozpočet

| Fáza | Čas |
|---|---|
| Príprava + personalizácia | 5 minút |
| Kubernetes application | 5 minút |
| Helm instalácie | 20 minút (+ čakanie) |
| Docker builds | 10 minút |
| Workload application | 5 minút |
| **TOTAL** | **~45 minút** |

---

## 🎓 Čo SI sa Naučil

1. **Kubernetes** — Deployments, Services, Ingress, Namespaces
2. **Helm** — Package manager pre K8s
3. **cert-manager** — Automatic SSL/TLS (Let's Encrypt)
4. **Keycloak** — OAuth2/OIDC authentication
5. **ingress-nginx** — Routing a Load Balancing
6. **Docker** — Containerization (Multi-stage builds)
7. **GitLab CI/CD** — Automated build & deploy (optional)
8. **Azure** — AKS, ACR, PostgreSQL, Networks

---

## 🚀 Ďalšie Kroky (Po Úspešnom Deploy)

1. **TASKS z TASKS.md** — Implementuj úlohy zadané
   - [ ] **Úloha 1:** nip.io domain + TLS (v DEVOPS-SETUP.md)
   - [ ] **Úloha 2:** Gitlab CI/CD Deploy stage (deploymentý commented v .gitlab-ci.yml)
   - [ ] **Úloha 3:** Health checks (liveness/readiness probes)
   - [ ] **Úloha 4:** Monitoring (Prometheus, Loki, Grafana)

2. **Monitoring** — Pridaj health checks
   ```yaml
   livenessProbe:
     httpGet:
       path: /actuator/health
       port: 8080
   ```

3. **Backup** — Keycloak realm export, database backups

4. **Scaling** — Replicas, HPA (Horizontal Pod Autoscaling)

5. **Custom Domain** — Miesto nip.io, pridam custom domain

---

## 📞 Support

- **Detaily** → DEVOPS-SETUP.md
- **Technické** → kubernetes/README.md
- **Rýchle otázky** → QUICKSTART.md
- **Príkazy** → Priamo v súboroch (find & replace)

---

## ✨ Zhrnutie

Vytvoril som pre teba **kompletný DevOps setup** pre EventfindR:

✅ **Dokumentácia** — Kompletná, step-by-step (DEVOPS-SETUP.md)  
✅ **Kubernetes** — Ready manifesty (workload/)  
✅ **Helm** — Ready values (helm/helm-values/)  
✅ **CI/CD** — .gitlab-ci.yml pre oba projekty  
✅ **Dockerfile** — Frontend kontainer  
✅ **Keycloak** — Ready-to-go realm  
✅ **Ingress** — nginx s Let's Encrypt SSL  
✅ **Runner** — GitLab Runner setup  

**Čo Ty spravíš:**
1. Personalizuješ súbory (zameniť prefixes)
2. Aplikuješ manifesty (kubectl apply)
3. Builduješ images (docker build & push)
4. Overuješ v prehliadači (https://app.<IP>.nip.io)

**Keď niečo nefunguje** → DEVOPS-SETUP.md → Troubleshooting

---

**Status**: ✅ **Kompletne hotovo a pripravené na aplikovanie!**

**Verzia**: 1.0  
**Posledná zmena**: 2026-05-06  
**Autor**: GitHub Copilot (EventfindR DevOps Setup)

