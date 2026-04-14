'use strict';

const PDFDocument = require('pdfkit');
const fs = require('fs');
const path = require('path');

const OUTPUT_FILE = path.join(__dirname, 'SMARTEK-Rapport-Technique.pdf');

// ── Colors & fonts ──────────────────────────────────────────────────────────
const DARK_BLUE   = '#1a3a5c';
const MID_BLUE    = '#2563a8';
const LIGHT_GRAY  = '#f0f4f8';
const BORDER_GRAY = '#c8d6e5';
const TEXT_DARK   = '#1e293b';
const TEXT_MID    = '#475569';
const WHITE       = '#ffffff';
const ACCENT      = '#e8f0fb';

const doc = new PDFDocument({
  size: 'A4',
  margins: { top: 60, bottom: 60, left: 55, right: 55 },
  info: {
    Title:   'SMARTEK - Rapport Technique',
    Author:  'SMARTEK Team - ESPRIT 4SAE1',
    Subject: 'Plateforme Microservices de Gestion de Formation et Emploi',
    Creator: 'generate-report.js / PDFKit',
  },
  autoFirstPage: false,
});

doc.pipe(fs.createWriteStream(OUTPUT_FILE));

// ── Helpers ──────────────────────────────────────────────────────────────────
const W  = doc.page ? doc.page.width  : 595.28;   // A4 width
const H  = doc.page ? doc.page.height : 841.89;   // A4 height
const ML = 55;   // margin left
const MR = 55;   // margin right
const CW = 595.28 - ML - MR;  // content width

let pageNumber = 0;

function addPage() {
  doc.addPage();
  pageNumber++;
  if (pageNumber > 1) drawPageChrome();
}

function drawPageChrome() {
  const pw = doc.page.width;
  // Header bar
  doc.rect(0, 0, pw, 38).fill(DARK_BLUE);
  doc.fontSize(10).fillColor(WHITE).font('Helvetica-Bold')
     .text('SMARTEK', ML, 13, { width: pw - ML - MR, align: 'left' });
  doc.fontSize(9).fillColor('#a8c4e0').font('Helvetica')
     .text('Rapport Technique — Avril 2026', ML, 13, { width: pw - ML - MR, align: 'right' });

  // Footer
  doc.rect(0, doc.page.height - 32, pw, 32).fill(DARK_BLUE);
  doc.fontSize(8).fillColor(WHITE).font('Helvetica')
     .text(`Page ${pageNumber}`, ML, doc.page.height - 20, { width: pw - ML - MR, align: 'center' });
}

function sectionTitle(num, title) {
  doc.moveDown(0.6);
  const y = doc.y;
  doc.rect(ML, y, CW, 28).fill(DARK_BLUE);
  doc.fontSize(13).fillColor(WHITE).font('Helvetica-Bold')
     .text(`${num}.  ${title}`, ML + 10, y + 7, { width: CW - 20 });
  doc.moveDown(0.2);
  doc.y = y + 36;
}

function subTitle(text) {
  doc.moveDown(0.4);
  doc.fontSize(11).fillColor(MID_BLUE).font('Helvetica-Bold').text(text, ML);
  doc.moveDown(0.15);
}

function bodyText(text, opts = {}) {
  doc.fontSize(10).fillColor(TEXT_DARK).font('Helvetica')
     .text(text, ML, doc.y, { width: CW, align: 'justify', ...opts });
  doc.moveDown(0.3);
}

function bullet(text) {
  const bx = ML + 10;
  const bw = CW - 10;
  doc.fontSize(10).fillColor(TEXT_DARK).font('Helvetica')
     .text(`•  ${text}`, bx, doc.y, { width: bw });
}

function infoBox(lines) {
  const padding = 10;
  const lineH   = 16;
  const boxH    = lines.length * lineH + padding * 2;
  const y       = doc.y;
  doc.rect(ML, y, CW, boxH).fill(LIGHT_GRAY).stroke(BORDER_GRAY);
  lines.forEach((line, i) => {
    doc.fontSize(9.5).fillColor(TEXT_DARK).font('Helvetica')
       .text(line, ML + padding, y + padding + i * lineH, { width: CW - padding * 2 });
  });
  doc.y = y + boxH + 6;
}

function tableRow(cols, widths, isHeader = false) {
  const rowH   = isHeader ? 22 : 18;
  const y      = doc.y;
  let x        = ML;
  const bg     = isHeader ? DARK_BLUE : (doc._tableRowAlt ? LIGHT_GRAY : WHITE);
  doc._tableRowAlt = !doc._tableRowAlt;

  doc.rect(ML, y, CW, rowH).fill(bg).stroke(BORDER_GRAY);

  cols.forEach((col, i) => {
    const w = widths[i];
    doc.fontSize(isHeader ? 9 : 9).fillColor(isHeader ? WHITE : TEXT_DARK)
       .font(isHeader ? 'Helvetica-Bold' : 'Helvetica')
       .text(col, x + 5, y + (rowH - 9) / 2, { width: w - 10, lineBreak: false });
    x += w;
  });
  doc.y = y + rowH;
}

function checkPageBreak(needed = 80) {
  if (doc.y + needed > doc.page.height - 80) addPage();
}

// ════════════════════════════════════════════════════════════════════════════
// COVER PAGE
// ════════════════════════════════════════════════════════════════════════════
doc.addPage();
pageNumber++;

const pw = doc.page.width;
const ph = doc.page.height;

// Full-page dark blue background
doc.rect(0, 0, pw, ph).fill(DARK_BLUE);

// Decorative accent strip
doc.rect(0, ph * 0.55, pw, 6).fill(MID_BLUE);
doc.rect(0, ph * 0.55 + 8, pw, 2).fill('#4a90d9');

// Logo / brand block
doc.rect(ML, 80, 6, 60).fill('#4a90d9');
doc.fontSize(42).fillColor(WHITE).font('Helvetica-Bold')
   .text('SMARTEK', ML + 20, 82, { width: CW });

doc.fontSize(13).fillColor('#a8c4e0').font('Helvetica')
   .text('Plateforme Microservices de Gestion de Formation et Emploi', ML + 20, 132, { width: CW - 20 });

// Main title box
doc.rect(ML, 200, CW, 90).fill('#0f2540');
doc.fontSize(22).fillColor(WHITE).font('Helvetica-Bold')
   .text('Rapport Technique', ML + 20, 220, { width: CW - 40, align: 'center' });
doc.fontSize(12).fillColor('#a8c4e0').font('Helvetica')
   .text('Architecture · Sécurité · Déploiement', ML + 20, 252, { width: CW - 40, align: 'center' });

// Info grid
const infoY = 330;
const col1W = CW / 2 - 5;
const col2W = CW / 2 - 5;

[[ML, 'Équipe', 'SMARTEK Team'], [ML + col1W + 10, 'Promotion', 'ESPRIT - 4SAE1']].forEach(([x, label, val]) => {
  doc.rect(x, infoY, col1W, 55).fill('#0f2540');
  doc.fontSize(9).fillColor('#a8c4e0').font('Helvetica').text(label, x + 12, infoY + 10, { width: col1W - 20 });
  doc.fontSize(13).fillColor(WHITE).font('Helvetica-Bold').text(val, x + 12, infoY + 24, { width: col1W - 20 });
});

doc.rect(ML, infoY + 65, CW, 55).fill('#0f2540');
doc.fontSize(9).fillColor('#a8c4e0').font('Helvetica').text('Date', ML + 12, infoY + 75, { width: CW - 20 });
doc.fontSize(13).fillColor(WHITE).font('Helvetica-Bold').text('Avril 2026', ML + 12, infoY + 89, { width: CW - 20 });

// Bottom bar
doc.rect(0, ph - 50, pw, 50).fill('#0f2540');
doc.fontSize(9).fillColor('#a8c4e0').font('Helvetica')
   .text('Document confidentiel — Usage interne ESPRIT', ML, ph - 30, { width: pw - ML * 2, align: 'center' });

// ════════════════════════════════════════════════════════════════════════════
// TABLE OF CONTENTS
// ════════════════════════════════════════════════════════════════════════════
addPage();

// TOC header
doc.rect(ML, 55, CW, 36).fill(DARK_BLUE);
doc.fontSize(16).fillColor(WHITE).font('Helvetica-Bold')
   .text('Table des Matières', ML + 12, 65, { width: CW - 24 });

doc.y = 105;

const tocEntries = [
  ['1', 'Présentation du Projet',          '3'],
  ['2', 'Architecture Technique',           '4'],
  ['3', 'Stack Technologique',              '5'],
  ['4', 'Sécurité',                         '6'],
  ['5', 'Fonctionnalités par Service',      '7'],
  ['6', 'Base de Données',                  '9'],
  ['7', 'Communication Inter-Services',    '10'],
  ['8', 'Déploiement',                     '11'],
];

tocEntries.forEach(([num, title, pg], idx) => {
  const ty = doc.y;
  const bg = idx % 2 === 0 ? LIGHT_GRAY : WHITE;
  doc.rect(ML, ty, CW, 22).fill(bg);
  doc.fontSize(10).fillColor(DARK_BLUE).font('Helvetica-Bold')
     .text(`${num}.`, ML + 8, ty + 6, { width: 20 });
  doc.fontSize(10).fillColor(TEXT_DARK).font('Helvetica')
     .text(title, ML + 30, ty + 6, { width: CW - 80 });
  doc.fontSize(10).fillColor(MID_BLUE).font('Helvetica-Bold')
     .text(pg, ML, ty + 6, { width: CW - 8, align: 'right' });
  doc.y = ty + 22;
});

// ════════════════════════════════════════════════════════════════════════════
// SECTION 1 — Présentation du Projet
// ════════════════════════════════════════════════════════════════════════════
addPage();
sectionTitle('1', 'Présentation du Projet');

bodyText(
  'SMARTEK est une plateforme complète de gestion de formation et d\'emploi, conçue selon une architecture microservices moderne. ' +
  'Elle répond aux besoins des établissements d\'enseignement, des entreprises et des apprenants en centralisant la gestion des formations, ' +
  'des offres d\'emploi, des examens et des certifications au sein d\'un écosystème unifié et scalable.'
);

bodyText(
  'Développée dans le cadre du projet de fin d\'études à ESPRIT par l\'équipe 4SAE1, SMARTEK adopte les meilleures pratiques ' +
  'du développement logiciel moderne : séparation des responsabilités, déploiement conteneurisé, sécurité JWT stateless, ' +
  'et une interface utilisateur réactive construite avec Angular 18.'
);

subTitle('Objectifs principaux');
[
  'Centraliser la gestion des formations, cours et certifications pour les apprenants et formateurs.',
  'Faciliter la mise en relation entre candidats et entreprises via un module d\'offres d\'emploi intégré.',
  'Automatiser la délivrance de badges et certifications à l\'issue des examens réussis.',
  'Offrir une expérience utilisateur fluide grâce à une interface Angular moderne et responsive.',
  'Garantir la sécurité des données et des accès via JWT, BCrypt et un contrôle d\'accès basé sur les rôles (RBAC).',
  'Assurer la scalabilité et la résilience grâce à une architecture microservices orchestrée par Docker Compose.',
].forEach(b => bullet(b));

doc.moveDown(0.5);
subTitle('Périmètre fonctionnel');
infoBox([
  '  Gestion des utilisateurs et authentification (Auth Service)',
  '  Gestion des événements et planification (Event & Planning Services)',
  '  Gestion des formations et cours (Training & Course Services)',
  '  Offres d\'emploi et candidatures (Offers Service)',
  '  Examens et évaluations (Exam Service)',
  '  Certifications et badges numériques (Certification Badge Service)',
]);

// ════════════════════════════════════════════════════════════════════════════
// SECTION 2 — Architecture Technique
// ════════════════════════════════════════════════════════════════════════════
addPage();
sectionTitle('2', 'Architecture Technique');

bodyText(
  'L\'architecture de SMARTEK repose sur un ensemble de microservices indépendants, chacun responsable d\'un domaine métier précis. ' +
  'Les services communiquent via l\'API Gateway, qui assure le routage, la sécurité JWT et l\'équilibrage de charge. ' +
  'La découverte de services est gérée par Netflix Eureka, et la configuration centralisée par Spring Cloud Config Server.'
);

doc.moveDown(0.3);
subTitle('Cartographie des microservices');

// Table header
doc._tableRowAlt = false;
tableRow(['Service', 'Port', 'Base de données', 'Rôle'], [160, 55, 120, CW - 160 - 55 - 120], true);

const services = [
  ['Eureka Server',              '8761', '—',                  'Service Discovery & Registry'],
  ['Config Server',              '8888', '—',                  'Configuration centralisée'],
  ['API Gateway',                '8090', '—',                  'Routage et sécurité JWT'],
  ['Auth Service',               '8081', 'smartek_db',         'Authentification et gestion utilisateurs'],
  ['Event Service',              '8082', 'smartek_events',     'Gestion des événements'],
  ['Planning Service',           '8083', 'smartek_planning',   'Planification des sessions'],
  ['Training Service',           '8084', 'training_db',        'Gestion des formations'],
  ['Offers Service',             '8085', 'offers_db',          'Offres d\'emploi et candidatures'],
  ['Course Service',             '8086', 'course_db',          'Gestion des cours et contenus'],
  ['Exam Service',               '8087', 'exam_db',            'Gestion des examens'],
  ['Certification Badge Service','—',    'smartek_db',         'Certifications et badges numériques'],
  ['Frontend Angular',           '4200', '—',                  'Interface utilisateur SPA'],
];

services.forEach(row => {
  checkPageBreak(22);
  tableRow(row, [160, 55, 120, CW - 160 - 55 - 120]);
});

doc.moveDown(0.6);
subTitle('Flux de communication');
bodyText(
  'Chaque requête cliente transite par l\'API Gateway (port 8090), qui valide le token JWT avant de router la requête ' +
  'vers le microservice cible. Les services s\'enregistrent automatiquement auprès d\'Eureka Server au démarrage, ' +
  'permettant une découverte dynamique et un équilibrage de charge transparent via Spring Cloud LoadBalancer.'
);

// ════════════════════════════════════════════════════════════════════════════
// SECTION 3 — Stack Technologique
// ════════════════════════════════════════════════════════════════════════════
addPage();
sectionTitle('3', 'Stack Technologique');

subTitle('Backend');
doc._tableRowAlt = false;
tableRow(['Technologie', 'Version', 'Usage'], [160, 100, CW - 260], true);
[
  ['Spring Boot',          '3.2.0',        'Framework principal des microservices'],
  ['Java',                 '17 (LTS)',      'Langage de développement backend'],
  ['Spring Cloud',         '2023.0.0',      'Eureka, Config, Gateway, Feign'],
  ['Maven',                '3.x',           'Gestion des dépendances et build'],
  ['MySQL',                '8.0',           'Base de données relationnelle'],
  ['Netflix Eureka',       '—',             'Service Discovery'],
  ['Spring Cloud Gateway', '—',             'API Gateway et routage'],
  ['JJWT',                 '—',             'Génération et validation des tokens JWT'],
  ['Spring Security BCrypt','—',            'Hachage sécurisé des mots de passe'],
  ['Spring Data JPA',      '—',             'ORM et accès aux données'],
].forEach(r => { checkPageBreak(20); tableRow(r, [160, 100, CW - 260]); });

doc.moveDown(0.5);
subTitle('Frontend');
doc._tableRowAlt = false;
tableRow(['Technologie', 'Version', 'Usage'], [160, 100, CW - 260], true);
[
  ['Angular',       '18.2.0',   'Framework SPA principal'],
  ['TypeScript',    '5.5.2',    'Langage de développement frontend'],
  ['Tailwind CSS',  '3.4.19',   'Framework CSS utilitaire'],
  ['Angular CLI',   '18.2.21',  'Outillage de développement Angular'],
  ['jsPDF',         '—',        'Génération de PDF côté client'],
  ['html2canvas',   '—',        'Capture de composants en image'],
  ['RxJS',          '—',        'Programmation réactive et gestion des flux'],
].forEach(r => { checkPageBreak(20); tableRow(r, [160, 100, CW - 260]); });

doc.moveDown(0.5);
subTitle('Infrastructure & DevOps');
doc._tableRowAlt = false;
tableRow(['Technologie', 'Usage'], [180, CW - 180], true);
[
  ['Docker',         'Conteneurisation de chaque microservice'],
  ['Docker Compose', 'Orchestration locale de l\'ensemble des services'],
  ['MySQL 8.0',      'Instances de bases de données isolées par service'],
].forEach(r => { checkPageBreak(20); tableRow(r, [180, CW - 180]); });

// ════════════════════════════════════════════════════════════════════════════
// SECTION 4 — Sécurité
// ════════════════════════════════════════════════════════════════════════════
addPage();
sectionTitle('4', 'Sécurité');

bodyText(
  'La sécurité de SMARTEK est assurée par une approche multicouche combinant authentification stateless JWT, ' +
  'hachage des mots de passe BCrypt, contrôle d\'accès basé sur les rôles (RBAC) et configuration CORS stricte.'
);

subTitle('Authentification JWT Stateless');
[
  'Chaque utilisateur authentifié reçoit un token JWT signé par l\'Auth Service.',
  'Le token est transmis dans l\'en-tête Authorization (Bearer) à chaque requête.',
  'L\'API Gateway valide le token avant de router la requête vers le service cible.',
  'Aucune session serveur n\'est maintenue — architecture 100 % stateless.',
  'Expiration configurable des tokens avec mécanisme de refresh.',
].forEach(b => bullet(b));

doc.moveDown(0.4);
subTitle('Hachage des mots de passe');
bodyText(
  'Tous les mots de passe sont hachés avec BCrypt (Spring Security) avant stockage en base de données. ' +
  'Le facteur de coût BCrypt est configuré pour résister aux attaques par force brute tout en maintenant ' +
  'des performances acceptables lors de l\'authentification.'
);

subTitle('Contrôle d\'accès basé sur les rôles (RBAC)');
bodyText('SMARTEK définit six rôles distincts avec des permissions granulaires :');

doc._tableRowAlt = false;
tableRow(['Rôle', 'Description', 'Accès principaux'], [130, 180, CW - 310], true);
[
  ['LEARNER',      'Apprenant inscrit',          'Formations, cours, examens, certifications'],
  ['ADMIN',        'Administrateur système',      'Accès complet à tous les services'],
  ['TRAINER',      'Formateur / Instructeur',     'Gestion des formations et cours'],
  ['RH_SMARTEK',   'RH interne SMARTEK',          'Gestion des utilisateurs et planification'],
  ['RH_COMPANY',   'RH d\'une entreprise cliente', 'Offres d\'emploi, candidatures'],
  ['PARTNER',      'Partenaire externe',          'Accès limité aux événements et offres'],
].forEach(r => { checkPageBreak(20); tableRow(r, [130, 180, CW - 310]); });

doc.moveDown(0.5);
subTitle('Configuration CORS');
bodyText(
  'Chaque microservice est configuré avec des règles CORS strictes, autorisant uniquement les origines ' +
  'connues (frontend Angular sur le port 4200 en développement, domaine de production en déploiement). ' +
  'L\'API Gateway centralise également la gestion CORS pour les requêtes cross-origin.'
);

// ════════════════════════════════════════════════════════════════════════════
// SECTION 5 — Fonctionnalités par Service
// ════════════════════════════════════════════════════════════════════════════
addPage();
sectionTitle('5', 'Fonctionnalités par Service');

const serviceFeatures = [
  {
    name: 'Auth Service (8081)',
    features: [
      'Inscription et connexion des utilisateurs avec validation des données.',
      'Génération et validation des tokens JWT.',
      'Gestion des rôles et permissions (RBAC).',
      'Mise à jour du profil utilisateur et changement de mot de passe.',
      'Réinitialisation de mot de passe par email.',
    ],
  },
  {
    name: 'Event Service (8082)',
    features: [
      'Création et gestion des événements (conférences, ateliers, webinaires).',
      'Inscription des participants aux événements.',
      'Notifications automatiques aux inscrits.',
      'Gestion du calendrier des événements.',
    ],
  },
  {
    name: 'Planning Service (8083)',
    features: [
      'Planification des sessions de formation.',
      'Gestion des créneaux horaires et des salles.',
      'Synchronisation avec les formateurs et apprenants.',
      'Export du planning en formats PDF et iCal.',
    ],
  },
  {
    name: 'Training Service (8084)',
    features: [
      'Création et gestion des formations (titre, description, durée, niveau).',
      'Inscription des apprenants aux formations.',
      'Suivi de la progression des apprenants.',
      'Gestion des prérequis et des parcours de formation.',
    ],
  },
  {
    name: 'Offers Service (8085)',
    features: [
      'Publication d\'offres d\'emploi par les entreprises partenaires.',
      'Candidature en ligne avec dépôt de CV.',
      'Suivi du statut des candidatures (en attente, acceptée, refusée).',
      'Matching automatique entre profils apprenants et offres.',
      'Notifications aux candidats lors des changements de statut.',
    ],
  },
  {
    name: 'Course Service (8086)',
    features: [
      'Gestion des cours avec support de contenus multimédias (PDF, vidéo).',
      'Organisation des cours en modules et chapitres.',
      'Upload et stockage sécurisé des ressources pédagogiques.',
      'Suivi de la complétion des cours par les apprenants.',
    ],
  },
  {
    name: 'Exam Service (8087)',
    features: [
      'Création d\'examens avec questions à choix multiples et questions ouvertes.',
      'Passage d\'examens en ligne avec minuterie.',
      'Correction automatique des QCM.',
      'Génération des résultats et relevés de notes.',
      'Déclenchement automatique de la certification en cas de réussite.',
    ],
  },
  {
    name: 'Certification Badge Service',
    features: [
      'Délivrance automatique de badges numériques après réussite d\'un examen.',
      'Génération de certificats PDF personnalisés.',
      'Vérification de l\'authenticité des certifications via un identifiant unique.',
      'Partage des badges sur les réseaux professionnels.',
      'Historique complet des certifications par apprenant.',
    ],
  },
];

serviceFeatures.forEach(svc => {
  checkPageBreak(60);
  doc.moveDown(0.3);
  // Service name box
  const sy = doc.y;
  doc.rect(ML, sy, CW, 22).fill(ACCENT).stroke(BORDER_GRAY);
  doc.fontSize(10.5).fillColor(DARK_BLUE).font('Helvetica-Bold')
     .text(svc.name, ML + 8, sy + 6, { width: CW - 16 });
  doc.y = sy + 26;
  svc.features.forEach(f => bullet(f));
  doc.moveDown(0.2);
});

// ════════════════════════════════════════════════════════════════════════════
// SECTION 6 — Base de Données
// ════════════════════════════════════════════════════════════════════════════
addPage();
sectionTitle('6', 'Base de Données');

bodyText(
  'SMARTEK adopte le principe de base de données par service (Database per Service pattern). ' +
  'Chaque microservice possède sa propre instance de base de données MySQL 8.0, garantissant ' +
  'l\'isolation des données, l\'indépendance des schémas et la scalabilité individuelle de chaque service.'
);

subTitle('Schéma des bases de données');
doc._tableRowAlt = false;
tableRow(['Base de données', 'Service propriétaire', 'Tables principales'], [140, 160, CW - 300], true);
[
  ['smartek_db',       'Auth Service / Cert. Badge', 'users, roles, user_roles, badges, certifications'],
  ['smartek_events',   'Event Service',              'events, event_registrations, notifications'],
  ['smartek_planning', 'Planning Service',           'sessions, time_slots, rooms, assignments'],
  ['training_db',      'Training Service',           'trainings, enrollments, progress, prerequisites'],
  ['offers_db',        'Offers Service',             'job_offers, applications, companies, cv_files'],
  ['course_db',        'Course Service',             'courses, modules, chapters, resources, completions'],
  ['exam_db',          'Exam Service',               'exams, questions, answers, attempts, results'],
].forEach(r => { checkPageBreak(20); tableRow(r, [140, 160, CW - 300]); });

doc.moveDown(0.5);
subTitle('Principes de conception');
[
  'Isolation complète : aucune jointure cross-service au niveau base de données.',
  'Cohérence éventuelle : les données partagées sont synchronisées via appels Feign inter-services.',
  'Migrations gérées par Flyway (Certification Badge Service) et Spring JPA auto-DDL pour les autres.',
  'Indexation optimisée sur les clés étrangères et colonnes de recherche fréquentes.',
  'Données sensibles (mots de passe) jamais stockées en clair — BCrypt systématique.',
].forEach(b => bullet(b));

// ════════════════════════════════════════════════════════════════════════════
// SECTION 7 — Communication Inter-Services
// ════════════════════════════════════════════════════════════════════════════
checkPageBreak(120);
doc.moveDown(0.5);
sectionTitle('7', 'Communication Inter-Services');

bodyText(
  'La communication entre microservices SMARTEK repose sur trois mécanismes complémentaires : ' +
  'la découverte de services via Eureka, les appels REST synchrones via Feign Clients, ' +
  'et l\'équilibrage de charge automatique via Spring Cloud LoadBalancer.'
);

subTitle('Netflix Eureka — Service Discovery');
[
  'Chaque microservice s\'enregistre auprès d\'Eureka Server (port 8761) au démarrage.',
  'Eureka maintient un registre dynamique de toutes les instances disponibles.',
  'En cas de défaillance d\'une instance, Eureka la retire automatiquement du registre.',
  'Le tableau de bord Eureka (http://localhost:8761) offre une vue en temps réel de l\'état des services.',
].forEach(b => bullet(b));

doc.moveDown(0.4);
subTitle('Feign Clients — Appels REST déclaratifs');
bodyText(
  'Spring Cloud OpenFeign permet de définir des clients HTTP de manière déclarative via des interfaces annotées. ' +
  'Les services SMARTEK utilisent Feign pour les communications synchrones inter-services, ' +
  'notamment entre l\'Exam Service et le Certification Badge Service pour la délivrance automatique de badges.'
);

infoBox([
  '  Exemple : ExamService → CertificationBadgeService',
  '  Après validation d\'un examen, l\'Exam Service appelle le Certification Badge Service',
  '  via un Feign Client pour déclencher la génération automatique du badge et du certificat.',
]);

doc.moveDown(0.3);
subTitle('Load Balancing');
bodyText(
  'Spring Cloud LoadBalancer assure la distribution des requêtes entre les instances d\'un même service. ' +
  'En combinaison avec Eureka, il permet un équilibrage de charge côté client, ' +
  'améliorant la résilience et les performances de la plateforme.'
);

// ════════════════════════════════════════════════════════════════════════════
// SECTION 8 — Déploiement
// ════════════════════════════════════════════════════════════════════════════
addPage();
sectionTitle('8', 'Déploiement');

bodyText(
  'SMARTEK est entièrement conteneurisé avec Docker. Chaque microservice dispose de son propre Dockerfile, ' +
  'et l\'ensemble de la plateforme est orchestré via Docker Compose pour un déploiement en une seule commande.'
);

subTitle('Docker Compose — Orchestration');
[
  'Un fichier docker-compose.yml unique orchestre l\'ensemble des services et bases de données.',
  'Les services démarrent dans l\'ordre correct grâce aux dépendances (depends_on).',
  'Les variables d\'environnement sont externalisées pour faciliter la configuration par environnement.',
  'Les volumes Docker persistent les données MySQL entre les redémarrages.',
  'Un réseau Docker dédié (smartek-network) isole les communications inter-services.',
].forEach(b => bullet(b));

doc.moveDown(0.4);
subTitle('Mapping des ports exposés');
doc._tableRowAlt = false;
tableRow(['Service', 'Port hôte', 'Port conteneur', 'Protocole'], [180, 90, 110, CW - 380], true);
[
  ['Eureka Server',              '8761', '8761', 'HTTP'],
  ['Config Server',              '8888', '8888', 'HTTP'],
  ['API Gateway',                '8090', '8090', 'HTTP / HTTPS'],
  ['Auth Service',               '8081', '8081', 'HTTP'],
  ['Event Service',              '8082', '8082', 'HTTP'],
  ['Planning Service',           '8083', '8083', 'HTTP'],
  ['Training Service',           '8084', '8084', 'HTTP'],
  ['Offers Service',             '8085', '8085', 'HTTP'],
  ['Course Service',             '8086', '8086', 'HTTP'],
  ['Exam Service',               '8087', '8087', 'HTTP'],
  ['Frontend Angular',           '4200', '4200', 'HTTP'],
  ['MySQL (Auth/Cert)',           '3306', '3306', 'TCP'],
].forEach(r => { checkPageBreak(20); tableRow(r, [180, 90, 110, CW - 380]); });

doc.moveDown(0.5);
subTitle('Commandes de déploiement');
infoBox([
  '  # Construire et démarrer tous les services',
  '  docker-compose up --build -d',
  '',
  '  # Vérifier l\'état des conteneurs',
  '  docker-compose ps',
  '',
  '  # Consulter les logs d\'un service',
  '  docker-compose logs -f auth-service',
  '',
  '  # Arrêter tous les services',
  '  docker-compose down',
]);

doc.moveDown(0.4);
subTitle('Ordre de démarrage recommandé');
[
  '1. MySQL databases — toutes les instances de bases de données.',
  '2. Config Server (8888) — doit être disponible avant les autres services.',
  '3. Eureka Server (8761) — registre de services.',
  '4. Microservices métier (Auth, Event, Planning, Training, Offers, Course, Exam, Certification).',
  '5. API Gateway (8090) — après enregistrement des services dans Eureka.',
  '6. Frontend Angular (4200) — interface utilisateur.',
].forEach(b => bullet(b));

// ════════════════════════════════════════════════════════════════════════════
// FINAL PAGE — Conclusion
// ════════════════════════════════════════════════════════════════════════════
addPage();

doc.rect(ML, 55, CW, 36).fill(DARK_BLUE);
doc.fontSize(16).fillColor(WHITE).font('Helvetica-Bold')
   .text('Conclusion', ML + 12, 65, { width: CW - 24 });
doc.y = 105;

bodyText(
  'SMARTEK représente une solution complète et moderne pour la gestion de la formation et de l\'emploi. ' +
  'Son architecture microservices garantit la scalabilité, la résilience et la maintenabilité à long terme. ' +
  'La combinaison de Spring Boot 3.2, Angular 18 et Docker Compose offre un socle technologique robuste ' +
  'et aligné avec les standards de l\'industrie.'
);

bodyText(
  'La sécurité multicouche (JWT, BCrypt, RBAC, CORS) assure la protection des données sensibles des utilisateurs. ' +
  'La séparation des responsabilités entre microservices permet des évolutions indépendantes et des déploiements ' +
  'sans interruption de service.'
);

bodyText(
  'Ce projet démontre la maîtrise des technologies cloud-native et des architectures distribuées par l\'équipe ' +
  'SMARTEK de la promotion 4SAE1 d\'ESPRIT, constituant une base solide pour un déploiement en production.'
);

doc.moveDown(1);
// Signature block
const sigY = doc.y;
doc.rect(ML, sigY, CW, 70).fill(LIGHT_GRAY).stroke(BORDER_GRAY);
doc.fontSize(10).fillColor(DARK_BLUE).font('Helvetica-Bold')
   .text('SMARTEK Team — ESPRIT 4SAE1', ML + 15, sigY + 12, { width: CW - 30 });
doc.fontSize(9).fillColor(TEXT_MID).font('Helvetica')
   .text('Plateforme Microservices de Gestion de Formation et Emploi', ML + 15, sigY + 28, { width: CW - 30 });
doc.fontSize(9).fillColor(TEXT_MID).font('Helvetica')
   .text('Avril 2026', ML + 15, sigY + 44, { width: CW - 30 });

// ── Finalize ─────────────────────────────────────────────────────────────────
doc.end();

doc.on('end', () => {
  console.log(`\n✅  PDF généré avec succès : ${OUTPUT_FILE}\n`);
});
