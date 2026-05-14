export const environment = {
  production: true,
  apiBaseUrl: 'http://localhost:8080',
  apiUrl: 'http://localhost:8080/api',
  authApiUrl: 'http://localhost:8080/api/auth',
  sponsorApiUrl: 'http://localhost:8080/api/v1/sponsors',
  contractApiUrl: 'http://localhost:8080/api/v1/contracts',
  sponsorshipApiUrl: 'http://localhost:8080/api/v1/sponsorships',

  // Services CRUD pour Learner et Trainer (via API Gateway)
  eventApiUrl: 'http://localhost:8080/api/events',
  planningApiUrl: 'http://localhost:8080/api/plannings',
  trainingApiUrl: 'http://localhost:8080/api/trainings',
  courseApiUrl: 'http://localhost:8080/api/courses',
  examApiUrl: 'http://localhost:8080/api/exams'
};
