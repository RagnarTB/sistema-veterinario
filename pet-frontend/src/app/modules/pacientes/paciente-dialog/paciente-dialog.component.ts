import { Component, Inject, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged, switchMap, of, map, startWith } from 'rxjs';

import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { PacienteRequest, PacienteResponse, EspecieResponse, ClienteResponse } from '../../../core/models/models';
import { EspecieService } from '../../../core/services/especie.service';
import { ClienteService } from '../../../core/services/cliente.service';

@Component({
  selector: 'app-paciente-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' }
  ],
  template: `
    <h2 mat-dialog-title class="dialog-title">
      <mat-icon>{{ isEdit ? 'pets' : 'add_circle' }}</mat-icon>
      {{ isEdit ? 'Editar Paciente' : 'Registrar Nuevo Paciente' }}
    </h2>
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <mat-dialog-content class="dialog-content-scroll">
        <p class="dialog-subtitle">
          Completa los datos de la mascota y asociala con su dueño (cliente).
        </p>

        <div class="form-grid">
          <!-- Nombre del Paciente -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Nombre de la mascota</mat-label>
            <mat-icon matPrefix class="prefix-icon">pets</mat-icon>
            <input matInput formControlName="nombre" placeholder="Ej. Firulais" required />
            <mat-error *ngIf="form.get('nombre')?.hasError('required')">El nombre es requerido</mat-error>
          </mat-form-field>

          <!-- Autocomplete Cliente (Dueño) -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Dueño (Cliente)</mat-label>
            <mat-icon matPrefix class="prefix-icon">person</mat-icon>
            <input type="text" matInput [matAutocomplete]="autoCliente" [formControl]="clienteSearchCtrl" placeholder="Buscar cliente activo..." />
            <mat-icon matSuffix *ngIf="!searchingClientes()">search</mat-icon>
            <mat-spinner matSuffix [diameter]="20" *ngIf="searchingClientes()"></mat-spinner>
            <mat-autocomplete #autoCliente="matAutocomplete" [displayWith]="displayCliente">
              <mat-option *ngFor="let cliente of clientesOptions" [value]="cliente">
                <span class="cliente-op-name">{{ cliente.nombre }} {{ cliente.apellido }}</span>
                <span class="cliente-op-doc"> (DNI: {{ cliente.dni }})</span>
              </mat-option>
            </mat-autocomplete>
            <mat-error *ngIf="form.get('clienteId')?.hasError('required')">Debe seleccionar un cliente registrado</mat-error>
          </mat-form-field>

          <!-- Especie -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Especie</mat-label>
            <mat-icon matPrefix class="prefix-icon">category</mat-icon>
            <mat-select formControlName="especieId" required>
              <mat-option *ngFor="let esp of especiesOptions" [value]="esp.id">
                {{ esp.nombre }}
              </mat-option>
            </mat-select>
            <mat-error *ngIf="form.get('especieId')?.hasError('required')">La especie es requerida</mat-error>
          </mat-form-field>

          <!-- Raza -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Raza (Opcional)</mat-label>
            <mat-icon matPrefix class="prefix-icon">style</mat-icon>
            <input matInput formControlName="raza" placeholder="Ej. Bulldog, Persa..." />
          </mat-form-field>

          <!-- Fecha de Nacimiento -->
          <mat-form-field appearance="outline" class="full-width col-span-2">
            <mat-label>Fecha de Nacimiento</mat-label>
            <mat-icon matPrefix class="prefix-icon">event</mat-icon>
            <input matInput [matDatepicker]="picker" formControlName="fechaNacimiento" [max]="today" placeholder="DD/MM/AAAA" required>
            <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
            <mat-error *ngIf="form.get('fechaNacimiento')?.hasError('required')">La fecha es requerida</mat-error>
          </mat-form-field>
        </div>
      </mat-dialog-content>
      
      <mat-dialog-actions align="end" class="dialog-actions">
        <button mat-stroked-button type="button" (click)="onCancel()" class="btn-cancel">Cancelar</button>
        <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || isSubmitting">
          {{ isEdit ? 'Guardar Cambios' : 'Registrar Paciente' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    .dialog-title { display: flex; align-items: center; gap: 10px; font-weight: 700; color: var(--text-primary); margin: 0; padding: 24px 24px 12px; }
    .dialog-title mat-icon { color: var(--color-primary-400); font-size: 28px; height: 28px; width: 28px; }
    .dialog-content-scroll { max-height: 65vh; padding: 0 24px 24px; overflow-y: auto; }
    .dialog-subtitle { margin-top: 0; margin-bottom: 20px; color: var(--text-secondary); font-size: 0.95rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
    .col-span-2 { grid-column: span 2; }
    .full-width { width: 100%; }
    .dialog-actions { padding: 0 24px 24px; }
    .btn-cancel { border-color: var(--border-color); color: var(--text-secondary); }
    .prefix-icon { color: var(--text-muted); margin-right: 8px; font-size: 20px; text-align: center; }
    
    .cliente-op-name { font-weight: 500; color: var(--text-primary); }
    .cliente-op-doc { font-size: 0.85em; color: var(--text-muted); }

    /* Adaptar inputs de material a dark theme custom */
    ::ng-deep .mat-mdc-text-field-wrapper { background-color: var(--bg-card) !important; }
    ::ng-deep .mat-mdc-form-field-icon-prefix { color: var(--text-muted); padding: 0 8px; }

    @media (max-width: 600px) {
      .form-grid { grid-template-columns: 1fr; }
      .col-span-2 { grid-column: span 1; }
    }
  `]
})
export class PacienteDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private especieService = inject(EspecieService);
  private clienteService = inject(ClienteService);
  public dialogRef = inject(MatDialogRef<PacienteDialogComponent>);
  public data: PacienteResponse | null = inject(MAT_DIALOG_DATA);

  form!: FormGroup;
  isEdit = false;
  isSubmitting = false;

  especiesOptions: EspecieResponse[] = [];
  clientesOptions: ClienteResponse[] = [];
  
  clienteSearchCtrl = new FormControl<string | ClienteResponse>('');
  searchingClientes = signal(false);

  today = new Date(); // Para el max datepicker

  ngOnInit(): void {
    this.isEdit = !!this.data;

    this.form = this.fb.group({
      nombre: [this.data?.nombre || '', Validators.required],
      especieId: [{value: null, disabled: this.isEdit}, Validators.required],
      raza: [this.data?.raza || ''],
      fechaNacimiento: [{value: this.data?.fechaNacimiento ? new Date(this.data.fechaNacimiento) : null, disabled: this.isEdit}, Validators.required],
      clienteId: [{value: this.data?.clienteId || null, disabled: this.isEdit}, Validators.required]
    });

    if (this.isEdit) {
      this.clienteSearchCtrl.disable();
    }

    this.cargarEspecies();
    this.setupClienteAutocomplete();
  }

  cargarEspecies() {
    this.especieService.listar().subscribe({
      next: (res) => {
        // Only load active species when creating or loading list
        this.especiesOptions = res.filter(e => e.activo || (this.isEdit && e.nombre === this.data?.especie));
        if (this.isEdit && this.data) {
          // Pre-select especie based on nombre since the DTO from backend lost the ID (it only exposes especieNombre)
          const matchedEsp = res.find(e => e.nombre === this.data?.especie);
          if (matchedEsp) {
            this.form.patchValue({ especieId: matchedEsp.id });
          }
        }
      }
    });
  }

  setupClienteAutocomplete() {
    // Si estamos editando, tratamos de precargar el cliente localmente o con el DTO
    if (this.isEdit && this.data) {
      // Mock un cliente solo para el display inicial basado en el "clienteNombre" que agregamos
      const initialClient: ClienteResponse = {
        id: this.data.clienteId!,
        nombre: this.data.clienteNombre || 'Dueño Actual',
        apellido: '',
        dni: '',
        telefono: '',
        email: '',
        activo: true,
        verificado: true
      };
      this.clienteSearchCtrl.setValue(initialClient);
    }

    this.clienteSearchCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((value) => {
        if (typeof value !== 'string') {
          // It's an object selection
          this.form.patchValue({ clienteId: value?.id });
          return of([]);
        }
        
        if (value.length < 2) return of([]);

        this.searchingClientes.set(true);
        // Page 0, 10 items, query = value
        return this.clienteService.listar(0, 10, value).pipe(
          map(page => page.content.filter(c => c.activo))
        );
      })
    ).subscribe({
      next: (clientes) => {
        if (clientes.length > 0) {
          this.clientesOptions = clientes;
        }
        this.searchingClientes.set(false);
      },
      error: () => this.searchingClientes.set(false)
    });
  }

  displayCliente(cliente: ClienteResponse): string {
    return cliente ? `${cliente.nombre} ${cliente.apellido}`.trim() : '';
  }

  onSubmit(): void {
    if (this.form.valid) {
      const result: PacienteRequest = { ...this.form.getRawValue() };
      
      if (result.raza) result.raza = result.raza.trim().toUpperCase();
      if (result.nombre) result.nombre = result.nombre.trim();
      
      // Convertir fecha a string ISO YYYY-MM-DD
      const dateVal = this.form.get('fechaNacimiento')?.value;
      if (dateVal) {
        const d = new Date(dateVal);
        result.fechaNacimiento = d.toISOString().split('T')[0];
      }
      
      this.dialogRef.close(result);
    } else {
      // Force display errors
      this.form.markAllAsTouched();
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
