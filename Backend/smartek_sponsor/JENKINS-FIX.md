# Fix Jenkins Git Checkout Issue

## Problem
Jenkins shows: "fatal: not in a git directory"

## Solution

### Option 1: Reconfigure Pipeline (Recommended)

1. Open Jenkins: http://localhost:9091
2. Click on **smartek-sponsor-git-pipeline**
3. Click **Configure** (left sidebar)
4. Scroll down to **Pipeline** section
5. Make sure these settings are correct:

   **Definition:** `Pipeline script from SCM`
   
   **SCM:** `Git`
   
   **Repository URL:** `https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git`
   
   **Credentials:** `- none -` (it's a public repo)
   
   **Branch Specifier:** `*/sponsor`
   
   **Script Path:** `Backend/smartek_sponsor/Jenkinsfile`

6. Click **Save**
7. Click **Build Now**

---

### Option 2: Create New Pipeline

If the above doesn't work, create a fresh pipeline:

1. In Jenkins, click **New Item**
2. Enter name: `smartek-sponsor-pipeline`
3. Select: **Pipeline**
4. Click **OK**
5. Scroll to **Pipeline** section
6. Set:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git`
   - Branch: `*/sponsor`
   - Script Path: `Backend/smartek_sponsor/Jenkinsfile`
7. Click **Save**
8. Click **Build Now**

---

### Option 3: Use Pipeline Script Directly

If Git checkout still fails, use the Jenkinsfile content directly:

1. In Jenkins, click on your pipeline
2. Click **Configure**
3. In **Pipeline** section:
   - Definition: `Pipeline script` (not from SCM)
   - Copy the entire content from `Backend/smartek_sponsor/Jenkinsfile`
   - Paste it in the **Script** box
4. **IMPORTANT:** Change the first stage to:
   ```groovy
   stage('1. Checkout') {
       steps {
           echo '📥 ========================================='
           echo '📥 STAGE 1: CHECKOUT CODE FROM GIT'
           echo '📥 ========================================='
           git branch: 'sponsor',
               url: 'https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git'
           sh '''
               echo "✅ Repository: https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git"
               echo "✅ Branch: sponsor"
               echo "✅ Commit: $(git rev-parse --short HEAD)"
               echo "✅ Author: $(git log -1 --pretty=format:'%an')"
               pwd
               ls -la Backend/smartek_sponsor
           '''
       }
   }
   ```
5. Click **Save**
6. Click **Build Now**

---

## Verify Git is Accessible

Before running the pipeline, verify Git works in Jenkins:

1. In Jenkins, go to **Manage Jenkins** → **Script Console**
2. Run this script:
   ```groovy
   def proc = "git --version".execute()
   proc.waitFor()
   println "Git version: ${proc.text}"
   ```
3. You should see Git version output

---

## Expected Result

After fixing, the pipeline should:
- ✅ Clone the repository from GitHub
- ✅ Checkout the sponsor branch
- ✅ Find the Jenkinsfile at Backend/smartek_sponsor/Jenkinsfile
- ✅ Execute all 12 stages

---

## If Still Failing

Check Jenkins logs:
```powershell
docker logs jenkins
```

Or restart Jenkins:
```powershell
docker restart jenkins
```

Then try the pipeline again.
