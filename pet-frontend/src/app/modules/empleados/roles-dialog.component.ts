import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';

import { RolService } from '../../core/services/rol.service';
import { RolResponse } from '../../core/models/models';

@Component({
  selector: 'app-roles-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Gestión de Roles</h2>
    
    <mat-dialog-content>
      <!-- Formulario para Nuevo Rol -->
      <form [formGroup]="form" (ngSubmit)="agregarRol()" class="flex gap-2 items-center mb-4">
        <div class="form-group flex-1 m-0">
          <input
            type="text"
            formControlName="nombre"
            class="form-control uppercase"
            placeholder="NUEVO_ROL"
            style="text-transform: uppercase;"
          />
        </div>
        <button type="submit" class="btn btn-primary" [disabled]="form.invalid || loading()">
          <mat-icon>add</mat-icon> Agregar
        </button>
      </form>

      @if (loading()) {
        <div class="flex justify-center p-4">
          <mat-spinner diameter="30"></mat-spinner>
        </div>
      } @else {
        <div class="roles-list">
          @for (rol of roles(); track rol.id) {
            <div class="role-item flex justify-between items-center p-3 border-b border-[var(--border-color)]">
              <span class="font-medium text-[var(--text-primary)]">
                {{ rol.nombre.replace('ROLE_', '') }}
                @if (!rol.activo) {
                  <span class="badge ml-2" style="background: rgba(239, 68, 68, 0.1); color: #ef4444; font-size: 0.7rem; padding: 2px 6px; border-radius: 4px;">Inactivo</span>
                }
              </span>
              
              <div class="flex gap-2 items-center">
                @if (isProtegido(rol.nombre)) {
                  <span class="badge" style="background: rgba(100, 116, 139, 0.1); color: var(--text-muted); font-size: 0.75rem; padding: 2px 6px; border-radius: 4px;">
                    Protegido
                  </span>
                } @else {
                  <button type="button" class="btn btn-icon" 
                          [class.text-green-500]="!rol.activo" [class.hover:bg-green-50]="!rol.activo"
                          [class.text-red-500]="rol.activo" [class.hover:bg-red-50]="rol.activo"
                          (click)="cambiarEstado(rol)" [title]="rol.activo ? 'Desactivar Rol' : 'Activar Rol'">
                    <mat-icon>{{ rol.activo ? 'block' : 'check_circle' }}</mat-icon>
                  </button>
                  <button type="button" class="btn btn-icon text-red-500 hover:bg-red-50" 
                          (click)="eliminarRol(rol)" title="Eliminar Rol permanentemente">
                    <mat-icon>delete</mat-icon>
                  </button>
                }
              </div>
            </div>
          } @empty {
            <div class="text-center p-4 text-[var(--text-muted)]">No hay roles registrados</div>
          }
        </div>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cerrar</button>
    </mat-dialog-actions>
  `,
  styles: [`
    :host {
      display: block;
      min-width: 400px;
    }
    .role-item:last-child {
      border-bottom: none;
    }
  `]
})
export class RolesDialogComponent implements OnInit {
  roles = signal<RolResponse[]>([]);
  loading = signal(false);
  form: FormGroup;

  rolesProtegidos = ['ROLE_ADMIN', 'ROLE_CLIENTE', 'ROLE_VETERINARIO', 'ROLE_RECEPCIONISTA'];

  constructor(
    private dialogRef: MatDialogRef<RolesDialogComponent>,
    private rolService: RolService,
    private fb: FormBuilder,
    private snack: MatSnackBar
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.cargarRoles();
  }

  cargarRoles() {
    this.loading.set(true);
    this.rolService.listarTodos().subscribe({
      next: (data: any) => {
        this.roles.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.snack.open('Error al cargar roles', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  isProtegido(nombre: string): boolean {
    return this.rolesProtegidos.includes(nombre);
  }

  agregarRol() {
    if (this.form.invalid) return;

    const nombreRol = this.form.value.nombre.toUpperCase().trim();
    this.loading.set(true);

    this.rolService.crear({ nombre: nombreRol }).subscribe({
      next: () => {
        this.snack.open('Rol agregado correctamente', 'Cerrar', { duration: 3000 });
        this.form.reset();
        this.cargarRoles();
      },
      error: (err: any) => {
        this.snack.open(err.error?.mensaje || 'Error al agregar rol', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  eliminarRol(rol: RolResponse) {
    if (!confirm(`¿Está seguro de eliminar el rol ${rol.nombre}?`)) return;

    this.loading.set(true);
    this.rolService.eliminar(rol.id).subscribe({
      next: () => {
        this.snack.open('Rol eliminado correctamente', 'Cerrar', { duration: 3000 });
        this.cargarRoles();
      },
      error: (err: any) => {
        this.snack.open(err.error?.mensaje || 'Error al eliminar rol', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  cambiarEstado(rol: RolResponse) {
    const accion = rol.activo ? 'desactivar' : 'activar';
    if (!confirm(`¿Está seguro de ${accion} el rol ${rol.nombre.replace('ROLE_', '')}?`)) return;

    this.loading.set(true);
    this.rolService.cambiarEstado(rol.id, !rol.activo).subscribe({
      next: () => {
        this.snack.open(`Rol ${!rol.activo ? 'activado' : 'desactivado'} correctamente`, 'Cerrar', { duration: 3000 });
        this.cargarRoles();
      },
      error: (err: any) => {
        this.snack.open(err.error?.mensaje || `Error al ${accion} rol`, 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }
}
