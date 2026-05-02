export interface Sponsor {
  id?: number;
  name: string;
  email: string;
  password?: string;
  phone?: string;
  companyName?: string;
  industry?: string;
  website?: string;
  logoUrl?: string;
  status?: 'ACTIVE' | 'INACTIVE';
}

export interface Contract {
  id?: number;
  contractNumber: string;
  startDate: string;
  endDate: string;
  amount?: number;
  currency?: string;
  description?: string;
  status?: 'DRAFT' | 'ACTIVE' | 'EXPIRING_SOON' | 'EXPIRED' | 'TERMINATED';
  type?: 'COURSE' | 'EVENT' | 'CERTIFICATION' | 'GLOBAL';
  sponsor?: Sponsor;
  sponsorId?: number;
  sponsorName?: string;
  sponsorEmail?: string;
  availableBudget?: number;
  spentBudget?: number;
  reservedBudget?: number;
}

export interface Sponsorship {
  id?: number;
  sponsorshipType?: 'COURSE' | 'EVENT' | 'CERTIFICATION' | 'OFFER';
  amountAllocated?: number;
  startDate?: string;
  endDate?: string;
  visibilityLevel?: 'LOGO' | 'FEATURED' | 'TITLE';
  targetType?: 'COURSE' | 'EVENT' | 'CERTIFICATION' | 'OFFER';
  targetId?: number;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';
  rejectionReason?: string;
  approvedBy?: number;
  approvedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  contract?: Contract;
  contractId?: number;
  contractNumber?: string;
  sponsorId?: number;
  sponsorName?: string;
  sponsorEmail?: string;
}

export interface BudgetSummary {
  contractId?: number;
  contractNumber?: string;
  totalBudget: number;
  spent: number;
  reserved: number;
  available: number;
  usagePercentage: number;
  warningLevel: 'NORMAL' | 'WARNING' | 'CRITICAL';
}

export interface StatusSummary {
  sponsorId: number;
  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
  completedCount: number;
  cancelledCount: number;
}

export interface SponsorDashboard {
  sponsor: Sponsor;
  contracts: Contract[];
  sponsorships: Sponsorship[];
  totalContractAmount: number;
  totalSpent: number;
  remainingBalance: number;
}

export interface CreateSponsorshipRequest {
  contractId: number;
  sponsorshipType: 'COURSE' | 'EVENT' | 'CERTIFICATION' | 'OFFER';
  amountAllocated: number;
  startDate: string;
  endDate: string;
  visibilityLevel: 'LOGO' | 'FEATURED' | 'TITLE';
  targetType: 'COURSE' | 'EVENT' | 'CERTIFICATION' | 'OFFER';
  targetId: number;
}

export interface ApprovalRequest {
  adminId: number;
  reason: string;
}

