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
        {{ pasoActual === 1 ? (isEdit ? 'edit' : 'person_add') : 'verified' }}
      </span>
      {{ pasoActual === 1 ? (isEdit ? 'Editar Cliente' : 'Nuevo Cliente') : 'Confirmar Datos' }}
    </h2>

    <form [formGroup]="form" *ngIf="pasoActual === 1">
      <mat-dialog-content class="dialog-content-scroll">
        <p class="dialog-subtitle">
          {{ isEdit ? 'Modifica los datos del cliente a continuación.' : 'Ingresa los datos personales para registrar al nuevo cliente.' }}
        </p>

        <div class="form-grid">
          <div class="dni-search-container col-span-2">
            <mat-form-field appearance="outline" class="dni-field">
              <mat-label>DNI / Documento</mat-label>
              <mat-icon matPrefix class="prefix-icon">badge</mat-icon>
              <input matInput formControlName="dni" placeholder="Ej. 12345678" required maxlength="8" (keydown)="soloNumeros($event)"/>
              <mat-error *ngIf="form.get('dni')?.hasError('required')">El DNI es obligatorio</mat-error>
              <mat-error *ngIf="form.get('dni')?.hasError('pattern')">Debe tener exactamente 8 dígitos numéricos</mat-error>
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
            <input matInput formControlName="nombre" placeholder="Ej. Juan" required (keydown)="soloLetras($event)"/>
            <mat-error *ngIf="form.get('nombre')?.hasError('required')">El nombre es obligatorio</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Apellido</mat-label>
            <mat-icon matPrefix class="prefix-icon">person_outline</mat-icon>
            <input matInput formControlName="apellido" placeholder="Ej. Pérez" required (keydown)="soloLetras($event)"/>
            <mat-error *ngIf="form.get('apellido')?.hasError('required')">El apellido es obligatorio</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Teléfono</mat-label>
            <mat-icon matPrefix class="prefix-icon">phone</mat-icon>
            <input matInput formControlName="telefono" placeholder="Ej. 987654321" required  maxlength="9" (keydown)="soloNumeros($event)"/>
            <mat-error *ngIf="form.get('telefono')?.hasError('required')">El teléfono es obligatorio</mat-error>
            <mat-error *ngIf="form.get('telefono')?.hasError('pattern')">Debe tener exactamente 9 dígitos numéricos</mat-error>
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
        <button mat-flat-button color="primary" type="button" (click)="irAResumen()" [disabled]="form.invalid || isSubmitting">
          Continuar
        </button>
      </mat-dialog-actions>
    </form>

    <div *ngIf="pasoActual === 2">
      <mat-dialog-content class="dialog-content-scroll">
        <p class="dialog-subtitle">Verifica que los datos sean correctos antes de guardarlos en el sistema.</p>
        
        <div class="resumen-box">
          <p><strong>DNI:</strong> {{ datosResumen?.dni }}</p>
          <p><strong>Nombres:</strong> {{ datosResumen?.nombre }} {{ datosResumen?.apellido }}</p>
          <p><strong>Teléfono:</strong> {{ datosResumen?.telefono }}</p>
          <p><strong>Correo:</strong> {{ datosResumen?.email }}</p>
          <p><strong>Dirección:</strong> {{ datosResumen?.direccion || 'No especificada' }}</p>
        </div>
      </mat-dialog-content>

      <mat-dialog-actions align="end" class="dialog-actions">
        <button mat-stroked-button type="button" (click)="regresarAEditar()" class="btn-cancel">
          Regresar a editar
        </button>
        <button mat-flat-button color="primary" type="button" (click)="confirmarYGuardar()" [disabled]="isSubmitting">
          <mat-icon style="margin-right: 4px;">save</mat-icon> Confirmar
        </button>
      </mat-dialog-actions>
    </div>
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

    .resumen-box {
    background-color: rgba(0, 0, 0, 0.04);
    border: 1px solid var(--border-color, #e0e0e0);
    padding: 16px 24px;
    border-radius: 8px;
    margin-bottom: 8px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .resumen-box p {
    margin: 0;
    font-size: 1rem;
    color: var(--text-primary);
  }
  `]
})
export class ClienteDialogComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  isSubmitting = false;
  buscandoDni = false;
  pasoActual = 1;
  datosResumen: any = null

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
      nombre: [{ value: this.data?.nombre || '', disabled: this.isEdit }, Validators.required,],
      apellido: [{ value: this.data?.apellido || '', disabled: this.isEdit }, Validators.required],
      dni: [{ value: this.data?.dni || '', disabled: this.isEdit }, [Validators.required, Validators.pattern('^[0-9]{8}$')]],
      telefono: [this.data?.telefono || '', [Validators.required, Validators.pattern('^[0-9]{9}$')]],
      email: [{ value: this.data?.email || '', disabled: this.isEdit }, [Validators.required, Validators.email]],
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
          this.form.get('nombre')?.disable();
          this.form.get('apellido')?.disable();
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

  soloLetras(event: KeyboardEvent): void {
    const teclas_permitidas = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab', 'Home', 'End'];
    const patron = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]$/;
    if (!teclas_permitidas.includes(event.key) && !patron.test(event.key)) {
      event.preventDefault();
    }
  }
  
  soloNumeros(event: KeyboardEvent): void {
    const teclas_permitidas = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab', 'Home', 'End'];
    const patron = /^[0-9]$/;
    if (!teclas_permitidas.includes(event.key) && !patron.test(event.key)) {
      event.preventDefault();
    }
  }

    // Si estamos editando y el email / dni se usa para login, a veces no se debería poder editar, 
    // pero lo dejamos habilitado a menos que el backend lo restrinja.

  irAResumen(): void {
    if (this.form.valid) {
      this.datosResumen = this.form.getRawValue();
      this.pasoActual = 2;
    }
  }

  regresarAEditar(): void {
    this.pasoActual = 1;
  }

  confirmarYGuardar(): void {
    this.dialogRef.close(this.datosResumen);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}