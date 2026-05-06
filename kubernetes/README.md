# EventfindR Kubernetes Infrastructure

Dokumentácia komplnej Kubernetes infraštruktúry pre EventfindR aplikáciu.

---

## 📁 Štruktúra

```
kubernetes/
├── workload/
│   ├── 01-namespace.yaml          # Namespaces: app, infra, ingress-nginx, cert-manager
│   ├── 02-secrets.yaml            # PostgreSQL a Keycloak credentials
│   ├── 03-app-backend/            # Backend deployment a service
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   ├── 04-app-frontend/           # Frontend deployment a service
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   └── 05-ingress/
│       ├── app-ingress-nip.yaml   # Ingress s nip.io doménou + SSL
│       └── keycloak-ingress.yaml  # Keycloak específics

└── helm/helm-values/
    ├── ingress-nginx/
    │   └── override.yaml           # Load Balancer, Azure PIP konfigurácia
    ├── cert-manager/
    │   ├── override.yaml           # CRD, Prometheus disable
    │   └── letsencrypt-cluster-issuer.yaml  # Let's Encrypt ClusterIssuer
    ├── keycloak/
    │   ├── override.yaml           # Keycloak deployment config
    │   ├── keycloak-java-config.yaml    # Azure PostgreSQL fix
    │   └── realm-fsa-configmap.yaml     # Realm configuration
    └── gitlab-runner/
        └── override.yaml           # GitLab Runner config
```

---

## BEZPEČNOSTNÍ BRÁNA — Sekretní Hodnoty

**VŠETKY TIETO SÚBORY VYŽADUJÚ PERSONALIZÁCIU!**

### 1. `kubernetes/workload/02-secrets.yaml`

```yaml
db_url: cHNxbC1mc2EteWFqb2gucG9zdGdyZXMuZGF0YWJhc2UuYXp1cmUuY29t
```

**Pozor:** Zameni `psql-fsa-yajoh` za tvoj prefix! Hodnota je v base64.

**Ako zakódovať:**
```powershell
[System.Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("psql-fsa-YOUR-PREFIX.postgres.database.azure.com"))
```

### 2. `kubernetes/helm/helm-values/ingress-nginx/override.yaml`

```yaml
service.beta.kubernetes.io/azure-load-balancer-resource-group: "MC_rg-fsa-<YOUR-PREFIX>_<REGION>"
service.beta.kubernetes.io/azure-pip-name: "pip-fsa-<YOUR-PREFIX>"
```

**Ako zistiť:**
```powershell
# Node Resource Group
az aks show --resource-group rg-fsa-<prefix> --name aks-fsa-<prefix> --query nodeResourceGroup -o tsv

# Region
az aks show --resource-group rg-fsa-<prefix> --name aks-fsa-<prefix> --query location -o tsv
```

### 3. `kubernetes/helm/helm-values/keycloak/override.yaml`

```yaml
database:
  hostname: psql-fsa-<your-prefix>.postgres.database.azure.com
```

### 4. `kubernetes/workload/05-ingress/app-ingress-nip.yaml`

```yaml
- hosts:
  - app.<IP>.nip.io
  - keycloak.<IP>.nip.io
# ...
- host: app.<IP>.nip.io
```

**Zisti IP:**
```powershell
kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
```

### 5. `kubernetes/workload/03-app-backend/deployment.yaml` a `04-app-frontend/deployment.yaml`

```yaml
image: acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-backend:latest
```

Zameni `<your-prefix>` v **OBOCH** image URLs!

---

## 🔧 Custom Configuration Files

### Backend Deployment Environment Variables

V `kubernetes/workload/03-app-backend/deployment.yaml` si všimnite:

```yaml
- name: ISSUER_URI
  value: https://keycloak.<IP>.nip.io/auth/realms/FSA
- name: JWT_URI
  value: https://keycloak.<IP>.nip.io/auth/realms/FSA/protocol/openid-connect/certs
```

**Tieto hodnoty sa musia zhodovať s:**
1. Keycloak hostname v ingress
2. Realm name v `realm-fsa-configmap.yaml`

---

## 📋 Aplikovací Postup

```powershell
# 1. Namespaces
kubectl apply -f workload/01-namespace.yaml

# 2. Secrets (PO uprave 02-secrets.yaml!)
kubectl apply -f workload/02-secrets.yaml

# 3. Helm repozitáre
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo add jetstack https://charts.jetstack.io
helm repo add codecentric https://codecentric.github.io/helm-charts
helm repo add gitlab https://charts.gitlab.io
helm repo update

# 4. Ingress-Nginx (PO uprave override.yaml!)
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx `
  -n ingress-nginx --version 4.15.1 `
  -f helm/helm-values/ingress-nginx/override.yaml

# 5. Cert-Manager
helm upgrade --install cert-manager jetstack/cert-manager `
  -n cert-manager --version 1.20.1 `
  -f helm/helm-values/cert-manager/override.yaml

kubectl apply -f helm/helm-values/cert-manager/letsencrypt-cluster-issuer.yaml

# 6. Keycloak (PO uprave override.yaml a realm!)
kubectl apply -f helm/helm-values/keycloak/keycloak-java-config.yaml
kubectl apply -f helm/helm-values/keycloak/realm-fsa-configmap.yaml

helm upgrade --install keycloak -n app codecentric/keycloakx `
  --version 7.1.9 `
  -f helm/helm-values/keycloak/override.yaml

# 7. GitLab Runner (PO uprave runner token!)
helm upgrade --install gitlab-runner -n infra gitlab/gitlab-runner `
  --version 0.87.0 `
  -f helm/helm-values/gitlab-runner/override.yaml

# 8. Workload (backend, frontend)
kubectl apply -f workload/03-app-backend/
kubectl apply -f workload/04-app-frontend/
kubectl apply -f workload/05-ingress/
```

---

## ✅ Validation Checklist

```powershell
# Namespaces
kubectl get namespaces | grep -E "app|infra|ingress-nginx|cert-manager"

# Secrets
kubectl get secrets -n app

# Ingress-Nginx
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx  # AJ EXTERNAL-IP!

# Cert-Manager
kubectl get pods -n cert-manager
kubectl get clusterissuer letsencrypt-prod

# Keycloak
kubectl get pods -n app | grep keycloak
kubectl logs -f -n app deployment/fsa-keycloak  # AJ ak je problém

# Backend/Frontend
kubectl get pods -n app
kubectl get svc -n app
kubectl get ingress -n app

# Certifikáty
kubectl get certificate -n app
kubectl get secrets -n app | grep tls
```

---

## 🚨 Troubleshooting

### "ImagePullBackOff" pri deploymente

Uisti sa, že:
1. ACR image existuje: `docker push acrfsa<prefix>.azurecr.io/fsa-eventfindr-backend:latest`
2. Image URL je správna v deployment.yaml (bez typy)

### Keycloak nevyprintuje sa

```powershell
# Skontroluj logy
kubectl logs -f -n app deployment/fsa-keycloak

# Skontroluj events
kubectl describe pod -n app -l app=fsa-keycloak
```

Common problémy:
- Database connectivita (skontroluj db_url v secrets)
- Persistent volume nedostupný
- Resource limity (memory, CPU)

### Ingress certificát sa nevygeneruje

```powershell
kubectl describe certificate app-tls-nip -n app
kubectl logs -f -n cert-manager deployment/cert-manager
```

Usually:
- email v ClusterIssuer je prázdny
- DNS nevisí (nip.io by mal pracovať sám)

---

## 📚 Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)
- [cert-manager](https://cert-manager.io/docs/)
- [Keycloak nip.io](https://www.keycloak.org/)
- [ingress-nginx Azure](https://kubernetes.github.io/ingress-nginx/deploy/#azure)

---

**Verzia:** 1.0  
**Posledná aktualizácia:** 2026-05-06

