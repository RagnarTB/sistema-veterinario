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
          {{ confirmando() ? 'Confirmar Ingreso' : 'Registrar Ingreso (Compra)' }}
        </h2>
        <button class="btn-icon" (click)="cerrar()">
          <span class="material-icons-round">close</span>
        </button>
      </div>

      <div class="dialog-content" style="padding: 1.5rem;">
        
        @if (!confirmando()) {
          <div style="margin-bottom: 1rem; padding: 0.75rem; background: var(--bg-tertiary); border-radius: 8px; border: 1px solid var(--border-color);">
            <p class="text-sm" style="color: var(--text-secondary)">Producto Seleccionado:</p>
            <p class="font-bold" style="color: var(--text-primary)">{{ data.producto.nombre }}</p>
            @if (data.producto.unidadCompraNombre) {
              <p class="text-xs" style="color: var(--text-muted); margin-top: 4px;">
                La cantidad debe ser en: <strong>{{ data.producto.unidadCompraNombre }}</strong>
                @if (!permiteDecimales) {
                  <span style="color: #fca5a5; display: block; margin-top: 2px;">(No permite decimales)</span>
                }
              </p>
            }
          </div>

          <form [formGroup]="form" (ngSubmit)="irAConfirmar()">
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
                <label>Cantidad Comprada *</label>
                <input type="number" formControlName="cantidadComprada" class="form-control" [step]="permiteDecimales ? '0.01' : '1'" min="0.01"
                       (keypress)="permiteDecimales ? null : soloEnteros($event)">
              </div>

              <div class="form-group" style="grid-column: span 2;">
                <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                  <label style="margin-bottom: 0; cursor: pointer; display: flex; align-items: center; gap: 6px;">
                    <input type="checkbox" formControlName="tieneVencimiento"
                           style="width: 16px; height: 16px; accent-color: var(--color-primary-400); cursor: pointer;">
                    ¿Este producto tiene fecha de vencimiento?
                  </label>
                </div>
                @if (form.get('tieneVencimiento')?.value) {
                  <input type="date" formControlName="fechaVencimiento" class="form-control" [min]="fechaMinima" onkeydown="return false">
                }
              </div>

              <div class="form-group" style="grid-column: span 2;">
                <label>Motivo / Observación</label>
                <input type="text" formControlName="motivo" class="form-control" placeholder="Opcional...">
              </div>
            </div>

            <div style="margin-top: 1.5rem; display: flex; justify-content: flex-end; gap: 0.75rem;">
              <button type="button" class="btn btn-secondary" (click)="cerrar()">Cancelar</button>
              <button type="submit" class="btn btn-primary" style="background: #16a34a; border-color: #16a34a;" [disabled]="form.invalid">
                Revisar Ingreso
              </button>
            </div>
          </form>
        } @else {
          <!-- VISTA DE CONFIRMACIÓN -->
          <div style="background: var(--bg-secondary); border-radius: 12px; border: 1px solid var(--border-color); padding: 1.25rem; margin-bottom: 1.5rem;">
            <p class="text-xs font-bold" style="color: var(--text-secondary); text-transform: uppercase; margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.5rem;">Resumen de Operación</p>
            
            <div class="summary-item" style="margin-bottom: 0.75rem;">
              <span class="text-xs" style="color: var(--text-muted)">Producto:</span>
              <p class="font-medium">{{ data.producto.nombre }}</p>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="summary-item">
                <span class="text-xs" style="color: var(--text-muted)">Cantidad a Ingresar:</span>
                <p class="font-bold" style="color: #4ade80; font-size: 1.1rem;">{{ form.get('cantidadComprada')?.value }} {{ data.producto.unidadCompraNombre }}</p>
              </div>
              <div class="summary-item">
                <span class="text-xs" style="color: var(--text-muted)">Nro. Lote:</span>
                <p class="font-medium">{{ form.get('numeroLote')?.value }}</p>
              </div>
              <div class="summary-item">
                <span class="text-xs" style="color: var(--text-muted)">Vencimiento:</span>
                <p class="font-medium">{{ form.get('tieneVencimiento')?.value ? (form.get('fechaVencimiento')?.value | date:'dd/MM/yyyy') : 'No aplica' }}</p>
              </div>
              <div class="summary-item">
                <span class="text-xs" style="color: var(--text-muted)">Proveedor:</span>
                <p class="font-medium">{{ nombreProveedorSeleccionado || 'No asignado' }}</p>
              </div>
            </div>

            @if (data.producto.factorConversion && data.producto.factorConversion > 1) {
              <div style="margin-top: 1rem; padding: 0.75rem; background: rgba(59,130,246,0.1); border: 1px solid rgba(59,130,246,0.3); border-radius: 8px;">
                <p class="text-sm" style="color: #93c5fd;">
                  Equivale a <strong>{{ cantidadConvertida | number:'1.0-2' }} {{ data.producto.unidadVentaNombre }}</strong> en stock.
                </p>
              </div>
            }
          </div>

          <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
            <button type="button" class="btn btn-secondary" (click)="confirmando.set(false)" [disabled]="cargando()">
              <span class="material-icons-round" style="font-size: 18px;">edit</span> Volver a Editar
            </button>
            <button type="button" class="btn btn-primary" style="background: #16a34a; border-color: #16a34a;" (click)="guardar()" [disabled]="cargando()">
              {{ cargando() ? 'Procesando...' : 'Confirmar y Guardar' }}
            </button>
          </div>
        }
      </div>
    </div>
  `
})
export class IngresoStockDialogComponent {
  form: FormGroup;
  cargando = signal(false);
  confirmando = signal(false);
  fechaMinima: string;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<IngresoStockDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { producto: any; proveedores: any[]; sedeId: number },
    private inventarioService: InventarioService,
    private snackBar: MatSnackBar
  ) {
    const hoy = new Date();
    this.fechaMinima = hoy.toISOString().split('T')[0];

    this.form = this.fb.group({
      proveedorId: [null],
      numeroLote: ['', Validators.required],
      tieneVencimiento: [true],
      fechaVencimiento: [''],
      cantidadComprada: [1, [Validators.required, Validators.min(0.01)]],
      motivo: ['Compra de stock']
    });
  }

  get permiteDecimales(): boolean {
    // Asumimos que data.producto tiene la info de la unidad
    // Si no, podríamos buscarla en la lista global, pero usualmente viene en el objeto producto
    return this.data.producto.unidadCompraPermiteDecimales !== false;
  }

  get nombreProveedorSeleccionado(): string {
    const id = this.form.get('proveedorId')?.value;
    return this.data.proveedores.find(p => p.id === id)?.razonSocial || '';
  }

  get cantidadConvertida(): number {
    const cantidad = this.form.get('cantidadComprada')?.value || 0;
    const factor = this.data.producto.factorConversion || 1;
    return cantidad * factor;
  }

  soloEnteros(event: KeyboardEvent) {
    if (!/[0-9]/.test(event.key)) {
      event.preventDefault();
    }
  }

  irAConfirmar() {
    if (this.form.invalid) return;
    
    // Validación extra de decimales en frontend
    if (!this.permiteDecimales) {
      const cant = this.form.get('cantidadComprada')?.value;
      if (cant % 1 !== 0) {
        this.msg('Esta unidad no permite decimales. Ingrese un número entero.');
        return;
      }
    }

    if (this.form.get('tieneVencimiento')?.value) {
      const fecha = this.form.get('fechaVencimiento')?.value;
      if (!fecha) {
        this.msg('Debe ingresar una fecha de vencimiento');
        return;
      }
      if (fecha < this.fechaMinima) {
        this.msg('La fecha de vencimiento no puede ser anterior a hoy');
        return;
      }
    }

    this.confirmando.set(true);
  }

  guardar() {
    this.cargando.set(true);

    const sedeId = this.data.sedeId || Number(localStorage.getItem('vet_sede_id')) || 1;
    const values = this.form.value;

    const payload = {
      productoId: this.data.producto.id,
      sedeId: sedeId,
      proveedorId: values.proveedorId,
      numeroLote: values.numeroLote,
      fechaVencimiento: values.tieneVencimiento ? values.fechaVencimiento : null,
      cantidadComprada: values.cantidadComprada,
      motivo: values.motivo
    };

    this.inventarioService.registrarIngreso(payload).subscribe({
      next: () => {
        this.snackBar.open('Stock ingresado correctamente', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.msg(err.error?.message || 'Error al registrar el ingreso');
        this.cargando.set(false);
      }
    });
  }

  cerrar() {
    this.dialogRef.close();
  }

  private msg(text: string) {
    this.snackBar.open(text, 'Cerrar', { duration: 3000 });
  }
}
