#!/bin/bash

echo "=========================================="
echo "Git Push Script for Event/Planning Service"
echo "=========================================="

# Vérifier si on est dans un repo git
if [ ! -d ".git" ]; then
    echo "Initializing Git repository..."
    git init
fi

# Vérifier le statut
echo "1. Checking Git status..."
git status

# Configurer l'identité Git (remplacez par vos vraies informations)
echo "2. Configuring Git identity..."
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# Ajouter tous les fichiers
echo "3. Adding all files..."
git add .

# Vérifier les fichiers ajoutés
echo "4. Checking added files..."
git status

# Commit avec message détaillé
echo "5. Creating commit..."
git commit -m "feat: Enhanced Planning & Events with advanced features

- Added update/refresh buttons for real-time data sync
- Implemented advanced dynamic search across all fields
- Added comprehensive sorting system (time, title, type, location, availability)
- Complete English translation for international accessibility
- Enhanced trainer events management with filtering and sorting
- Improved trainer weekly planning with advanced controls
- Added synchronization between planning and training services
- Enhanced UI/UX with better visual feedback and loading states
- Added results summary and improved empty states
- Implemented business logic for planning-training integration

Features:
- Dynamic search and filtering
- Multi-criteria sorting with visual indicators
- Real-time updates and refresh functionality
- English localization
- Enhanced user experience
- Planning-Training service synchronization"

# Ajouter le remote origin (si pas déjà fait)
echo "6. Setting up remote origin..."
git remote remove origin 2>/dev/null || true
git remote add origin https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git

# Vérifier le remote
echo "7. Verifying remote..."
git remote -v

# Créer et basculer vers la branche
echo "8. Creating and switching to event/planning-service branch..."
git checkout -b event/planning-service 2>/dev/null || git checkout event/planning-service

# Pousser vers GitHub
echo "9. Pushing to GitHub..."
git push -u origin event/planning-service

echo "=========================================="
echo "Push completed successfully!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Go to: https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek"
echo "2. Create a Pull Request from event/planning-service to main"
echo "3. Use PULL-REQUEST-SUMMARY.md content as description"
echo ""