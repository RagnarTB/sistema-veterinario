import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="login-container fade-in-up">
      <!-- Logo -->
      <div class="login-logo">
        <span class="material-icons-round">pets</span>
      </div>

      <h2 class="login-title">Bienvenido de nuevo</h2>
      <p class="login-sub">Inicia sesión en tu cuenta</p>

      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="login-form" novalidate>

        <!-- Email -->
        <div class="form-group">
          <label class="form-label" for="email">Correo electrónico</label>
          <div class="input-wrapper">
            <span class="input-icon material-icons-round">mail_outline</span>
            <input
              id="email"
              type="email"
              formControlName="email"
              class="form-control with-icon"
              placeholder="veterinario@clinica.com"
              autocomplete="email"
            />
          </div>
          @if (f['email'].touched && f['email'].errors?.['required']) {
            <span class="form-error">El email es obligatorio</span>
          }
          @if (f['email'].touched && f['email'].errors?.['email']) {
            <span class="form-error">Formato de email inválido</span>
          }
        </div>

        <!-- Password -->
        <div class="form-group">
          <label class="form-label" for="password">Contraseña</label>
          <div class="input-wrapper">
            <span class="input-icon material-icons-round">lock_outline</span>
            <input
              id="password"
              [type]="showPass() ? 'text' : 'password'"
              formControlName="password"
              class="form-control with-icon with-action"
              placeholder="••••••••"
              autocomplete="current-password"
            />
            <button
              type="button"
              class="input-action"
              (click)="showPass.update(v => !v)"
              [attr.aria-label]="showPass() ? 'Ocultar contraseña' : 'Mostrar contraseña'"
            >
              <span class="material-icons-round">
                {{ showPass() ? 'visibility_off' : 'visibility' }}
              </span>
            </button>
          </div>
          @if (f['password'].touched && f['password'].errors?.['required']) {
            <span class="form-error">La contraseña es obligatoria</span>
          }
        </div>

        <!-- Error global -->
        @if (errorMsg()) {
          <div class="alert-error">
            <span class="material-icons-round">error_outline</span>
            {{ errorMsg() }}
          </div>
        }

        <!-- Submit -->
        <button
          type="submit"
          class="btn btn-primary submit-btn"
          [disabled]="loading()"
          id="btn-login"
        >
          @if (loading()) {
            <mat-spinner diameter="20" color="accent" />
            Iniciando sesión...
          } @else {
            <span class="material-icons-round">login</span>
            Iniciar sesión
          }
        </button>

      </form>

      <p class="login-footer">
        ¿Primera vez? Solicita acceso a un administrador.
      </p>
    </div>
  `,
  styles: [`
    .login-container {
      width: 100%;
      max-width: 420px;
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-xl);
      padding: 2.5rem 2rem;
      box-shadow: var(--shadow-lg), 0 0 40px rgba(0,189,189,0.06);
    }

    .login-logo {
      width: 56px; height: 56px;
      background: linear-gradient(135deg, var(--color-primary-800), var(--color-primary-500));
      border-radius: var(--radius-lg);
      display: flex; align-items: center; justify-content: center;
      margin: 0 auto 1.5rem;
      box-shadow: 0 6px 20px rgba(0,189,189,0.3);
    }

    .login-logo .material-icons-round { font-size: 28px; color: white; }

    .login-title {
      font-size: 1.5rem;
      font-weight: 700;
      text-align: center;
      color: var(--text-primary);
      margin-bottom: 0.25rem;
    }

    .login-sub {
      font-size: 0.875rem;
      color: var(--text-muted);
      text-align: center;
      margin-bottom: 2rem;
    }

    .login-form { display: flex; flex-direction: column; gap: 0; }

    .input-wrapper {
      position: relative;
      display: flex;
      align-items: center;
    }

    .input-icon {
      position: absolute;
      left: 12px;
      font-size: 18px;
      color: var(--text-muted);
      pointer-events: none;
      z-index: 1;
    }

    .form-control.with-icon { padding-left: 2.5rem; }
    .form-control.with-action { padding-right: 2.75rem; }

    .input-action {
      position: absolute;
      right: 10px;
      background: transparent;
      border: none;
      cursor: pointer;
      color: var(--text-muted);
      padding: 4px;
      border-radius: var(--radius-sm);
      display: flex; align-items: center;
      transition: color var(--transition-fast);
    }
    .input-action:hover { color: var(--text-primary); }
    .input-action .material-icons-round { font-size: 18px; }

    .alert-error {
      display: flex;
      align-items: center;
      gap: 8px;
      background: rgba(239,68,68,0.1);
      border: 1px solid rgba(239,68,68,0.3);
      border-radius: var(--radius-md);
      padding: 0.75rem 1rem;
      color: #f87171;
      font-size: 0.875rem;
      margin-bottom: var(--space-md);
    }

    .alert-error .material-icons-round { font-size: 18px; flex-shrink: 0; }

    .submit-btn {
      width: 100%;
      justify-content: center;
      padding: 0.8rem;
      font-size: 0.9375rem;
      margin-top: var(--space-sm);
    }

    .login-footer {
      text-align: center;
      font-size: 0.8rem;
      color: var(--text-muted);
      margin-top: 1.5rem;
    }
  `],
})
export class LoginComponent {
  loading = signal(false);
  showPass = signal(false);
  errorMsg = signal('');

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snack: MatSnackBar
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set('');

    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/app/citas']);
      },
      error: (err) => {
        this.loading.set(false);
        const msg =
          err?.error?.mensaje ||
          'Credenciales incorrectas. Verifica tu email y contraseña.';
        this.errorMsg.set(msg);
      },
    });
  }
}
