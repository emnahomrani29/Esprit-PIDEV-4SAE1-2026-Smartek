#!/usr/bin/env node

/**
 * Script de test rapide pour le système de routage par rôle
 * Usage: node quick-test.js [role]
 * Exemple: node quick-test.js ADMIN
 */

const roles = {
  ADMIN: {
    path: '/admin/users',
    label: 'Administrateur',
    routes: ['/admin/users', '/admin/companies', '/admin/contracts']
  },
  LEARNER: {
    path: '/learner/courses',
    label: 'Apprenant',
    routes: ['/learner/courses', '/learner/exams', '/learner/certifications', '/learner/participation']
  },
  TRAINER: {
    path: '/trainer/courses',
    label: 'Formateur',
    routes: ['/trainer/courses', '/trainer/training-management', '/trainer/skill-evidence', '/trainer/badge-management']
  },
  RH_SMARTEK: {
    path: '/rh-smartek/certifications',
    label: 'RH SMARTEK',
    routes: ['/rh-smartek/certifications', '/rh-smartek/courses', '/rh-smartek/exams', '/rh-smartek/interviews', '/rh-smartek/schedule', '/rh-smartek/events']
  },
  RH_COMPANY: {
    path: '/rh-company/offers',
    label: 'RH Entreprise',
    routes: ['/rh-company/offers', '/rh-company/participation']
  },
  PARTNER: {
    path: '/partner/sponsorship',
    label: 'Partenaire',
    routes: ['/partner/sponsorship', '/partner/events']
  }
};

const role = process.argv[2]?.toUpperCase();

console.log('\n🧪 SMARTEK - Test de Routage par Rôle\n');

if (!role || !roles[role]) {
  console.log('📋 Rôles disponibles:');
  Object.keys(roles).forEach(r => {
    console.log(`   - ${r} (${roles[r].label})`);
  });
  console.log('\n💡 Usage: node quick-test.js [ROLE]');
  console.log('   Exemple: node quick-test.js ADMIN\n');
  process.exit(0);
}

const roleInfo = roles[role];

console.log(`✅ Test du rôle: ${role} (${roleInfo.label})\n`);
console.log(`📍 Page d'accueil: ${roleInfo.path}`);
console.log(`\n📄 Routes accessibles:`);
roleInfo.routes.forEach(route => {
  console.log(`   ✓ ${route}`);
});

console.log(`\n🔒 Routes interdites (exemples):`);
const otherRoles = Object.keys(roles).filter(r => r !== role);
otherRoles.slice(0, 2).forEach(r => {
  console.log(`   ✗ ${roles[r].path} (${roles[r].label})`);
});

console.log('\n🚀 Pour tester:');
console.log('   1. Démarrez l\'application: npm start');
console.log('   2. Ouvrez: http://localhost:4200/test/roles');
console.log(`   3. Cliquez sur "${role}"`);
console.log(`   4. Vérifiez la redirection vers ${roleInfo.path}\n`);

console.log('📝 Code JavaScript pour la console:');
console.log('─'.repeat(60));
console.log(`localStorage.setItem('token', 'mock-token');
localStorage.setItem('userInfo', JSON.stringify({
  token: 'mock-token',
  userId: 1,
  email: 'test-${role.toLowerCase()}@smartek.com',
  firstName: 'Test ${role}',
  role: '${role}'
}));
location.href = '${roleInfo.path}';`);
console.log('─'.repeat(60));
console.log('');
