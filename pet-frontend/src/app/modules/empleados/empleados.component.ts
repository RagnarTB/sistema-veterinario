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

import { EmpleadoService } from '../../core/services/empleado.service';
import { EmpleadoResponse } from '../../core/models/models';
import { EmpleadoDialogComponent } from './empleado-dialog.component';
import { RolesDialogComponent } from './roles-dialog.component';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion/modal-confirmacion.component';

@Component({
  selector: 'app-empleados',
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
          <h1 class="page-title text-2xl font-bold text-[var(--text-primary)]">Empleados</h1>
          <p class="page-description text-[var(--text-muted)]">Gestión de personal y accesos</p>
        </div>
        <div class="flex gap-2">
          <button mat-stroked-button color="primary" (click)="gestionarRoles()">
            <mat-icon>shield</mat-icon> Roles
          </button>
          <button mat-flat-button color="primary" (click)="abrirModal()">
            <mat-icon>add</mat-icon> Nuevo Empleado
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
              placeholder="Buscar por DNI, nombre o apellido..." 
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
              <th mat-header-cell *matHeaderCellDef> Empleado </th>
              <td mat-cell *matCellDef="let element">
                <div class="flex flex-col">
                  <span class="font-medium text-[var(--text-primary)]">{{element.nombre}} {{element.apellido}}</span>
                  <span class="text-xs text-[var(--text-muted)]">{{element.email}}</span>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="dni">
              <th mat-header-cell *matHeaderCellDef> DNI </th>
              <td mat-cell *matCellDef="let element"> {{element.dni}} </td>
            </ng-container>

            <ng-container matColumnDef="roles">
              <th mat-header-cell *matHeaderCellDef> Roles </th>
              <td mat-cell *matCellDef="let element">
                <div class="flex flex-wrap gap-1">
                  @for (rol of element.nombresRoles; track rol) {
                    <span class="badge" style="background: rgba(0, 189, 189, 0.1); color: var(--color-primary-600); font-size: 0.75rem; padding: 2px 6px; border-radius: 4px;">
                      {{ rol.replace('ROLE_', '') }}
                    </span>
                  }
                </div>
              </td>
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
                </div>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="hover:bg-gray-50 transition-colors"></tr>
            
            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell p-4 text-center text-[var(--text-muted)]" colspan="5">
                No se encontraron empleados
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
export class EmpleadosComponent implements OnInit {
  displayedColumns: string[] = ['nombre', 'dni', 'roles', 'estado', 'acciones'];
  dataSource = signal<EmpleadoResponse[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);
  loading = signal(false);

  searchControl = new FormControl('');

  constructor(
    private empleadoService: EmpleadoService,
    private dialog: MatDialog,
    private snack: MatSnackBar
  ) {
    this.searchControl.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => {
      this.pageIndex.set(0);
      this.cargarEmpleados();
    });
  }

  ngOnInit() {
    this.cargarEmpleados();
  }

  cargarEmpleados() {
    this.loading.set(true);
    const searchTerm = this.searchControl.value || '';
    this.empleadoService.listar(this.pageIndex(), this.pageSize(), searchTerm).subscribe({
      next: (page: any) => {
        this.dataSource.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.snack.open('Error al cargar empleados', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargarEmpleados();
  }

  abrirModal(empleado?: EmpleadoResponse) {
    const dialogRef = this.dialog.open(EmpleadoDialogComponent, {
      width: '600px',
      data: { empleado },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.cargarEmpleados();
    });
  }

  gestionarRoles() {
    this.dialog.open(RolesDialogComponent, {
      width: '500px',
      disableClose: false
    });
  }

  cambiarEstado(empleado: EmpleadoResponse) {
    const nuevoEstado = !empleado.activo;
    const accion = nuevoEstado ? 'concederá acceso' : 'revocará el acceso';

    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: nuevoEstado ? 'Activar Empleado' : 'Desactivar Empleado',
        message: `¿Estás seguro de ${nuevoEstado ? 'activar' : 'desactivar'} a ${empleado.nombre}? Se le ${accion} al sistema.`,
        confirmText: nuevoEstado ? 'Sí, Activar' : 'Sí, Desactivar',
        isDestructive: !nuevoEstado
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);
        this.empleadoService.cambiarEstado(empleado.id, nuevoEstado).subscribe({
          next: () => {
            this.snack.open(`Empleado ${nuevoEstado ? 'activado' : 'desactivado'}`, 'Cerrar', { duration: 3000 });
            this.cargarEmpleados();
          },
          error: () => {
            this.snack.open(`Error al cambiar el estado del empleado`, 'Cerrar', { duration: 3000 });
            this.loading.set(false);
          }
        });
      }
    });
  }
}
