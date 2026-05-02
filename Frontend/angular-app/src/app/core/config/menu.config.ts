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
  // Dashboard (visible to all)
  {
    label: 'Dashboard',
    icon: 'dashboard',
    route: '/dashboard',
    permissions: []
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
  }
];
