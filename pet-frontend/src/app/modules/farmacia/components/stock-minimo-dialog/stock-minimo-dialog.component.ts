import { Component, Inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';

@Component({
  selector: 'app-stock-minimo-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule],
  template: `
    <div class="dialog-container" style="min-width: 350px;">
      <div class="dialog-header" style="background: rgba(96, 165, 250, 0.08); border-bottom: 1px solid rgba(96, 165, 250, 0.2);">
        <h2 style="font-size: 1.1rem; font-weight: 700; color: #3b82f6; display: flex; align-items: center; gap: 8px; margin: 0;">
          <span class="material-icons-round">notifications_active</span>
          Configurar Alerta de Stock
        </h2>
      </div>

      <div class="dialog-content" style="padding: 1.5rem;">
        <div style="margin-bottom: 1.25rem;">
          <p class="text-xs" style="color: var(--text-secondary); text-transform: uppercase; font-weight: 700; margin-bottom: 4px;">Producto</p>
          <p class="font-medium" style="color: var(--text-primary); margin: 0;">{{ data.productoNombre }}</p>
        </div>

        <div class="form-group">
          <label style="font-size: 0.85rem; font-weight: 600; color: var(--text-primary);">Stock Mínimo Deseado</label>
          <div style="position: relative; margin-top: 6px;">
            <input type="number" [(ngModel)]="stockMinimo" class="form-control" 
                   style="padding-left: 40px; font-size: 1.1rem; font-weight: 700; color: #3b82f6;"
                   placeholder="0.00" min="0" step="0.01">
            <span class="material-icons-round" style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--text-muted);">
              straighten
            </span>
          </div>
          <p class="text-xs" style="color: var(--text-muted); margin-top: 8px;">
            El sistema emitirá una alerta visual cuando el stock actual sea igual o menor a este valor.
          </p>
        </div>

        <div style="margin-top: 2rem; display: flex; justify-content: flex-end; gap: 0.75rem;">
          <button class="btn btn-secondary" (click)="cerrar()">Cancelar</button>
          <button class="btn btn-primary" (click)="guardar()" [disabled]="stockMinimo < 0">
            Guardar Configuración
          </button>
        </div>
      </div>
    </div>
  `
})
export class StockMinimoDialogComponent {
  stockMinimo: number;

  constructor(
    private dialogRef: MatDialogRef<StockMinimoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { productoNombre: string, stockMinimoActual: number }
  ) {
    this.stockMinimo = data.stockMinimoActual;
  }

  cerrar() {
    this.dialogRef.close();
  }

  guardar() {
    this.dialogRef.close(this.stockMinimo);
  }
}
