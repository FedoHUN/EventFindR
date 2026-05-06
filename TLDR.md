# 🚀 TLDR — Iba Príkazy (Bez Vysvetľovania)

**Pre tých, čo vedia čo robia a potrebujú len príkazový referenčný list.**

---

## ✏️ PERSONALIZÁCIA — Find & Replace

```
<your-prefix>                    → janko (tvoj ACR prefix)
<REGION>                         → eastus (tvoj region)
<IP>                             → 52.138.207.76 (External IP)
psql-fsa-<prefix>              → psql-fsa-janko
your-email@example.com         → tvoj-email@skdomain.sk
MC_rg-fsa-<PREFIX>_<REGION>   → MC_rg-fsa-janko_eastus
```

---

## 1️⃣ AZURE + K8S CONNECT

```powershell
az login
az account set --subscription "<SUBSCRIPTION_ID>"
az aks get-credentials --resource-group rg-fsa-janko --name aks-fsa-janko --admin
kubectl get nodes
```

---

## 2️⃣ KUBERNETES APPLY

```powershell
kubectl apply -f kubernetes/workload/01-namespace.yaml
kubectl apply -f kubernetes/workload/02-secrets.yaml
```

**⚠️ PRED TÝM: Personalizuj 02-secrets.yaml (db_url base64!)**

---

## 3️⃣ HELM REPOS

```powershell
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo add jetstack https://charts.jetstack.io
helm repo add codecentric https://codecentric.github.io/helm-charts
helm repo add gitlab https://charts.gitlab.io
helm repo update
```

---

## 4️⃣ INGRESS-NGINX

```powershell
# PRED TÝM: Personalizuj kubernetes/helm/helm-values/ingress-nginx/override.yaml

helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx `
  -n ingress-nginx `
  --version 4.15.1 `
  -f kubernetes/helm/helm-values/ingress-nginx/override.yaml

kubectl rollout status deployment/ingress-nginx-controller -n ingress-nginx

# Zisti External IP:
kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
```

---

## 5️⃣ CERT-MANAGER

```powershell
helm upgrade --install cert-manager jetstack/cert-manager `
  -n cert-manager `
  --version 1.20.1 `
  -f kubernetes/helm/helm-values/cert-manager/override.yaml

kubectl apply -f kubernetes/helm/helm-values/cert-manager/letsencrypt-cluster-issuer.yaml

# PRED TÝM: Zmeniť email v letsencrypt-cluster-issuer.yaml!
```

---

## 6️⃣ KEYCLOAK

```powershell
# PRED TÝM: Personalizuj kubernetes/helm/helm-values/keycloak/override.yaml

kubectl apply -f kubernetes/helm/helm-values/keycloak/keycloak-java-config.yaml
kubectl apply -f kubernetes/helm/helm-values/keycloak/realm-fsa-configmap.yaml

helm upgrade --install keycloak -n app codecentric/keycloakx `
  --version 7.1.9 `
  -f kubernetes/helm/helm-values/keycloak/override.yaml

# Čakaj ~10 minút
kubectl rollout status deployment/fsa-keycloak -n app
```

---

## 7️⃣ DOCKER BUILDS

```powershell
az acr login --name acrfsajanko

# Backend
cd C:\Users\fedoh\Documents\EventfindR\fsa-eventfindr-backend
docker build -t acrfsajanko.azurecr.io/fsa-eventfindr-backend:latest .
docker push acrfsajanko.azurecr.io/fsa-eventfindr-backend:latest

# Frontend
cd C:\Users\fedoh\Documents\EventfindR\fsa-eventfindr-frontend
docker build -t acrfsajanko.azurecr.io/fsa-eventfindr-frontend:latest .
docker push acrfsajanko.azurecr.io/fsa-eventfindr-frontend:latest
```

---

## 8️⃣ WORKLOAD & INGRESS

```powershell
kubectl apply -f kubernetes/workload/03-app-backend/
kubectl apply -f kubernetes/workload/04-app-frontend/

# PRED TÝM: Personalizuj kubernetes/workload/05-ingress/app-ingress-nip.yaml
# Zameniť <IP> za External IP z Bodu 4️⃣!

kubectl apply -f kubernetes/workload/05-ingress/app-ingress-nip.yaml
```

---

## ✅ OVERENIE

```powershell
# Zisti IP
$IP = kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
Write-Host "https://app.$IP.nip.io"

# Overenie
kubectl get pods -n app
kubectl get svc -n app
kubectl get certificate -n app        # Mal by byť READY=True
kubectl get ingress -n app

# Test v prehliadači alebo curl:
curl https://app.$IP.nip.io
curl https://keycloak.$IP.nip.io
```

---

## 🎯 GITLAB SETUP (Optional)

```powershell
# Vytvor Group v GitLab: https://gitlab.fullstackacademy.sk
# Group name: fsa-janko

# Push Backend
cd C:\Users\fedoh\Documents\EventfindR\fsa-eventfindr-backend
git remote add gitlab https://GITLAB_URL.git
git push gitlab master

# Push Frontend
cd C:\Users\fedoh\Documents\EventfindR\fsa-eventfindr-frontend
git remote add gitlab https://GITLAB_URL.git
git push gitlab main

# Runner (keď máš token)
kubectl create secret generic gitlab-runner-secret `
  --from-literal=runner-token=<TOKEN> `
  -n infra --dry-run=client -o yaml | kubectl apply -f -

helm upgrade --install gitlab-runner -n infra gitlab/gitlab-runner `
  --version 0.87.0 `
  -f kubernetes/helm/helm-values/gitlab-runner/override.yaml
```

---

## 🔍 TROUBLESHOOTING — Rýchle Príkazy

```powershell
# Status všetkého
kubectl get pods -A
kubectl get svc -A

# Logy
kubectl logs -f -n app deployment/eventfindr-be
kubectl logs -f -n app deployment/eventfindr-fe
kubectl logs -f -n app deployment/fsa-keycloak
kubectl logs -f -n cert-manager deployment/cert-manager

# Detaily
kubectl describe pod -n app <pod-name>
kubectl describe certificate app-tls-nip -n app

# Rollout status
kubectl rollout status deployment/eventfindr-be -n app
kubectl rollout status deployment/eventfindr-fe -n app
kubectl rollout status deployment/fsa-keycloak -n app
```

---

## 📋 Skontroluj Personalizácie — MUSÍŠ!

| Súbor | Nájdi | Zameniaj |
|---|---|---|
| `02-secrets.yaml` | `db_url:` | tvoj db_url (base64!) |
| `ingress-nginx/override.yaml` | `azure-load-balancer-resource-group` | `MC_rg-fsa-<prefix>_<region>` |
| `ingress-nginx/override.yaml` | `azure-pip-name` | `pip-fsa-<prefix>` |
| `keycloak/override.yaml` | `hostname:` | `psql-fsa-<prefix>.postgres...` |
| `03-app-backend/deployment.yaml` | `image:` | `acrfsa<prefix>.azurecr.io/...` |
| `04-app-frontend/deployment.yaml` | `image:` | `acrfsa<prefix>.azurecr.io/...` |
| `app-ingress-nip.yaml` | `<IP>` | `52.138.207.76` |
| `letsencrypt-cluster-issuer.yaml` | `email:` | `your-email@example.com` |

---

## 🎯 Base64 Encoding (PowerShell)

```powershell
# Príklad:
[System.Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("psql-fsa-janko.postgres.database.azure.com"))
# Output: cHNxbC1mc2EtamFua28ucG9zdGdyZXMuZGF0YWJhc2UuYXp1cmUuY29t
```

---

## 🔑 Zisti Azure Info

```powershell
# Node Resource Group
az aks show --resource-group rg-fsa-<prefix> --name aks-fsa-<prefix> --query nodeResourceGroup -o tsv

# Region
az aks show --resource-group rg-fsa-<prefix> --name aks-fsa-<prefix> --query location -o tsv

# External IP (keď máš ingress-nginx running):
kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
```

---

## 📞 GIT PUSH

```powershell
# Generate PAT v GitLab: https://gitlab.fullstackacademy.sk → Ed it Profile → Access Tokens

# Backend
cd fsa-eventfindr-backend
git remote add gitlab https://USERNAME:GLPAT@gitlab.fullstackacademy.sk/fsa-<prefix>/fsa-eventfindr-backend.git
git push -u gitlab master

# Frontend
cd fsa-eventfindr-frontend
git remote add gitlab https://USERNAME:GLPAT@gitlab.fullstackacademy.sk/fsa-<prefix>/fsa-eventfindr-frontend.git
git push -u gitlab main
```

---

## ⏭️ Keď Je Všetko Hotové

```powershell
# Test v prehliadači:
# https://app.<IP>.nip.io
# https://keycloak.<IP>.nip.io/auth

# Prihlás sa Keycloakom:
# username: admin
# password: admin (z 02-secrets.yaml)

# ÚLOHA 1: nip.io domain s TLS ✅ HOTOVO!
# (viď DEVOPS-SETUP.md pre Keycloak redirect URIs update)
```

---

**To je všetko. Teraz len aplikuj.:)**

---

*DEVOPS-SETUP.md má všetky detaily.*  
*Toto je len príkazový zoznam bez vysvetľovania.*

