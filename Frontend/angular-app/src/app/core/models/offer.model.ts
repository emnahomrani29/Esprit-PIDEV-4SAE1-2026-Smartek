export interface Offer {
  id?: number;
  title: string;
  description: string;
  companyName: string;
  location: string;
  contractType: string;
  salary?: string;
  companyId: number;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface OfferRequest {
  title: string;
  description: string;
  companyName: string;
  location: string;
  contractType: string;
  salary?: string;
  companyId: number;
  status?: string;
}
