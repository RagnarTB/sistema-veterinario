import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';

import { DiaBloqueadoService } from '../../core/services/dia-bloqueado.service';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion/modal-confirmacion.component';
import { DiaBloqueadoRequest, DiaBloqueadoResponse } from '../../core/models/models';

@Component({
  selector: 'app-dias-bloqueados',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  template: `
    <div class="p-4">
      <h3 class="text-lg font-semibold mb-4 text-[var(--text-primary)]">Días Bloqueados (Permisos / Vacaciones)</h3>

      <form [formGroup]="form" class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
        
        <mat-form-field appearance="outline" class="md:col-span-1">
          <mat-label>Tipo</mat-label>
          <mat-select formControlName="tipo">
            <mat-option value="DIA_BLOQUEADO">Día Bloqueado</mat-option>
            <mat-option value="VACACIONES">Vacaciones</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="md:col-span-1">
          <mat-label>{{ form.get('tipo')?.value === 'VACACIONES' ? 'Fecha Inicio' : 'Fecha' }}</mat-label>
          <input matInput [matDatepicker]="picker" [min]="minDate" formControlName="fecha" required readonly>
          <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
          <mat-datepicker #picker></mat-datepicker>
        </mat-form-field>

        <mat-form-field appearance="outline" class="md:col-span-1" *ngIf="form.get('tipo')?.value === 'VACACIONES'">
          <mat-label>Fecha Fin</mat-label>
          <input matInput [matDatepicker]="pickerFin" [min]="form.get('fecha')?.value || minDate" formControlName="fechaFin" required readonly>
          <mat-datepicker-toggle matIconSuffix [for]="pickerFin"></mat-datepicker-toggle>
          <mat-datepicker #pickerFin></mat-datepicker>
        </mat-form-field>

        <mat-form-field appearance="outline" [ngClass]="form.get('tipo')?.value === 'VACACIONES' ? 'md:col-span-1' : 'md:col-span-2'">
          <mat-label>Motivo</mat-label>
          <input matInput formControlName="motivo" placeholder="Ej. Permiso, Vacaciones, etc." required>
        </mat-form-field>

        <div class="col-span-1 md:col-span-4 flex justify-end">
          <button mat-flat-button color="primary" [disabled]="form.invalid || loading()" (click)="agregarDia()">
            <mat-icon>block</mat-icon> {{ form.get('tipo')?.value === 'VACACIONES' ? 'Programar Vacaciones' : 'Bloquear Día' }}
          </button>
        </div>
      </form>

      <table mat-table [dataSource]="dias()" class="w-full">
        <ng-container matColumnDef="fecha">
          <th mat-header-cell *matHeaderCellDef> Fecha / Inicio </th>
          <td mat-cell *matCellDef="let element"> {{ element.fecha }} </td>
        </ng-container>

        <ng-container matColumnDef="fechaFin">
          <th mat-header-cell *matHeaderCellDef> Fecha Fin </th>
          <td mat-cell *matCellDef="let element"> {{ element.tipo === 'VACACIONES' ? element.fechaFin : '-' }} </td>
        </ng-container>

        <ng-container matColumnDef="tipo">
          <th mat-header-cell *matHeaderCellDef> Tipo </th>
          <td mat-cell *matCellDef="let element"> 
            <span class="badge" [ngClass]="element.tipo === 'VACACIONES' ? 'bg-blue-100 text-blue-800' : 'bg-red-100 text-red-800'">
              {{ element.tipo === 'VACACIONES' ? 'Vacaciones' : 'Bloqueo' }}
            </span>
          </td>
        </ng-container>

        <ng-container matColumnDef="motivo">
          <th mat-header-cell *matHeaderCellDef> Motivo </th>
          <td mat-cell *matCellDef="let element"> {{ element.motivo }} </td>
        </ng-container>

        <ng-container matColumnDef="acciones">
          <th mat-header-cell *matHeaderCellDef> Acciones </th>
          <td mat-cell *matCellDef="let element">
            <button mat-icon-button color="warn" (click)="eliminar(element.id)" title="Desbloquear">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        
        <tr class="mat-row" *matNoDataRow>
          <td class="mat-cell p-4 text-center text-gray-500" colspan="5">
            No hay días bloqueados o vacaciones registrados.
          </td>
        </tr>
      </table>
    </div>
  `,
  styles: [`
    .badge { padding: 4px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 500; }
  `]
})
export class DiasBloqueadosComponent implements OnInit {
  @Input() veterinarioId!: number;
  
  form: FormGroup;
  loading = signal(false);
  dias = signal<DiaBloqueadoResponse[]>([]);
  displayedColumns: string[] = ['fecha', 'fechaFin', 'tipo', 'motivo', 'acciones'];
  minDate = new Date(); // Bloquear días pasados

  constructor(
    private fb: FormBuilder,
    private diaBloqueadoService: DiaBloqueadoService,
    private snack: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.form = this.fb.group({
      tipo: ['DIA_BLOQUEADO', Validators.required],
      fecha: ['', Validators.required],
      fechaFin: [''],
      motivo: ['', Validators.required]
    });

    // Validar fechaFin si es vacaciones
    this.form.get('tipo')?.valueChanges.subscribe(val => {
      const fechaFinCtrl = this.form.get('fechaFin');
      if (val === 'VACACIONES') {
        fechaFinCtrl?.setValidators([Validators.required]);
      } else {
        fechaFinCtrl?.clearValidators();
        fechaFinCtrl?.setValue('');
      }
      fechaFinCtrl?.updateValueAndValidity();
    });
  }

  ngOnInit() {
    this.cargarDias();
  }

  cargarDias() {
    this.loading.set(true);
    this.diaBloqueadoService.listar().subscribe({
      next: (res) => {
        const delVet = res.filter(d => d.veterinarioId === this.veterinarioId);
        this.dias.set(delVet);
        this.loading.set(false);
      },
      error: () => {
        this.snack.open('Error al cargar datos', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  agregarDia() {
    if (this.form.invalid) return;
    this.loading.set(true);

    const f = new Date(this.form.value.fecha);
    const fechaFormat = f.getFullYear() + '-' + String(f.getMonth() + 1).padStart(2, '0') + '-' + String(f.getDate()).padStart(2, '0');

    let fechaFinFormat = undefined;
    if (this.form.value.tipo === 'VACACIONES' && this.form.value.fechaFin) {
      const ff = new Date(this.form.value.fechaFin);
      fechaFinFormat = ff.getFullYear() + '-' + String(ff.getMonth() + 1).padStart(2, '0') + '-' + String(ff.getDate()).padStart(2, '0');
    }

    const dto: DiaBloqueadoRequest = {
      veterinarioId: this.veterinarioId,
      tipo: this.form.value.tipo,
      fecha: fechaFormat,
      fechaFin: fechaFinFormat,
      motivo: this.form.value.motivo
    };

    this.diaBloqueadoService.crear(dto).subscribe({
      next: () => {
        this.snack.open('Registrado exitosamente', 'Cerrar', { duration: 3000 });
        this.cargarDias();
        const tipoActual = this.form.value.tipo;
        this.form.reset({ tipo: tipoActual });
      },
      error: (err) => {
        this.loading.set(false);
      }
    });
  }

  eliminar(id: number) {
    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: 'Desbloquear/Cancelar',
        message: '¿Estás seguro de que deseas eliminar este bloqueo? El veterinario volverá a estar disponible en esa fecha/rango.',
        confirmText: 'Sí, Eliminar',
        isDestructive: true
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);
        this.diaBloqueadoService.eliminar(id).subscribe({
          next: () => {
            this.snack.open('Eliminado', 'Cerrar', { duration: 3000 });
            this.cargarDias();
          },
          error: () => {
            this.snack.open('Error al eliminar', 'Cerrar', { duration: 3000 });
            this.loading.set(false);
          }
        });
      }
    });
  }
}
