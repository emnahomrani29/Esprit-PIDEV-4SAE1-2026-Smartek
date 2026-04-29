# ✅ FINAL STEPS - Execute These Now

## 🎯 Current Status
- ✅ All code pushed to GitHub
- ✅ Docker Compose services running (7 services)
- ✅ Kubernetes cluster ready
- ✅ Tests fixed (using H2 database)
- ✅ Jenkinsfile updated with proper credentials
- ⚠️ Jenkins needs Docker access
- ⚠️ Credentials need to be configured

---

## 🚀 Execute These Commands Now

### 1. Restart Jenkins with Docker Access
```powershell
cd Backend/smartek_sponsor
.\scripts\restart-jenkins-with-docker.ps1
```
**Time: 2 minutes**

This will:
- Stop current Jenkins
- Restart with Docker socket mounted
- Install Docker CLI inside Jenkins
- Jenkins can now build Docker images ✅

---

### 2. Configure SonarQube Token

**In SonarQube (http://localhost:9000):**
1. Login: `admin` / `admin123`
2. Click **"A"** (top right) → **My Account**
3. Click **Security** tab
4. Under **Generate Tokens**:
   - Name: `jenkins`
   - Type: `Global Analysis Token`
   - Click **Generate**
5. **COPY THE TOKEN** (you won't see it again!)

**In Jenkins (http://localhost:9091):**
1. Login: `admin` / `admin123`
2. Go to: **Manage Jenkins** → **Credentials**
3. Click: **System** → **Global credentials (unrestricted)**
4. Click: **Add Credentials**
5. Fill in:
   - Kind: **Secret text**
   - Scope: **Global**
   - Secret: **[PASTE YOUR SONARQUBE TOKEN]**
   - ID: `sonarqube-token` (EXACTLY this!)
   - Description: `SonarQube Authentication Token`
6. Click **Create**

---

### 3. Configure Nexus Credentials

**In Jenkins (http://localhost:9091):**
1. Go to: **Manage Jenkins** → **Credentials** → **System** → **Global credentials**
2. Click: **Add Credentials**
3. Fill in:
   - Kind: **Username with password**
   - Scope: **Global**
   - Username: `admin`
   - Password: `admin123`
   - ID: `nexus-credentials` (EXACTLY this!)
   - Description: `Nexus Repository Credentials`
4. Click **Create**

---

### 4. Run the Pipeline

**In Jenkins (http://localhost:9091):**
1. Click on: **smartek-sponsor-git-pipeline**
2. Click: **Build Now** (left sidebar)
3. Watch the build execute!

You should see all 12 stages:
```
✅ Stage 1: Checkout
✅ Stage 2: Build
✅ Stage 3: Unit Tests
✅ Stage 4: SonarQube Analysis
✅ Stage 5: Quality Gate
✅ Stage 6: Package
✅ Stage 7: Nexus Upload
✅ Stage 8: Docker Build
✅ Stage 9: Security Scan
✅ Stage 10: Docker Registry Push
✅ Stage 11: Kubernetes Deploy
✅ Stage 12: Health Check
```

---

### 5. Verify Everything Works

```powershell
.\scripts\test-everything.ps1
```

This will check:
- ✅ All 7 Docker services
- ✅ Kubernetes pods (3 replicas)
- ✅ Application endpoints
- ✅ Docker images in registry
- ✅ Unit tests pass
- ✅ Jenkins pipeline status

---

## 📊 Expected Results

### After Step 1 (Jenkins Restart):
```
✅ Jenkins restarted successfully!
✅ Jenkins can now build Docker images
🔗 Access Jenkins at: http://localhost:9091
```

### After Steps 2-3 (Credentials):
```
✅ SonarQube token configured
✅ Nexus credentials configured
```

### After Step 4 (Pipeline Run):
```
✅ PIPELINE COMPLETED SUCCESSFULLY!
✅ Build Number: X
✅ All 12 stages passed
✅ Docker Image: localhost:5000/smartek-sponsor:X
✅ Kubernetes: DEPLOYED
✅ Replicas: 3/3 RUNNING
✅ Status: PRODUCTION READY
```

### After Step 5 (Test):
```
✅ All services are running
✅ Application: HEALTHY
✅ Jenkins: RUNNING
✅ SonarQube: RUNNING
✅ Nexus: RUNNING
✅ Kubernetes: 3/3 pods RUNNING
```

---

## 🎓 For Your Professor Demo

Once everything is working, you can demonstrate:

### 1. Infrastructure (Show running services)
```powershell
docker ps
kubectl get all -n smartek-production
```

### 2. CI/CD Pipeline (Trigger build)
- Open Jenkins: http://localhost:9091
- Click "Build Now"
- Show all 12 stages executing

### 3. Code Quality (Show SonarQube)
- Open: http://localhost:9000
- Show project analysis
- Show metrics: bugs, vulnerabilities, code smells

### 4. Artifact Repository (Show Nexus)
- Open: http://localhost:8081
- Browse → maven-releases → com.smartek → smartek-sponsor
- Show versioned artifacts

### 5. Container Registry (Show Docker images)
```powershell
docker images | Select-String "smartek-sponsor"
Invoke-RestMethod http://localhost:5000/v2/_catalog
```

### 6. Kubernetes Deployment (Show pods)
```powershell
kubectl get pods -n smartek-production
kubectl describe deployment smartek-sponsor-deployment -n smartek-production
kubectl get hpa -n smartek-production
```

### 7. Application (Show it works)
- Health: http://localhost:8080/actuator/health
- Swagger: http://localhost:8080/swagger-ui.html
- Metrics: http://localhost:8080/actuator/prometheus

### 8. Monitoring (Show Prometheus/Grafana)
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

---

## 🔗 Quick Reference

| What | URL | Credentials |
|------|-----|-------------|
| Application | http://localhost:8080 | - |
| Jenkins | http://localhost:9091 | admin/admin123 |
| SonarQube | http://localhost:9000 | admin/admin123 |
| Nexus | http://localhost:8081 | admin/admin123 |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin/admin |
| Swagger | http://localhost:8080/swagger-ui.html | - |

---

## ⚡ Quick Commands

```powershell
# Restart Jenkins with Docker
.\scripts\restart-jenkins-with-docker.ps1

# Test everything
.\scripts\test-everything.ps1

# View Jenkins logs
docker logs -f jenkins

# Check Kubernetes
kubectl get all -n smartek-production

# Check Docker images
docker images | Select-String "smartek"

# Check registry
Invoke-RestMethod http://localhost:5000/v2/_catalog
```

---

## 🆘 Troubleshooting

### Jenkins can't build Docker images
```powershell
# Run this again
.\scripts\restart-jenkins-with-docker.ps1

# Verify Docker is accessible
docker exec jenkins docker ps
```

### SonarQube authentication fails
- Make sure you used a **token**, not password
- Verify credential ID is **exactly**: `sonarqube-token`
- Token type must be: **Global Analysis Token**

### Nexus upload fails
- Verify credential ID is **exactly**: `nexus-credentials`
- Check Nexus is running: http://localhost:8081
- Verify credentials: admin/admin123

### Tests fail
- Tests now use H2 (no MySQL needed)
- Check test reports in Jenkins
- Run locally: `mvn test -Dspring.profiles.active=test`

### Kubernetes pods not starting
```powershell
kubectl get pods -n smartek-production
kubectl logs -n smartek-production <pod-name>
kubectl describe pod -n smartek-production <pod-name>
```

---

## ✅ Success Checklist

Before your demo, verify:
- [ ] Jenkins accessible at http://localhost:9091
- [ ] SonarQube token configured in Jenkins
- [ ] Nexus credentials configured in Jenkins
- [ ] Pipeline runs successfully (all 12 stages)
- [ ] SonarQube shows analysis results
- [ ] Nexus contains artifacts
- [ ] Docker registry has images
- [ ] Kubernetes has 3 running pods
- [ ] Application responds: http://localhost:8080/actuator/health
- [ ] All tests pass

---

## 🎉 You're Ready!

Execute the 5 steps above and your complete CI/CD pipeline will be fully operational!

**Time needed: ~10 minutes**

Good luck! 🚀
