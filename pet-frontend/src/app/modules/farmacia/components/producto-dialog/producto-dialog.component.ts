import { Component, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProductoService } from '../../services/producto.service';
import { CatalogoService, CategoriaProducto, UnidadMedida, Proveedor } from '../../services/catalogo.service';
import { InventarioService, LoteInventario, MovimientoInventario } from '../../services/inventario.service';
import { IngresoStockDialogComponent } from '../ingreso-stock-dialog/ingreso-stock-dialog.component';
import { SalidaStockDialogComponent } from '../salida-stock-dialog/salida-stock-dialog.component';

@Component({
  selector: 'app-producto-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MatDialogModule, MatTabsModule],
  template: `
    <div class="dialog-container">
      <div class="dialog-header" style="display: flex; justify-content: space-between; align-items: center;">
        <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--text-primary); margin: 0;">
          {{ data.isEditing ? 'Detalles del Producto' : (confirmando() ? 'Confirmar Datos' : 'Nuevo Producto') }}
        </h2>
        <button class="btn-icon" (click)="cerrar()">
          <span class="material-icons-round">close</span>
        </button>
      </div>

      <div class="dialog-content" style="padding: 0 !important;">
        <mat-tab-group animationDuration="200ms" class="custom-tabs" [selectedIndex]="tabIndex()">
          
          <!-- TAB 1: DATOS BÁSICOS -->
          <mat-tab label="Datos del Producto">
            <div style="padding: 1.5rem;">
              @if (!confirmando()) {
                <form [formGroup]="form" (ngSubmit)="irAConfirmar()">
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
                      Revisar Datos
                    </button>
                  </div>
                </form>
              } @else {
                <!-- VISTA DE CONFIRMACIÓN -->
                <div style="background: var(--bg-secondary); border-radius: 12px; border: 1px solid var(--border-color); padding: 1.25rem; margin-bottom: 1.5rem;">
                  <p class="text-xs font-bold" style="color: var(--text-secondary); text-transform: uppercase; margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.5rem;">Resumen del Producto (Normalizado)</p>
                  
                  <div class="grid grid-cols-2 gap-x-6 gap-y-4">
                    <div class="summary-item">
                      <span class="text-xs" style="color: var(--text-muted)">Nombre:</span>
                      <p class="font-bold" style="color: var(--color-primary-400); text-transform: lowercase;">{{ normalizar(form.get('nombre')?.value) }}</p>
                    </div>
                    <div class="summary-item">
                      <span class="text-xs" style="color: var(--text-muted)">Marca:</span>
                      <p class="font-medium" style="text-transform: lowercase;">{{ normalizar(form.get('marca')?.value) || '—' }}</p>
                    </div>
                    <div class="summary-item">
                      <span class="text-xs" style="color: var(--text-muted)">Precio:</span>
                      <p class="font-bold">S/ {{ form.get('precio')?.value | number:'1.2-2' }}</p>
                    </div>
                    <div class="summary-item">
                      <span class="text-xs" style="color: var(--text-muted)">Categoría:</span>
                      <p class="font-medium">{{ obtenerNombreCat() }}</p>
                    </div>
                    <div class="summary-item">
                      <span class="text-xs" style="color: var(--text-muted)">U. Compra:</span>
                      <p class="font-medium">{{ obtenerNombreUni('unidadCompraId') }}</p>
                    </div>
                    <div class="summary-item">
                      <span class="text-xs" style="color: var(--text-muted)">U. Venta:</span>
                      <p class="font-medium">{{ obtenerNombreUni('unidadVentaId') }}</p>
                    </div>
                    <div class="summary-item" style="grid-column: span 2;">
                      <span class="text-xs" style="color: var(--text-muted)">Conversión:</span>
                      <p class="text-sm">1 {{ obtenerNombreUni('unidadCompraId') }} = <strong>{{ form.get('factorConversion')?.value }}</strong> {{ obtenerNombreUni('unidadVentaId') }}</p>
                    </div>
                  </div>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
                  <button type="button" class="btn btn-secondary" (click)="confirmando.set(false)" [disabled]="cargando()">
                    <span class="material-icons-round" style="font-size: 18px;">edit</span> Volver a Editar
                  </button>
                  <button type="button" class="btn btn-primary" (click)="guardar()" [disabled]="cargando()">
                    {{ cargando() ? 'Guardando...' : 'Confirmar y Guardar' }}
                  </button>
                </div>
              }
            </div>
          </mat-tab>

          <!-- TAB 2: CONTROL DE STOCK (Solo si está editando) -->
          @if (data.isEditing) {
            <mat-tab label="Control de Stock y Lotes">
              <div style="padding: 1.5rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                  <h3 style="font-weight: 600; color: var(--text-primary); margin: 0;">Lotes Activos</h3>
                  <div style="display: flex; gap: 8px;">
                    <button class="btn btn-secondary" style="padding: 0.4rem 0.85rem; color: #f87171; border-color: rgba(248,113,113,0.3);" (click)="abrirSalidaStock()">
                      <span class="material-icons-round" style="font-size: 16px;">remove_circle_outline</span>
                      Salida / Ajuste
                    </button>
                    <button class="btn btn-primary" style="padding: 0.4rem 0.85rem;" (click)="abrirIngresoStock()">
                      <span class="material-icons-round" style="font-size: 16px;">add_box</span>
                      Registrar Ingreso
                    </button>
                  </div>
                </div>

                <div class="table-container">
                  <table class="table">
                    <thead>
                      <tr>
                        <th>Nro. Lote</th>
                        <th>Vencimiento</th>
                        <th>Proveedor</th>
                        <th>Stock</th>
                        <th class="text-center">Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (lote of lotes(); track lote.id) {
                        <tr>
                          <td>
                            @if (editandoLoteId() === lote.id) {
                              <input [(ngModel)]="loteEditData.numeroLote" class="form-control" style="padding: 2px 4px; font-size: 0.75rem;">
                            } @else {
                              <span class="font-medium">{{ lote.numeroLote }}</span>
                            }
                          </td>
                          <td>
                            @if (editandoLoteId() === lote.id) {
                              <input type="date" [(ngModel)]="loteEditData.fechaVencimiento" class="form-control" style="padding: 2px 4px; font-size: 0.75rem;">
                            } @else {
                              <span class="badge" [class.badge-error]="estaPorVencer(lote.fechaVencimiento)">
                                {{ lote.fechaVencimiento || 'Sin fecha' }}
                              </span>
                            }
                          </td>
                          <td>
                            @if (editandoLoteId() === lote.id) {
                              <select [(ngModel)]="loteEditData.proveedorId" class="form-control" style="padding: 2px 4px; font-size: 0.75rem;">
                                <option [ngValue]="null">Sin proveedor</option>
                                @for (p of proveedores(); track p.id) {
                                  <option [ngValue]="p.id">{{ p.razonSocial }}</option>
                                }
                              </select>
                            } @else {
                              {{ lote.proveedorNombre || 'N/A' }}
                            }
                          </td>
                          <td class="font-bold" style="color: var(--color-primary-400)">{{ lote.stockRestante }}</td>
                          <td class="text-center">
                            @if (editandoLoteId() === lote.id) {
                              <div style="display: flex; gap: 4px; justify-content: center;">
                                <button class="btn-icon" (click)="guardarEdicionLote()" title="Guardar"><span class="material-icons-round" style="color: #4ade80; font-size: 18px;">check</span></button>
                                <button class="btn-icon" (click)="cancelarEdicionLote()" title="Cancelar"><span class="material-icons-round" style="color: #f87171; font-size: 18px;">close</span></button>
                              </div>
                            } @else {
                              <button class="btn-icon" (click)="iniciarEdicionLote(lote)" title="Editar Lote">
                                <span class="material-icons-round" style="color: #60a5fa; font-size: 18px;">edit</span>
                              </button>
                            }
                          </td>
                        </tr>
                        @if (editandoLoteId() === lote.id) {
                          <tr>
                            <td colspan="5" style="background: rgba(59,130,246,0.05); padding: 8px 12px;">
                              <div style="display: flex; align-items: center; gap: 8px;">
                                <span class="text-xs font-bold" style="color: var(--text-secondary);">Motivo del cambio:</span>
                                <input [(ngModel)]="loteEditData.motivo" class="form-control" placeholder="Ej. Corrección de error de digitación..." style="flex: 1; padding: 4px 8px; font-size: 0.75rem;">
                              </div>
                            </td>
                          </tr>
                        }
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
    .summary-item p { margin: 0; }
  `]
})
export class ProductoDialogComponent implements OnInit {
  form: FormGroup;
  cargando = signal(false);
  confirmando = signal(false);
  tabIndex = signal(0);
  editandoLoteId = signal<number | null>(null);
  loteEditData = {
    numeroLote: '',
    fechaVencimiento: '',
    proveedorId: null as number | null,
    motivo: ''
  };

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

  normalizar(texto: string): string {
    if (!texto) return '';
    return texto.toLowerCase()
      .replace(/[^a-z0-9áéíóúñ\s]/g, '')
      .replace(/\s+/g, ' ')
      .trim();
  }

  obtenerNombreCat(): string {
    const id = this.form.get('categoriaId')?.value;
    return this.categorias().find(c => c.id === id)?.nombre || 'Sin categoría';
  }

  obtenerNombreUni(control: string): string {
    const id = this.form.get(control)?.value;
    return this.unidades().find(u => u.id === id)?.nombre || 'Sin unidad';
  }

  irAConfirmar() {
    if (this.form.invalid) return;
    this.confirmando.set(true);
  }

  guardar() {
    this.cargando.set(true);

    // Los datos ya se normalizan en el backend, pero los mandamos limpios
    const values = {
      ...this.form.value,
      nombre: this.normalizar(this.form.value.nombre),
      marca: this.normalizar(this.form.value.marca)
    };

    const obs$ = this.data.isEditing
      ? this.productoService.actualizar(this.data.producto.id, values)
      : this.productoService.crear(values);

    obs$.subscribe({
      next: () => {
        this.snackBar.open('Producto guardado exitosamente', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.snackBar.open(err.error?.message || 'Error al guardar el producto', 'Cerrar', { duration: 3000 });
        this.cargando.set(false);
      }
    });
  }

  cargarLotes() {
    const sedeId = Number(localStorage.getItem('vet_sede_id')) || 1;
    this.inventarioService.obtenerLotes(this.data.producto.id, sedeId).subscribe({
      next: (res) => this.lotes.set(res),
      error: () => {}
    });
  }

  abrirIngresoStock() {
    // Buscar la unidad para pasar la info de decimales
    const uniId = this.data.producto.unidadCompraId;
    const unidad = this.unidades().find(u => u.id === uniId);

    const dialogRef = this.dialog.open(IngresoStockDialogComponent, {
      width: '500px',
      data: {
        producto: {
          ...this.data.producto,
          unidadCompraPermiteDecimales: unidad ? unidad.permiteDecimales : false
        },
        proveedores: this.proveedores()
      }
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res) { this.cargarLotes(); this.cargarMovimientos(); }
    });
  }

  abrirSalidaStock() {
    const uniId = this.data.producto.unidadVentaId;
    const unidad = this.unidades().find(u => u.id === uniId);

    const dialogRef = this.dialog.open(SalidaStockDialogComponent, {
      width: '500px',
      data: {
        producto: {
          ...this.data.producto,
          unidadVentaPermiteDecimales: unidad ? unidad.permiteDecimales : false
        }
      }
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res) { this.cargarLotes(); this.cargarMovimientos(); }
    });
  }

  estaPorVencer(fecha: string): boolean {
    if (!fecha) return false;
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

  iniciarEdicionLote(lote: any) {
    this.editandoLoteId.set(lote.id);
    this.loteEditData = {
      numeroLote: lote.numeroLote,
      fechaVencimiento: lote.fechaVencimiento || '',
      proveedorId: null, // Tendríamos que buscar el ID por el nombre si no viene, pero lo ideal es que venga en el DTO
      motivo: ''
    };
    // Intentar buscar el proveedor ID por nombre si no viene
    const prov = this.proveedores().find(p => p.razonSocial === lote.proveedorNombre);
    if (prov) this.loteEditData.proveedorId = prov.id;
  }

  cancelarEdicionLote() {
    this.editandoLoteId.set(null);
  }

  guardarEdicionLote() {
    if (!this.loteEditData.numeroLote || !this.loteEditData.motivo) {
      this.snackBar.open('Complete el número de lote y el motivo', 'Cerrar', { duration: 3000 });
      return;
    }

    this.inventarioService.actualizarLote(this.editandoLoteId()!, this.loteEditData).subscribe({
      next: () => {
        this.snackBar.open('Lote actualizado correctamente', 'Cerrar', { duration: 3000 });
        this.editandoLoteId.set(null);
        this.cargarLotes();
        this.cargarMovimientos();
      },
      error: () => this.snackBar.open('Error al actualizar el lote', 'Cerrar', { duration: 3000 })
    });
  }
}
