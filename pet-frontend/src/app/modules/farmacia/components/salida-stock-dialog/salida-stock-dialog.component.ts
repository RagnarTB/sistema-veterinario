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
          {{ confirmando() ? 'Confirmar Movimiento' : 'Registrar Salida / Ajuste' }}
        </h2>
        <button class="btn-icon" (click)="cerrar()">
          <span class="material-icons-round">close</span>
        </button>
      </div>

      <div class="dialog-content" style="padding: 1.5rem;">
        
        @if (!confirmando()) {
          <div style="margin-bottom: 1rem; padding: 0.75rem; background: var(--bg-tertiary); border-radius: 8px; border: 1px solid var(--border-color);">
            <p class="text-sm" style="color: var(--text-secondary)">Producto:</p>
            <p class="font-bold" style="color: var(--text-primary)">{{ data.producto.nombre }}</p>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px;">
              <p class="text-xs" style="color: var(--text-muted);">
                Unidad: <strong>{{ data.producto.unidadVentaNombre }}</strong> 
                @if (!permiteDecimales) {
                  <span style="color: #fca5a5;">(No permite decimales)</span>
                }
              </p>
              <div style="text-align: right;">
                <span class="text-xs" style="color: var(--text-muted);">Stock Actual:</span>
                <p class="font-bold" style="color: var(--color-primary-400); font-size: 1rem;">{{ data.producto.stockActual || 0 }}</p>
              </div>
            </div>
          </div>

          <form [formGroup]="form" (ngSubmit)="irAConfirmar()">
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
                <input type="number" formControlName="cantidad" class="form-control" [step]="permiteDecimales ? '0.01' : '1'" min="0.01"
                       (keypress)="permiteDecimales ? null : soloEnteros($event)">
              </div>

              <div class="form-group" style="grid-column: span 2;">
                <label>Motivo / Observación *</label>
                <textarea formControlName="motivo" class="form-control" rows="2" placeholder="Describa el motivo del movimiento..."></textarea>
              </div>
            </div>

            <div style="margin-top: 1.5rem; display: flex; justify-content: flex-end; gap: 0.75rem;">
              <button type="button" class="btn btn-secondary" (click)="cerrar()">Cancelar</button>
              <button type="submit" class="btn btn-primary" [disabled]="form.invalid">
                Revisar Movimiento
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

            <div class="grid grid-cols-3 gap-4">
              <div class="summary-item">
                <span class="text-xs" style="color: var(--text-muted)">Stock Actual:</span>
                <p class="font-medium">{{ data.producto.stockActual || 0 }}</p>
              </div>
              <div class="summary-item">
                <span class="text-xs" style="color: var(--text-muted)">Ajuste:</span>
                <p class="font-bold" [style.color]="esSalida() ? '#f87171' : '#4ade80'" style="font-size: 1.1rem;">
                  {{ esSalida() ? '-' : '+' }} {{ form.get('cantidad')?.value }}
                </p>
              </div>
              <div class="summary-item">
                <span class="text-xs" style="color: var(--text-muted)">Tipo:</span>
                <p class="font-medium">{{ getLabelTipo(form.get('tipoMovimiento')?.value) }}</p>
              </div>
            </div>

            <div class="summary-item" style="margin-top: 0.75rem;">
              <span class="text-xs" style="color: var(--text-muted)">Motivo:</span>
              <p class="text-sm" style="font-style: italic;">"{{ form.get('motivo')?.value }}"</p>
            </div>
          </div>

          <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
            <button type="button" class="btn btn-secondary" (click)="confirmando.set(false)" [disabled]="cargando()">
              <span class="material-icons-round" style="font-size: 18px;">edit</span> Volver a Editar
            </button>
            <button type="button" class="btn btn-primary" [style.background]="esSalida() ? '#dc2626' : '#16a34a'" (click)="guardar()" [disabled]="cargando()">
              {{ cargando() ? 'Procesando...' : 'Confirmar y Guardar' }}
            </button>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .summary-item p { margin: 0; }
  `]
})
export class SalidaStockDialogComponent {
  form: FormGroup;
  cargando = signal(false);
  confirmando = signal(false);

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<SalidaStockDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { producto: any; sedeId: number },
    private inventarioService: InventarioService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      tipoMovimiento: ['', Validators.required],
      cantidad: [1, [Validators.required, Validators.min(0.01)]],
      motivo: ['', Validators.required]
    });
  }

  get permiteDecimales(): boolean {
    return this.data.producto.unidadVentaPermiteDecimales !== false;
  }

  esSalida(): boolean {
    const tipo = this.form.get('tipoMovimiento')?.value || '';
    return tipo === 'SALIDA_CONSUMO_INTERNO' || tipo === 'AJUSTE_NEGATIVO' || tipo === 'MERMA_VENCIMIENTO';
  }

  getLabelTipo(tipo: string): string {
    const map: Record<string, string> = {
      'SALIDA_CONSUMO_INTERNO': 'Consumo Interno',
      'AJUSTE_NEGATIVO': 'Ajuste Negativo',
      'MERMA_VENCIMIENTO': 'Merma por Vencimiento',
      'AJUSTE_POSITIVO': 'Ajuste Positivo'
    };
    return map[tipo] || tipo;
  }

  soloEnteros(event: KeyboardEvent) {
    if (!/[0-9]/.test(event.key)) {
      event.preventDefault();
    }
  }

  irAConfirmar() {
    if (this.form.invalid) return;

    if (!this.permiteDecimales) {
      const cant = this.form.get('cantidad')?.value;
      if (cant % 1 !== 0) {
        this.msg('Esta unidad no permite decimales. Ingrese un número entero.');
        return;
      }
    }

    this.confirmando.set(true);
  }

  guardar() {
    this.cargando.set(true);
    const sedeId = this.data.sedeId || Number(localStorage.getItem('vet_sede_id')) || 1;

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
        this.msg(err.error?.message || 'Error al registrar el movimiento');
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
