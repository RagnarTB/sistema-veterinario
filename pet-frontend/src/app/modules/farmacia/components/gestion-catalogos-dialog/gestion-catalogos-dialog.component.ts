import { Component, OnInit, signal } from '@angular/core';
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
        <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--text-primary); margin: 0;">
          <span class="material-icons-round" style="vertical-align: middle; margin-right: 6px;">settings</span>
          Gestión de Catálogos
        </h2>
        <button class="btn-icon" (click)="dialogRef.close()">
          <span class="material-icons-round">close</span>
        </button>
      </div>

      <div class="dialog-content" style="padding: 0 !important;">
        <mat-tab-group animationDuration="200ms">

          <!-- CATEGORÍAS -->
          <mat-tab label="Categorías">
            <div style="padding: 1.25rem;">
              <div style="display: flex; gap: 8px; margin-bottom: 1rem;">
                <input [(ngModel)]="nuevaCategoriaNombre" class="form-control" placeholder="Nombre de la categoría..." style="flex:1;">
                <button class="btn btn-primary" style="padding: 0.4rem 0.85rem;" (click)="agregarCategoria()" [disabled]="!nuevaCategoriaNombre.trim()">
                  <span class="material-icons-round" style="font-size:16px;">add</span> Agregar
                </button>
              </div>
              <div class="table-container">
                <table class="table">
                  <thead><tr><th>Nombre</th><th>Descripción</th><th class="text-center">Acciones</th></tr></thead>
                  <tbody>
                    @for (cat of categorias(); track cat.id) {
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
                            <button class="btn-icon" (click)="guardarCategoria(cat)"><span class="material-icons-round" style="color:#4ade80">check</span></button>
                            <button class="btn-icon" (click)="editandoCatId = null"><span class="material-icons-round" style="color:#f87171">close</span></button>
                          } @else {
                            <button class="btn-icon" (click)="iniciarEditCategoria(cat)"><span class="material-icons-round" style="color:#60a5fa">edit</span></button>
                          }
                        </td>
                      </tr>
                    } @empty {
                      <tr><td colspan="3" class="text-center" style="padding:1.5rem;color:var(--text-muted)">Sin categorías</td></tr>
                    }
                  </tbody>
                </table>
              </div>
            </div>
          </mat-tab>

          <!-- UNIDADES -->
          <mat-tab label="Unidades de Medida">
            <div style="padding: 1.25rem;">
              <div style="display: flex; gap: 8px; margin-bottom: 1rem;">
                <input [(ngModel)]="nuevaUnidadNombre" class="form-control" placeholder="Nombre..." style="flex:1;">
                <input [(ngModel)]="nuevaUnidadAbrev" class="form-control" placeholder="Abrev." style="width:80px;">
                <button class="btn btn-primary" style="padding: 0.4rem 0.85rem;" (click)="agregarUnidad()" [disabled]="!nuevaUnidadNombre.trim()">
                  <span class="material-icons-round" style="font-size:16px;">add</span> Agregar
                </button>
              </div>
              <div class="table-container">
                <table class="table">
                  <thead><tr><th>Nombre</th><th>Abreviatura</th><th class="text-center">Acciones</th></tr></thead>
                  <tbody>
                    @for (uni of unidades(); track uni.id) {
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
                            <button class="btn-icon" (click)="guardarUnidad(uni)"><span class="material-icons-round" style="color:#4ade80">check</span></button>
                            <button class="btn-icon" (click)="editandoUniId = null"><span class="material-icons-round" style="color:#f87171">close</span></button>
                          } @else {
                            <button class="btn-icon" (click)="iniciarEditUnidad(uni)"><span class="material-icons-round" style="color:#60a5fa">edit</span></button>
                          }
                        </td>
                      </tr>
                    } @empty {
                      <tr><td colspan="3" class="text-center" style="padding:1.5rem;color:var(--text-muted)">Sin unidades</td></tr>
                    }
                  </tbody>
                </table>
              </div>
            </div>
          </mat-tab>

          <!-- PROVEEDORES -->
          <mat-tab label="Proveedores">
            <div style="padding: 1.25rem;">
              <div style="display: flex; gap: 8px; margin-bottom: 1rem; flex-wrap: wrap;">
                <input [(ngModel)]="nuevoProvNombre" class="form-control" placeholder="Razón Social..." style="flex:1; min-width:180px;">
                <input [(ngModel)]="nuevoProvRuc" class="form-control" placeholder="RUC" style="width:140px;">
                <input [(ngModel)]="nuevoProvTelefono" class="form-control" placeholder="Teléfono" style="width:120px;">
                <button class="btn btn-primary" style="padding: 0.4rem 0.85rem;" (click)="agregarProveedor()" [disabled]="!nuevoProvNombre.trim()">
                  <span class="material-icons-round" style="font-size:16px;">add</span> Agregar
                </button>
              </div>
              <div class="table-container">
                <table class="table">
                  <thead><tr><th>Razón Social</th><th>RUC</th><th>Contacto</th><th>Teléfono</th><th class="text-center">Acciones</th></tr></thead>
                  <tbody>
                    @for (prov of proveedores(); track prov.id) {
                      <tr>
                        <td>
                          @if (editandoProvId === prov.id) {
                            <input [(ngModel)]="editProvNombre" class="form-control" style="padding:4px 8px;">
                          } @else {
                            <span class="font-medium">{{ prov.razonSocial }}</span>
                          }
                        </td>
                        <td>
                          @if (editandoProvId === prov.id) {
                            <input [(ngModel)]="editProvRuc" class="form-control" style="padding:4px 8px; width: 130px;">
                          } @else {
                            {{ prov.ruc || '—' }}
                          }
                        </td>
                        <td>{{ prov.contacto || '—' }}</td>
                        <td>
                          @if (editandoProvId === prov.id) {
                            <input [(ngModel)]="editProvTelefono" class="form-control" style="padding:4px 8px; width:120px;">
                          } @else {
                            {{ prov.telefono || '—' }}
                          }
                        </td>
                        <td class="text-center">
                          @if (editandoProvId === prov.id) {
                            <button class="btn-icon" (click)="guardarProveedor(prov)"><span class="material-icons-round" style="color:#4ade80">check</span></button>
                            <button class="btn-icon" (click)="editandoProvId = null"><span class="material-icons-round" style="color:#f87171">close</span></button>
                          } @else {
                            <button class="btn-icon" (click)="iniciarEditProveedor(prov)"><span class="material-icons-round" style="color:#60a5fa">edit</span></button>
                          }
                        </td>
                      </tr>
                    } @empty {
                      <tr><td colspan="5" class="text-center" style="padding:1.5rem;color:var(--text-muted)">Sin proveedores</td></tr>
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
  `]
})
export class GestionCatalogosDialogComponent implements OnInit {
  categorias = signal<CategoriaProducto[]>([]);
  unidades = signal<UnidadMedida[]>([]);
  proveedores = signal<Proveedor[]>([]);

  // Categorias add/edit
  nuevaCategoriaNombre = '';
  editandoCatId: number | null = null;
  editCatNombre = '';
  editCatDesc = '';

  // Unidades add/edit
  nuevaUnidadNombre = '';
  nuevaUnidadAbrev = '';
  editandoUniId: number | null = null;
  editUniNombre = '';
  editUniAbrev = '';

  // Proveedores add/edit
  nuevoProvNombre = '';
  nuevoProvRuc = '';
  nuevoProvTelefono = '';
  editandoProvId: number | null = null;
  editProvNombre = '';
  editProvRuc = '';
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
    this.catalogoService.listarCategorias().subscribe(r => this.categorias.set(r));
    this.catalogoService.listarUnidades().subscribe(r => this.unidades.set(r));
    this.catalogoService.listarProveedores().subscribe(r => this.proveedores.set(r));
  }

  // ---- Categorías ----
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

  // ---- Unidades ----
  agregarUnidad() {
    this.catalogoService.crearUnidad({ nombre: this.nuevaUnidadNombre.trim(), abreviatura: this.nuevaUnidadAbrev.trim() || '?' }).subscribe({
      next: () => { this.nuevaUnidadNombre = ''; this.nuevaUnidadAbrev = ''; this.cargar(); this.msg('Unidad creada'); },
      error: () => this.msg('Error al crear unidad')
    });
  }

  iniciarEditUnidad(uni: UnidadMedida) {
    this.editandoUniId = uni.id;
    this.editUniNombre = uni.nombre;
    this.editUniAbrev = uni.abreviatura;
  }

  guardarUnidad(uni: UnidadMedida) {
    this.catalogoService.actualizarUnidad(uni.id, { nombre: this.editUniNombre, abreviatura: this.editUniAbrev }).subscribe({
      next: () => { this.editandoUniId = null; this.cargar(); this.msg('Unidad actualizada'); },
      error: () => this.msg('Error al actualizar')
    });
  }

  // ---- Proveedores ----
  agregarProveedor() {
    this.catalogoService.crearProveedor({
      razonSocial: this.nuevoProvNombre.trim(),
      ruc: this.nuevoProvRuc.trim(),
      telefono: this.nuevoProvTelefono.trim()
    }).subscribe({
      next: () => {
        this.nuevoProvNombre = ''; this.nuevoProvRuc = ''; this.nuevoProvTelefono = '';
        this.cargar(); this.msg('Proveedor creado');
      },
      error: () => this.msg('Error al crear proveedor')
    });
  }

  iniciarEditProveedor(prov: Proveedor) {
    this.editandoProvId = prov.id;
    this.editProvNombre = prov.razonSocial;
    this.editProvRuc = prov.ruc || '';
    this.editProvTelefono = prov.telefono || '';
  }

  guardarProveedor(prov: Proveedor) {
    this.catalogoService.actualizarProveedor(prov.id, {
      razonSocial: this.editProvNombre,
      ruc: this.editProvRuc,
      telefono: this.editProvTelefono
    }).subscribe({
      next: () => { this.editandoProvId = null; this.cargar(); this.msg('Proveedor actualizado'); },
      error: () => this.msg('Error al actualizar')
    });
  }

  private msg(text: string) {
    this.snackBar.open(text, 'Cerrar', { duration: 3000 });
  }
}
