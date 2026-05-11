import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-servicios-medicos',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container fade-in-up p-6">
      <div class="page-header mb-6">
        <h1 class="text-2xl font-bold text-[var(--text-primary)]">Catálogo de Servicios Médicos</h1>
        <p class="text-[var(--text-muted)]">Módulo en construcción...</p>
      </div>
      <div class="card p-8 text-center text-gray-500">
        <span class="material-icons-round text-6xl mb-4 text-gray-300">medical_information</span>
        <h2 class="text-xl font-semibold">Panel de Servicios</h2>
        <p>Próximamente podrá gestionar precios, duración y tipos de servicios médicos aquí.</p>
      </div>
    </div>
  `
})
export class ServiciosMedicosComponent {}
