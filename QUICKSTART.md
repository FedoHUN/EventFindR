# EventfindR DevOps — QuickStart Guide

**V tomto súbore nájdeš super rýchly start bez detailov. Pre detaily, skontroluj `DEVOPS-SETUP.md`**

---

## 🚀 5 Minút Setup (Checklista)

### Fáza 1: Azure + Kubernetes (5 minút)

```powershell
# 1. Prihlás sa
az login
az account set --subscription "<SUBSCRIPTION_ID>"

# 2. Stiahni kubeconfig
az aks get-credentials --resource-group rg-fsa-<prefix> --name aks-fsa-<prefix> --admin

# 3. Overenie
kubectl get nodes
# ✅ Mal by si vidieť 2-3 uzly
```

---

### Fáza 2: Personalizuj Súbory (2 minúty)

**Súbory, ktoré MUSÍŠ upraviť:**

| Súbor | Čo zmeniť | Príklad |
|---|---|---|
| `kubernetes/workload/02-secrets.yaml` | `db_url` base64 | `psql-fsa-YOUR-PREFIX.postgres...` |
| `kubernetes/helm/helm-values/ingress-nginx/override.yaml` | RG name + PIP name | `rg-fsa-YOUR-PREFIX`, `pip-fsa-YOUR-PREFIX` |
| `kubernetes/helm/helm-values/keycloak/override.yaml` | `database.hostname` | `psql-fsa-YOUR-PREFIX...postgres.database...` |
| `kubernetes/workload/03-app-backend/deployment.yaml` | ACR image URL | `acrfsaYOURPREFIX.azurecr.io/...` |
| `kubernetes/workload/04-app-frontend/deployment.yaml` | ACR image URL | `acrfsaYOURPREFIX.azurecr.io/...` |

**Tip:** Use `Ctrl+H` (Find & Replace) v editore:
- Zameni `<your-prefix>` → `janko` (alebo tvoj prefix)
- Zameni `<IP>` → tvoja external IP (až neskôr, keď máš ingress-nginx running)

---

### Fáza 3: Aplikuj Kubernetes Manifesty (3 minúty)

```powershell
# Prejdi do zložky EventfindR
cd C:\Users\fedoh\Documents\EventfindR

# Aplikuj základnú infraštruktúru
kubectl apply -f kubernetes/workload/01-namespace.yaml
kubectl apply -f kubernetes/workload/02-secrets.yaml

# ✅ Overenie
kubectl get namespaces | grep app
kubectl get secrets -n app
```

---

### Fáza 4: Inštalácia Helm Komponentov (10 minút)

```powershell
# Pridaj Helm repozitáre
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo add jetstack https://charts.jetstack.io
helm repo add codecentric https://codecentric.github.io/helm-charts
helm repo update

# 1. ingress-nginx (LoadBalancer s Public IP)
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx `
  -n ingress-nginx `
  --version 4.15.1 `
  -f kubernetes/helm/helm-values/ingress-nginx/override.yaml

# ⏳ Čakaj ~3-5 minút
kubectl rollout status deployment/ingress-nginx-controller -n ingress-nginx

# ✅ Overenie — skopíruj si IP!
kubectl get svc -n ingress-nginx ingress-nginx-controller
# Váž EXTERNAL-IP!

# 2. cert-manager (SSL/TLS automatika)
helm upgrade --install cert-manager jetstack/cert-manager `
  -n cert-manager `
  --version 1.20.1 `
  -f kubernetes/helm/helm-values/cert-manager/override.yaml

kubectl apply -f kubernetes/helm/helm-values/cert-manager/letsencrypt-cluster-issuer.yaml

# ✅ Overenie
kubectl get clusterissuer letsencrypt-prod

# 3. Keycloak (SSO/OAuth2)
kubectl apply -f kubernetes/helm/helm-values/keycloak/keycloak-java-config.yaml
kubectl apply -f kubernetes/helm/helm-values/keycloak/realm-fsa-configmap.yaml

helm upgrade --install keycloak -n app codecentric/keycloakx `
  --version 7.1.9 `
  -f kubernetes/helm/helm-values/keycloak/override.yaml

# ⏳ Čakaj ~5-10 minút!
kubectl rollout status deployment/fsa-keycloak -n app

# ✅ Overenie
kubectl get pods -n app | grep keycloak
```

---

### Fáza 5: Nasadi Svoju Aplikáciu (2 minúty)

```powershell
# 1. Aplikuj backend a frontend deployments
kubectl apply -f kubernetes/workload/03-app-backend/
kubectl apply -f kubernetes/workload/04-app-frontend/

# 2. Aplikuj ingress (MUSÍŠ ZAMENIŤ <IP> SVÝ IP!)
# a. Otvor kubernetes/workload/05-ingress/app-ingress-nip.yaml
# b. Zameni VŠETKY <IP> za skutočnú IP (napr. 52.138.207.76)
# c. Ulož a aplikuj:
kubectl apply -f kubernetes/workload/05-ingress/app-ingress-nip.yaml

# ✅ Overenie
kubectl get pods -n app
kubectl get ingress -n app
kubectl get certificate -n app  # Čakaj kým READY=True
```

---

### Fáza 6: Build Docker Images (5-10 minút)

```powershell
# 1. Login do ACR
az acr login --name acrfsa<your-prefix>

# 2. Build Backend
cd C:\Users\fedoh\Documents\EventfindR\fsa-eventfindr-backend
docker build -t acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-backend:latest .
docker push acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-backend:latest

# 3. Build Frontend
cd C:\Users\fedoh\Documents\EventfindR\fsa-eventfindr-frontend
docker build -t acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-frontend:latest .
docker push acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-frontend:latest

# ✅ Overenie
kubectl get pods -n app  # Mal by byť "Running" keď sa images stiahli
```

---

### Fáza 7: Overenie že Všetko Funguje (3 minúty)

```powershell
# Obsah all-in-one overenie
$IP = kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}'

Write-Host "🌐 Aplikácia: https://app.$IP.nip.io"
Write-Host "🔐 Keycloak: https://keycloak.$IP.nip.io/auth"

# Test cURLom (alebo prehliadač)
curl https://app.$IP.nip.io         # Malo by vrátiť HTML Angular apku
curl https://keycloak.$IP.nip.io    # Malo by vrátiť Keycloak login
```

---

## 🔗 GitLab Setup (Automátický Build & Deploy)

### Krok 1: Vytvor Group

1. Prihlás sa: https://gitlab.fullstackacademy.sk
2. **Menu → Groups → New group**
3. **Group name:** `fsa-<your-prefix>`

### Krok 2: Push Repozitárov

```powershell
# Vytvor Personal Access Token v GitLab (Edit Profile → Access Tokens)
# Scopes: read_repository, write_repository

# Backend
cd C:\Users\fedoh\Documents\EventfindR\fsa-eventfindr-backend
git remote add gitlab https://<USERNAME>:<GLPAT>@gitlab.fullstackacademy.sk/fsa-<prefix>/fsa-eventfindr-backend.git
git push gitlab master

# Frontend
cd C:\Users\fedoh\Documents\EventfindR\fsa-eventfindr-frontend
git remote add gitlab https://<USERNAME>:<GLPAT>@gitlab.fullstackacademy.sk/fsa-<prefix>/fsa-eventfindr-frontend.git
git push gitlab main
```

### Krok 3: CI/CD Variables

1. GitLab: **fsa-<prefix> Group → Settings → CI/CD → Variables**
2. Pridaj:

```
ACR_REGISTRY = acrfsa<your-prefix>.azurecr.io
DOCKER_USERNAME = <ACR admin username>
DOCKER_PASSWORD = <ACR admin password>  ✅ Masked!
KUBECONFIG_BASE64 = <base64 kubeconfig>  ✅ Masked!
```

### Krok 4: GitLab Runner

1. **Build → Runners → New group runner**
2. **Tag:** `fsa`
3. Skopiraj token a ulož do K8s:

```powershell
kubectl create secret generic gitlab-runner-secret `
  --from-literal=runner-token=<TOKEN> `
  -n infra --dry-run=client -o yaml | kubectl apply -f -

helm repo add gitlab https://charts.gitlab.io
helm repo update

helm upgrade --install gitlab-runner -n infra gitlab/gitlab-runner `
  --version 0.87.0 `
  -f kubernetes/helm/helm-values/gitlab-runner/override.yaml
```

✅ GitLab → Build → Runners: Runner by mal byť "online"

---

## 📝 Príkazy na Rýchlu Kontrolu

```powershell
# Všetko je ready?
kubectl get nodes
kubectl get pods -n app
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx
kubectl get certificate -n app
kubectl get ingress -n app

# Problémy?
kubectl logs -f -n app deployment/fsa-keycloak        # Keycloak logy
kubectl logs -f -n app deployment/eventfindr-be       # Backend logy
kubectl logs -f -n ingress-nginx deployment/ingress-nginx-controller  # Ingress logy
kubectl describe certificate app-tls-nip -n app       # Certificate issues
```

---

## ❓ Časté Problémy

### "ImagePullBackOff"
**Príčina:** Docker image neexistuje v ACR  
**Riešenie:** 
```powershell
docker build -t acrfsa<prefix>.azurecr.io/fsa-eventfindr-backend:latest .
docker push acrfsa<prefix>.azurecr.io/fsa-eventfindr-backend:latest
```

### ingress-nginx nemá EXTERNAL-IP
**Príčina:** LoadBalancer nie je ready  
**Riešenie:** Čakaj 5 minút a skontroluj:
```powershell
kubectl describe svc ingress-nginx-controller -n ingress-nginx
```

### Keycloak sa nespúšťa
**Príčina:** Database connectivita alebo konfigurácia  
**Riešenie:**
```powershell
kubectl logs -f -n app deployment/fsa-keycloak
# Ak je "Database connection error" — skontroluj db_url v secrets
```

### Certifikát sa nevygeneruje
**Príčina:** DNS, email, alebo Let's Encrypt problém  
**Riešenie:**
```powershell
kubectl describe certificate app-tls-nip -n app
kubectl logs -f -n cert-manager deployment/cert-manager | tail -30
```

---

## 📚 Kompletná Dokumentácia

- **DEVOPS-SETUP.md** — Detailný step-by-step návod (ČÍTAJ TOTO!)
- **kubernetes/README.md** — Kubernetes infraštruktúra details
- **DEVOPS-CHECKLIST.md** — Finálny checklist pred deployment

---

**Máš otázky? Vrátni sa na DEVOPS-SETUP.md — je tam všetko podrobne vysvetlené!**

⏱️ **Typický čas na kompletný setup:** ~30 minút (s čakaním)

