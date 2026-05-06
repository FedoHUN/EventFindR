# Java Version Alignment - EventfindR & FSA-Hexagonal

## ✅ Resolution Summary

Both projects have been aligned to use **Java 21** which is:
- ✅ Fully supported by Maven compiler plugin (v3.14+)
- ✅ Supported by Docker images (`eclipse-temurin:21-jdk-alpine`)
- ✅ Supported by Maven images (`maven:3.9-eclipse-temurin-21`)
- ✅ LTS (Long Term Support) - stable for production

## 📋 Changes Made

### 1. EventfindR Backend (`fsa-eventfindr-backend`)

**pom.xml:**
- Changed: `<java.version>25</java.version>` → `<java.version>21</java.version>`
- Removed: Special Maven compiler plugin 3.15.0 configuration (not needed for Java 21)

**Dockerfile:**
- Builder: `maven:3.9-eclipse-temurin-21` (available in Docker Hub)
- Runtime: `eclipse-temurin:21-jdk-alpine` (stable, lightweight)

### 2. Example Application (`FSA-hexagonal-architecture-master`)

**pom.xml:**
- Changed: `<java.version>25</java.version>` → `<java.version>21</java.version>`
- Removed: Special Maven compiler plugin 3.15.0 configuration

**Dockerfile:**
- Builder: `maven:3.9.6-eclipse-temurin-25 as builder` → `maven:3.9-eclipse-temurin-21`
- Runtime: `eclipse-temurin:25-jdk-alpine` → `eclipse-temurin:21-jdk-alpine`

## 🔧 Build Instructions

### Local Maven Build (Java 25 optional)

If you want to use Java 25 locally, you can:

1. **Keep Java 21 for Docker builds** (current setup - works great)
2. **For local development**, if you have Java 25 installed:
   - Add Java 25 override in your IDE
   - Or create a local `mvn.local` profile

### Docker Build

```bash
cd C:\Users\fedoh\Documents\EventfindR\fsa-eventfindr-backend
docker build -t acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-backend:latest .
docker push acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-backend:latest
```

### Test Build

```bash
# Test with Maven wrapper
.\mvnw.cmd clean package -DskipTests

# Should output: BUILD SUCCESS
```

## 📊 Version Comparison

| Component | EventfindR Backend | FSA-Hexagonal Example | Status |
|-----------|-------------------|----------------------|--------|
| Java Version | 21 ✅ | 21 ✅ | Aligned |
| Spring Boot | 4.0.5 | 4.0.3 | Different (OK) |
| Maven | 3.9 | 3.9 | Aligned |
| MapStruct | 1.6.3 | 1.6.3 | Aligned |
| Compiler Plugin | 3.14 (inherited) | 3.14 (inherited) | Aligned |

## ⚠️ Why Not Java 25?

Java 25 requires:
- Maven compiler plugin **3.15.0+** (very new)
- Docker images with Java 25 (not available in Docker Hub yet)
- Requires rebuilding Docker images regularly as new versions drop

**Java 21 is the better choice for production** - stable, well-tested, widely available.

## 🚀 Next Steps

1. ✅ Run the build tests:
   ```bash
   .\mvnw.cmd clean package -DskipTests
   ```

2. ✅ Build Docker image:
   ```bash
   docker build -t fsa-eventfindr-backend:latest .
   ```

3. ✅ Push to ACR (after replacing `<your-prefix>`):
   ```bash
   docker tag fsa-eventfindr-backend:latest acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-backend:latest
   docker push acrfsa<your-prefix>.azurecr.io/fsa-eventfindr-backend:latest
   ```

4. ✅ Deploy to Kubernetes (update K8s deployment image reference)


