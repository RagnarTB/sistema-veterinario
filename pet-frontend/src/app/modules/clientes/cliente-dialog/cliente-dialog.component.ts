import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ClienteRequest, ClienteResponse } from '../../../core/models/models';
import { ExternoService } from '../../../core/services/externo.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
@Component({
  selector: 'app-cliente-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <h2 mat-dialog-title class="dialog-title">
      <span class="material-icons-round">
        {{ isEdit ? 'edit' : 'person_add' }}
      </span>
      {{ isEdit ? 'Editar Cliente' : 'Nuevo Cliente' }}
    </h2>
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <mat-dialog-content class="dialog-content-scroll">
        <p class="dialog-subtitle">
          {{ isEdit ? 'Modifica los datos del cliente a continuación.' : 'Ingresa los datos personales para registrar al nuevo cliente.' }}
        </p>

        <div class="form-grid">
          <!-- Modificado para poner DNI a la cabeza en 2 columnas y el botón fuera -->
          <div class="dni-search-container col-span-2">
            <mat-form-field appearance="outline" class="dni-field">
              <mat-label>DNI / Documento</mat-label>
              <mat-icon matPrefix class="prefix-icon">badge</mat-icon>
              <input matInput formControlName="dni" placeholder="Ej. 12345678" required />
              <mat-error *ngIf="form.get('dni')?.hasError('required')">El DNI es obligatorio</mat-error>
              <mat-error *ngIf="form.get('dni')?.hasError('pattern')">Solo números</mat-error>
            </mat-form-field>
            <button mat-flat-button color="primary" type="button" class="btn-search-dni" 
                    (click)="buscarDni()" 
                    [disabled]="form.get('dni')?.invalid || form.get('dni')?.value.length !== 8 || buscandoDni || isEdit">
              <mat-icon *ngIf="!buscandoDni">search</mat-icon>
              <mat-spinner diameter="20" *ngIf="buscandoDni" class="spinner-dni"></mat-spinner>
              {{ buscandoDni ? 'Buscando...' : 'Buscar Reniec' }}
            </button>
          </div>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Nombre</mat-label>
            <mat-icon matPrefix class="prefix-icon">person</mat-icon>
            <input matInput formControlName="nombre" placeholder="Ej. Juan" required />
            <mat-error *ngIf="form.get('nombre')?.hasError('required')">El nombre es obligatorio</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Apellido</mat-label>
            <mat-icon matPrefix class="prefix-icon">person_outline</mat-icon>
            <input matInput formControlName="apellido" placeholder="Ej. Pérez" required />
            <mat-error *ngIf="form.get('apellido')?.hasError('required')">El apellido es obligatorio</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Teléfono</mat-label>
            <mat-icon matPrefix class="prefix-icon">phone</mat-icon>
            <input matInput formControlName="telefono" placeholder="Ej. 987654321" required />
            <mat-error *ngIf="form.get('telefono')?.hasError('required')">El teléfono es obligatorio</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width col-span-2">
            <mat-label>Correo Electrónico</mat-label>
            <mat-icon matPrefix class="prefix-icon">mail_outline</mat-icon>
            <input matInput formControlName="email" type="email" placeholder="juan@correo.com" required />
            <mat-error *ngIf="form.get('email')?.hasError('required')">El email es obligatorio</mat-error>
            <mat-error *ngIf="form.get('email')?.hasError('email')">Formato de correo inválido</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width col-span-2">
            <mat-label>Dirección (Opcional)</mat-label>
            <mat-icon matPrefix class="prefix-icon">location_on</mat-icon>
            <input matInput formControlName="direccion" placeholder="Ej. Av. Primavera 123" />
          </mat-form-field>
        </div>
      </mat-dialog-content>
      <mat-dialog-actions align="end" class="dialog-actions">
        <button mat-stroked-button type="button" (click)="onCancel()" class="btn-cancel">Cancelar</button>
        <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || isSubmitting">
          {{ isEdit ? 'Guardar Cambios' : 'Registrar Cliente' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    .dialog-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0;
      padding: 24px 24px 12px;
    }
    .dialog-title .material-icons-round { color: var(--color-primary-400); font-size: 28px; }
    .dialog-content-scroll { max-height: 65vh; padding: 0 24px 24px; overflow-y: auto; }
    .dialog-subtitle { margin-top: 0; margin-bottom: 20px; color: var(--text-secondary); font-size: 0.95rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
    .col-span-2 { grid-column: span 2; }
    .full-width { width: 100%; }
    .dialog-actions { padding: 0 24px 24px; }
    .btn-cancel { border-color: var(--border-color); color: var(--text-secondary); }
    .prefix-icon { color: var(--text-muted); margin-right: 8px; font-size: 20px; }
    
    .dni-search-container { display: flex; gap: 16px; align-items: flex-start; }
    .dni-field { flex: 1; }
    .btn-search-dni { height: 52px; font-weight: 600; font-size: 1rem; border-radius: 8px; padding: 0 20px; display:flex; align-items:center; gap:8px;}
    .spinner-dni circle { stroke: white !important; }
    
    
    /* Adaptar inputs de material a dark theme custom */
    ::ng-deep .mat-mdc-text-field-wrapper { background-color: var(--bg-card) !important; }
    ::ng-deep .mat-mdc-form-field-icon-prefix { color: var(--text-muted); padding: 0 8px; }
    
    @media (max-width: 600px) {
      .form-grid { grid-template-columns: 1fr; }
      .col-span-2 { grid-column: span 1; }
    }
  `]
})
export class ClienteDialogComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  isSubmitting = false;
  buscandoDni = false;

  constructor(
    private fb: FormBuilder,
    private externoService: ExternoService,
    private snack: MatSnackBar,
    public dialogRef: MatDialogRef<ClienteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ClienteResponse | null
  ) {
    this.isEdit = !!data;
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      nombre: [{value: this.data?.nombre || '', disabled: this.isEdit}, Validators.required],
      apellido: [{value: this.data?.apellido || '', disabled: this.isEdit}, Validators.required],
      dni: [{value: this.data?.dni || '', disabled: this.isEdit}, [Validators.required, Validators.pattern('^[0-9A-Za-z]+$')]],
      telefono: [this.data?.telefono || '', Validators.required],
      email: [{value: this.data?.email || '', disabled: this.isEdit}, [Validators.required, Validators.email]],
      direccion: [(this.data as any)?.direccion || '']
    });
  }

  buscarDni() {
    const dni = this.form.get('dni')?.value;
    if (!dni || dni.length !== 8) {
      this.snack.open('Por favor ingresa un DNI válido de 8 dígitos', 'Cerrar', { duration: 3000 });
      return;
    }

    this.buscandoDni = true;
    this.externoService.consultarDni(dni).subscribe({
      next: (res) => {
        if (res && res.first_name) {
          this.form.patchValue({
            nombre: res.first_name,
            apellido: `${res.first_last_name} ${res.second_last_name}`.trim()
          });
          this.snack.open('DNI encontrado exitosamente', 'Cerrar', { duration: 3000 });
        }
        this.buscandoDni = false;
      },
      error: (err) => {
        this.buscandoDni = false;
        this.snack.open('No se pudo encontrar información para este documento', 'Cerrar', { duration: 4000 });
      }
    });
  }

    // Si estamos editando y el email / dni se usa para login, a veces no se debería poder editar, 
    // pero lo dejamos habilitado a menos que el backend lo restrinja.

  onSubmit(): void {
    if (this.form.valid) {
      const result: ClienteRequest = this.form.getRawValue();
      this.dialogRef.close(result);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
