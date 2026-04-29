# 🎯 START HERE - Quick Setup Guide

## 🚀 You're Almost Done!

All your CI/CD infrastructure is ready. Just follow these 3 simple steps:

---

## Step 1: Restart Jenkins (2 minutes)

Jenkins needs Docker access to build images. Run this script:

```powershell
cd Backend/smartek_sponsor
.\scripts\restart-jenkins-with-docker.ps1
```

**Wait 30 seconds** for Jenkins to start, then login:
- URL: http://localhost:9091
- Username: `admin`
- Password: `admin123`

---

## Step 2: Configure Credentials (3 minutes)

### Get SonarQube Token:
1. Open http://localhost:9000 (login: admin/admin123)
2. Click **"A"** icon → **My Account** → **Security**
3. Generate Token: Name = `jenkins`, Type = `Global Analysis Token`
4. **Copy the token!**

### Add to Jenkins:
1. Open http://localhost:9091
2. **Manage Jenkins** → **Credentials** → **System** → **Global credentials** → **Add Credentials**

**First Credential (SonarQube):**
- Kind: `Secret text`
- Secret: `[paste SonarQube token]`
- ID: `sonarqube-token`
- Click **Create**

**Second Credential (Nexus):**
- Click **Add Credentials** again
- Kind: `Username with password`
- Username: `admin`
- Password: `admin123`
- ID: `nexus-credentials`
- Click **Create**

---

## Step 3: Run Pipeline (1 minute)

1. In Jenkins, click: **smartek-sponsor-git-pipeline**
2. Click: **Build Now**
3. Watch all 12 stages execute! ✅

---

## ✅ Test Everything

After the pipeline completes, run:

```powershell
.\scripts\test-everything.ps1
```

This checks all services, endpoints, and deployments.

---

## 📊 What You'll See

Your pipeline will execute these 12 stages:

1. ✅ **Checkout** - Pull code from GitHub
2. ✅ **Build** - Compile Java code
3. ✅ **Unit Tests** - Run tests with H2 database
4. ✅ **SonarQube** - Analyze code quality
5. ✅ **Quality Gate** - Verify standards
6. ✅ **Package** - Create JAR file
7. ✅ **Nexus** - Upload artifact
8. ✅ **Docker Build** - Build container image
9. ✅ **Security Scan** - Scan for vulnerabilities
10. ✅ **Registry Push** - Push to Docker registry
11. ✅ **Kubernetes Deploy** - Deploy to K8s cluster
12. ✅ **Health Check** - Verify deployment

---

## 🔗 All Your Services

| Service | URL | Login |
|---------|-----|-------|
| Application | http://localhost:8080 | - |
| Jenkins | http://localhost:9091 | admin/admin123 |
| SonarQube | http://localhost:9000 | admin/admin123 |
| Nexus | http://localhost:8081 | admin/admin123 |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin/admin |

---

## 🎓 For Your Demo

1. **Show services running:**
   ```powershell
   docker ps
   kubectl get pods -n smartek-production
   ```

2. **Trigger pipeline in Jenkins** - Show all 12 stages

3. **Show results:**
   - SonarQube: Code quality analysis
   - Nexus: Uploaded artifacts
   - Kubernetes: Running pods
   - Application: http://localhost:8080/actuator/health

---

## 🆘 Need Help?

- **Detailed guide:** Read `COMPLETE-SETUP.md`
- **Troubleshooting:** Run `.\scripts\test-everything.ps1`
- **Configure credentials:** Run `.\scripts\configure-jenkins-credentials.ps1`

---

## 🎉 That's It!

Three simple steps and your complete CI/CD pipeline is running!

**Good luck with your presentation!** 🚀
