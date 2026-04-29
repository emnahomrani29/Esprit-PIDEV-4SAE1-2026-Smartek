# ✅ Checklist de Test - Cochez au Fur et à Mesure

## 🎯 Tests de Base (5 minutes)

### Test 1 : Application Spring Boot
- [ ] Ouvrir http://localhost:8080/actuator/health
- [ ] Voir `{"status":"UP"}`
- [ ] ✅ **Application fonctionne !**

### Test 2 : Grafana
- [ ] Ouvrir http://localhost:3000
- [ ] Login : `admin` / `admin`
- [ ] Voir le dashboard Grafana
- [ ] ✅ **Grafana fonctionne !**

### Test 3 : Prometheus
- [ ] Ouvrir http://localhost:9090
- [ ] Taper `up` dans la barre de recherche
- [ ] Cliquer "Execute"
- [ ] Voir les métriques
- [ ] ✅ **Prometheus fonctionne !**

### Test 4 : Jenkins
- [ ] Ouvrir http://localhost:9091
- [ ] Voir la page Jenkins
- [ ] ✅ **Jenkins est accessible !**

---

## 🔧 Configuration Jenkins (10 minutes)

### Étape 1 : Première Connexion
- [ ] Coller le mot de passe : `bf8a489fb7634770a439175fb535faa0`
- [ ] Cliquer "Continue"
- [ ] Cliquer "Install suggested plugins"
- [ ] Attendre 2-3 minutes
- [ ] ✅ **Plugins installés !**

### Étape 2 : Créer un Compte
- [ ] Username : `admin`
- [ ] Password : `admin123`
- [ ] Email : `admin@smartek.com`
- [ ] Cliquer "Save and Continue"
- [ ] Cliquer "Save and Finish"
- [ ] Cliquer "Start using Jenkins"
- [ ] ✅ **Compte créé !**

### Étape 3 : Configurer Maven
- [ ] Manage Jenkins → Tools
- [ ] Maven installations → Add Maven
- [ ] Name : `Maven-3.9.6`
- [ ] ☑ Install automatically
- [ ] Version : `3.9.6`
- [ ] ✅ **Maven configuré !**

### Étape 4 : Configurer JDK
- [ ] JDK installations → Add JDK
- [ ] Name : `JDK-17`
- [ ] ☑ Install automatically
- [ ] Version : `jdk-17.0.9+9`
- [ ] Cliquer "Save"
- [ ] ✅ **JDK configuré !**

---

## 📦 Créer le Pipeline (5 minutes)

### Étape 1 : Nouveau Job
- [ ] Cliquer "New Item"
- [ ] Nom : `smartek-sponsor-pipeline`
- [ ] Type : Pipeline
- [ ] Cliquer "OK"
- [ ] ✅ **Job créé !**

### Étape 2 : Configuration
- [ ] Description : `Pipeline CI/CD pour Smartek Sponsor`
- [ ] Pipeline → Definition : Pipeline script
- [ ] Copier le script depuis TEST-GUIDE.md
- [ ] Coller dans le champ "Script"
- [ ] Cliquer "Save"
- [ ] ✅ **Pipeline configuré !**

---

## 🚀 Premier Build (10 minutes)

### Étape 1 : Lancer
- [ ] Cliquer "Build Now"
- [ ] Voir le build #1 apparaître
- [ ] ✅ **Build lancé !**

### Étape 2 : Suivre
- [ ] Cliquer sur #1
- [ ] Cliquer "Console Output"
- [ ] Voir les logs en temps réel
- [ ] ✅ **Logs visibles !**

### Étape 3 : Attendre
- [ ] Attendre 7-10 minutes
- [ ] Voir les étapes s'exécuter :
  - [ ] ✅ Checkout
  - [ ] ✅ Build
  - [ ] ✅ Tests
  - [ ] ✅ Package
  - [ ] ✅ Docker Build
  - [ ] ✅ Verification

### Étape 4 : Résultat
- [ ] Voir "Finished: SUCCESS"
- [ ] ✅ **Build réussi !**

---

## 📊 Vérifications Finales (2 minutes)

### Vérifier le JAR
```powershell
dir Backend\smartek_sponsor\target\*.jar
```
- [ ] Voir `smartek-sponsor-0.0.1-SNAPSHOT.jar`
- [ ] ✅ **JAR créé !**

### Vérifier l'Image Docker
```powershell
docker images | findstr smartek-sponsor
```
- [ ] Voir `smartek-sponsor:1` et `smartek-sponsor:latest`
- [ ] ✅ **Image Docker créée !**

### Vérifier l'Historique Jenkins
- [ ] Retourner sur http://localhost:9091
- [ ] Cliquer sur `smartek-sponsor-pipeline`
- [ ] Voir le build #1 avec une boule bleue
- [ ] ✅ **Historique visible !**

---

## 🎓 Test de Présentation (5 minutes)

### Scénario Complet
- [ ] Ouvrir http://localhost:8080/actuator/health
- [ ] Montrer `{"status":"UP"}`
- [ ] Ouvrir http://localhost:3000
- [ ] Montrer Grafana
- [ ] Ouvrir http://localhost:9091
- [ ] Montrer Jenkins
- [ ] Cliquer "Build Now"
- [ ] Montrer les logs
- [ ] Attendre le SUCCESS
- [ ] ✅ **Présentation testée !**

---

## 🎉 Résultat Final

Si toutes les cases sont cochées :

✅ **VOUS ÊTES PRÊT POUR LA PRÉSENTATION !**

---

## 📞 En Cas de Problème

### Jenkins ne répond pas
```powershell
docker restart jenkins
```
Attendre 1 minute, réessayer

### Build échoue
1. Cliquer sur le build
2. Cliquer "Console Output"
3. Lire l'erreur
4. Corriger
5. Relancer

### Application ne répond pas
```powershell
docker-compose restart smartek-sponsor
```
Attendre 30 secondes, réessayer

---

## 📚 Documentation

- **Guide complet** : [TEST-GUIDE.md](TEST-GUIDE.md)
- **Script de démo** : [DEMO-SCRIPT.md](DEMO-SCRIPT.md)
- **Démarrage rapide** : [START-HERE.md](START-HERE.md)

---

**Bonne chance ! 🚀**
