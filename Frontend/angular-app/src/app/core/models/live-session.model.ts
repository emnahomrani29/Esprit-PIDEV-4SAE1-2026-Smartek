/**
 * Enum représentant le mode de livraison d'un cours
 */
export enum DeliveryMode {
  PRESENTIEL = 'PRESENTIEL',
  EN_LIGNE = 'EN_LIGNE'
}

/**
 * Enum représentant le statut d'une session live
 */
export enum SessionStatus {
  SCHEDULED = 'SCHEDULED',   // Planifiée
  ONGOING = 'ONGOING',       // En cours
  COMPLETED = 'COMPLETED',   // Terminée
  CANCELLED = 'CANCELLED'    // Annulée
}

/**
 * Interface pour créer ou modifier une session live
 */
export interface LiveSessionRequest {
  courseId: number;
  title: string;
  description?: string;
  trainerId: number;
  startTime: string; // Format ISO: YYYY-MM-DDTHH:mm:ss
  endTime: string;   // Format ISO: YYYY-MM-DDTHH:mm:ss
  maxParticipants?: number;
  // Le roomId sera généré automatiquement côté backend
}

/**
 * Interface pour la réponse d'une session live
 */
export interface LiveSessionResponse {
  sessionId: number;
  courseId: number;
  courseTitle: string;
  title: string;
  description?: string;
  trainerId: number;
  startTime: string;
  endTime: string;
  roomId: string; // ID unique de la salle de visioconférence
  status: SessionStatus;
  maxParticipants?: number;
  currentParticipants?: number;
  createdAt: string;
  updatedAt: string;
  message?: string;
}

/**
 * Interface pour afficher une session avec des informations calculées
 */
export interface LiveSessionDisplay extends LiveSessionResponse {
  isUpcoming: boolean;
  isOngoing: boolean;
  isPast: boolean;
  timeUntilStart?: string;
  duration?: string;
  statusLabel: string;
  statusColor: string;
}
