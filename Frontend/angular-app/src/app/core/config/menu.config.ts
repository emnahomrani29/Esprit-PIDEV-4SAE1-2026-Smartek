import { Permission } from '../enums/permission.enum';
import { Role } from '../enums/role.enum';

export interface MenuItem {
  label: string;
  icon: string;
  route?: string;
  permissions?: Permission[];
  roles?: Role[];
  children?: MenuItem[];
  divider?: boolean;
  header?: string;
}

export const MENU_ITEMS: MenuItem[] = [
  // Dashboard (visible to all dashboard roles)
  {
    label: 'Dashboard',
    icon: 'dashboard',
    route: '/dashboard',
    roles: [Role.ADMIN, Role.RH_COMPANY, Role.RH_SMARTEK]
  },

  // Divider
  {
    label: '',
    icon: '',
    divider: true
  },

  // --- SPONSOR-ONLY SECTION ---
  {
    label: '',
    icon: '',
    header: 'My Sponsoring',
    roles: [Role.SPONSOR]
  },
  {
    label: 'My Dashboard',
    icon: 'analytics',
    route: '/dashboard/sponsor-dashboard',
    roles: [Role.SPONSOR]
  },

  // --- ADMIN-ONLY SECTION ---
  {
    label: '',
    icon: '',
    header: 'Management',
    roles: [Role.ADMIN]
  },
  {
    label: 'Sponsors',
    icon: 'people',
    route: '/dashboard/sponsors',
    roles: [Role.ADMIN]
  },
  {
    label: 'Contracts',
    icon: 'description',
    route: '/dashboard/contracts',
    roles: [Role.ADMIN]
  },
  {
    label: 'Sponsorships',
    icon: 'card_giftcard',
    route: '/dashboard/sponsorships',
    roles: [Role.ADMIN]
  },
  {
    label: 'Pending Approvals',
    icon: 'pending_actions',
    route: '/dashboard/admin-approvals',
    roles: [Role.ADMIN, Role.RH_SMARTEK]
  },

  // Admin : Pédagogie
  {
    label: '',
    icon: '',
    header: 'Pédagogie',
    roles: [Role.ADMIN]
  },
  {
    label: 'Formations',
    icon: 'school',
    route: '/dashboard/training-management',
    roles: [Role.ADMIN]
  },
  {
    label: 'Cours',
    icon: 'menu_book',
    route: '/dashboard/course-management',
    roles: [Role.ADMIN]
  },
  {
    label: 'Examens',
    icon: 'quiz',
    route: '/dashboard/exam-management',
    roles: [Role.ADMIN]
  },
  {
    label: 'Événements',
    icon: 'event',
    route: '/dashboard/event-management',
    roles: [Role.ADMIN]
  },
  {
    label: 'Planning',
    icon: 'calendar_month',
    route: '/dashboard/planning-management',
    roles: [Role.ADMIN]
  },
  {
    label: 'Parcours d\'apprentissage',
    icon: 'route',
    route: '/dashboard/learning-paths',
    roles: [Role.ADMIN]
  },

  // Admin : Certifications & Badges
  {
    label: '',
    icon: '',
    header: 'Certifications',
    roles: [Role.ADMIN]
  },
  {
    label: 'Templates Badges',
    icon: 'military_tech',
    route: '/dashboard/badge-management',
    roles: [Role.ADMIN]
  },
  {
    label: 'Templates Certifications',
    icon: 'workspace_premium',
    route: '/dashboard/certification-management',
    roles: [Role.ADMIN]
  },
  {
    label: 'Certifications Utilisateurs',
    icon: 'verified',
    route: '/dashboard/certifications',
    roles: [Role.ADMIN]
  },

  // Admin : Utilisateurs & RH
  {
    label: '',
    icon: '',
    header: 'Utilisateurs & RH',
    roles: [Role.ADMIN]
  },
  {
    label: 'Gestion Utilisateurs',
    icon: 'manage_accounts',
    route: '/dashboard/users',
    roles: [Role.ADMIN]
  },
  {
    label: 'Skill Validation',
    icon: 'fact_check',
    route: '/dashboard/skill-validation',
    roles: [Role.ADMIN]
  },
  {
    label: 'Analytics Apprenants',
    icon: 'analytics',
    route: '/dashboard/learner-analytics',
    roles: [Role.ADMIN]
  },
  {
    label: 'Employés',
    icon: 'group',
    route: '/dashboard/employees',
    roles: [Role.ADMIN]
  },
  {
    label: 'Offres d\'emploi',
    icon: 'work',
    route: '/dashboard/job-offers',
    roles: [Role.ADMIN]
  },

  // Divider
  {
    label: '',
    icon: '',
    divider: true,
    roles: [Role.ADMIN]
  },

  // Admin Tools
  {
    label: '',
    icon: '',
    header: 'System',
    roles: [Role.ADMIN]
  },
  {
    label: 'Settings',
    icon: 'settings',
    route: '/dashboard/settings',
    roles: [Role.ADMIN]
  },

  // --- RH_COMPANY SECTION ---
  {
    label: '',
    icon: '',
    header: 'HR Management',
    roles: [Role.RH_COMPANY]
  },
  {
    label: 'My Employees',
    icon: 'group',
    route: '/dashboard/employees',
    roles: [Role.RH_COMPANY]
  },
  {
    label: 'Training Enrollments',
    icon: 'school',
    route: '/dashboard/enrollments',
    roles: [Role.RH_COMPANY]
  },
  {
    label: 'Job Offers & Recruitment',
    icon: 'work',
    route: '/dashboard/job-offers',
    roles: [Role.RH_COMPANY]
  },
  {
    label: 'Employee Certifications',
    icon: 'workspace_premium',
    route: '/dashboard/employee-certifications',
    roles: [Role.RH_COMPANY]
  },
  {
    label: 'Skill Evidence',
    icon: 'verified',
    route: '/dashboard/employee-skills',
    roles: [Role.RH_COMPANY]
  },

  // --- RH_SMARTEK SECTION ---
  {
    label: '',
    icon: '',
    header: 'SMARTEK HR',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Utilisateurs',
    icon: 'manage_accounts',
    route: '/rh-smartek/users',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Validation Compétences',
    icon: 'verified',
    route: '/rh-smartek/skill-validation',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Certifications',
    icon: 'workspace_premium',
    route: '/rh-smartek/certifications',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Formations',
    icon: 'school',
    route: '/rh-smartek/training',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Cours',
    icon: 'menu_book',
    route: '/rh-smartek/courses',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Examens',
    icon: 'quiz',
    route: '/rh-smartek/exams',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Entretiens',
    icon: 'record_voice_over',
    route: '/rh-smartek/interviews',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Planning',
    icon: 'calendar_month',
    route: '/rh-smartek/schedule',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Événements',
    icon: 'event',
    route: '/rh-smartek/events',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Offres d\'emploi',
    icon: 'work',
    route: '/rh-smartek/offers',
    roles: [Role.RH_SMARTEK]
  },
  {
    label: 'Approbations',
    icon: 'pending_actions',
    route: '/dashboard/admin-approvals',
    roles: [Role.RH_SMARTEK]
  }
];
