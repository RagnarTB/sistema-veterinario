import { Component, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProductoService } from '../../services/producto.service';
import { CatalogoService, CategoriaProducto, UnidadMedida, Proveedor } from '../../services/catalogo.service';
import { InventarioService, LoteInventario, MovimientoInventario } from '../../services/inventario.service';
import { IngresoStockDialogComponent } from '../ingreso-stock-dialog/ingreso-stock-dialog.component';

@Component({
  selector: 'app-producto-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatTabsModule],
  template: `
    <div class="dialog-container">
      <div class="dialog-header" style="display: flex; justify-content: space-between; align-items: center;">
        <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--text-primary); margin: 0;">
          {{ data.isEditing ? 'Detalles del Producto' : 'Nuevo Producto' }}
        </h2>
        <button class="btn-icon" (click)="cerrar()">
          <span class="material-icons-round">close</span>
        </button>
      </div>

      <div class="dialog-content" style="padding: 0 !important;">
        <mat-tab-group animationDuration="200ms" class="custom-tabs">

          <!-- TAB 1: DATOS BÁSICOS -->
          <mat-tab label="Datos del Producto">
            <div style="padding: 1.5rem;">
              <form [formGroup]="form" (ngSubmit)="guardar()">
                <div class="grid grid-cols-2 gap-4">
                  <div class="form-group">
                    <label>Nombre del Producto *</label>
                    <input type="text" formControlName="nombre" class="form-control" placeholder="Ej. Bravecto 20-40kg">
                  </div>
                  <div class="form-group">
                    <label>Marca</label>
                    <input type="text" formControlName="marca" class="form-control" placeholder="Ej. MSD Animal Health">
                  </div>

                  <div class="form-group" style="grid-column: span 2;">
                    <label>Descripción</label>
                    <textarea formControlName="descripcion" class="form-control" rows="2" placeholder="Opcional..."></textarea>
                  </div>

                  <div class="form-group">
                    <label>Precio de Venta (S/) *</label>
                    <input type="number" formControlName="precio" class="form-control" step="0.01">
                  </div>
                  <div class="form-group">
                    <label>Categoría</label>
                    <select formControlName="categoriaId" class="form-control">
                      <option [ngValue]="null">Seleccione una categoría...</option>
                      @for (cat of categorias(); track cat.id) {
                        <option [ngValue]="cat.id">{{ cat.nombre }}</option>
                      }
                    </select>
                  </div>

                  <div style="grid-column: span 2; margin-top: 1rem; margin-bottom: 0.5rem;">
                    <h3 style="font-size: 0.75rem; font-weight: 700; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.05em;">Unidades de Medida y Conversión</h3>
                    <hr style="border-color: var(--border-color); margin-top: 0.25rem;">
                  </div>

                  <div class="form-group">
                    <label>Se compra en (Ingreso)</label>
                    <select formControlName="unidadCompraId" class="form-control">
                      <option [ngValue]="null">Seleccione...</option>
                      @for (uni of unidades(); track uni.id) {
                        <option [ngValue]="uni.id">{{ uni.nombre }} ({{ uni.abreviatura }})</option>
                      }
                    </select>
                  </div>
                  <div class="form-group">
                    <label>Se vende por (Salida)</label>
                    <select formControlName="unidadVentaId" class="form-control">
                      <option [ngValue]="null">Seleccione...</option>
                      @for (uni of unidades(); track uni.id) {
                        <option [ngValue]="uni.id">{{ uni.nombre }} ({{ uni.abreviatura }})</option>
                      }
                    </select>
                  </div>

                  <div class="form-group" style="grid-column: span 2;">
                    <label>Factor de Conversión</label>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                      <span class="text-sm" style="color: var(--text-muted)">1 Unidad de Compra equivale a</span>
                      <input type="number" formControlName="factorConversion" class="form-control" style="width: 100px;" step="0.01">
                      <span class="text-sm" style="color: var(--text-muted)">Unidades de Venta</span>
                    </div>
                  </div>
                </div>

                <div style="margin-top: 1.5rem; display: flex; justify-content: flex-end; gap: 0.75rem;">
                  <button type="button" class="btn btn-secondary" (click)="cerrar()">Cancelar</button>
                  <button type="submit" class="btn btn-primary" [disabled]="form.invalid || cargando()">
                    {{ cargando() ? 'Guardando...' : 'Guardar Producto' }}
                  </button>
                </div>
              </form>
            </div>
          </mat-tab>

          <!-- TAB 2: CONTROL DE STOCK (Solo si está editando) -->
          @if (data.isEditing) {
            <mat-tab label="Control de Stock y Lotes">
              <div style="padding: 1.5rem;">

                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                  <h3 style="font-weight: 600; color: var(--text-primary); margin: 0;">Lotes Activos</h3>
                  <button class="btn btn-primary" style="padding: 0.4rem 0.85rem;" (click)="abrirIngresoStock()">
                    <span class="material-icons-round" style="font-size: 16px;">add_box</span>
                    Registrar Ingreso
                  </button>
                </div>

                <div class="table-container">
                  <table class="table">
                    <thead>
                      <tr>
                        <th>Nro. Lote</th>
                        <th>Vencimiento</th>
                        <th>Proveedor</th>
                        <th>Stock Restante</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (lote of lotes(); track lote.id) {
                        <tr>
                          <td class="font-medium">{{ lote.numeroLote }}</td>
                          <td>
                            <span class="badge" [class.badge-error]="estaPorVencer(lote.fechaVencimiento)">
                              {{ lote.fechaVencimiento }}
                            </span>
                          </td>
                          <td>{{ lote.proveedorNombre || 'N/A' }}</td>
                          <td class="font-bold" style="color: var(--color-primary-400)">{{ lote.stockRestante }}</td>
                        </tr>
                      } @empty {
                        <tr>
                          <td colspan="4" class="text-center" style="padding: 2rem 0; color: var(--text-muted);">
                            No hay lotes con stock para este producto.
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              </div>
            </mat-tab>

            <!-- TAB 3: KARDEX -->
            <mat-tab label="Kardex (Movimientos)">
              <div style="padding: 1.5rem;">
                <h3 style="font-weight: 600; color: var(--text-primary); margin: 0 0 1rem 0;">Historial de Movimientos</h3>
                <div class="table-container">
                  <table class="table">
                    <thead>
                      <tr>
                        <th>Fecha</th>
                        <th>Tipo</th>
                        <th>Cantidad</th>
                        <th>Motivo</th>
                        <th>Responsable</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (mov of movimientos(); track mov.id) {
                        <tr>
                          <td class="text-xs">{{ mov.fecha | date:'dd/MM/yyyy HH:mm' }}</td>
                          <td>
                            <span class="badge" [class.badge-success]="esEntrada(mov.tipoMovimiento)" [class.badge-error]="!esEntrada(mov.tipoMovimiento)">
                              {{ traducirTipo(mov.tipoMovimiento) }}
                            </span>
                          </td>
                          <td class="font-bold"
                              [style.color]="esEntrada(mov.tipoMovimiento) ? '#4ade80' : '#f87171'">
                            {{ esEntrada(mov.tipoMovimiento) ? '+' : '-' }}{{ mov.cantidad }}
                          </td>
                          <td class="text-xs">{{ mov.motivo || '—' }}</td>
                          <td class="text-xs">{{ mov.responsableNombre }}</td>
                        </tr>
                      } @empty {
                        <tr>
                          <td colspan="5" class="text-center" style="padding: 2rem 0; color: var(--text-muted);">
                            Sin movimientos registrados.
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              </div>
            </mat-tab>
          }
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
export class ProductoDialogComponent implements OnInit {
  form: FormGroup;
  cargando = signal(false);

  categorias = signal<CategoriaProducto[]>([]);
  unidades = signal<UnidadMedida[]>([]);
  proveedores = signal<Proveedor[]>([]);
  lotes = signal<LoteInventario[]>([]);
  movimientos = signal<MovimientoInventario[]>([]);

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ProductoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { isEditing: boolean; producto?: any },
    private productoService: ProductoService,
    private catalogoService: CatalogoService,
    private inventarioService: InventarioService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      marca: [''],
      descripcion: [''],
      precio: [0, [Validators.required, Validators.min(0)]],
      categoriaId: [null],
      unidadCompraId: [null],
      unidadVentaId: [null],
      factorConversion: [1, [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit() {
    this.cargarCatalogos();

    if (this.data.isEditing && this.data.producto) {
      this.form.patchValue(this.data.producto);
      this.cargarLotes();
      this.cargarMovimientos();
    }
  }

  cargarCatalogos() {
    this.catalogoService.listarCategorias().subscribe(res => this.categorias.set(res));
    this.catalogoService.listarUnidades().subscribe(res => this.unidades.set(res));
    this.catalogoService.listarProveedores().subscribe(res => this.proveedores.set(res));
  }

  cargarLotes() {
    const sedeId = Number(localStorage.getItem('vet_sede_id')) || 1;
    this.inventarioService.obtenerLotes(this.data.producto.id, sedeId).subscribe({
      next: (res) => this.lotes.set(res),
      error: () => {}
    });
  }

  guardar() {
    if (this.form.invalid) return;
    this.cargando.set(true);

    const obs$ = this.data.isEditing
      ? this.productoService.actualizar(this.data.producto.id, this.form.value)
      : this.productoService.crear(this.form.value);

    obs$.subscribe({
      next: () => {
        this.snackBar.open('Producto guardado exitosamente', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: () => {
        this.snackBar.open('Error al guardar el producto', 'Cerrar', { duration: 3000 });
        this.cargando.set(false);
      }
    });
  }

  abrirIngresoStock() {
    const dialogRef = this.dialog.open(IngresoStockDialogComponent, {
      width: '500px',
      data: {
        producto: this.data.producto,
        proveedores: this.proveedores()
      }
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res) this.cargarLotes();
    });
  }

  estaPorVencer(fecha: string): boolean {
    const hoy = new Date();
    const fVencimiento = new Date(fecha);
    const diffTime = fVencimiento.getTime() - hoy.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 30;
  }

  cerrar() {
    this.dialogRef.close();
  }

  cargarMovimientos() {
    const sedeId = Number(localStorage.getItem('vet_sede_id')) || 1;
    this.inventarioService.obtenerMovimientos(this.data.producto.id, sedeId).subscribe({
      next: (res) => this.movimientos.set(res),
      error: () => {}
    });
  }

  esEntrada(tipo: string): boolean {
    return tipo === 'ENTRADA_COMPRA' || tipo === 'AJUSTE_POSITIVO';
  }

  traducirTipo(tipo: string): string {
    const map: Record<string, string> = {
      'ENTRADA_COMPRA': 'Compra',
      'SALIDA_VENTA': 'Venta',
      'SALIDA_CONSUMO_INTERNO': 'Consumo Interno',
      'AJUSTE_POSITIVO': 'Ajuste (+)',
      'AJUSTE_NEGATIVO': 'Ajuste (-)',
      'MERMA_VENCIMIENTO': 'Merma'
    };
    return map[tipo] || tipo;
  }
}
