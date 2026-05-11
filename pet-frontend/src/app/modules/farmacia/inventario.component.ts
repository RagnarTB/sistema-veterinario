import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProductoService, Producto } from './services/producto.service';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion/modal-confirmacion.component';
import { ProductoDialogComponent } from './components/producto-dialog/producto-dialog.component';
import { GestionCatalogosDialogComponent } from './components/gestion-catalogos-dialog/gestion-catalogos-dialog.component';
import { InventarioService } from './services/inventario.service';
import { StockMinimoDialogComponent } from './components/stock-minimo-dialog/stock-minimo-dialog.component';
import { AuthService } from '../../core/services/auth.service';
import { SedeService } from '../../core/services/sede.service';
import { SedeResponse } from '../../core/models/models';

@Component({
  selector: 'app-inventario',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  template: `
    <div class="page-container fade-in-up">
      <!-- HEADER -->
      <div class="page-header">
        <div>
          <h1 class="page-title">Catálogo de Productos</h1>
          <p class="page-subtitle">Gestión de insumos, medicinas y productos de retail</p>
        </div>
        <div class="flex gap-3">
          @if (esAdmin()) {
            <button class="btn btn-secondary" (click)="abrirGestionCatalogos()">
              <span class="material-icons-round">settings</span>
              Catálogos
            </button>
          }
          @if (puedeOperar()) {
            <button class="btn btn-primary" (click)="abrirModalNuevo()">
              <span class="material-icons-round">add</span>
              Nuevo Producto
            </button>
          }
        </div>
      </div>

      <!-- FILTERS -->
      <div class="card p-4 mb-6 flex flex-wrap gap-4 items-end">
        <div class="search-box flex-1" style="min-width: 250px;">
          <label class="text-xs font-bold" style="color: var(--text-muted); display: block; margin-bottom: 4px;">BUSCAR PRODUCTO</label>
          <div style="position: relative;">
            <span class="material-icons-round search-icon">search</span>
            <input
              type="text"
              [(ngModel)]="busqueda"
              (ngModelChange)="onSearch()"
              class="form-control search-input"
              placeholder="Nombre o marca..."
            >
          </div>
        </div>

        <div style="min-width: 220px;">
          <label class="text-xs font-bold" style="color: var(--text-muted); display: block; margin-bottom: 4px;">SEDE ACTUAL</label>
          <div style="position: relative;">
            <span class="material-icons-round" style="position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #60a5fa; font-size: 18px;">storefront</span>
            <select [(ngModel)]="sedeSeleccionadaId" (change)="onSedeChange()" class="form-control" 
                    style="padding-left: 36px; border-color: #60a5fa; font-weight: 600; cursor: pointer;">
              @for (sede of sedes(); track sede.id) {
                <option [ngValue]="sede.id">{{ sede.nombre }}</option>
              }
            </select>
          </div>
        </div>
      </div>

      <!-- TABLE -->
      <div class="card overflow-hidden">
        <div class="table-container">
          <table class="table">
            <thead>
              <tr>
                <th>Producto</th>
                <th>Categoría</th>
                <th>Precio (S/)</th>
                <th class="text-center">Stock Actual</th>
                <th class="text-center">Stock Mín.</th>
                <th>Estado</th>
                <th class="text-center">Acciones</th>
              </tr>
            </thead>
            <tbody>
              @for (producto of productos; track producto.id) {
                <tr class="table-row-hover">
                  <td>
                    <div class="font-medium" style="color: var(--text-primary)">{{ producto.nombre }}</div>
                    <div class="text-xs" style="color: var(--text-muted)">{{ producto.marca || 'Sin marca' }}</div>
                  </td>
                  <td>
                    <span class="badge badge-info">
                      {{ producto.categoriaNombre || 'Sin categoría' }}
                    </span>
                  </td>
                  <td class="font-semibold" style="color: var(--color-primary-400)">
                    S/ {{ producto.precio | number:'1.2-2' }}
                  </td>
                  <td class="text-center">
                    <div style="display: flex; flex-direction: column; align-items: center;">
                      <span class="font-bold" [style.color]="(producto.stockActual || 0) <= (producto.stockMinimo || 0) ? '#f87171' : '#4ade80'" style="font-size: 1.1rem;">
                        {{ producto.stockActual || 0 }}
                      </span>
                      <span class="text-xs" style="color: var(--text-muted)">{{ producto.unidadVentaNombre }}</span>
                    </div>
                  </td>
                  <td class="text-center">
                    @if (puedeOperar()) {
                      <div class="stock-minimo-badge" (click)="editarStockMinimo(producto)" matTooltip="Click para configurar stock mínimo">
                        <span class="material-icons-round">notifications_active</span>
                        <span>{{ producto.stockMinimo || 0 }}</span>
                      </div>
                    } @else {
                      <div style="color: var(--text-muted); font-size: 0.85rem; font-weight: 500;">
                        {{ producto.stockMinimo || 0 }}
                      </div>
                    }
                  </td>
                  <td>
                    <label class="toggle-switch" [style.opacity]="puedeOperar() ? 1 : 0.5" [style.pointer-events]="puedeOperar() ? 'auto' : 'none'">
                      <input type="checkbox"
                             [checked]="producto.activo"
                             [disabled]="!puedeOperar()"
                             (change)="confirmarCambioEstado(producto)">
                      <span class="slider"></span>
                    </label>
                  </td>
                  <td>
                    <div class="flex justify-center gap-2">
                      <button class="btn-icon" (click)="abrirModalEditar(producto)" 
                              [matTooltip]="puedeOperar() ? 'Gestionar producto (Lotes/Kardex)' : 'Ver detalle (Lectura)'">
                        <span class="material-icons-round" [style.color]="puedeOperar() ? '#60a5fa' : '#94a3b8'">
                          {{ puedeOperar() ? 'edit_note' : 'visibility' }}
                        </span>
                      </button>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="7" class="text-center py-8" style="color: var(--text-muted)">
                    No se encontraron productos.
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <!-- PAGINATION -->
        <div class="pagination-bar">
          <span class="text-sm" style="color: var(--text-muted)">
            Mostrando {{ productos.length }} de {{ totalElements }} productos
          </span>
          <div class="flex gap-2">
            <button class="btn btn-secondary btn-sm"
                    [disabled]="currentPage === 0"
                    (click)="cambiarPagina(currentPage - 1)">
              Anterior
            </button>
            <button class="btn btn-secondary btn-sm"
                    [disabled]="currentPage >= totalPages - 1"
                    (click)="cambiarPagina(currentPage + 1)">
              Siguiente
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .search-box { position: relative; }
    .search-icon {
      position: absolute; left: 12px; top: 50%; transform: translateY(-50%);
      color: var(--text-muted); font-size: 20px;
    }
    .search-input { padding-left: 40px !important; }
    .table-row-hover { transition: background 0.15s; }
    .table-row-hover:hover { background: var(--bg-tertiary); }
    .pagination-bar {
      padding: 1rem 1.25rem;
      border-top: 1px solid var(--border-color);
      display: flex; justify-content: space-between; align-items: center;
    }
    .btn-sm { padding: 0.35rem 0.85rem !important; font-size: 0.8rem; }
    
    .stock-minimo-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      background: rgba(59, 130, 246, 0.08);
      border: 1px solid rgba(59, 130, 246, 0.2);
      border-radius: 20px;
      color: #60a5fa;
      font-size: 0.85rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }
    .stock-minimo-badge:hover {
      background: rgba(59, 130, 246, 0.15);
      border-color: #60a5fa;
      transform: translateY(-1px);
    }
    .stock-minimo-badge .material-icons-round {
      font-size: 16px;
    }
  `]
})
export class InventarioComponent implements OnInit {
  private productoService = inject(ProductoService);
  private inventarioService = inject(InventarioService);
  private authService = inject(AuthService);
  private sedeService = inject(SedeService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  productos: Producto[] = [];
  sedes = signal<SedeResponse[]>([]);
  sedeSeleccionadaId = 1;
  userSedeId = 1;

  esAdmin = () => this.authService.isAdmin();
  puedeOperar = () => {
    if (this.esAdmin()) return true;
    const misSedes = this.authService.currentSedeIds();
    return misSedes.includes(this.sedeSeleccionadaId);
  };
  busqueda = '';
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  private searchTimeout: any;

  ngOnInit() {
    this.userSedeId = Number(localStorage.getItem('vet_sede_id')) || 1;
    this.sedeSeleccionadaId = this.userSedeId;
    this.cargarSedes();
    this.cargarProductos();
  }

  cargarSedes() {
    this.sedeService.listarActivas().subscribe({
      next: (res) => this.sedes.set(res),
      error: () => {}
    });
  }

  onSedeChange() {
    this.sedeSeleccionadaId = Number(this.sedeSeleccionadaId);
    this.currentPage = 0;
    this.cargarProductos();
  }

  cargarProductos() {
    const sedeId = this.sedeSeleccionadaId;
    this.productoService.listar(this.busqueda, this.currentPage, this.pageSize, sedeId)
      .subscribe({
        next: (page) => {
          this.productos = page.content;
          this.totalElements = page.totalElements;
          this.totalPages = page.totalPages;
        },
        error: () => this.mostrarMensaje('Error al cargar productos')
      });
  }

  onSearch() {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.currentPage = 0;
      this.cargarProductos();
    }, 400);
  }

  cambiarPagina(nuevaPagina: number) {
    this.currentPage = nuevaPagina;
    this.cargarProductos();
  }

  abrirModalNuevo() {
    const dialogRef = this.dialog.open(ProductoDialogComponent, {
      width: '800px',
      disableClose: true,
      data: { 
        isEditing: false,
        sedeId: this.sedeSeleccionadaId,
        readOnly: !this.puedeOperar()
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.cargarProductos();
    });
  }

  abrirModalEditar(producto: Producto) {
    const dialogRef = this.dialog.open(ProductoDialogComponent, {
      width: '800px',
      disableClose: true,
      data: { 
        isEditing: true, 
        producto,
        sedeId: this.sedeSeleccionadaId,
        readOnly: !this.puedeOperar()
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.cargarProductos();
    });
  }

  confirmarCambioEstado(producto: Producto) {
    const accion = producto.activo ? 'desactivar' : 'activar';
    const nuevoEstado = !producto.activo;

    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: 'Confirmar Acción',
        message: '¿Está seguro que desea ' + accion + ' el producto "' + producto.nombre + '"?',
        confirmText: 'Sí, continuar',
        cancelText: 'Cancelar',
        isDestructive: producto.activo
      }
    });

    dialogRef.afterClosed().subscribe(confirmado => {
      if (confirmado) {
        this.productoService.cambiarEstado(producto.id, nuevoEstado).subscribe({
          next: () => {
            this.mostrarMensaje('Producto ' + accion + 'do exitosamente');
            this.cargarProductos();
          },
          error: () => this.mostrarMensaje('Error al cambiar estado')
        });
      }
    });
  }

  private mostrarMensaje(mensaje: string) {
    this.snackBar.open(mensaje, 'Cerrar', { duration: 3000 });
  }

  abrirGestionCatalogos() {
    this.dialog.open(GestionCatalogosDialogComponent, {
      width: '750px',
      disableClose: false
    });
  }

  editarStockMinimo(producto: Producto) {
    if (!this.puedeOperar()) {
      this.mostrarMensaje('No tiene permisos para modificar el stock en esta sede');
      return;
    }

    const dialogRef = this.dialog.open(StockMinimoDialogComponent, {
      width: '400px',
      data: { 
        productoNombre: producto.nombre, 
        stockMinimoActual: producto.stockMinimo || 0 
      }
    });

    dialogRef.afterClosed().subscribe(nuevoStock => {
      if (nuevoStock !== undefined && nuevoStock !== null) {
        const sedeId = this.sedeSeleccionadaId;
        this.inventarioService.actualizarStockMinimo(producto.id, sedeId, nuevoStock).subscribe({
          next: () => {
            this.mostrarMensaje('Stock mínimo actualizado');
            // Forzar actualización inmediata de la lista
            this.cargarProductos();
          },
          error: () => this.mostrarMensaje('Error al actualizar stock mínimo')
        });
      }
    });
  }
}
