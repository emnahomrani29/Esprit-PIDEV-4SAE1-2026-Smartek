export interface Event {
  eventId?: number;
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  location: string;
  physicalCapacity: number;
  onlineCapacity: number;
  physicalRegistered?: number;
  onlineRegistered?: number;
  maxParticipations: number;
  currentParticipations?: number;
  status?: EventStatus;
  mode: EventMode;
  price?: number;
  isPaid?: boolean;
  createdBy?: number;
  isAvailable?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export enum EventStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  FULL = 'FULL',
  ONGOING = 'ONGOING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export enum EventMode {
  PHYSICAL = 'PHYSICAL',
  ONLINE = 'ONLINE',
  HYBRID = 'HYBRID'
}

export interface EventRequest {
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  location: string;
  physicalCapacity?: number;
  onlineCapacity?: number;
  maxParticipations: number;
  mode?: string;
  price?: number;
  isPaid?: boolean;
  createdBy?: number;
}
