import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-confirmar-cuenta',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    MatFormFieldModule, 
    MatInputModule, 
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="auth-container">
      <div class="auth-card premium-card fade-in-up">
        
        <!-- Header -->
        <div class="auth-header text-center">
          <div class="brand">
            <mat-icon color="primary" class="brand-icon">pets</mat-icon>
            <h1 class="brand-name">VetCare</h1>
          </div>
          <h2 class="auth-title">Activar tu Cuenta</h2>
          <p class="auth-subtitle">Crea una contraseña segura para tu portal de cliente.</p>
        </div>

        @if (loading()) {
            <div class="loading-state">
                <mat-spinner diameter="40"></mat-spinner>
                <p>Validando tu enlace...</p>
            </div>
        } @else if (errorMsg()) {
            <div class="error-msg text-center">
                <mat-icon color="warn">error_outline</mat-icon>
                <p>{{ errorMsg() }}</p>
                <button mat-stroked-button color="primary" (click)="irALogin()" class="mt-16">Volver al Inicio</button>
            </div>
        } @else if (successMsg()) {
            <div class="success-msg text-center">
                <mat-icon class="green-icon">check_circle</mat-icon>
                <p>{{ successMsg() }}</p>
                <button mat-flat-button color="primary" (click)="irALogin()" class="mt-16 btn-block">Ir a Iniciar Sesión</button>
            </div>
        } @else {
            <form [formGroup]="form" (ngSubmit)="onSubmit()" class="auth-form">
                <mat-form-field appearance="outline" class="w-full">
                    <mat-label>Nueva Contraseña</mat-label>
                    <mat-icon matPrefix>lock</mat-icon>
                    <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password" required>
                    <button mat-icon-button matSuffix type="button" (click)="togglePassword($event)">
                        <mat-icon>{{hidePassword() ? 'visibility_off' : 'visibility'}}</mat-icon>
                    </button>
                    <mat-error *ngIf="form.get('password')?.hasError('required')">La contraseña es requerida</mat-error>
                    <mat-error *ngIf="form.get('password')?.hasError('minlength')">Debe tener al menos 6 caracteres</mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="w-full">
                    <mat-label>Repetir Contraseña</mat-label>
                    <mat-icon matPrefix>lock_outline</mat-icon>
                    <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="confirmPassword" required>
                    <mat-error *ngIf="form.hasError('passwordsMismatch')">Las contraseñas no coinciden</mat-error>
                </mat-form-field>

                <button mat-flat-button color="primary" class="w-full btn-large" type="submit" [disabled]="form.invalid || isSubmitting()">
                    {{ isSubmitting() ? 'Guardando...' : 'Activar mi Cuenta' }}
                </button>
            </form>
        }

      </div>
    </div>
  `,
  styles: [`
    .auth-container { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: var(--bg-default); padding: 20px; }
    .auth-card { width: 100%; max-width: 450px; padding: 40px; background: var(--bg-card); border-radius: var(--radius-xl); border: 1px solid var(--border-color); }
    .auth-header { margin-bottom: 32px; }
    .brand { display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 16px; }
    .brand-icon { font-size: 32px; height: 32px; width: 32px; display: flex; align-items: center; justify-content: center; }
    .brand-name { font-size: 1.5rem; font-weight: 800; color: var(--text-primary); margin: 0; background: linear-gradient(135deg, var(--color-primary-400), #8b5cf6); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    .auth-title { font-size: 1.8rem; font-weight: 700; color: var(--text-primary); margin: 0 0 8px; }
    .auth-subtitle { color: var(--text-secondary); margin: 0; font-size: 0.95rem; }
    
    .loading-state, .error-msg, .success-msg { display: flex; flex-direction: column; align-items: center; padding: 24px 0; gap: 16px; color: var(--text-secondary); }
    .error-msg mat-icon { font-size: 48px; width: 48px; height: 48px; }
    .success-msg p { color: var(--text-primary); font-size: 1.1rem; font-weight: 500; }
    .green-icon { color: #10b981; font-size: 56px; width: 56px; height: 56px; }

    .w-full { width: 100%; }
    .btn-large { height: 48px; font-weight: 600; font-size: 1rem; border-radius: var(--radius-md); margin-top: 16px; }
    .btn-block { width: 100%; }
    .mt-16 { margin-top: 16px; }
    .text-center { text-align: center; }
    
    ::ng-deep .mat-mdc-text-field-wrapper { background-color: rgba(0, 0, 0, 0.2) !important; }
  `]
})
export class ConfirmarCuentaComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  token = '';
  form!: FormGroup;
  
  hidePassword = signal(true);
  loading = signal(true);
  isSubmitting = signal(false);
  errorMsg = signal('');
  successMsg = signal('');

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'] || '';

    if (!this.token) {
      this.errorMsg.set('El enlace es inválido o no contiene un token.');
      this.loading.set(false);
    } else {
      this.loading.set(false);
    }

    this.form = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordsMatch });
  }

  passwordsMatch(group: FormGroup) {
    const pass = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return pass === confirm ? null : { passwordsMismatch: true };
  }

  togglePassword(event: MouseEvent) {
    event.stopPropagation();
    this.hidePassword.set(!this.hidePassword());
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.isSubmitting.set(true);
    const password = this.form.get('password')?.value;

    this.authService.confirmarCuenta(this.token, password).subscribe({
      next: (res) => {
        this.successMsg.set(res.mensaje || '¡Cuenta activada exitosamente!');
        this.isSubmitting.set(false);
      },
      error: (err) => {
        this.errorMsg.set(err.error?.mensaje || 'Error al validar el enlace. Es posible que haya expirado.');
        this.isSubmitting.set(false);
      }
    });
  }

  irALogin() {
    this.router.navigate(['/login']);
  }
}
