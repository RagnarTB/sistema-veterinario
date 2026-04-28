import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { SedeService } from '../../core/services/sede.service';
import { SedeResponse } from '../../core/models/models';
import { SedeDialogComponent } from './sede-dialog.component';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion/modal-confirmacion.component';

@Component({
  selector: 'app-sedes',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  template: `
    <div class="page-container fade-in-up">
      <div class="page-header flex justify-between items-center mb-6">
        <div>
          <h1 class="page-title text-2xl font-bold text-[var(--text-primary)]">Sedes</h1>
          <p class="page-description text-[var(--text-muted)]">Gestión de sucursales y ubicaciones</p>
        </div>
        <div class="flex gap-2">
          <button mat-flat-button color="primary" (click)="abrirModal()">
            <mat-icon>add</mat-icon> Nueva Sede
          </button>
        </div>
      </div>

      <div class="card p-4">
        <!-- Buscador -->
        <div class="search-bar mb-4">
          <div class="input-wrapper" style="max-width: 400px; position: relative;">
            <mat-icon class="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)]">search</mat-icon>
            <input 
              [formControl]="searchControl" 
              class="form-control pl-10 w-full" 
              placeholder="Buscar por nombre o dirección..." 
            />
          </div>
        </div>

        <!-- Tabla -->
        <div class="table-container relative">
          @if (loading()) {
            <div class="absolute inset-0 bg-white/50 z-10 flex justify-center items-center backdrop-blur-sm">
              <mat-spinner diameter="40"></mat-spinner>
            </div>
          }
          
          <table mat-table [dataSource]="dataSource()" class="w-full">
            
            <ng-container matColumnDef="nombre">
              <th mat-header-cell *matHeaderCellDef> Nombre de la Sede </th>
              <td mat-cell *matCellDef="let element">
                <div class="flex flex-col">
                  <span class="font-medium text-[var(--text-primary)]">{{element.nombre}}</span>
                  <span class="text-xs text-[var(--text-muted)]">{{element.direccion}}</span>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="telefono">
              <th mat-header-cell *matHeaderCellDef> Teléfono </th>
              <td mat-cell *matCellDef="let element"> {{element.telefono}} </td>
            </ng-container>

            <ng-container matColumnDef="estado">
              <th mat-header-cell *matHeaderCellDef> Estado </th>
              <td mat-cell *matCellDef="let element">
                <span class="badge" [class.bg-green-100]="element.activo" [class.text-green-800]="element.activo"
                                   [class.bg-red-100]="!element.activo" [class.text-red-800]="!element.activo">
                  {{ element.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="acciones">
              <th mat-header-cell *matHeaderCellDef> Acciones </th>
              <td mat-cell *matCellDef="let element">
                <div class="flex gap-2">
                  <button type="button" class="btn btn-icon text-blue-500 hover:bg-blue-50" (click)="abrirModal(element)" title="Editar">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button type="button" class="btn btn-icon" 
                          [class.text-red-500]="element.activo" [class.hover:bg-red-50]="element.activo"
                          [class.text-green-500]="!element.activo" [class.hover:bg-green-50]="!element.activo"
                          (click)="cambiarEstado(element)" [title]="element.activo ? 'Desactivar' : 'Activar'">
                    <mat-icon>{{ element.activo ? 'block' : 'check_circle' }}</mat-icon>
                  </button>
                  <button type="button" class="btn btn-icon text-red-600 hover:bg-red-50" (click)="eliminarFisico(element)" title="Eliminar Permanentemente">
                    <mat-icon>delete_forever</mat-icon>
                  </button>
                </div>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="hover:bg-gray-50 transition-colors"></tr>
            
            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell p-4 text-center text-[var(--text-muted)]" colspan="4">
                No se encontraron sedes
              </td>
            </tr>
          </table>

          <mat-paginator 
            [length]="totalElements()"
            [pageSize]="pageSize()"
            [pageSizeOptions]="[5, 10, 25]"
            (page)="onPageChange($event)"
            showFirstLastButtons>
          </mat-paginator>
        </div>
      </div>
    </div>
  `
})
export class SedesComponent implements OnInit {
  displayedColumns: string[] = ['nombre', 'telefono', 'estado', 'acciones'];
  dataSource = signal<SedeResponse[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);
  loading = signal(false);

  searchControl = new FormControl('');

  constructor(
    private sedeService: SedeService,
    private dialog: MatDialog,
    private snack: MatSnackBar
  ) {
    this.searchControl.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => {
      this.pageIndex.set(0);
      this.cargarSedes();
    });
  }

  ngOnInit() {
    this.cargarSedes();
  }

  cargarSedes() {
    this.loading.set(true);
    const searchTerm = this.searchControl.value || '';
    this.sedeService.listar(this.pageIndex(), this.pageSize(), searchTerm).subscribe({
      next: (page: any) => {
        this.dataSource.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.snack.open('Error al cargar sedes', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargarSedes();
  }

  abrirModal(sede?: SedeResponse) {
    const dialogRef = this.dialog.open(SedeDialogComponent, {
      width: '500px',
      data: { sede },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.cargarSedes();
    });
  }

  cambiarEstado(sede: SedeResponse) {
    const nuevoEstado = !sede.activo;
    
    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: nuevoEstado ? 'Activar Sede' : 'Desactivar Sede',
        message: `¿Estás seguro de ${nuevoEstado ? 'activar' : 'desactivar'} la sede ${sede.nombre}?`,
        confirmText: nuevoEstado ? 'Sí, Activar' : 'Sí, Desactivar',
        isDestructive: !nuevoEstado
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);
        this.sedeService.cambiarEstado(sede.id, nuevoEstado).subscribe({
          next: () => {
            this.snack.open(`Sede ${nuevoEstado ? 'activada' : 'desactivada'}`, 'Cerrar', { duration: 3000 });
            this.cargarSedes();
          },
          error: () => {
            this.snack.open(`Error al cambiar estado de la sede`, 'Cerrar', { duration: 3000 });
            this.loading.set(false);
          }
        });
      }
    });
  }

  eliminarFisico(sede: SedeResponse) {
    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: 'Eliminar Sede',
        message: `¿Está completamente seguro de eliminar la sede ${sede.nombre}? Esta acción no se puede deshacer y fallará si hay empleados asignados.`,
        confirmText: 'Sí, Eliminar',
        isDestructive: true
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);
        this.sedeService.eliminar(sede.id).subscribe({
          next: () => {
            this.snack.open('Sede eliminada permanentemente', 'Cerrar', { duration: 3000 });
            this.cargarSedes();
          },
          error: (err: any) => {
            this.snack.open(err.error?.mensaje || 'Error al eliminar sede. Es posible que esté en uso.', 'Cerrar', { duration: 4000 });
            this.loading.set(false);
          }
        });
      }
    });
  }
}
