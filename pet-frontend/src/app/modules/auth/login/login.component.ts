import { Component, signal, OnInit, NgZone } from '@angular/core';
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
import { RolNombre } from '../../../core/models/models';
import { environment } from '../../../../environments/environment';

declare var google: any;

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
      <div class="login-logo">
        <span class="material-icons-round">pets</span>
      </div>

      <h2 class="login-title">Bienvenido de nuevo</h2>
      <p class="login-sub">
        {{ step() === 3 ? 'Comienza tu registro en VetCare' : 'Inicia sesión en tu cuenta' }}
      </p>

      <!-- PASO 2: Selección de Rol -->
      @if (step() === 2) {
        <div class="roles-container">
          <p class="text-center mb-4">Tienes múltiples roles asignados. Por favor selecciona cómo deseas ingresar:</p>
          <div class="role-options">
            @for (rol of availableRoles(); track rol) {
              <button class="btn btn-outline role-btn" (click)="selectRole(rol)">
                <span class="material-icons-round">
                  {{ getRoleIcon(rol) }}
                </span>
                {{ getRoleName(rol) }}
              </button>
            }
          </div>
        </div>
      }

      <!-- PASO 1 y 3: Formularios -->
      <div [style.display]="step() === 1 || step() === 3 ? 'block' : 'none'">
        
        <!-- STEP 1: Login Form -->
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="login-form" novalidate [style.display]="step() === 1 ? 'flex' : 'none'">
          <!-- Email -->
          <div class="form-group">
            <label class="form-label" for="email">Correo electrónico</label>
            <div class="input-wrapper">
              <span class="input-icon material-icons-round">mail_outline</span>
              <input id="email" type="email" formControlName="email" class="form-control with-icon" placeholder="veterinario@clinica.com" autocomplete="email"/>
            </div>
            @if (f['email'].touched && f['email'].errors?.['required']) { <span class="form-error">El email es obligatorio</span> }
            @if (f['email'].touched && f['email'].errors?.['email']) { <span class="form-error">Formato de email inválido</span> }
          </div>

          <!-- Password -->
          <div class="form-group">
            <label class="form-label" for="password">Contraseña</label>
            <div class="input-wrapper">
              <span class="input-icon material-icons-round">lock_outline</span>
              <input id="password" [type]="showPass() ? 'text' : 'password'" formControlName="password" class="form-control with-icon with-action" placeholder="••••••••" autocomplete="current-password"/>
              <button type="button" class="input-action" (click)="showPass.update(v => !v)" [attr.aria-label]="showPass() ? 'Ocultar contraseña' : 'Mostrar contraseña'">
                <span class="material-icons-round">{{ showPass() ? 'visibility_off' : 'visibility' }}</span>
              </button>
            </div>
            @if (f['password'].touched && f['password'].errors?.['required']) { <span class="form-error">La contraseña es obligatoria</span> }
          </div>

          @if (errorMsg()) {
            <div class="alert-error">
              <span class="material-icons-round">error_outline</span> {{ errorMsg() }}
            </div>
          }

          <button type="submit" class="btn btn-primary submit-btn" [disabled]="loading()" id="btn-login">
            @if (loading()) { <mat-spinner diameter="20" color="accent" /> Iniciando sesión... } @else { <span class="material-icons-round">login</span> Iniciar sesión }
          </button>
        </form>

        <!-- STEP 3: Solicitar Registro por Correo -->
        <div class="roles-container fade-in-up" [style.display]="step() === 3 ? 'block' : 'none'">
          <p class="text-center mb-4">Ingresa tu correo para enviarte un enlace de registro:</p>
          <div class="form-group">
            <div class="input-wrapper">
              <span class="input-icon material-icons-round">mail_outline</span>
              <input type="email" #regEmail class="form-control with-icon" placeholder="tu@correo.com">
            </div>
          </div>
          <button class="btn btn-primary submit-btn" (click)="solicitarRegistro(regEmail.value)" [disabled]="loading()">
            @if (loading()) { <mat-spinner diameter="20" color="accent" /> } @else { Enviar Enlace }
          </button>
          
          <button class="btn btn-outline submit-btn mt-2" (click)="setStep(1)">Volver al Login</button>
        </div>

        <!-- Google Button (Shared for Step 1 and 3) -->
        <div class="google-btn-container mt-4 mb-2">
          <div id="googleButton"></div>
        </div>

        <p class="login-footer mt-4 text-center" [hidden]="step() !== 1">
          ¿No tienes cuenta? <a href="javascript:void(0)" (click)="setStep(3)" class="text-primary font-medium hover:underline">Regístrate gratis</a>
        </p>
      </div>
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

    .role-options {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .role-btn {
      display: flex;
      align-items: center;
      justify-content: flex-start;
      gap: 1rem;
      padding: 1rem;
      font-size: 1rem;
      text-align: left;
    }
    
    .role-btn .material-icons-round {
      font-size: 24px;
      color: var(--color-primary-500);
    }
    .google-btn-container {
      display: flex;
      justify-content: center;
      width: 100%;
    }
  `],
})
export class LoginComponent implements OnInit {
  step = signal(1); // 1 = Login, 2 = Select Role, 3 = Register Email
  loading = signal(false);
  showPass = signal(false);
  errorMsg = signal('');
  availableRoles = signal<RolNombre[]>([]);

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snack: MatSnackBar,
    private ngZone: NgZone
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  setStep(newStep: number) {
    this.step.set(newStep);
    if (newStep === 1 || newStep === 3) {
      setTimeout(() => this.renderGoogleButton(), 100);
    }
  }

  ngOnInit() {
    this.renderGoogleButton();
  }

  renderGoogleButton(retries = 0) {
    if (typeof google !== 'undefined' && google.accounts) {
      google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: (response: any) => this.ngZone.run(() => this.handleGoogleCredential(response)),
        auto_select: false,
        cancel_on_tap_outside: true
      });
      
      let rendered = false;
      const btn1 = document.getElementById('googleButton');
      if (btn1) {
        google.accounts.id.renderButton(btn1, { theme: 'outline', size: 'large', width: '100%' });
        rendered = true;
      }
      const btn2 = document.getElementById('googleButtonRegister');
      if (btn2) {
        google.accounts.id.renderButton(btn2, { theme: 'outline', size: 'large', width: '100%' });
        rendered = true;
      }

      if (!rendered && retries < 10) {
        setTimeout(() => this.renderGoogleButton(retries + 1), 150);
      }
    } else {
      if (retries < 20) {
        setTimeout(() => this.renderGoogleButton(retries + 1), 500);
      }
    }
  }

  handleGoogleCredential(response: any) {
    this.loading.set(true);
    this.authService.loginConGoogle(response.credential).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.requireRegistration) {
          // Navegar a /completar-registro con los datos
          this.router.navigate(['/completar-registro'], {
            state: { data: res }
          });
        } else {
          // Login exitoso
          const roles = res.roles as RolNombre[];
          if (roles.length > 1) {
            this.availableRoles.set(roles);
            this.step.set(2);
          } else {
            this.navigateToDashboard(roles[0]);
          }
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open('Error al iniciar sesión con Google', 'Cerrar', { duration: 3000 });
      }
    });
  }

  solicitarRegistro(email: string) {
    if (!email) return;
    this.loading.set(true);
    this.authService.solicitarRegistroCorreo(email).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.setStep(1);
        this.snack.open(res.mensaje || 'Enlace enviado a tu correo', 'Cerrar', { duration: 5000 });
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err.error?.mensaje || 'Error al solicitar registro', 'Cerrar', { duration: 3000 });
      }
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
      next: (res) => {
        this.loading.set(false);
        const roles = res.roles as RolNombre[];
        if (roles.length > 1) {
          this.availableRoles.set(roles);
          this.step.set(2);
        } else {
          this.navigateToDashboard(roles[0]);
        }
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

  selectRole(rol: RolNombre): void {
    this.authService.setActiveRole(rol);
    this.navigateToDashboard(rol);
  }

  private navigateToDashboard(rol?: RolNombre): void {
    if (rol === 'ROLE_CLIENTE') {
      // If we had a specific client portal, we would route there. 
      // For now, everyone goes to app
      this.router.navigate(['/app']);
    } else {
      this.router.navigate(['/app/citas']);
    }
  }

  getRoleName(rol: string): string {
    const names: Record<string, string> = {
      'ROLE_ADMIN': 'Administrador',
      'ROLE_CLIENTE': 'Cliente',
      'ROLE_VETERINARIO': 'Veterinario',
      'ROLE_RECEPCIONISTA': 'Recepcionista'
    };
    return names[rol] || rol.replace('ROLE_', '');
  }

  getRoleIcon(rol: string): string {
    const icons: Record<string, string> = {
      'ROLE_ADMIN': 'admin_panel_settings',
      'ROLE_CLIENTE': 'person',
      'ROLE_VETERINARIO': 'medical_services',
      'ROLE_RECEPCIONISTA': 'support_agent'
    };
    return icons[rol] || 'badge';
  }
}
