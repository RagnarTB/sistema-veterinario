import { Component, Inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { InventarioService } from '../../services/inventario.service';

@Component({
  selector: 'app-ingreso-stock-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule],
  template: `
    <div class="dialog-container">
      <div class="dialog-header" style="display: flex; justify-content: space-between; align-items: center; background: rgba(34,197,94,0.08); border-bottom: 1px solid rgba(34,197,94,0.2);">
        <h2 style="font-size: 1.25rem; font-weight: 700; color: #4ade80; display: flex; align-items: center; gap: 8px; margin: 0;">
          <span class="material-icons-round">add_shopping_cart</span>
          Registrar Ingreso (Compra)
        </h2>
        <button class="btn-icon" (click)="cerrar()">
          <span class="material-icons-round">close</span>
        </button>
      </div>

      <div class="dialog-content" style="padding: 1.5rem;">
        <div style="margin-bottom: 1rem; padding: 0.75rem; background: var(--bg-tertiary); border-radius: 8px; border: 1px solid var(--border-color);">
          <p class="text-sm" style="color: var(--text-secondary)">Producto Seleccionado:</p>
          <p class="font-bold" style="color: var(--text-primary)">{{ data.producto.nombre }}</p>
          @if (data.producto.unidadCompraNombre) {
            <p class="text-xs" style="color: var(--text-muted); margin-top: 4px;">
              La cantidad debe ser en: <strong>{{ data.producto.unidadCompraNombre }}</strong>
            </p>
          }
        </div>

        <form [formGroup]="form" (ngSubmit)="guardar()">
          <div class="grid grid-cols-2 gap-4">

            <div class="form-group" style="grid-column: span 2;">
              <label>Proveedor</label>
              <select formControlName="proveedorId" class="form-control">
                <option [ngValue]="null">Seleccione un proveedor...</option>
                @for (prov of data.proveedores; track prov.id) {
                  <option [ngValue]="prov.id">{{ prov.razonSocial }}</option>
                }
              </select>
            </div>

            <div class="form-group">
              <label>Nro. de Lote *</label>
              <input type="text" formControlName="numeroLote" class="form-control" placeholder="Ej. L-998877">
            </div>

            <div class="form-group">
              <label>Fecha de Vencimiento *</label>
              <input type="date" formControlName="fechaVencimiento" class="form-control">
            </div>

            <div class="form-group">
              <label>Cantidad Comprada *</label>
              <input type="number" formControlName="cantidadComprada" class="form-control" step="0.01" min="0.01">
            </div>

            <div class="form-group" style="grid-column: span 2;">
              <label>Motivo / Observación</label>
              <input type="text" formControlName="motivo" class="form-control" placeholder="Opcional...">
            </div>

          </div>

          <!-- Preview de Conversión -->
          @if (cantidadValida && data.producto.factorConversion) {
            <div style="margin-top: 1rem; padding: 0.75rem; background: rgba(59,130,246,0.1); border: 1px solid rgba(59,130,246,0.3); border-radius: 8px;">
              <p class="text-sm" style="color: #93c5fd;">
                <span class="material-icons-round" style="font-size: 16px; vertical-align: middle; margin-right: 4px;">info</span>
                Se ingresarán <strong>{{ cantidadConvertida | number:'1.0-2' }}</strong>
                {{ data.producto.unidadVentaNombre || 'Unidades de Venta' }} a tu stock.
              </p>
            </div>
          }

          <div style="margin-top: 1.5rem; display: flex; justify-content: flex-end; gap: 0.75rem;">
            <button type="button" class="btn btn-secondary" (click)="cerrar()">Cancelar</button>
            <button type="submit" class="btn btn-primary" style="background: #16a34a; border-color: #16a34a;" [disabled]="form.invalid || cargando()">
              {{ cargando() ? 'Guardando...' : 'Confirmar Ingreso' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `
})
export class IngresoStockDialogComponent {
  form: FormGroup;
  cargando = signal(false);

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<IngresoStockDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { producto: any; proveedores: any[] },
    private inventarioService: InventarioService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      proveedorId: [null],
      numeroLote: ['', Validators.required],
      fechaVencimiento: ['', Validators.required],
      cantidadComprada: [1, [Validators.required, Validators.min(0.01)]],
      motivo: ['Compra de stock']
    });
  }

  get cantidadValida(): boolean {
    const ctrl = this.form.get('cantidadComprada');
    return ctrl !== null && ctrl.valid && ctrl.value > 0;
  }

  get cantidadConvertida(): number {
    const cantidad = this.form.get('cantidadComprada')?.value || 0;
    const factor = this.data.producto.factorConversion || 1;
    return cantidad * factor;
  }

  guardar() {
    if (this.form.invalid) return;
    this.cargando.set(true);

    const sedeId = Number(localStorage.getItem('vet_sede_id')) || 1;

    const payload = {
      ...this.form.value,
      productoId: this.data.producto.id,
      sedeId: sedeId
    };

    this.inventarioService.registrarIngreso(payload).subscribe({
      next: () => {
        this.snackBar.open('Stock ingresado correctamente', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: () => {
        this.snackBar.open('Error al registrar el ingreso de stock', 'Cerrar', { duration: 3000 });
        this.cargando.set(false);
      }
    });
  }

  cerrar() {
    this.dialogRef.close();
  }
}
