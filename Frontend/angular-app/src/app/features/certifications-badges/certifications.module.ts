import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CertificationTemplateListComponent } from './certification-template-list/certification-template-list.component';
import { CertificationTemplateFormComponent } from './certification-template-form/certification-template-form.component';
import { AwardCertificationComponent } from './award-certification/award-certification.component';

const routes: Routes = [
  { path: '', component: CertificationTemplateListComponent },
  { path: 'new', component: CertificationTemplateFormComponent },
  { path: 'edit/:id', component: CertificationTemplateFormComponent },
  { path: 'award', component: AwardCertificationComponent }
];

@NgModule({
  declarations: [
    CertificationTemplateListComponent,
    CertificationTemplateFormComponent,
    AwardCertificationComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class CertificationsModule { }
