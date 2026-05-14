import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { BadgeTemplateListComponent } from './badge-template-list/badge-template-list.component';
import { BadgeTemplateFormComponent } from './badge-template-form/badge-template-form.component';
import { AwardBadgeComponent } from './award-badge/award-badge.component';

const routes: Routes = [
  { path: '', component: BadgeTemplateListComponent },
  { path: 'new', component: BadgeTemplateFormComponent },
  { path: 'edit/:id', component: BadgeTemplateFormComponent },
  { path: 'award', component: AwardBadgeComponent }
];

@NgModule({
  declarations: [
    BadgeTemplateListComponent,
    BadgeTemplateFormComponent,
    AwardBadgeComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class BadgesModule { }
