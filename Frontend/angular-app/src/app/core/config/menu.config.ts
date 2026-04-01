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
  // Dashboard - Accessible à tous
  {
    label: 'Dashboard',
    icon: 'dashboard',
    route: '/dashboard',
    permissions: []
  },

  // Course Management - RH_SMARTEK
  {
    label: 'Course Management',
    icon: 'school',
    route: '/dashboard/courses',
    permissions: [Permission.COURSES_VIEW, Permission.COURSES_CREATE]
  },

  // My Courses - LEARNER
  {
    label: 'My Courses',
    icon: 'book',
    route: '/dashboard/my-courses',
    roles: [Role.LEARNER],
    permissions: [Permission.COURSES_VIEW]
  },

  // Exam Management - RH_SMARTEK
  {
    label: 'Exam Management',
    icon: 'assignment',
    route: '/dashboard/exams',
    permissions: [Permission.EXAMS_VIEW, Permission.EXAMS_CREATE]
  },

  // My Exams - LEARNER
  {
    label: 'My Exams',
    icon: 'quiz',
    route: '/dashboard/my-exams',
    roles: [Role.LEARNER],
    permissions: [Permission.EXAMS_TAKE]
  },

  // Training Management - TRAINER, RH_SMARTEK
  {
    label: 'Training Management',
    icon: 'fitness_center',
    route: '/dashboard/training',
    permissions: [Permission.TRAINING_VIEW, Permission.TRAINING_CREATE]
  },

  // My Training - LEARNER
  {
    label: 'My Training',
    icon: 'model_training',
    route: '/dashboard/my-training',
    roles: [Role.LEARNER],
    permissions: [Permission.TRAINING_VIEW]
  },

  // Certification & Badge Management - RH_SMARTEK
  {
    label: 'Certifications',
    icon: 'workspace_premium',
    route: '/dashboard/certifications',
    roles: [Role.TRAINER, Role.RH_SMARTEK, Role.ADMIN],
    permissions: [Permission.CERTIFICATIONS_VIEW]
  },
  {
    label: 'Badges',
    icon: 'military_tech',
    route: '/dashboard/badges',
    roles: [Role.TRAINER, Role.RH_SMARTEK, Role.ADMIN],
    permissions: [Permission.BADGES_VIEW]
  },

  // My Certifications - LEARNER
  {
    label: 'My Certifications',
    icon: 'verified',
    route: '/dashboard/my-certifications',
    roles: [Role.LEARNER],
    permissions: [Permission.CERTIFICATIONS_VIEW]
  },
  {
    label: 'My Badges',
    icon: 'verified',
    route: '/dashboard/my-badges',
    roles: [Role.LEARNER],
    permissions: [Permission.BADGES_VIEW]
  },

  // Skill Evidence - LEARNER, TRAINER, RH_SMARTEK
  {
    label: 'Skill Evidence',
    icon: 'psychology',
    route: '/dashboard/skill-evidence',
    permissions: [Permission.SKILL_EVIDENCE_VIEW, Permission.SKILL_EVIDENCE_VIEW_ALL]
  },

  // Interview Management - RH_COMPANY, RH_SMARTEK
  {
    label: 'Interview Management',
    icon: 'event_seat',
    route: '/dashboard/interviews',
    permissions: [Permission.INTERVIEWS_VIEW, Permission.INTERVIEWS_CREATE]
  },

  // Job Offers - RH_COMPANY
  {
    label: 'Job Offers',
    icon: 'work',
    route: '/dashboard/job-offers',
    permissions: [Permission.JOB_OFFERS_VIEW, Permission.JOB_OFFERS_CREATE]
  },

  // Planning/Schedule - TRAINER, RH_COMPANY
  {
    label: 'Planning',
    icon: 'calendar_month',
    route: '/dashboard/planning',
    permissions: [Permission.PLANNING_VIEW, Permission.PLANNING_CREATE]
  },

  // Event Management - TRAINER, RH_COMPANY, RH_SMARTEK
  {
    label: 'Event Management',
    icon: 'event',
    route: '/dashboard/events',
    permissions: [Permission.EVENTS_VIEW, Permission.EVENTS_CREATE]
  },

  // User Management - ADMIN, RH_SMARTEK, TRAINER
  {
    label: 'User Management',
    icon: 'people',
    route: '/dashboard/users',
    permissions: [Permission.USERS_VIEW]
  },

  // Company Management - ADMIN, RH_SMARTEK
  {
    label: 'Company Management',
    icon: 'business',
    route: '/dashboard/companies',
    permissions: [Permission.COMPANIES_VIEW, Permission.COMPANIES_CREATE]
  },

  // Sponsor Management - ADMIN
  {
    label: 'Sponsor Management',
    icon: 'handshake',
    route: '/dashboard/sponsors',
    permissions: [Permission.SPONSORS_VIEW]
  },

  // Contact Management - ADMIN, RH_SMARTEK
  {
    label: 'Contact Management',
    icon: 'contacts',
    route: '/dashboard/contacts',
    permissions: [Permission.CONTACTS_VIEW]
  },

  // Participation - ADMIN, RH_SMARTEK, SPONSOR
  {
    label: 'Participation',
    icon: 'groups',
    route: '/dashboard/participation',
    permissions: [Permission.PARTICIPATION_VIEW, Permission.PARTICIPATION_VIEW_ALL]
  },

  // Learning Path - RH_SMARTEK, LEARNER
  {
    label: 'Learning Paths',
    icon: 'route',
    route: '/dashboard/learning-paths',
    permissions: [Permission.LEARNING_PATH_VIEW]
  },

  // Divider
  {
    label: '',
    icon: '',
    divider: true
  },

  // Header for Settings
  {
    label: '',
    icon: '',
    header: 'Settings'
  },

  // System Settings - ADMIN
  {
    label: 'System Settings',
    icon: 'settings',
    route: '/dashboard/settings',
    permissions: [Permission.SYSTEM_SETTINGS]
  },

  // Profile - Tous
  {
    label: 'Profile',
    icon: 'person',
    route: '/dashboard/profile',
    permissions: [Permission.PROFILE_VIEW]
  },

  // Divider
  {
    label: '',
    icon: '',
    divider: true
  },

  // Header for Pages
  {
    label: '',
    icon: '',
    header: 'Pages'
  },

  // Back to Website - Tous
  {
    label: 'Back to Website',
    icon: 'home',
    route: '/',
    permissions: []
  }
];
