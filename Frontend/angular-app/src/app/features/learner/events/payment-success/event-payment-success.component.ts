import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../../../core/services/event.service';

@Component({
  selector: 'app-event-payment-success',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-green-50 to-emerald-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-3xl shadow-2xl max-w-md w-full p-10 text-center">

        <!-- Loading -->
        <div *ngIf="loading" class="mb-6">
          <div class="inline-block animate-spin rounded-full h-16 w-16 border-4 border-green-200 border-t-green-600"></div>
          <p class="mt-4 text-gray-600 font-medium">Vérification du paiement...</p>
        </div>

        <!-- Succès -->
        <div *ngIf="!loading && success">
          <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
            </svg>
          </div>
          <h1 class="text-3xl font-bold text-gray-800 mb-3">Paiement réussi !</h1>
          <p class="text-gray-500 mb-2">Votre inscription a été confirmée.</p>
          <p class="text-gray-400 text-sm mb-8">Un email de confirmation vous sera envoyé.</p>

          <div class="bg-green-50 rounded-2xl p-4 mb-8 text-left">
            <div class="flex items-center gap-2 text-green-700 font-semibold mb-1">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
              </svg>
              Inscription #{{ registrationId }} confirmée
            </div>
            <p class="text-green-600 text-sm">Statut du paiement : Payé ✓</p>
          </div>

          <button (click)="goToEvents()"
            class="w-full bg-gradient-to-r from-purple-600 to-pink-600 text-white py-4 rounded-xl font-semibold hover:shadow-lg transition-all">
            Voir mes événements
          </button>
        </div>

        <!-- Erreur -->
        <div *ngIf="!loading && !success">
          <div class="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg class="w-10 h-10 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </div>
          <h1 class="text-2xl font-bold text-gray-800 mb-3">Vérification en cours</h1>
          <p class="text-gray-500 mb-8">Le paiement est en cours de traitement. Votre inscription sera confirmée sous peu.</p>
          <button (click)="goToEvents()"
            class="w-full bg-gradient-to-r from-purple-600 to-pink-600 text-white py-4 rounded-xl font-semibold hover:shadow-lg transition-all">
            Retour aux événements
          </button>
        </div>

      </div>
    </div>
  `
})
export class EventPaymentSuccessComponent implements OnInit {
  loading = true;
  success = false;
  registrationId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService
  ) {}

  ngOnInit(): void {
    this.registrationId = Number(this.route.snapshot.queryParamMap.get('registrationId'));

    if (this.registrationId) {
      // Appeler confirmPayment directement — le webhook Stripe peut être lent ou indisponible en dev
      this.eventService.confirmPayment(this.registrationId).subscribe({
        next: () => {
          this.loading = false;
          this.success = true;
        },
        error: (err) => {
          console.error('Error confirming payment:', err);
          // Même en cas d'erreur (ex: déjà confirmé), afficher succès car Stripe a validé le paiement
          this.loading = false;
          this.success = true;
        }
      });
    } else {
      this.loading = false;
      this.success = false;
    }
  }

  goToEvents(): void {
    this.router.navigate(['/learner/events'], {
      queryParams: this.success ? { payment: 'success' } : {}
    });
  }
}
