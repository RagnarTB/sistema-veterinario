import { Component, Inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { InventarioService } from '../../services/inventario.service';

@Component({
  selector: 'app-salida-stock-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule],
  template: `
    <div class="dialog-container">
      <div class="dialog-header" style="display: flex; justify-content: space-between; align-items: center; background: rgba(239,68,68,0.08); border-bottom: 1px solid rgba(239,68,68,0.2);">
        <h2 style="font-size: 1.25rem; font-weight: 700; color: #f87171; display: flex; align-items: center; gap: 8px; margin: 0;">
          <span class="material-icons-round">remove_shopping_cart</span>
          Registrar Salida / Ajuste
        </h2>
        <button class="btn-icon" (click)="cerrar()">
          <span class="material-icons-round">close</span>
        </button>
      </div>

      <div class="dialog-content" style="padding: 1.5rem;">
        <div style="margin-bottom: 1rem; padding: 0.75rem; background: var(--bg-tertiary); border-radius: 8px; border: 1px solid var(--border-color);">
          <p class="text-sm" style="color: var(--text-secondary)">Producto:</p>
          <p class="font-bold" style="color: var(--text-primary)">{{ data.producto.nombre }}</p>
          <p class="text-xs" style="color: var(--text-muted); margin-top: 4px;">
            Stock actual: <strong style="color: var(--color-primary-400)">{{ data.producto.stockActual ?? 'N/A' }}</strong>
          </p>
        </div>

        <form [formGroup]="form" (ngSubmit)="guardar()">
          <div class="grid grid-cols-2 gap-4">
            <div class="form-group" style="grid-column: span 2;">
              <label>Tipo de Movimiento *</label>
              <select formControlName="tipoMovimiento" class="form-control">
                <option value="">Seleccione...</option>
                <option value="SALIDA_CONSUMO_INTERNO">Consumo Interno</option>
                <option value="AJUSTE_NEGATIVO">Ajuste Negativo (Corrección)</option>
                <option value="MERMA_VENCIMIENTO">Merma por Vencimiento</option>
                <option value="AJUSTE_POSITIVO">Ajuste Positivo (Corrección)</option>
              </select>
            </div>

            <div class="form-group">
              <label>Cantidad *</label>
              <input type="number" formControlName="cantidad" class="form-control" step="0.01" min="0.01">
            </div>

            <div class="form-group" style="grid-column: span 2;">
              <label>Motivo / Observación *</label>
              <textarea formControlName="motivo" class="form-control" rows="2" placeholder="Describa el motivo del movimiento..."></textarea>
            </div>
          </div>

          @if (form.get('tipoMovimiento')?.value) {
            <div style="margin-top: 1rem; padding: 0.75rem; border-radius: 8px;"
                 [style.background]="esSalida() ? 'rgba(239,68,68,0.1)' : 'rgba(34,197,94,0.1)'"
                 [style.border]="esSalida() ? '1px solid rgba(239,68,68,0.3)' : '1px solid rgba(34,197,94,0.3)'">
              <p class="text-sm" [style.color]="esSalida() ? '#fca5a5' : '#86efac'">
                <span class="material-icons-round" style="font-size: 16px; vertical-align: middle; margin-right: 4px;">info</span>
                {{ esSalida() ? 'Se DESCONTARÁN' : 'Se SUMARÁN' }}
                <strong>{{ form.get('cantidad')?.value || 0 }}</strong> unidades del stock.
              </p>
            </div>
          }

          <div style="margin-top: 1.5rem; display: flex; justify-content: flex-end; gap: 0.75rem;">
            <button type="button" class="btn btn-secondary" (click)="cerrar()">Cancelar</button>
            <button type="submit" class="btn btn-primary"
                    [style.background]="esSalida() ? '#dc2626' : '#16a34a'"
                    [style.border-color]="esSalida() ? '#dc2626' : '#16a34a'"
                    [disabled]="form.invalid || cargando()">
              {{ cargando() ? 'Procesando...' : 'Confirmar Movimiento' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `
})
export class SalidaStockDialogComponent {
  form: FormGroup;
  cargando = signal(false);

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<SalidaStockDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { producto: any },
    private inventarioService: InventarioService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      tipoMovimiento: ['', Validators.required],
      cantidad: [1, [Validators.required, Validators.min(0.01)]],
      motivo: ['', Validators.required]
    });
  }

  esSalida(): boolean {
    const tipo = this.form.get('tipoMovimiento')?.value || '';
    return tipo === 'SALIDA_CONSUMO_INTERNO' || tipo === 'AJUSTE_NEGATIVO' || tipo === 'MERMA_VENCIMIENTO';
  }

  guardar() {
    if (this.form.invalid) return;
    this.cargando.set(true);

    const sedeId = Number(localStorage.getItem('vet_sede_id')) || 1;

    const payload = {
      productoId: this.data.producto.id,
      sedeId: sedeId,
      ...this.form.value
    };

    this.inventarioService.registrarSalidaAjuste(payload).subscribe({
      next: () => {
        this.snackBar.open('Movimiento registrado correctamente', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (err) => {
        const errorMsg = err?.error?.message || 'Error al registrar el movimiento';
        this.snackBar.open(errorMsg, 'Cerrar', { duration: 3000 });
        this.cargando.set(false);
      }
    });
  }

  cerrar() {
    this.dialogRef.close();
  }
}
