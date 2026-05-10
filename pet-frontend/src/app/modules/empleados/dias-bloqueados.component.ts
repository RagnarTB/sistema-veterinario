import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import { DiaBloqueadoService, DiaBloqueadoRequest, DiaBloqueadoResponse } from '../../core/services/dia-bloqueado.service';

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
    MatDatepickerModule,
    MatNativeDateModule
  ],
  template: `
    <div class="p-4">
      <h3 class="text-lg font-semibold mb-4 text-[var(--text-primary)]">Días Bloqueados (Permisos / Vacaciones)</h3>

      <form [formGroup]="form" class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
        <mat-form-field appearance="outline">
          <mat-label>Fecha</mat-label>
          <input matInput [matDatepicker]="picker" [min]="minDate" formControlName="fecha" required readonly>
          <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
          <mat-datepicker #picker></mat-datepicker>
        </mat-form-field>

        <mat-form-field appearance="outline" class="md:col-span-2">
          <mat-label>Motivo</mat-label>
          <input matInput formControlName="motivo" placeholder="Ej. Permiso médico, vacaciones, etc." required>
        </mat-form-field>

        <div class="col-span-1 md:col-span-3 flex justify-end">
          <button mat-flat-button color="primary" [disabled]="form.invalid || loading()" (click)="agregarDia()">
            <mat-icon>block</mat-icon> Bloquear Día
          </button>
        </div>
      </form>

      <table mat-table [dataSource]="dias()" class="w-full">
        <ng-container matColumnDef="fecha">
          <th mat-header-cell *matHeaderCellDef> Fecha </th>
          <td mat-cell *matCellDef="let element"> {{ element.fecha }} </td>
        </ng-container>

        <ng-container matColumnDef="motivo">
          <th mat-header-cell *matHeaderCellDef> Motivo </th>
          <td mat-cell *matCellDef="let element"> {{ element.motivo }} </td>
        </ng-container>

        <ng-container matColumnDef="acciones">
          <th mat-header-cell *matHeaderCellDef> Acciones </th>
          <td mat-cell *matCellDef="let element">
            <button mat-icon-button color="warn" (click)="eliminar(element.id)" title="Desbloquear día">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        
        <tr class="mat-row" *matNoDataRow>
          <td class="mat-cell p-4 text-center text-gray-500" colspan="3">
            No hay días bloqueados registrados.
          </td>
        </tr>
      </table>
    </div>
  `
})
export class DiasBloqueadosComponent implements OnInit {
  @Input() veterinarioId!: number;
  
  form: FormGroup;
  loading = signal(false);
  dias = signal<DiaBloqueadoResponse[]>([]);
  displayedColumns: string[] = ['fecha', 'motivo', 'acciones'];
  minDate = new Date(); // Bloquear días pasados

  constructor(
    private fb: FormBuilder,
    private diaBloqueadoService: DiaBloqueadoService,
    private snack: MatSnackBar
  ) {
    this.form = this.fb.group({
      fecha: ['', Validators.required],
      motivo: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.cargarDias();
  }

  cargarDias() {
    this.loading.set(true);
    this.diaBloqueadoService.listar().subscribe({
      next: (res) => {
        // En un caso real, el backend debería filtrar. Si no, filtramos aquí:
        const delVet = res.filter(d => d.veterinarioId === this.veterinarioId);
        this.dias.set(delVet);
        this.loading.set(false);
      },
      error: () => {
        this.snack.open('Error al cargar días bloqueados', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  agregarDia() {
    if (this.form.invalid) return;
    this.loading.set(true);

    // Formatear la fecha a YYYY-MM-DD
    const f = new Date(this.form.value.fecha);
    const fechaFormat = f.getFullYear() + '-' + String(f.getMonth() + 1).padStart(2, '0') + '-' + String(f.getDate()).padStart(2, '0');

    const dto: DiaBloqueadoRequest = {
      veterinarioId: this.veterinarioId,
      fecha: fechaFormat,
      motivo: this.form.value.motivo
    };

    this.diaBloqueadoService.crear(dto).subscribe({
      next: () => {
        this.snack.open('Día bloqueado exitosamente', 'Cerrar', { duration: 3000 });
        this.cargarDias();
        this.form.reset();
      },
      error: (err) => {
        this.snack.open(err.error?.mensaje || 'Error al bloquear día', 'Cerrar', { duration: 4000 });
        this.loading.set(false);
      }
    });
  }

  eliminar(id: number) {
    this.loading.set(true);
    this.diaBloqueadoService.eliminar(id).subscribe({
      next: () => {
        this.snack.open('Día desbloqueado', 'Cerrar', { duration: 3000 });
        this.cargarDias();
      },
      error: () => {
        this.snack.open('Error al eliminar', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }
}
