import { Component, OnInit, ViewChild, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
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

import { ClienteService } from '../../core/services/cliente.service';
import { AuthService } from '../../core/services/auth.service';
import { ClienteResponse } from '../../core/models/models';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion/modal-confirmacion.component';
import { ClienteDialogComponent } from './cliente-dialog/cliente-dialog.component';

@Component({
  selector: 'app-clientes',
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
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  template: `
    <div class="page-container fade-in-up">
      <!-- Header de la vista -->
      <div class="page-header">
        <div>
          <h2 class="page-title">Directorio de Clientes</h2>
          <p class="page-subtitle">Gestiona la información de los dueños de pacientes</p>
        </div>
        <button mat-flat-button color="primary" class="btn-create" (click)="abrirModalCliente()">
          <span class="material-icons-round">person_add</span>
          Nuevo Cliente
        </button>
      </div>

      <!-- Barra de herramientas (Buscador) -->
      <div class="toolbar premium-card">
        <mat-form-field appearance="outline" class="search-field">
          <mat-icon matPrefix>search</mat-icon>
          <mat-label>Buscar cliente...</mat-label>
          <input matInput [formControl]="searchControl" placeholder="Nombre, apellido o DNI" />
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
            
            <!-- Nombre/Avatar Column -->
            <ng-container matColumnDef="nombre">
              <th mat-header-cell *matHeaderCellDef> Cliente </th>
              <td mat-cell *matCellDef="let element">
                <div class="user-cell">
                  <div class="user-avatar" [ngClass]="element.activo ? 'bg-primary' : 'bg-disabled'">
                    {{ element.nombre ? element.nombre.charAt(0).toUpperCase() : '?' }}{{ element.apellido ? element.apellido.charAt(0).toUpperCase() : '' }}
                  </div>
                  <div class="user-info">
                    <span class="user-name">{{ element.nombre ? element.nombre + ' ' + element.apellido : 'Usuario Pendiente' }}</span>
                    <span class="user-doc">DNI: {{ element.dni || 'Pendiente' }}</span>
                  </div>
                </div>
              </td>
            </ng-container>

            <!-- Contacto Column -->
            <ng-container matColumnDef="contacto">
              <th mat-header-cell *matHeaderCellDef> Contacto </th>
              <td mat-cell *matCellDef="let element">
                <div class="contact-info">
                  <span><mat-icon class="icon-small">phone</mat-icon> {{ element.telefono }}</span>
                  <span class="text-muted"><mat-icon class="icon-small">mail</mat-icon> {{ element.email }}</span>
                </div>
              </td>
            </ng-container>

            <!-- Estado Column -->
            <ng-container matColumnDef="estado">
              <th mat-header-cell *matHeaderCellDef> Estado </th>
              <td mat-cell *matCellDef="let element">
                <span class="status-badge" [ngClass]="element.activo ? 'status-active' : 'status-inactive'">
                  {{ element.activo ? 'Activo' : 'Pendiente' }}
                </span>
              </td>
            </ng-container>

            <!-- Acciones Column -->
            <ng-container matColumnDef="acciones">
              <th mat-header-cell *matHeaderCellDef style="text-align: right"> Acciones </th>
              <td mat-cell *matCellDef="let element" style="text-align: right">
                <button mat-icon-button color="primary" matTooltip="Editar" (click)="abrirModalCliente(element)">
                  <mat-icon>edit</mat-icon>
                </button>
                @if(isAdmin && element.verificado) {
                  <button mat-icon-button [color]="element.activo ? 'warn' : 'accent'" 
                          [matTooltip]="element.activo ? 'Desactivar' : 'Activar'"
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
                  <p>No se encontraron clientes que coincidan con la búsqueda.</p>
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
    .btn-create .material-icons-round { font-size: 20px; }
    
    .toolbar { padding: 16px 24px; margin-bottom: 24px; display: flex; align-items: center; }
    .search-field { width: 100%; max-width: 400px; }
    ::ng-deep .search-field .mat-mdc-form-field-subscript-wrapper { display: none; }

    .table-card { position: relative; overflow: hidden; }
    .loading-overlay { position: absolute; inset: 0; background: rgba(11, 17, 32, 0.7); z-index: 10; display: flex; justify-content: center; align-items: center; backdrop-filter: blur(2px); }
    
    .table-responsive { width: 100%; overflow-x: auto; }
    .custom-table { width: 100%; }
    
    .user-cell { display: flex; align-items: center; gap: 16px; padding: 12px 0; }
    .user-avatar { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: white; flex-shrink: 0; }
    .bg-primary { background: linear-gradient(135deg, var(--color-primary-600), var(--color-primary-400)); }
    .bg-disabled { background: #64748b; }
    .user-info { display: flex; flex-direction: column; }
    .user-name { font-weight: 600; color: var(--text-primary); }
    .user-doc { font-size: 0.8rem; color: var(--text-muted); }
    
    .contact-info { display: flex; flex-direction: column; gap: 4px; font-size: 0.85rem; }
    .contact-info span { display: flex; align-items: center; gap: 6px; }
    .icon-small { font-size: 16px; width: 16px; height: 16px; opacity: 0.7; }
    
    .status-badge { padding: 4px 12px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
    .status-active { background: rgba(16, 185, 129, 0.15); color: #10b981; }
    .status-inactive { background: rgba(239, 68, 68, 0.15); color: #ef4444; }
    
    .row-disabled { opacity: 0.6; }
    
    .empty-cell { padding: 48px !important; text-align: center; }
    .empty-state { display: flex; flex-direction: column; align-items: center; gap: 16px; color: var(--text-muted); }
    .empty-state mat-icon { font-size: 48px; width: 48px; height: 48px; opacity: 0.5; }
  `]
})
export class ClientesComponent implements OnInit {
  private clienteService = inject(ClienteService);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  displayedColumns: string[] = ['nombre', 'contacto', 'estado', 'acciones'];
  dataSource = signal<ClienteResponse[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);
  loading = signal(false);

  searchControl = new FormControl('');
  isAdmin = this.authService.isAdmin();

  ngOnInit(): void {
    this.cargarClientes();

    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex.set(0);
        this.cargarClientes();
      });
  }

  cargarClientes() {
    this.loading.set(true);
    const search = this.searchControl.value || '';
    
    this.clienteService.listar(this.pageIndex(), this.pageSize(), search).subscribe({
      next: (pageData) => {
        this.dataSource.set(pageData.content);
        this.totalElements.set(pageData.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.snack.open('Error al cargar la lista de clientes', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargarClientes();
  }

  abrirModalCliente(cliente?: ClienteResponse) {
    const dialogRef = this.dialog.open(ClienteDialogComponent, {
      width: '600px',
      data: cliente || null,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading.set(true);
        if (cliente) {
          this.clienteService.actualizar(cliente.id, result).subscribe({
            next: () => {
              this.snack.open('Cliente actualizado correctamente', 'Cerrar', { duration: 3000 });
              this.cargarClientes();
            },
            error: (err) => {
              this.loading.set(false);
              this.snack.open(err.error?.mensaje || 'Error al actualizar', 'Cerrar', { duration: 4000 });
            }
          });
        } else {
          this.clienteService.crear(result).subscribe({
            next: () => {
              this.snack.open('Cliente creado correctamente', 'Cerrar', { duration: 3000 });
              this.cargarClientes();
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

  cambiarEstado(cliente: ClienteResponse) {
    const nuevoEstado = !cliente.activo;
    const accion = nuevoEstado ? 'concederá acceso' : 'revocará el acceso';

    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: nuevoEstado ? 'Activar Cliente' : 'Desactivar Cliente',
        message: `¿Estás seguro de ${nuevoEstado ? 'activar' : 'desactivar'} a ${cliente.nombre} ${cliente.apellido}? Se le ${accion} al sistema.`,
        confirmText: nuevoEstado ? 'Sí, Activar' : 'Sí, Desactivar',
        isDestructive: !nuevoEstado
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);
        this.clienteService.cambiarEstado(cliente.id, nuevoEstado).subscribe({
          next: () => {
            this.snack.open(`Cliente ${nuevoEstado ? 'activado' : 'desactivado'}`, 'Cerrar', { duration: 3000 });
            this.cargarClientes();
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
