export interface RouteConfig {
  path: string;
  label: string;
  icon?: string;
}

export const ROLE_ROUTES: Record<string, RouteConfig[]> = {
  ADMIN: [
    { path: '/admin/users', label: 'User Management', icon: 'people' },
    { path: '/admin/companies', label: 'Company Management', icon: 'business' },
    { path: '/admin/contracts', label: 'Contract Management', icon: 'description' }
  ],
  
  LEARNER: [
    { path: '/learner/courses', label: 'Consult Courses', icon: 'school' },
    { path: '/learner/exams', label: 'Exams Management', icon: 'assignment' },
    { path: '/learner/certifications', label: 'Certification & Badge Management', icon: 'verified' },
    { path: '/learner/participation', label: 'Participation', icon: 'event' }
  ],
  
  TRAINER: [
    { path: '/trainer/courses', label: 'Course Management', icon: 'school' },
    { path: '/trainer/training-management', label: 'Training Management', icon: 'event_note' },
    { path: '/trainer/skill-evidence', label: 'Skill Evidence Management', icon: 'workspace_premium' },
    { path: '/trainer/badge-management', label: 'Badge Management', icon: 'military_tech' }
  ],
  
  RH_SMARTEK: [
    { path: '/rh-smartek/certifications', label: 'Assign Certification & Design', icon: 'verified' },
    { path: '/rh-smartek/courses', label: 'Consult Courses', icon: 'school' },
    { path: '/rh-smartek/exams', label: 'Take an Exam', icon: 'assignment' },
    { path: '/rh-smartek/interviews', label: 'Interview Management', icon: 'record_voice_over' },
    { path: '/rh-smartek/schedule', label: 'Schedule Management', icon: 'calendar_today' },
    { path: '/rh-smartek/events', label: 'Event Management', icon: 'event' }
  ],
  
  RH_COMPANY: [
    { path: '/rh-company/offers', label: 'Offers & Opportunities Management', icon: 'work' },
    { path: '/rh-company/participation', label: 'Participation', icon: 'event' }
  ],
  
  PARTNER: [
    { path: '/partner/sponsorship', label: 'Sponsor Management', icon: 'volunteer_activism' },
    { path: '/partner/events', label: 'Event Management', icon: 'event' }
  ]
};

export function getRoutesForRole(role: string): RouteConfig[] {
  return ROLE_ROUTES[role] || [];
}
