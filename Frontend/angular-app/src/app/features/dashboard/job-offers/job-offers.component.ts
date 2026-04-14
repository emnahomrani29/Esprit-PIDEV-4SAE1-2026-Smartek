import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OfferService } from '../../../core/services/offer.service';
import { ApplicationService } from '../../../core/services/application.service';
import { InterviewService } from '../../../core/services/interview.service';
import { AuthService } from '../../../core/services/auth.service';
import { Offer, OfferRequest } from '../../../core/models/offer.model';
import { Application } from '../../../core/models/application.model';
import { Interview, InterviewRequest } from '../../../core/models/interview.model';

@Component({
  selector: 'app-job-offers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './job-offers.component.html',
  styleUrls: ['./job-offers.component.css']
})
export class JobOffersComponent implements OnInit {
  offers: Offer[] = [];
  filteredOffers: Offer[] = [];
  showModal = false;
  showApplicationsModal = false;
  showInterviewModal = false;
  isEditMode = false;
  selectedOffer: Offer | null = null;
  selectedApplication: Application | null = null;
  applications: Application[] = [];
  loadingApplications = false;
  
  interviewForm: InterviewRequest = {
    applicationId: 0,
    interviewDate: '',
    location: '',
    meetingLink: '',
    notes: '',
    createdBy: 1 // TODO: Get from auth service
  };
  
  offerForm: OfferRequest = {
    title: '',
    description: '',
    companyName: '',
    location: '',
    contractType: 'CDI',
    salary: '',
    companyId: 1, // TODO: Get from auth service
    status: 'ACTIVE'
  };

  contractTypes = ['CDI', 'CDD', 'Stage', 'Alternance', 'Freelance'];
  statusTypes = ['ACTIVE', 'CLOSED', 'DRAFT'];
  filterStatus = 'ALL';
  loading = false;
  errorMessage = '';

  constructor(
    private offerService: OfferService,
    private applicationService: ApplicationService,
    private interviewService: InterviewService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadOffers();
  }

  loadOffers() {
    this.loading = true;
    this.errorMessage = '';
    
    this.offerService.getAllOffers().subscribe({
      next: (data) => {
        this.offers = data;
        this.applyFilter();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading offers:', error);
        this.errorMessage = 'Erreur lors du chargement des offres';
        this.loading = false;
      }
    });
  }

  applyFilter() {
    if (this.filterStatus === 'ALL') {
      this.filteredOffers = this.offers;
    } else {
      this.filteredOffers = this.offers.filter(offer => offer.status === this.filterStatus);
    }
  }

  openCreateModal() {
    this.isEditMode = false;
    this.selectedOffer = null;
    this.resetForm();
    this.showModal = true;
  }

  openEditModal(offer: Offer) {
    this.isEditMode = true;
    this.selectedOffer = offer;
    this.offerForm = {
      title: offer.title,
      description: offer.description,
      companyName: offer.companyName,
      location: offer.location,
      contractType: offer.contractType,
      salary: offer.salary || '',
      companyId: offer.companyId,
      status: offer.status
    };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.resetForm();
  }

  resetForm() {
    this.offerForm = {
      title: '',
      description: '',
      companyName: '',
      location: '',
      contractType: 'CDI',
      salary: '',
      companyId: 1,
      status: 'ACTIVE'
    };
  }

  saveOffer() {
    if (this.isEditMode && this.selectedOffer) {
      this.updateOffer();
    } else {
      this.createOffer();
    }
  }

  createOffer() {
    this.loading = true;
    this.errorMessage = '';
    
    this.offerService.createOffer(this.offerForm).subscribe({
      next: (data) => {
        this.offers.push(data);
        this.applyFilter();
        this.closeModal();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error creating offer:', error);
        this.errorMessage = 'Erreur lors de la création de l\'offre';
        this.loading = false;
      }
    });
  }

  updateOffer() {
    if (!this.selectedOffer?.id) return;
    
    this.loading = true;
    this.offerService.updateOffer(this.selectedOffer.id, this.offerForm).subscribe({
      next: (data) => {
        const index = this.offers.findIndex(o => o.id === data.id);
        if (index !== -1) {
          this.offers[index] = data;
        }
        this.applyFilter();
        this.closeModal();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error updating offer:', error);
        this.errorMessage = 'Erreur lors de la mise à jour de l\'offre';
        this.loading = false;
      }
    });
  }

  deleteOffer(id: number) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette offre ?')) return;
    
    this.loading = true;
    this.offerService.deleteOffer(id).subscribe({
      next: () => {
        this.offers = this.offers.filter(o => o.id !== id);
        this.applyFilter();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error deleting offer:', error);
        this.errorMessage = 'Erreur lors de la suppression de l\'offre';
        this.loading = false;
      }
    });
  }

  onFilterChange() {
    this.applyFilter();
  }

  viewApplications(offer: Offer) {
    if (!offer.id) return;
    
    this.selectedOffer = offer;
    this.loadingApplications = true;
    this.showApplicationsModal = true;
    this.applications = [];
    
    this.applicationService.getApplicationsByOffer(offer.id).subscribe({
      next: (data) => {
        this.applications = data;
        this.loadingApplications = false;
      },
      error: (error) => {
        console.error('Error loading applications:', error);
        this.errorMessage = 'Erreur lors du chargement des candidatures';
        this.loadingApplications = false;
      }
    });
  }

  closeApplicationsModal() {
    this.showApplicationsModal = false;
    this.selectedOffer = null;
    this.applications = [];
  }

  updateApplicationStatus(applicationId: number, status: string) {
    this.applicationService.updateApplicationStatus(applicationId, status).subscribe({
      next: (updatedApp) => {
        const index = this.applications.findIndex(a => a.id === applicationId);
        if (index !== -1) {
          this.applications[index] = updatedApp;
        }
      },
      error: (error) => {
        console.error('Error updating application status:', error);
        this.errorMessage = 'Erreur lors de la mise à jour du statut';
      }
    });
  }

  downloadCV(application: Application) {
    if (!application.cvBase64 || !application.cvFileName) return;
    
    const byteCharacters = atob(application.cvBase64);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: 'application/pdf' });
    
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = application.cvFileName;
    link.click();
  }

  openInterviewModal(application: Application) {
    this.selectedApplication = application;
    this.interviewForm = {
      applicationId: application.id!,
      interviewDate: '',
      location: '',
      meetingLink: '',
      notes: '',
      createdBy: this.authService.getUserInfo()?.userId || 1
    };
    this.showInterviewModal = true;
  }

  closeInterviewModal() {
    this.showInterviewModal = false;
    this.selectedApplication = null;
  }

  scheduleInterview() {
    if (!this.interviewForm.interviewDate || !this.interviewForm.location) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.interviewService.createInterview(this.interviewForm).subscribe({
      next: (interview) => {
        console.log('Interview scheduled successfully:', interview);
        alert('Entretien planifié avec succès!');
        this.closeInterviewModal();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error scheduling interview:', error);
        this.errorMessage = 'Erreur lors de la planification de l\'entretien';
        this.loading = false;
      }
    });
  }
}
