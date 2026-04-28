import { Component, Inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { SedeService } from '../../core/services/sede.service';
import { SedeResponse } from '../../core/models/models';

export interface SedeDialogData {
  sede?: SedeResponse;
}

@Component({
  selector: 'app-sede-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule
  ],
  template: `
    <h2 mat-dialog-title class="dialog-title">
      <span class="material-icons-round text-primary">
        {{ isEdit() ? 'edit' : 'storefront' }}
      </span>
      {{ isEdit() ? 'Editar Sede' : 'Nueva Sede' }}
    </h2>
    
    <mat-dialog-content>
      <form [formGroup]="form" class="flex flex-col gap-4 py-4">
        
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Nombre de la Sede</mat-label>
          <mat-icon matPrefix>store</mat-icon>
          <input matInput formControlName="nombre" placeholder="Ej. Sede Central" />
          <mat-error *ngIf="form.get('nombre')?.hasError('required')">El nombre es obligatorio</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Dirección</mat-label>
          <mat-icon matPrefix>location_on</mat-icon>
          <input matInput formControlName="direccion" placeholder="Ej. Av. Principal 123" />
          <mat-error *ngIf="form.get('direccion')?.hasError('required')">La dirección es obligatoria</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Teléfono</mat-label>
          <mat-icon matPrefix>phone</mat-icon>
          <input matInput formControlName="telefono" placeholder="Ej. 987654321" />
          <mat-error *ngIf="form.get('telefono')?.hasError('required')">El teléfono es obligatorio</mat-error>
        </mat-form-field>

      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end" class="p-4">
      <button mat-stroked-button mat-dialog-close>Cancelar</button>
      <button mat-flat-button color="primary" [disabled]="form.invalid || loading()" (click)="guardar()">
        <mat-icon>save</mat-icon> {{ isEdit() ? 'Guardar Cambios' : 'Registrar Sede' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    :host { display: block; min-width: 400px; }
    .dialog-title { display: flex; align-items: center; gap: 8px; font-weight: bold; margin-bottom: 8px; }
    ::ng-deep .mat-mdc-text-field-wrapper { background-color: var(--bg-card) !important; }
  `]
})
export class SedeDialogComponent implements OnInit {
  isEdit = signal(false);
  loading = signal(false);
  form: FormGroup;

  constructor(
    private dialogRef: MatDialogRef<SedeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: SedeDialogData,
    private fb: FormBuilder,
    private sedeService: SedeService,
    private snack: MatSnackBar
  ) {
    this.isEdit.set(!!data?.sede);
    
    this.form = this.fb.group({
      nombre: [data?.sede?.nombre || '', Validators.required],
      direccion: [data?.sede?.direccion || '', Validators.required],
      telefono: [data?.sede?.telefono || '', Validators.required]
    });
  }

  ngOnInit() {}

  guardar() {
    if (this.form.invalid) return;

    this.loading.set(true);
    const dto = this.form.getRawValue();

    const obs = this.isEdit()
      ? this.sedeService.actualizar(this.data.sede!.id, dto)
      : this.sedeService.crear(dto);

    obs.subscribe({
      next: (res: any) => {
        this.snack.open(this.isEdit() ? 'Sede actualizada' : 'Sede registrada', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(res);
      },
      error: (err: any) => {
        this.snack.open(err.error?.mensaje || 'Error al guardar', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }
}
