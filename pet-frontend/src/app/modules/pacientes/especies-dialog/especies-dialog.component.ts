import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { EspecieService } from '../../../core/services/especie.service';
import { EspecieResponse } from '../../../core/models/models';
import { ModalConfirmacionComponent } from '../../../shared/components/modal-confirmacion/modal-confirmacion.component';

@Component({
  selector: 'app-especies-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  template: `
    <h2 mat-dialog-title class="dialog-title">
      <span class="material-icons-round">pets</span>
      Gestión de Especies
    </h2>

    <mat-dialog-content class="dialog-content-scroll">
      <div class="add-container">
        <mat-form-field appearance="outline" class="add-input">
          <mat-label>Nueva Especie</mat-label>
          <input matInput [formControl]="nuevoNombre" placeholder="Ej. Canino, Felino..." (keyup.enter)="agregar()" />
        </mat-form-field>
        <button mat-flat-button color="primary" class="btn-add" (click)="agregar()" [disabled]="nuevoNombre.invalid || loading()">
          <mat-icon>add</mat-icon> Guardar
        </button>
      </div>

      <div class="table-container premium-card">
        @if (loading()) {
          <div class="loading-overlay">
            <mat-spinner diameter="40"></mat-spinner>
          </div>
        }
        
        <table mat-table [dataSource]="dataSource()" class="custom-table">
          <ng-container matColumnDef="nombre">
            <th mat-header-cell *matHeaderCellDef> Especie </th>
            <td mat-cell *matCellDef="let element" class="cell-nombre"> 
              {{ element.nombre }} 
              <span class="status-badge" [ngClass]="element.activo ? 'status-active' : 'status-inactive'">
                {{ element.activo ? 'Activa' : 'Inactiva' }}
              </span>
            </td>
          </ng-container>

          <ng-container matColumnDef="acciones">
            <th mat-header-cell *matHeaderCellDef style="text-align: right"> Acciones </th>
            <td mat-cell *matCellDef="let element" style="text-align: right">
              <button mat-icon-button [color]="element.activo ? 'warn' : 'accent'" 
                      [matTooltip]="element.activo ? 'Desactivar' : 'Activar'"
                      (click)="cambiarEstado(element)">
                <mat-icon>{{ element.activo ? 'block' : 'check_circle' }}</mat-icon>
              </button>
              <button mat-icon-button color="warn" matTooltip="Eliminar Permanente" (click)="eliminarFisicamente(element)">
                <mat-icon>delete_forever</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;" [class.row-disabled]="!row.activo"></tr>
          
          <tr class="mat-row" *matNoDataRow>
            <td class="mat-cell empty-cell" [attr.colspan]="displayedColumns.length">
              <div class="empty-state">
                <mat-icon>category</mat-icon>
                <p>No hay especies registradas</p>
              </div>
            </td>
          </tr>
        </table>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end" class="dialog-actions">
      <button mat-flat-button mat-dialog-close class="btn-cancel">Cerrar Panel</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-title { display: flex; align-items: center; gap: 10px; font-weight: 700; color: var(--text-primary); margin: 0; padding: 24px 24px 12px; }
    .dialog-title .material-icons-round { color: var(--color-primary-400); font-size: 28px; }
    .dialog-content-scroll { max-height: 70vh; padding: 0 24px 24px; overflow-y: auto; }
    .dialog-actions { padding: 0 24px 24px; }
    .btn-cancel { background: var(--bg-card); color: var(--text-primary); }
    
    .add-container { display: flex; gap: 16px; margin-bottom: 24px; align-items: flex-start; }
    .add-input { flex: 1; margin-bottom: -16px; }
    .btn-add { height: 52px; font-weight: 600; }
    
    .table-container { position: relative; overflow: hidden; }
    .loading-overlay { position: absolute; inset: 0; background: rgba(11, 17, 32, 0.7); z-index: 10; display: flex; justify-content: center; align-items: center; backdrop-filter: blur(2px); }
    .custom-table { width: 100%; }
    
    .cell-nombre { font-weight: 600; color: var(--text-primary); display:flex; align-items:center; gap: 12px; }
    .status-badge { padding: 2px 10px; border-radius: 12px; font-size: 0.7rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; margin-top:2px;}
    .status-active { background: rgba(16, 185, 129, 0.15); color: #10b981; }
    .status-inactive { background: rgba(239, 68, 68, 0.15); color: #ef4444; }
    
    .row-disabled { opacity: 0.6; }
    .empty-cell { padding: 32px !important; text-align: center; }
    .empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; color: var(--text-muted); }
    .empty-state mat-icon { font-size: 36px; width: 36px; height: 36px; opacity: 0.5; }
  `]
})
export class EspeciesDialogComponent implements OnInit {
  private especieService = inject(EspecieService);
  private snack = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  dataSource = signal<EspecieResponse[]>([]);
  displayedColumns: string[] = ['nombre', 'acciones'];
  loading = signal(false);

  nuevoNombre = new FormControl('', [Validators.required, Validators.minLength(2)]);

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.loading.set(true);
    this.especieService.listar().subscribe({
      next: (res) => {
        this.dataSource.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.snack.open('Error al cargar especies', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  agregar() {
    if (this.nuevoNombre.invalid) return;
    this.loading.set(true);
    const nombreEspecie = this.nuevoNombre.value?.trim().toUpperCase();
    this.especieService.crear({ nombre: nombreEspecie }).subscribe({
      next: () => {
        this.snack.open('Especie guardada', 'Exito', { duration: 3000 });
        this.nuevoNombre.reset();
        this.cargar();
      },
      error: (err) => {
        this.snack.open(err.error?.mensaje || 'Error al guardar', 'Cerrar', { duration: 4000 });
        this.loading.set(false);
      }
    });
  }

  cambiarEstado(especie: EspecieResponse) {
    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: especie.activo ? 'Desactivar Especie' : 'Activar Especie',
        message: `¿Seguro que deseas ${especie.activo ? 'desactivar' : 'activar'} la especie ${especie.nombre}?`,
        confirmText: especie.activo ? 'Desactivar' : 'Activar',
        isDestructive: especie.activo
      }
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.loading.set(true);
        this.especieService.cambiarEstado(especie.id).subscribe({
          next: () => {
            this.snack.open('Estado modificado', 'Ok', { duration: 3000 });
            this.cargar();
          },
          error: (err) => {
            this.loading.set(false);
            this.snack.open(err.error?.mensaje || 'No se puede desactivar (probablemente esté en uso)', 'Cerrar', { duration: 5000 });
          }
        });
      }
    });
  }

  eliminarFisicamente(especie: EspecieResponse) {
    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: 'Borrado Físico Definitivo',
        message: `¿ELIMINAR PERMANENTEMENTE la especie ${especie.nombre}? No debe tener pacientes afiliados.`,
        confirmText: 'Borrar Definitivamente',
        isDestructive: true
      }
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.loading.set(true);
        this.especieService.eliminarFisicamente(especie.id).subscribe({
          next: () => {
            this.snack.open('Especie eliminada', 'Ok', { duration: 3000 });
            this.cargar();
          },
          error: (err) => {
            this.loading.set(false);
            // This is where our backend throws the error because it is tied!
            this.snack.open(err.error?.mensaje || 'No se puede borrar porque está en uso por pacientes.', 'Cerrar', { duration: 5000 });
          }
        });
      }
    });
  }
}
