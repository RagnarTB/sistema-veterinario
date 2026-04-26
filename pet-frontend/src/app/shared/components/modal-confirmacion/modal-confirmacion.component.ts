import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface ConfirmData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  isDestructive?: boolean;
}

@Component({
  selector: 'app-modal-confirmacion',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title class="dialog-title">
      <span class="material-icons-round" [class.danger]="data.isDestructive">
        {{ data.isDestructive ? 'warning' : 'help_outline' }}
      </span>
      {{ data.title }}
    </h2>
    <mat-dialog-content class="dialog-content">
      <p>{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end" class="dialog-actions">
      <button mat-stroked-button (click)="onCancel()" class="btn-cancel">
        {{ data.cancelText || 'Cancelar' }}
      </button>
      <button mat-flat-button color="primary" (click)="onConfirm()" [class.btn-danger]="data.isDestructive">
        {{ data.confirmText || 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0;
      padding: 24px 24px 0;
    }
    .dialog-title .material-icons-round { color: var(--color-primary-400); font-size: 28px; }
    .dialog-title .material-icons-round.danger { color: #ef4444; }
    .dialog-content { margin-top: 16px; color: var(--text-secondary); font-size: 1rem; line-height: 1.5; padding: 0 24px 24px; }
    .dialog-actions { padding: 0 24px 24px; }
    .btn-cancel { border-color: var(--border-color); color: var(--text-secondary); }
    ::ng-deep .btn-danger { background-color: #ef4444 !important; color: white !important; }
  `]
})
export class ModalConfirmacionComponent {
  constructor(
    public dialogRef: MatDialogRef<ModalConfirmacionComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmData
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
