import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CatalogoService, CategoriaProducto, UnidadMedida, Proveedor } from '../../services/catalogo.service';

@Component({
  selector: 'app-gestion-catalogos-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatTabsModule],
  template: `
    <div class="dialog-container">
      <div class="dialog-header" style="display: flex; justify-content: space-between; align-items: center;">
        <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--text-primary); margin: 0; display: flex; align-items: center; gap: 8px;">
          <span class="material-icons-round">settings</span>
          Gestión de Catálogos
        </h2>
        <button class="btn-icon" (click)="dialogRef.close()">
          <span class="material-icons-round">close</span>
        </button>
      </div>

      <div class="dialog-content" style="padding: 0 !important;">
        <mat-tab-group animationDuration="200ms">

          <!-- ===== CATEGORÍAS ===== -->
          <mat-tab label="Categorías">
            <div style="padding: 1.25rem;">
              <div style="display: flex; gap: 8px; margin-bottom: 1rem;">
                <input [(ngModel)]="nuevaCategoriaNombre" class="form-control" placeholder="Nombre de la categoría..." style="flex:1;">
                <button class="btn btn-primary" style="padding: 0.4rem 0.85rem;" (click)="agregarCategoria()" [disabled]="!nuevaCategoriaNombre.trim()">
                  <span class="material-icons-round" style="font-size:16px;">add</span> Agregar
                </button>
              </div>

              <div class="section-title">Categorías Activas</div>
              <div class="table-container mb-4">
                <table class="table">
                  <thead><tr><th>Nombre</th><th>Descripción</th><th class="text-center" style="width:120px;">Acciones</th></tr></thead>
                  <tbody>
                    @for (cat of categoriasActivas(); track cat.id) {
                      <tr>
                        <td>
                          @if (editandoCatId === cat.id) {
                            <input [(ngModel)]="editCatNombre" class="form-control" style="padding:4px 8px;">
                          } @else {
                            <span class="font-medium">{{ cat.nombre }}</span>
                          }
                        </td>
                        <td>
                          @if (editandoCatId === cat.id) {
                            <input [(ngModel)]="editCatDesc" class="form-control" style="padding:4px 8px;">
                          } @else {
                            <span class="text-xs" style="color:var(--text-muted)">{{ cat.descripcion || '—' }}</span>
                          }
                        </td>
                        <td class="text-center">
                          @if (editandoCatId === cat.id) {
                            <button class="btn-icon" (click)="guardarCategoria(cat)" title="Guardar"><span class="material-icons-round" style="color:#4ade80">check</span></button>
                            <button class="btn-icon" (click)="editandoCatId = null" title="Cancelar"><span class="material-icons-round" style="color:#f87171">close</span></button>
                          } @else {
                            <button class="btn-icon" (click)="iniciarEditCategoria(cat)" title="Editar"><span class="material-icons-round" style="color:#60a5fa">edit</span></button>
                            <button class="btn-icon" (click)="eliminarCat(cat)" title="Desactivar"><span class="material-icons-round" style="color:#f87171">block</span></button>
                          }
                        </td>
                      </tr>
                    } @empty {
                      <tr><td colspan="3" class="text-center" style="padding:1rem;color:var(--text-muted)">No hay categorías activas</td></tr>
                    }
                  </tbody>
                </table>
              </div>

              <div class="section-title" style="color:#f87171">Categorías Desactivadas</div>
              <div class="table-container">
                <table class="table">
                  <thead><tr><th>Nombre</th><th>Descripción</th><th class="text-center" style="width:120px;">Acciones</th></tr></thead>
                  <tbody>
                    @for (cat of categoriasInactivas(); track cat.id) {
                      <tr style="opacity:0.7">
                        <td><span class="font-medium">{{ cat.nombre }}</span></td>
                        <td><span class="text-xs">{{ cat.descripcion || '—' }}</span></td>
                        <td class="text-center">
                          <button class="btn-icon" (click)="activarCat(cat)" title="Reactivar"><span class="material-icons-round" style="color:#4ade80">settings_backup_restore</span></button>
                          <button class="btn-icon" (click)="eliminarCat(cat)" title="Eliminar Permanente"><span class="material-icons-round" style="color:#f87171">delete_forever</span></button>
                        </td>
                      </tr>
                    } @empty {
                      <tr><td colspan="3" class="text-center" style="padding:1rem;color:var(--text-muted)">No hay categorías desactivadas</td></tr>
                    }
                  </tbody>
                </table>
              </div>
            </div>
          </mat-tab>

          <!-- ===== UNIDADES ===== -->
          <mat-tab label="Unidades de Medida">
            <div style="padding: 1.25rem;">
              <div style="display: flex; gap: 8px; margin-bottom: 1rem; align-items: center;">
                <input [(ngModel)]="nuevaUnidadNombre" class="form-control" placeholder="Nombre..." style="flex:1;">
                <input [(ngModel)]="nuevaUnidadAbrev" class="form-control" placeholder="Abrev." style="width:80px;">
                <label style="display: flex; align-items: center; gap: 4px; font-size: 0.75rem; white-space: nowrap; cursor: pointer;">
                  <input type="checkbox" [(ngModel)]="nuevaUnidadDec"> Decimales
                </label>
                <button class="btn btn-primary" style="padding: 0.4rem 0.85rem;" (click)="agregarUnidad()" [disabled]="!nuevaUnidadNombre.trim()">
                  <span class="material-icons-round" style="font-size:16px;">add</span> Agregar
                </button>
              </div>

              <div class="section-title">Unidades Activas</div>
              <div class="table-container mb-4">
                <table class="table">
                  <thead><tr><th>Nombre</th><th>Abrev.</th><th class="text-center">Dec.</th><th class="text-center" style="width:120px;">Acciones</th></tr></thead>
                  <tbody>
                    @for (uni of unidadesActivas(); track uni.id) {
                      <tr>
                        <td>
                          @if (editandoUniId === uni.id) {
                            <input [(ngModel)]="editUniNombre" class="form-control" style="padding:4px 8px;">
                          } @else {
                            <span class="font-medium">{{ uni.nombre }}</span>
                          }
                        </td>
                        <td>
                          @if (editandoUniId === uni.id) {
                            <input [(ngModel)]="editUniAbrev" class="form-control" style="padding:4px 8px;width:80px;">
                          } @else {
                            {{ uni.abreviatura }}
                          }
                        </td>
                        <td class="text-center">
                          @if (editandoUniId === uni.id) {
                            <input type="checkbox" [(ngModel)]="editUniDec">
                          } @else {
                            <span class="material-icons-round" [style.color]="uni.permiteDecimales ? '#4ade80' : '#f87171'" style="font-size:18px;">
                              {{ uni.permiteDecimales ? 'check_circle' : 'cancel' }}
                            </span>
                          }
                        </td>
                        <td class="text-center">
                          @if (editandoUniId === uni.id) {
                            <button class="btn-icon" (click)="guardarUnidad(uni)" title="Guardar"><span class="material-icons-round" style="color:#4ade80">check</span></button>
                            <button class="btn-icon" (click)="editandoUniId = null" title="Cancelar"><span class="material-icons-round" style="color:#f87171">close</span></button>
                          } @else {
                            <button class="btn-icon" (click)="iniciarEditUnidad(uni)" title="Editar"><span class="material-icons-round" style="color:#60a5fa">edit</span></button>
                            <button class="btn-icon" (click)="eliminarUni(uni)" title="Desactivar"><span class="material-icons-round" style="color:#f87171">block</span></button>
                          }
                        </td>
                      </tr>
                    } @empty {
                      <tr><td colspan="3" class="text-center" style="padding:1rem;color:var(--text-muted)">No hay unidades activas</td></tr>
                    }
                  </tbody>
                </table>
              </div>

              <div class="section-title" style="color:#f87171">Unidades Desactivadas</div>
              <div class="table-container">
                <table class="table">
                  <thead><tr><th>Nombre</th><th>Abrev.</th><th class="text-center" style="width:120px;">Acciones</th></tr></thead>
                  <tbody>
                    @for (uni of unidadesInactivas(); track uni.id) {
                      <tr style="opacity:0.7">
                        <td><span class="font-medium">{{ uni.nombre }}</span></td>
                        <td>{{ uni.abreviatura }}</td>
                        <td class="text-center">
                          <button class="btn-icon" (click)="activarUni(uni)" title="Reactivar"><span class="material-icons-round" style="color:#4ade80">settings_backup_restore</span></button>
                          <button class="btn-icon" (click)="eliminarUni(uni)" title="Eliminar Permanente"><span class="material-icons-round" style="color:#f87171">delete_forever</span></button>
                        </td>
                      </tr>
                    } @empty {
                      <tr><td colspan="3" class="text-center" style="padding:1rem;color:var(--text-muted)">No hay unidades desactivadas</td></tr>
                    }
                  </tbody>
                </table>
              </div>
            </div>
          </mat-tab>

          <!-- ===== PROVEEDORES ===== -->
          <mat-tab label="Proveedores">
            <div style="padding: 1.25rem;">
              <!-- Formulario simplificado -->
              <div style="padding: 1rem; background: var(--bg-tertiary); border-radius: 10px; border: 1px solid var(--border-color); margin-bottom: 1rem;">
                <p class="text-xs font-bold" style="color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.75rem;">Nuevo Proveedor</p>
                <div style="display: flex; gap: 8px; align-items: flex-end; margin-bottom: 0.75rem;">
                  <div class="form-group" style="margin-bottom:0; width:160px;">
                    <label class="text-xs">RUC *</label>
                    <div style="display:flex; gap:4px;">
                      <input [(ngModel)]="nuevoProvRuc" class="form-control" maxlength="11"
                             (keypress)="soloNumeros($event)"
                             placeholder="20601030013" style="padding: 6px 8px;">
                      <button class="btn btn-primary" style="padding: 4px 8px; white-space:nowrap;" (click)="buscarRuc()" [disabled]="nuevoProvRuc.length !== 11 || buscandoRuc()">
                        <span class="material-icons-round" style="font-size:16px;">search</span>
                      </button>
                    </div>
                  </div>
                  <div class="form-group" style="margin-bottom:0; flex:1;">
                    <label class="text-xs">Razón Social</label>
                    <input [(ngModel)]="nuevoProvNombre" class="form-control" placeholder="Se llenará automáticamente..." style="padding: 6px 8px;" readonly>
                  </div>
                </div>
                <div style="display: flex; gap: 8px; align-items: flex-end;">
                  <div class="form-group" style="margin-bottom:0; flex:1;">
                    <label class="text-xs">Dirección</label>
                    <input [(ngModel)]="nuevoProvDireccion" class="form-control" placeholder="Se llenará automáticamente..." style="padding: 6px 8px;" readonly>
                  </div>
                  <div class="form-group" style="margin-bottom:0; width:140px;">
                    <label class="text-xs">Teléfono</label>
                    <input [(ngModel)]="nuevoProvTelefono" class="form-control" placeholder="999999999" style="padding: 6px 8px;"
                           (keypress)="soloNumeros($event)" maxlength="9">
                  </div>
                  <button class="btn btn-primary" style="padding: 6px 14px;" (click)="agregarProveedor()" [disabled]="!nuevoProvNombre.trim()">
                    <span class="material-icons-round" style="font-size:16px;">add</span> Agregar
                  </button>
                </div>
              </div>

              <div class="section-title">Proveedores Activos</div>
              <div class="table-container mb-4">
                <table class="table">
                  <thead><tr><th>RUC</th><th>Razón Social</th><th>Teléfono</th><th class="text-center" style="width:120px;">Acciones</th></tr></thead>
                  <tbody>
                    @for (prov of proveedoresActivos(); track prov.id) {
                      <tr>
                        <td class="text-xs">{{ prov.ruc || '—' }}</td>
                        <td>
                          @if (editandoProvId === prov.id) {
                            <input [(ngModel)]="editProvNombre" class="form-control" style="padding:4px 8px;">
                          } @else {
                            <span class="font-medium">{{ prov.razonSocial }}</span>
                          }
                        </td>
                        <td>
                          @if (editandoProvId === prov.id) {
                            <input [(ngModel)]="editProvTelefono" class="form-control" style="padding:4px 8px; width:120px;"
                                   (keypress)="soloNumeros($event)" maxlength="9">
                          } @else {
                            {{ prov.telefono || '—' }}
                          }
                        </td>
                        <td class="text-center">
                          @if (editandoProvId === prov.id) {
                            <button class="btn-icon" (click)="guardarProveedor(prov)" title="Guardar"><span class="material-icons-round" style="color:#4ade80">check</span></button>
                            <button class="btn-icon" (click)="editandoProvId = null" title="Cancelar"><span class="material-icons-round" style="color:#f87171">close</span></button>
                          } @else {
                            <button class="btn-icon" (click)="iniciarEditProveedor(prov)" title="Editar"><span class="material-icons-round" style="color:#60a5fa">edit</span></button>
                            <button class="btn-icon" (click)="eliminarProv(prov)" title="Desactivar"><span class="material-icons-round" style="color:#f87171">block</span></button>
                          }
                        </td>
                      </tr>
                    } @empty {
                      <tr><td colspan="4" class="text-center" style="padding:1rem;color:var(--text-muted)">No hay proveedores activos</td></tr>
                    }
                  </tbody>
                </table>
              </div>

              <div class="section-title" style="color:#f87171">Proveedores Desactivados</div>
              <div class="table-container">
                <table class="table">
                  <thead><tr><th>RUC</th><th>Razón Social</th><th class="text-center" style="width:120px;">Acciones</th></tr></thead>
                  <tbody>
                    @for (prov of proveedoresInactivos(); track prov.id) {
                      <tr style="opacity:0.7">
                        <td class="text-xs">{{ prov.ruc }}</td>
                        <td><span class="font-medium">{{ prov.razonSocial }}</span></td>
                        <td class="text-center">
                          <button class="btn-icon" (click)="activarProv(prov)" title="Reactivar"><span class="material-icons-round" style="color:#4ade80">settings_backup_restore</span></button>
                          <button class="btn-icon" (click)="eliminarProv(prov)" title="Eliminar Permanente"><span class="material-icons-round" style="color:#f87171">delete_forever</span></button>
                        </td>
                      </tr>
                    } @empty {
                      <tr><td colspan="3" class="text-center" style="padding:1rem;color:var(--text-muted)">No hay proveedores desactivados</td></tr>
                    }
                  </tbody>
                </table>
              </div>
            </div>
          </mat-tab>

        </mat-tab-group>
      </div>
    </div>
  `,
  styles: [`
    :host ::ng-deep .mat-mdc-tab-header {
      background-color: var(--bg-secondary);
      border-bottom: 1px solid var(--border-color);
    }
    .section-title {
      font-size: 0.75rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: var(--color-primary-400);
      margin-bottom: 0.5rem;
      margin-top: 1rem;
    }
    .mb-4 { margin-bottom: 1rem; }
  `]
})
export class GestionCatalogosDialogComponent implements OnInit {
  todasCategorias = signal<CategoriaProducto[]>([]);
  todasUnidades = signal<UnidadMedida[]>([]);
  todosProveedores = signal<Proveedor[]>([]);

  // Computed filters
  categoriasActivas = computed(() => this.todasCategorias().filter(c => c.activo));
  categoriasInactivas = computed(() => this.todasCategorias().filter(c => !c.activo));
  
  unidadesActivas = computed(() => this.todasUnidades().filter(u => u.activo));
  unidadesInactivas = computed(() => this.todasUnidades().filter(u => !u.activo));

  proveedoresActivos = computed(() => this.todosProveedores().filter(p => p.activo));
  proveedoresInactivos = computed(() => this.todosProveedores().filter(p => !p.activo));

  // Categorias
  nuevaCategoriaNombre = '';
  editandoCatId: number | null = null;
  editCatNombre = '';
  editCatDesc = '';

  // Unidades
  nuevaUnidadNombre = '';
  nuevaUnidadAbrev = '';
  nuevaUnidadDec = false;
  editandoUniId: number | null = null;
  editUniNombre = '';
  editUniAbrev = '';
  editUniDec = false;

  // Proveedores
  nuevoProvNombre = '';
  nuevoProvRuc = '';
  nuevoProvTelefono = '';
  nuevoProvDireccion = '';
  buscandoRuc = signal(false);
  editandoProvId: number | null = null;
  editProvNombre = '';
  editProvTelefono = '';

  constructor(
    public dialogRef: MatDialogRef<GestionCatalogosDialogComponent>,
    private catalogoService: CatalogoService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.catalogoService.listarCategoriasTodas().subscribe(r => this.todasCategorias.set(r));
    this.catalogoService.listarUnidadesTodas().subscribe(r => this.todasUnidades.set(r));
    this.catalogoService.listarProveedoresTodas().subscribe(r => this.todosProveedores.set(r));
  }

  soloNumeros(event: KeyboardEvent) {
    const char = event.key;
    if (!/[0-9]/.test(char)) {
      event.preventDefault();
    }
  }

  // ========== CATEGORÍAS ==========
  agregarCategoria() {
    this.catalogoService.crearCategoria({ nombre: this.nuevaCategoriaNombre.trim() }).subscribe({
      next: () => { this.nuevaCategoriaNombre = ''; this.cargar(); this.msg('Categoría creada'); },
      error: () => this.msg('Error al crear categoría')
    });
  }

  iniciarEditCategoria(cat: CategoriaProducto) {
    this.editandoCatId = cat.id;
    this.editCatNombre = cat.nombre;
    this.editCatDesc = cat.descripcion || '';
  }

  guardarCategoria(cat: CategoriaProducto) {
    this.catalogoService.actualizarCategoria(cat.id, { nombre: this.editCatNombre, descripcion: this.editCatDesc }).subscribe({
      next: () => { this.editandoCatId = null; this.cargar(); this.msg('Categoría actualizada'); },
      error: () => this.msg('Error al actualizar')
    });
  }

  activarCat(cat: CategoriaProducto) {
    this.catalogoService.activarCategoria(cat.id).subscribe({
      next: () => { this.cargar(); this.msg('Categoría reactivada'); },
      error: () => this.msg('Error al reactivar')
    });
  }

  eliminarCat(cat: CategoriaProducto) {
    this.catalogoService.eliminarCategoria(cat.id).subscribe({
      next: (res) => { this.cargar(); this.msg(res.mensaje); },
      error: (err) => {
        const errorMsg = err?.error?.message || 'Error al eliminar';
        this.msg(errorMsg);
      }
    });
  }

  // ========== UNIDADES ==========
  agregarUnidad() {
    this.catalogoService.crearUnidad({ 
      nombre: this.nuevaUnidadNombre.trim(), 
      abreviatura: this.nuevaUnidadAbrev.trim() || '?',
      permiteDecimales: this.nuevaUnidadDec
    }).subscribe({
      next: () => { 
        this.nuevaUnidadNombre = ''; this.nuevaUnidadAbrev = ''; this.nuevaUnidadDec = false;
        this.cargar(); this.msg('Unidad creada'); 
      },
      error: () => this.msg('Error al crear unidad')
    });
  }

  iniciarEditUnidad(uni: UnidadMedida) {
    this.editandoUniId = uni.id;
    this.editUniNombre = uni.nombre;
    this.editUniAbrev = uni.abreviatura;
    this.editUniDec = uni.permiteDecimales || false;
  }

  guardarUnidad(uni: UnidadMedida) {
    this.catalogoService.actualizarUnidad(uni.id, { 
      nombre: this.editUniNombre, 
      abreviatura: this.editUniAbrev,
      permiteDecimales: this.editUniDec
    }).subscribe({
      next: () => { this.editandoUniId = null; this.cargar(); this.msg('Unidad actualizada'); },
      error: () => this.msg('Error al actualizar')
    });
  }

  activarUni(uni: UnidadMedida) {
    this.catalogoService.activarUnidad(uni.id).subscribe({
      next: () => { this.cargar(); this.msg('Unidad reactivada'); },
      error: () => this.msg('Error al reactivar')
    });
  }

  eliminarUni(uni: UnidadMedida) {
    this.catalogoService.eliminarUnidad(uni.id).subscribe({
      next: (res) => { this.cargar(); this.msg(res.mensaje); },
      error: (err) => {
        const errorMsg = err?.error?.message || 'Error al eliminar';
        this.msg(errorMsg);
      }
    });
  }

  // ========== PROVEEDORES ==========
  buscarRuc() {
    if (this.nuevoProvRuc.length !== 11) return;
    this.buscandoRuc.set(true);
    this.catalogoService.consultarRuc(this.nuevoProvRuc).subscribe({
      next: (res) => {
        this.nuevoProvNombre = res.razon_social || '';
        this.nuevoProvDireccion = res.direccion || '';
        this.buscandoRuc.set(false);
        this.msg('Datos del RUC obtenidos correctamente');
      },
      error: () => {
        this.buscandoRuc.set(false);
        this.msg('No se encontró el RUC o hubo un error');
      }
    });
  }

  agregarProveedor() {
    this.catalogoService.crearProveedor({
      razonSocial: this.nuevoProvNombre.trim(),
      ruc: this.nuevoProvRuc.trim(),
      telefono: this.nuevoProvTelefono.trim(),
      direccion: this.nuevoProvDireccion.trim()
    }).subscribe({
      next: () => {
        this.nuevoProvNombre = ''; this.nuevoProvRuc = ''; this.nuevoProvTelefono = ''; this.nuevoProvDireccion = '';
        this.cargar(); this.msg('Proveedor creado');
      },
      error: () => this.msg('Error al crear proveedor')
    });
  }

  iniciarEditProveedor(prov: Proveedor) {
    this.editandoProvId = prov.id;
    this.editProvNombre = prov.razonSocial;
    this.editProvTelefono = prov.telefono || '';
  }

  guardarProveedor(prov: Proveedor) {
    this.catalogoService.actualizarProveedor(prov.id, {
      razonSocial: this.editProvNombre,
      ruc: prov.ruc,
      telefono: this.editProvTelefono,
      direccion: prov.direccion
    }).subscribe({
      next: () => { this.editandoProvId = null; this.cargar(); this.msg('Proveedor actualizado'); },
      error: () => this.msg('Error al actualizar')
    });
  }

  activarProv(prov: Proveedor) {
    this.catalogoService.activarProveedor(prov.id).subscribe({
      next: () => { this.cargar(); this.msg('Proveedor reactivado'); },
      error: () => this.msg('Error al reactivar')
    });
  }

  eliminarProv(prov: Proveedor) {
    this.catalogoService.eliminarProveedor(prov.id).subscribe({
      next: (res) => { this.cargar(); this.msg(res.mensaje); },
      error: (err) => {
        const errorMsg = err?.error?.message || 'Error al eliminar';
        this.msg(errorMsg);
      }
    });
  }

  private msg(text: string) {
    this.snackBar.open(text, 'Cerrar', { duration: 3000 });
  }
}
