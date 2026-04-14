import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OfferService } from '../../../core/services/offer.service';
import { ApplicationService } from '../../../core/services/application.service';
import { AuthService } from '../../../core/services/auth.service';
import { Offer } from '../../../core/models/offer.model';
import { ApplicationRequest } from '../../../core/models/application.model';

@Component({
  selector: 'app-job-offers-learner',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './job-offers-learner.component.html',
  styleUrls: ['./job-offers-learner.component.css']
})
export class JobOffersLearnerComponent implements OnInit {
  offers: Offer[] = [];
  filteredOffers: Offer[] = [];
  selectedOffer: Offer | null = null;
  showDetailModal = false;
  showApplicationModal = false;
  
  filterStatus = 'ALL';
  filterContractType = 'ALL';
  searchTerm = '';
  
  coverLetter = '';
  cvFile: File | null = null;
  cvFileName = '';
  
  contractTypes = ['ALL', 'CDI', 'CDD', 'Stage', 'Alternance', 'Freelance'];
  loading = false;
  errorMessage = '';
  successMessage = '';
  
  // Map pour stocker le statut de candidature par offre
  applicationStatusMap: Map<number, string> = new Map();
  currentUserId: number | null = null;

  constructor(
    private offerService: OfferService,
    private applicationService: ApplicationService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadOffers();
    this.loadCurrentUser();
  }

  loadCurrentUser() {
    const userInfo = this.authService.getUserInfo();
    if (userInfo) {
      this.currentUserId = userInfo.userId;
      this.loadUserApplications();
    }
  }

  loadUserApplications() {
    if (!this.currentUserId) return;
    
    this.applicationService.getApplicationsByLearner(this.currentUserId).subscribe({
      next: (applications) => {
        // Créer une map offerId -> status
        applications.forEach(app => {
          this.applicationStatusMap.set(app.offerId, app.status);
        });
      },
      error: (error) => {
        console.error('Error loading user applications:', error);
      }
    });
  }

  getApplicationStatus(offerId: number): string | null {
    return this.applicationStatusMap.get(offerId) || null;
  }

  hasApplied(offerId: number): boolean {
    return this.applicationStatusMap.has(offerId);
  }

  loadOffers() {
    this.loading = true;
    this.errorMessage = '';
    
    console.log('Learner: Loading offers...');
    
    this.offerService.getAllOffers().subscribe({
      next: (data) => {
        console.log('Learner: Offers loaded:', data);
        this.offers = data;
        this.applyFilters();
        console.log('Learner: Filtered offers:', this.filteredOffers);
        this.loading = false;
      },
      error: (error) => {
        console.error('Learner: Error loading offers:', error);
        this.errorMessage = 'Erreur lors du chargement des offres';
        this.loading = false;
      }
    });
  }

  applyFilters() {
    console.log('Learner: Applying filters...', {
      filterStatus: this.filterStatus,
      filterContractType: this.filterContractType,
      searchTerm: this.searchTerm,
      totalOffers: this.offers.length
    });
    
    this.filteredOffers = this.offers.filter(offer => {
      const statusMatch = this.filterStatus === 'ALL' || offer.status === this.filterStatus;
      const contractMatch = this.filterContractType === 'ALL' || offer.contractType === this.filterContractType;
      const searchMatch = this.searchTerm === '' || 
        offer.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        offer.companyName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        offer.location.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      return statusMatch && contractMatch && searchMatch;
    });
    
    console.log('Learner: Filtered result:', this.filteredOffers.length, 'offers');
  }

  onFilterChange() {
    this.applyFilters();
  }

  onSearchChange() {
    this.applyFilters();
  }

  viewDetails(offer: Offer) {
    this.selectedOffer = offer;
    this.showDetailModal = true;
  }

  closeDetailModal() {
    this.showDetailModal = false;
    this.selectedOffer = null;
  }

  applyToOffer(offer: Offer) {
    this.selectedOffer = offer;
    this.coverLetter = '';
    this.cvFile = null;
    this.cvFileName = '';
    this.errorMessage = '';
    this.successMessage = '';
    this.showDetailModal = false; // Fermer le modal de détails
    this.showApplicationModal = true; // Ouvrir le modal de candidature
  }

  closeApplicationModal() {
    this.showApplicationModal = false;
    this.selectedOffer = null;
    this.coverLetter = '';
    this.cvFile = null;
    this.cvFileName = '';
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Vérifier le type de fichier (PDF uniquement)
      if (file.type !== 'application/pdf') {
        this.errorMessage = 'Veuillez sélectionner un fichier PDF';
        return;
      }
      
      // Vérifier la taille du fichier (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.errorMessage = 'Le fichier ne doit pas dépasser 5 MB';
        return;
      }
      
      this.cvFile = file;
      this.cvFileName = file.name;
      this.errorMessage = '';
    }
  }

  removeFile() {
    this.cvFile = null;
    this.cvFileName = '';
  }

  submitApplication() {
    if (!this.selectedOffer) return;
    
    // Vérifier que le CV est fourni
    if (!this.cvFile) {
      this.errorMessage = 'Veuillez joindre votre CV (format PDF)';
      return;
    }
    
    // Vérifier si l'utilisateur est connecté
    if (!this.authService.isAuthenticated()) {
      this.errorMessage = 'Vous devez être connecté pour postuler. Redirection vers la page de connexion...';
      setTimeout(() => {
        window.location.href = '/auth/sign-in';
      }, 2000);
      return;
    }
    
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    // Récupérer les données utilisateur depuis la base de données
    this.authService.fetchUserData().subscribe({
      next: (userData) => {
        console.log('User data fetched from database:', userData);
        
        if (!userData || !userData.userId || !userData.email) {
          this.errorMessage = 'Impossible de récupérer vos informations. Veuillez vous reconnecter.';
          this.loading = false;
          setTimeout(() => {
            window.location.href = '/auth/sign-in';
          }, 2000);
          return;
        }
        
        // Convertir le fichier en base64
        const reader = new FileReader();
        reader.onload = () => {
          const base64CV = (reader.result as string).split(',')[1];
          
          const applicationRequest: ApplicationRequest = {
            offerId: this.selectedOffer!.id!,
            learnerId: userData.userId,
            learnerName: userData.firstName || userData.email,
            learnerEmail: userData.email,
            coverLetter: this.coverLetter,
            cvBase64: base64CV,
            cvFileName: this.cvFileName
          };
          
          console.log('Sending application:', applicationRequest);
          
          this.applicationService.applyToOffer(applicationRequest).subscribe({
            next: (response) => {
              console.log('Application submitted successfully:', response);
              this.successMessage = 'Votre candidature a été envoyée avec succès !';
              this.loading = false;
              // Recharger les candidatures pour mettre à jour le statut
              this.loadUserApplications();
              setTimeout(() => {
                this.closeApplicationModal();
                this.successMessage = '';
              }, 2000);
            },
            error: (error) => {
              console.error('Error submitting application:', error);
              if (error.status === 400) {
                this.errorMessage = 'Vous avez déjà postulé à cette offre';
              } else if (error.status === 0) {
                this.errorMessage = 'Impossible de se connecter au serveur. Vérifiez que le backend est démarré.';
              } else {
                this.errorMessage = 'Erreur lors de l\'envoi de la candidature: ' + (error.error?.message || error.message);
              }
              this.loading = false;
            }
          });
        };
        
        reader.onerror = () => {
          this.errorMessage = 'Erreur lors de la lecture du fichier CV';
          this.loading = false;
        };
        
        if (this.cvFile) {
          reader.readAsDataURL(this.cvFile);
        }
      },
      error: (error) => {
        console.error('Error fetching user data:', error);
        this.errorMessage = 'Erreur lors de la récupération de vos informations. Veuillez vous reconnecter.';
        this.loading = false;
        setTimeout(() => {
          window.location.href = '/auth/sign-in';
        }, 2000);
      }
    });
  }
}
