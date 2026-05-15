import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { ServicioMedicoResponse, TipoServicio } from '../../core/models/models';

@Component({
  selector: 'app-servicio-medico-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  template: `
    <div class="dialog-container">
      <div class="dialog-header">
        <h2 mat-dialog-title>{{ data ? 'Editar' : 'Nuevo' }} Servicio Médico</h2>
      </div>
      
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <mat-dialog-content class="dialog-content">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
            
            <mat-form-field appearance="outline" class="w-full md:col-span-2">
              <mat-label>Nombre del Servicio</mat-label>
              <input matInput formControlName="nombre" placeholder="Ej. Consulta General">
              @if (form.get('nombre')?.errors?.['required'] && form.get('nombre')?.touched) {
                <mat-error>El nombre es obligatorio</mat-error>
              }
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full md:col-span-2">
              <mat-label>Descripción</mat-label>
              <textarea matInput formControlName="descripcion" rows="2" placeholder="Breve descripción del servicio..."></textarea>
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Tipo de Servicio</mat-label>
              <mat-select formControlName="tipoServicio">
                @for (tipo of tipos; track tipo) {
                  <mat-option [value]="tipo">{{ tipo }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Precio</mat-label>
              <input matInput type="number" formControlName="precio" step="0.01" min="0">
              <span matPrefix>S/&nbsp;</span>
              @if (form.get('precio')?.errors?.['required'] && form.get('precio')?.touched) {
                <mat-error>El precio es obligatorio</mat-error>
              }
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Duración (minutos)</mat-label>
              <input matInput type="number" formControlName="duracionMinutos" min="1">
              <span matSuffix>min</span>
              @if (form.get('duracionMinutos')?.errors?.['required'] && form.get('duracionMinutos')?.touched) {
                <mat-error>Dato obligatorio</mat-error>
              }
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Buffer/Limpieza (minutos)</mat-label>
              <input matInput type="number" formControlName="bufferMinutos" min="0">
              <span matSuffix>min</span>
              @if (form.get('bufferMinutos')?.errors?.['required'] && form.get('bufferMinutos')?.touched) {
                <mat-error>Dato obligatorio</mat-error>
              }
            </mat-form-field>

          </div>
        </mat-dialog-content>

        <mat-dialog-actions align="end" class="dialog-actions">
          <button mat-button type="button" (click)="onCancel()">Cancelar</button>
          <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid">
            {{ data ? 'Guardar Cambios' : 'Crear Servicio' }}
          </button>
        </mat-dialog-actions>
      </form>
    </div>
  `,
  styles: [`
    .dialog-container { min-width: 320px; max-width: 600px; }
    .dialog-header { padding: 0; margin-bottom: 8px; }
    .dialog-content { padding-top: 8px; }
    .dialog-actions { padding: 16px 24px 24px; }
    textarea { resize: none; }
  `]
})
export class ServicioMedicoDialogComponent implements OnInit {
  form: FormGroup;
  tipos: TipoServicio[] = ['CONSULTA', 'VACUNACION', 'CIRUGIA', 'HOSPITALIZACION', 'ESTETICA', 'EXAMEN', 'OTRO'];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ServicioMedicoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ServicioMedicoResponse | null
  ) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required]],
      descripcion: [''],
      tipoServicio: ['CONSULTA', [Validators.required]],
      precio: [0, [Validators.required, Validators.min(0)]],
      duracionMinutos: [30, [Validators.required, Validators.min(1)]],
      bufferMinutos: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    if (this.data) {
      this.form.patchValue(this.data);
    }
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.dialogRef.close(this.form.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
