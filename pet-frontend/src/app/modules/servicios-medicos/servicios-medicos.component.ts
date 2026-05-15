import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { ServicioMedicoService } from './servicio-medico.service';
import { ServicioMedicoResponse, TipoServicio } from '../../core/models/models';
import { ServicioMedicoDialogComponent } from './servicio-medico-dialog.component';
import { InsumosServicioDialogComponent } from './insumos-servicio-dialog.component';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion/modal-confirmacion.component';

@Component({
  selector: 'app-servicios-medicos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatTabsModule,
    MatButtonModule
  ],
  template: `
    <div class="page-container fade-in-up">
      
      <!-- Header -->
      <div class="page-header flex justify-between items-center mb-6">
        <div>
          <h1 class="page-title text-2xl font-bold text-[var(--text-primary)]">Servicios Médicos</h1>
          <p class="page-description text-[var(--text-muted)]">Catálogo de consultas, cirugías y plantillas de insumos</p>
        </div>
        <button mat-flat-button color="primary" (click)="abrirDialogo()">
          <span class="material-icons-round mr-1">add</span> Nuevo Servicio
        </button>
      </div>

      <!-- Filtros y Búsqueda -->
      <div class="card p-4 mb-6">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex-1 min-w-[300px] relative">
            <span class="material-icons-round absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">search</span>
            <input 
              type="text" 
              class="form-control w-full pl-10" 
              placeholder="Buscar por nombre o descripción..."
              [ngModel]="filtroNombre()"
              (ngModelChange)="filtroNombre.set($event)"
            >
          </div>
          
          <div class="w-full md:w-64 relative">
            <span class="material-icons-round absolute left-3 top-1/2 -translate-y-1/2 text-blue-400" style="font-size: 18px;">category</span>
            <select class="form-control w-full pl-10" [ngModel]="filtroTipo()" (ngModelChange)="filtroTipo.set($event)">
              <option value="">Todos los Tipos</option>
              @for (tipo of tipos; track tipo) {
                <option [value]="tipo">{{ tipo }}</option>
              }
            </select>
          </div>
        </div>
      </div>

      <!-- Tabs y Tabla -->
      <div class="card p-0 overflow-hidden">
        <mat-tab-group class="custom-tabs" (selectedTabChange)="onTabChange($event)">
          <mat-tab label="Servicios Activos">
            <ng-template matTabContent>
              @if (loading()) {
                <div class="flex justify-center p-12">
                  <div class="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full"></div>
                </div>
              } @else {
                <div class="table-container">
                  <table class="table w-full">
                    <thead>
                      <tr>
                        <th class="pl-6">Tipo</th>
                        <th>Servicio</th>
                        <th class="text-right">Precio</th>
                        <th class="text-center">Duración / Buffer</th>
                        <th class="text-center">Plantilla</th>
                        <th class="text-center pr-6">Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (s of serviciosFiltrados(); track s.id) {
                        <tr>
                          <td class="pl-6">
                            <span class="chip" [ngClass]="getTipoClass(s.tipoServicio)">
                              {{ s.tipoServicio || 'OTRO' }}
                            </span>
                          </td>
                          <td>
                            <div class="flex flex-col">
                              <span class="font-bold text-[var(--text-primary)]">{{ s.nombre }}</span>
                              @if (s.descripcion) {
                                <span class="text-xs text-[var(--text-muted)] line-clamp-1">{{ s.descripcion }}</span>
                              }
                            </div>
                          </td>
                          <td class="text-right font-mono font-bold text-blue-600">
                            S/ {{ s.precio | number:'1.2-2' }}
                          </td>
                          <td class="text-center">
                            <div class="flex flex-col items-center">
                              <span class="badge badge-primary">{{ s.duracionMinutos }} min</span>
                              <span class="text-[10px] text-[var(--text-muted)] mt-1">Limpieza: {{ s.bufferMinutos }} min</span>
                            </div>
                          </td>
                          <td class="text-center">
                            <button class="btn-icon bg-blue-50" (click)="verInsumos(s)" title="Gestionar Plantilla">
                              <span class="material-icons-round text-blue-500">inventory_2</span>
                            </button>
                          </td>
                          <td class="text-center pr-6">
                            <div class="flex justify-center gap-1">
                              <button class="btn-icon" (click)="abrirDialogo(s)" title="Editar">
                                <span class="material-icons-round text-blue-400">edit</span>
                              </button>
                              <button class="btn-icon" (click)="toggleEstado(s)" title="Desactivar">
                                <span class="material-icons-round text-red-400">block</span>
                              </button>
                            </div>
                          </td>
                        </tr>
                      } @empty {
                        <tr>
                          <td colspan="6" class="text-center py-12 text-[var(--text-muted)]">
                            No se encontraron servicios médicos activos.
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </ng-template>
          </mat-tab>

          <mat-tab label="Servicios Inactivos">
            <ng-template matTabContent>
              <div class="table-container">
                <table class="table w-full">
                  <thead>
                    <tr>
                      <th class="pl-6">Tipo</th>
                      <th>Nombre</th>
                      <th class="text-right">Precio</th>
                      <th class="text-center pr-6">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    @for (s of serviciosFiltrados(); track s.id) {
                      <tr class="opacity-70 bg-gray-50/50">
                        <td class="pl-6"><span class="chip">{{ s.tipoServicio || 'OTRO' }}</span></td>
                        <td><span class="font-bold text-gray-500">{{ s.nombre }}</span></td>
                        <td class="text-right text-gray-500">S/ {{ s.precio | number:'1.2-2' }}</td>
                        <td class="text-center pr-6">
                          <button class="btn-icon" (click)="toggleEstado(s)" title="Reactivar">
                            <span class="material-icons-round text-green-400">settings_backup_restore</span>
                          </button>
                        </td>
                      </tr>
                    } @empty {
                      <tr>
                        <td colspan="4" class="text-center py-12 text-[var(--text-muted)]">
                          No hay servicios médicos inactivos.
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            </ng-template>
          </mat-tab>
        </mat-tab-group>
      </div>
    </div>
  `,
  styles: [`
    .chip { font-size: 10px; font-weight: 800; padding: 2px 8px; border-radius: 6px; text-transform: uppercase; }
    .chip-consulta { background: #dbeafe; color: #1e40af; }
    .chip-vacunacion { background: #dcfce7; color: #166534; }
    .chip-cirugia { background: #fee2e2; color: #991b1b; }
    .chip-hospitalizacion { background: #fef9c3; color: #854d0e; }
    .chip-estetica { background: #fae8ff; color: #86198f; }
    .chip-examen { background: #f3f4f6; color: #374151; }
    .chip-otro { background: #f3f4f6; color: #374151; }
    
    .badge { font-size: 11px; padding: 2px 6px; border-radius: 4px; font-weight: 600; }
    .badge-primary { background: rgba(var(--color-primary-rgb), 0.1); color: var(--color-primary-400); }
    
    .animate-spin { animation: spin 1s linear infinite; }
    @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
  `]
})
export class ServiciosMedicosComponent implements OnInit {
  servicios = signal<ServicioMedicoResponse[]>([]);
  loading = signal(false);
  filtroNombre = signal('');
  filtroTipo = signal('');
  mostrarActivos = signal(true);

  tipos: TipoServicio[] = ['CONSULTA', 'VACUNACION', 'CIRUGIA', 'HOSPITALIZACION', 'ESTETICA', 'EXAMEN', 'OTRO'];

  serviciosFiltrados = computed(() => {
    let filtrados = this.servicios().filter(s => {
      const matchNombre = s.nombre.toLowerCase().includes(this.filtroNombre().toLowerCase());
      const matchTipo = !this.filtroTipo() || s.tipoServicio === this.filtroTipo();
      const matchEstado = s.activo === this.mostrarActivos();
      return matchNombre && matchTipo && matchEstado;
    });

    // Ordenamiento por tipo y luego nombre
    return filtrados.sort((a, b) => {
      const tipoA = a.tipoServicio || 'OTRO';
      const tipoB = b.tipoServicio || 'OTRO';
      if (tipoA !== tipoB) return tipoA.localeCompare(tipoB);
      return a.nombre.localeCompare(b.nombre);
    });
  });

  constructor(
    private service: ServicioMedicoService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    // Cargamos todos los servicios (null) para filtrar localmente y que sea instantáneo
    this.service.listar().subscribe({
      next: (res) => {
        this.servicios.set(res);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onTabChange(event: any): void {
    this.mostrarActivos.set(event.index === 0);
  }

  abrirDialogo(servicio?: ServicioMedicoResponse): void {
    const dialogRef = this.dialog.open(ServicioMedicoDialogComponent, {
      width: '600px',
      data: servicio || null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (servicio) {
          this.service.actualizar(servicio.id, result).subscribe(() => {
            this.snackBar.open('Servicio actualizado con éxito', 'Cerrar', { duration: 3000 });
            this.cargar();
          });
        } else {
          this.service.crear(result).subscribe(() => {
            this.snackBar.open('Servicio creado con éxito', 'Cerrar', { duration: 3000 });
            this.cargar();
          });
        }
      }
    });
  }

  verInsumos(servicio: ServicioMedicoResponse): void {
    this.dialog.open(InsumosServicioDialogComponent, {
      width: '900px',
      data: servicio
    });
  }

  toggleEstado(servicio: ServicioMedicoResponse): void {
    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: servicio.activo ? 'Desactivar Servicio' : 'Reactivar Servicio',
        message: `¿Está seguro de ${servicio.activo ? 'desactivar' : 'reactivar'} el servicio "${servicio.nombre}"?`,
        confirmText: servicio.activo ? 'Desactivar' : 'Reactivar',
        isDestructive: servicio.activo
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.cambiarEstado(servicio.id, !servicio.activo).subscribe(() => {
          this.snackBar.open(`Servicio ${servicio.activo ? 'desactivado' : 'reactivado'}`, 'OK', { duration: 2000 });
          this.cargar();
        });
      }
    });
  }

  getTipoClass(tipo?: TipoServicio): string {
    if (!tipo) return 'chip-otro';
    return `chip-${tipo.toLowerCase()}`;
  }
}
