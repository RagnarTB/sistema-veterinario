import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { PacienteService } from '../../core/services/paciente.service';
import { AuthService } from '../../core/services/auth.service';
import { PacienteResponse } from '../../core/models/models';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion/modal-confirmacion.component';
import { PacienteDialogComponent } from './paciente-dialog/paciente-dialog.component';
import { EspeciesDialogComponent } from './especies-dialog/especies-dialog.component';

@Component({
  selector: 'app-pacientes',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatDialogModule,
    MatSnackBarModule,
    DatePipe
  ],
  providers: [DatePipe],
  template: `
    <div class="page-container fade-in-up">
      <!-- Header de la vista -->
      <div class="page-header">
        <div>
          <h2 class="page-title">Historial de Pacientes</h2>
          <p class="page-subtitle">Gestiona la información de las mascotas de la clínica</p>
        </div>
        <div style="display: flex; gap: 12px; align-items: center;">
          <button mat-stroked-button class="btn-create btn-outline" (click)="abrirModalEspecies()">
            <mat-icon>pets</mat-icon>
            Gestión Especies
          </button>
          <button mat-flat-button color="primary" class="btn-create" (click)="abrirModal()">
            <mat-icon>add_circle</mat-icon>
            Nuevo Paciente
          </button>
        </div>
      </div>

      <!-- Barra de herramientas (Buscador) -->
      <div class="toolbar premium-card">
        <mat-form-field appearance="outline" class="search-field">
          <mat-icon matPrefix>search</mat-icon>
          <mat-label>Buscar mascota...</mat-label>
          <input matInput [formControl]="searchControl" placeholder="Nombre de la mascota o dueño" />
          @if (searchControl.value) {
            <button matSuffix mat-icon-button aria-label="Clear" (click)="searchControl.setValue('')">
              <mat-icon>close</mat-icon>
            </button>
          }
        </mat-form-field>
      </div>

      <!-- Tabla principal -->
      <div class="table-card premium-card">
        @if (loading()) {
          <div class="loading-overlay">
            <mat-spinner diameter="40"></mat-spinner>
          </div>
        }

        <div class="table-responsive">
          <table mat-table [dataSource]="dataSource()" class="custom-table">
            
            <!-- Nombre/Avatar Paciente Column -->
            <ng-container matColumnDef="nombre">
              <th mat-header-cell *matHeaderCellDef> Paciente </th>
              <td mat-cell *matCellDef="let element">
                <div class="user-cell">
                  <div class="user-avatar bg-accent">
                    <mat-icon>pets</mat-icon>
                  </div>
                  <div class="user-info">
                    <span class="user-name">{{ element.nombre }}</span>
                    <span class="user-doc">{{ element.especie }} {{ element.raza ? ' - '+element.raza : '' }}</span>
                  </div>
                </div>
              </td>
            </ng-container>

            <!-- Dueño Column -->
            <ng-container matColumnDef="dueno">
              <th mat-header-cell *matHeaderCellDef> Familiar / Cliente </th>
              <td mat-cell *matCellDef="let element">
                <div class="contact-info">
                  <span class="font-semibold">{{ element.clienteNombre || 'Dueño Registrado' }}</span>
                </div>
              </td>
            </ng-container>

            <!-- Edad/Nacimiento Column -->
            <ng-container matColumnDef="nacimiento">
              <th mat-header-cell *matHeaderCellDef> Fecha de Nacimiento </th>
              <td mat-cell *matCellDef="let element">
                <div class="contact-info">
                  <span>{{ element.fechaNacimiento | date:'mediumDate' }}</span>
                </div>
              </td>
            </ng-container>

            <!-- Estado Column -->
            <ng-container matColumnDef="estado">
              <th mat-header-cell *matHeaderCellDef> Estado </th>
              <td mat-cell *matCellDef="let element">
                <span class="status-badge" [ngClass]="element.activo ? 'status-active' : 'status-inactive'">
                  {{ element.activo ? 'Alta' : 'Baja' }}
                </span>
              </td>
            </ng-container>

            <!-- Acciones Column -->
            <ng-container matColumnDef="acciones">
              <th mat-header-cell *matHeaderCellDef style="text-align: right"> Acciones </th>
              <td mat-cell *matCellDef="let element" style="text-align: right">
                <button mat-icon-button color="primary" matTooltip="Editar" (click)="abrirModal(element)">
                  <mat-icon>edit</mat-icon>
                </button>
                @if(isAdmin) {
                  <button mat-icon-button [color]="element.activo ? 'warn' : 'accent'" 
                          [matTooltip]="element.activo ? 'Dar de Baja' : 'Dar de Alta'"
                          (click)="cambiarEstado(element)">
                    <mat-icon>{{ element.activo ? 'block' : 'check_circle' }}</mat-icon>
                  </button>
                }
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="premium-row" [class.row-disabled]="!row.activo"></tr>

            <!-- Estado Vacío -->
            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell empty-cell" [attr.colspan]="displayedColumns.length">
                <div class="empty-state">
                  <mat-icon>search_off</mat-icon>
                  <p>No se encontraron pacientes que coincidan con la búsqueda.</p>
                </div>
              </td>
            </tr>
          </table>
        </div>

        <mat-paginator
          [length]="totalElements()"
          [pageSize]="pageSize()"
          [pageSizeOptions]="[5, 10, 25, 50]"
          (page)="onPageChange($event)"
          aria-label="Seleccionar página">
        </mat-paginator>
      </div>
    </div>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    .page-title { font-size: 1.8rem; font-weight: 800; color: var(--text-primary); margin: 0; }
    .page-subtitle { color: var(--text-muted); margin-top: 4px; }
    .btn-create { height: 44px; display: flex; align-items: center; gap: 8px; font-weight: 600; border-radius: var(--radius-md); }
    .btn-outline { border-color: var(--border-color); color: var(--text-primary); }
    
    .toolbar { padding: 16px 24px; margin-bottom: 24px; display: flex; align-items: center; }
    .search-field { width: 100%; max-width: 400px; }
    ::ng-deep .search-field .mat-mdc-form-field-subscript-wrapper { display: none; }

    .table-card { position: relative; overflow: hidden; }
    .loading-overlay { position: absolute; inset: 0; background: rgba(11, 17, 32, 0.7); z-index: 10; display: flex; justify-content: center; align-items: center; backdrop-filter: blur(2px); }
    
    .table-responsive { width: 100%; overflow-x: auto; }
    .custom-table { width: 100%; }
    
    .user-cell { display: flex; align-items: center; gap: 16px; padding: 12px 0; }
    .user-avatar { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: white; flex-shrink: 0; }
    .bg-accent { background: linear-gradient(135deg, #8b5cf6, #c084fc); }
    .user-info { display: flex; flex-direction: column; }
    .user-name { font-weight: 600; color: var(--text-primary); }
    .user-doc { font-size: 0.8rem; color: var(--text-muted); }
    
    .contact-info { display: flex; flex-direction: column; gap: 4px; font-size: 0.85rem; }
    
    .status-badge { padding: 4px 12px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
    .status-active { background: rgba(16, 185, 129, 0.15); color: #10b981; }
    .status-inactive { background: rgba(239, 68, 68, 0.15); color: #ef4444; }
    
    .row-disabled { opacity: 0.6; }
    
    .empty-cell { padding: 48px !important; text-align: center; }
    .empty-state { display: flex; flex-direction: column; align-items: center; gap: 16px; color: var(--text-muted); }
    .empty-state mat-icon { font-size: 48px; width: 48px; height: 48px; opacity: 0.5; }
  `]
})
export class PacientesComponent implements OnInit {
  private pacienteService = inject(PacienteService);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  displayedColumns: string[] = ['nombre', 'dueno', 'nacimiento', 'estado', 'acciones'];
  dataSource = signal<PacienteResponse[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);
  loading = signal(false);

  searchControl = new FormControl('');
  isAdmin = this.authService.isAdmin();

  ngOnInit(): void {
    this.cargarPacientes();

    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex.set(0);
        this.cargarPacientes();
      });
  }

  cargarPacientes() {
    this.loading.set(true);
    const search = this.searchControl.value || '';
    
    this.pacienteService.listar(this.pageIndex(), this.pageSize(), search).subscribe({
      next: (pageData) => {
        this.dataSource.set(pageData.content);
        this.totalElements.set(pageData.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.snack.open('Error al cargar la lista de pacientes', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargarPacientes();
  }

  abrirModalEspecies() {
    this.dialog.open(EspeciesDialogComponent, {
      width: '600px',
      disableClose: true
    });
  }

  abrirModal(paciente?: PacienteResponse) {
    const dialogRef = this.dialog.open(PacienteDialogComponent, {
      width: '600px',
      data: paciente || null,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading.set(true);
        if (paciente) {
          this.pacienteService.actualizar(paciente.id, result).subscribe({
            next: () => {
              this.snack.open('Paciente actualizado correctamente', 'Cerrar', { duration: 3000 });
              this.cargarPacientes();
            },
            error: (err) => {
              this.loading.set(false);
              this.snack.open(err.error?.mensaje || 'Error al actualizar', 'Cerrar', { duration: 4000 });
            }
          });
        } else {
          this.pacienteService.crear(result).subscribe({
            next: () => {
              this.snack.open('Paciente registrado correctamente', 'Cerrar', { duration: 3000 });
              this.cargarPacientes();
            },
            error: (err) => {
              this.loading.set(false);
              this.snack.open(err.error?.mensaje || 'Error al crear', 'Cerrar', { duration: 4000 });
            }
          });
        }
      }
    });
  }

  cambiarEstado(paciente: PacienteResponse) {
    const nuevoEstado = !paciente.activo;
    const accion = nuevoEstado ? 'reactivará' : 'dará de baja';

    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: nuevoEstado ? 'Dar de Alta' : 'Dar de Baja',
        message: `¿Estás seguro de que quieres dar de ${nuevoEstado ? 'alta' : 'baja'} a ${paciente.nombre}? Se le ${accion} en el sistema clínico.`,
        confirmText: nuevoEstado ? 'Sí, Dar de Alta' : 'Sí, Dar de Baja',
        isDestructive: !nuevoEstado
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);
        this.pacienteService.cambiarEstado(paciente.id, nuevoEstado).subscribe({
          next: () => {
            this.snack.open(`Paciente ${nuevoEstado ? 'activado' : 'dado de baja'}`, 'Cerrar', { duration: 3000 });
            this.cargarPacientes();
          },
          error: () => {
            this.loading.set(false);
            this.snack.open('Error al cambiar el estado', 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }
}
