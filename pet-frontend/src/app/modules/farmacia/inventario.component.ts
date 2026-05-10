import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProductoService, Producto } from './services/producto.service';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion/modal-confirmacion.component';
import { ProductoDialogComponent } from './components/producto-dialog/producto-dialog.component';
import { GestionCatalogosDialogComponent } from './components/gestion-catalogos-dialog/gestion-catalogos-dialog.component';

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
          <button class="btn btn-secondary" (click)="abrirGestionCatalogos()">
            <span class="material-icons-round">settings</span>
            Catálogos
          </button>
          <button class="btn btn-primary" (click)="abrirModalNuevo()">
            <span class="material-icons-round">add</span>
            Nuevo Producto
          </button>
        </div>
      </div>

      <!-- FILTERS -->
      <div class="card p-4 mb-6 flex flex-wrap gap-4 items-center">
        <div class="search-box flex-1" style="min-width: 250px;">
          <span class="material-icons-round search-icon">search</span>
          <input
            type="text"
            [(ngModel)]="busqueda"
            (ngModelChange)="onSearch()"
            class="form-control search-input"
            placeholder="Buscar por nombre o marca..."
          >
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
                <th>Conversión</th>
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
                  <td>
                    @if (producto.factorConversion && producto.unidadCompraNombre && producto.unidadVentaNombre) {
                      <div class="text-xs">
                        1 {{ producto.unidadCompraNombre }} = {{ producto.factorConversion }} {{ producto.unidadVentaNombre }}
                      </div>
                    } @else {
                      <span class="text-xs" style="color: var(--text-muted)">N/A</span>
                    }
                  </td>
                  <td>
                    <label class="toggle-switch">
                      <input type="checkbox"
                             [checked]="producto.activo"
                             (change)="confirmarCambioEstado(producto)">
                      <span class="slider"></span>
                    </label>
                  </td>
                  <td>
                    <div class="flex justify-center gap-2">
                      <button class="btn-icon" (click)="abrirModalEditar(producto)" matTooltip="Ver Detalles / Stock">
                        <span class="material-icons-round" style="color: #60a5fa">edit</span>
                      </button>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="6" class="text-center py-8" style="color: var(--text-muted)">
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
  `]
})
export class InventarioComponent implements OnInit {
  private productoService = inject(ProductoService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  productos: Producto[] = [];
  busqueda = '';
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  private searchTimeout: any;

  ngOnInit() {
    this.cargarProductos();
  }

  cargarProductos() {
    this.productoService.listar(this.busqueda, this.currentPage, this.pageSize)
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
      data: { isEditing: false }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.cargarProductos();
    });
  }

  abrirModalEditar(producto: Producto) {
    const dialogRef = this.dialog.open(ProductoDialogComponent, {
      width: '800px',
      disableClose: true,
      data: { isEditing: true, producto }
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
}
