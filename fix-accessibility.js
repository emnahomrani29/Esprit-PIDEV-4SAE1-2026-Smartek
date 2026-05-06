#!/usr/bin/env node
/**
 * Script pour corriger automatiquement les problèmes d'accessibilité SonarCloud
 * - Ajoute des attributs 'for' et 'id' aux labels et inputs
 * - Ajoute des attributs keyboard aux divs cliquables
 */

const fs = require('fs');
const path = require('path');

let labelCounter = 0;

function generateId(text) {
  const cleaned = text.replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
  return cleaned || `field${labelCounter++}`;
}

function fixFormLabels(content) {
  const lines = content.split('\n');
  const fixedLines = [];
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    // Détecter un label sans 'for'
    if (line.includes('<label') && !line.includes('for=') && line.includes('</label>')) {
      const labelMatch = line.match(/<label[^>]*>(.*?)<\/label>/);
      if (labelMatch) {
        const labelText = labelMatch[1].trim();
        const fieldId = generateId(labelText);
        
        // Remplacer le label
        const newLabel = line.replace(/<label([^>]*)>/, `<label$1 for="${fieldId}">`);
        fixedLines.push(newLabel);
        
        // Chercher l'input/select/textarea suivant
        for (let j = i + 1; j < Math.min(lines.length, i + 10); j++) {
          const nextLine = lines[j];
          if (/<input|<select|<textarea/.test(nextLine) && !nextLine.includes('id=')) {
            lines[j] = nextLine.replace(/(<(?:input|select|textarea)[^>]*)/, `$1 id="${fieldId}"`);
            break;
          }
        }
        continue;
      }
    }
    
    fixedLines.push(line);
  }
  
  return fixedLines.join('\n');
}

function fixClickableDivs(content) {
  return content.replace(
    /(<div[^>]*\(click\)="([^"]*)"[^>]*)(>)/g,
    (match, divTag, clickFunc, closing) => {
      if (/keydown|keypress|keyup/.test(divTag)) {
        return match;
      }
      
      let newTag = divTag;
      if (!newTag.includes('tabindex=')) {
        newTag += ' tabindex="0"';
      }
      if (!newTag.includes('role=')) {
        newTag += ' role="button"';
      }
      newTag += ` (keydown.enter)="${clickFunc}"`;
      
      return newTag + closing;
    }
  );
}

function fixClickableInputs(content) {
  return content.replace(
    /(<input[^>]*\(click\)="([^"]*)"[^>]*)(>|\/?>)/g,
    (match, inputTag, clickFunc, closing) => {
      if (/keydown|keypress|keyup/.test(inputTag)) {
        return match;
      }
      
      return inputTag + ` (keydown.enter)="${clickFunc}"` + closing;
    }
  );
}

function fixIframes(content) {
  return content.replace(
    /<iframe([^>]*)(>)/g,
    (match, iframeTag, closing) => {
      if (iframeTag.includes('title=')) {
        return match;
      }
      
      let title = 'Embedded content';
      if (/youtube|vimeo/.test(iframeTag)) {
        title = 'Video player';
      }
      
      return `<iframe${iframeTag} title="${title}"${closing}`;
    }
  );
}

function processFile(filePath) {
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    const originalContent = content;
    
    // Appliquer les corrections
    content = fixFormLabels(content);
    content = fixClickableDivs(content);
    content = fixClickableInputs(content);
    content = fixIframes(content);
    
    // Sauvegarder si modifié
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content, 'utf8');
      return true;
    }
    
    return false;
  } catch (error) {
    console.error(`Erreur lors du traitement de ${filePath}:`, error.message);
    return false;
  }
}

function findHtmlFiles(dir, fileList = []) {
  const files = fs.readdirSync(dir);
  
  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);
    
    if (stat.isDirectory()) {
      findHtmlFiles(filePath, fileList);
    } else if (file.endsWith('.html')) {
      fileList.push(filePath);
    }
  });
  
  return fileList;
}

function main() {
  const frontendPath = path.join('Frontend', 'angular-app', 'src', 'app');
  
  if (!fs.existsSync(frontendPath)) {
    console.error(`Erreur: Le dossier ${frontendPath} n'existe pas`);
    return 1;
  }
  
  const htmlFiles = findHtmlFiles(frontendPath);
  
  console.log(`🔍 Trouvé ${htmlFiles.length} fichiers HTML`);
  console.log('🔧 Correction en cours...\n');
  
  let fixedCount = 0;
  htmlFiles.forEach(file => {
    if (processFile(file)) {
      fixedCount++;
      const relativePath = path.relative(frontendPath, file);
      console.log(`  ✅ ${relativePath}`);
    }
  });
  
  console.log(`\n✅ Terminé ! ${fixedCount}/${htmlFiles.length} fichiers modifiés`);
  return 0;
}

process.exit(main());
