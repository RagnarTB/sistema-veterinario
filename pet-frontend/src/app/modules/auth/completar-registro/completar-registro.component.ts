import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';
import { ExternoService, ReniecResponse } from '../../../core/services/externo.service';

@Component({
  selector: 'app-completar-registro',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatSnackBarModule, MatProgressSpinnerModule],
  template: `
    <div class="login-container fade-in-up" style="max-width: 500px;">
      <div class="login-logo">
        <span class="material-icons-round">how_to_reg</span>
      </div>

      <h2 class="login-title">Completar Registro</h2>
      <p class="login-sub">Casi listo, completa tus datos para terminar.</p>

      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="login-form">
        <!-- DNI y Buscador -->
        <div class="form-group mb-4">
          <label class="form-label" for="dni">DNI</label>
          <div class="flex gap-2">
            <div class="input-wrapper flex-1">
              <span class="input-icon material-icons-round">badge</span>
              <input id="dni" type="text" formControlName="dni" class="form-control with-icon" placeholder="12345678" maxlength="8">
            </div>
            <button type="button" class="btn btn-outline" (click)="buscarDni()" [disabled]="form.get('dni')?.invalid || loadingReniec()">
              @if (loadingReniec()) {
                <mat-spinner diameter="20" color="accent" />
              } @else {
                <span class="material-icons-round">search</span> Buscar
              }
            </button>
          </div>
          @if (f['dni'].touched && f['dni'].invalid) { <span class="form-error">DNI inválido</span> }
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="form-group mb-4">
            <label class="form-label" for="nombre">Nombres</label>
            <input id="nombre" type="text" formControlName="nombre" class="form-control" placeholder="Ej. Juan">
          </div>
          <div class="form-group mb-4">
            <label class="form-label" for="apellido">Apellidos</label>
            <input id="apellido" type="text" formControlName="apellido" class="form-control" placeholder="Ej. Pérez">
          </div>
        </div>

        <div class="form-group mb-4">
          <label class="form-label" for="email">Correo electrónico</label>
          <div class="input-wrapper">
            <span class="input-icon material-icons-round">mail</span>
            <input id="email" type="email" formControlName="email" class="form-control with-icon" readonly>
          </div>
        </div>

        <div class="form-group mb-4">
          <label class="form-label" for="telefono">Teléfono</label>
          <div class="input-wrapper">
            <span class="input-icon material-icons-round">phone</span>
            <input id="telefono" type="text" formControlName="telefono" class="form-control with-icon" placeholder="987654321" maxlength="9">
          </div>
          @if (f['telefono'].touched && f['telefono'].invalid) { <span class="form-error">Teléfono requerido</span> }
        </div>

        @if (!isGoogleFlow()) {
          <div class="form-group mb-4">
            <label class="form-label" for="password">Contraseña</label>
            <div class="input-wrapper">
              <span class="input-icon material-icons-round">lock</span>
              <input id="password" type="password" formControlName="password" class="form-control with-icon" placeholder="••••••••">
            </div>
            @if (f['password'].touched && f['password'].invalid) { <span class="form-error">Mínimo 8 caracteres</span> }
          </div>
        }

        <button type="submit" class="btn btn-primary submit-btn w-full mt-4" [disabled]="loading() || form.invalid">
          @if (loading()) { <mat-spinner diameter="20" color="accent" /> } @else { Finalizar Registro }
        </button>
      </form>
    </div>
  `,
  styles: [`
    .login-container {
      width: 100%;
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-xl);
      padding: 2.5rem 2rem;
      box-shadow: var(--shadow-lg), 0 0 40px rgba(0,189,189,0.06);
      margin: 0 auto;
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
    .login-title { font-size: 1.5rem; font-weight: 700; text-align: center; margin-bottom: 0.25rem; }
    .login-sub { font-size: 0.875rem; color: var(--text-muted); text-align: center; margin-bottom: 2rem; }
    .form-group { display: flex; flex-direction: column; }
    .input-wrapper { position: relative; display: flex; align-items: center; }
    .input-icon { position: absolute; left: 12px; font-size: 18px; color: var(--text-muted); pointer-events: none; }
    .form-control.with-icon { padding-left: 2.5rem; }
  `]
})
export class CompletarRegistroComponent implements OnInit {
  form: FormGroup;
  loading = signal(false);
  loadingReniec = signal(false);
  isGoogleFlow = signal(false);
  token: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private externoService: ExternoService,
    private router: Router,
    private route: ActivatedRoute,
    private snack: MatSnackBar
  ) {
    this.form = this.fb.group({
      dni: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(8)]],
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', Validators.required],
      password: ['']
    });
  }

  ngOnInit() {
    // Verificar de dónde venimos
    const state = history.state.data;
    if (state && state.googleToken) {
      this.isGoogleFlow.set(true);
      this.token = state.googleToken;
      this.form.patchValue({
        email: state.email,
        nombre: state.nombre || '',
        apellido: state.apellido || ''
      });
      // La contraseña no es obligatoria para Google
      this.form.get('password')?.clearValidators();
      this.form.get('password')?.updateValueAndValidity();
    } else {
      // Venimos de enlace de correo
      this.route.queryParams.subscribe(params => {
        if (params['token']) {
          this.token = params['token'];
          if (params['email']) {
            this.form.patchValue({ email: params['email'] });
          }
          // Para correo la password es requerida
          this.form.get('password')?.setValidators([Validators.required, Validators.minLength(8)]);
          this.form.get('password')?.updateValueAndValidity();
        } else {
          this.snack.open('Token inválido o no encontrado', 'Cerrar');
          this.router.navigate(['/login']);
        }
      });
    }
  }

  get f() { return this.form.controls; }

  buscarDni() {
    const dni = this.form.get('dni')?.value;
    if (!dni || dni.length !== 8) return;

    this.loadingReniec.set(true);
    this.externoService.consultarDni(dni).subscribe({
      next: (res: ReniecResponse) => {
        this.loadingReniec.set(false);
        this.form.patchValue({
          nombre: res.first_name,
          apellido: res.first_last_name + ' ' + res.second_last_name
        });
      },
      error: () => {
        this.loadingReniec.set(false);
        this.snack.open('No se encontró el DNI. Ingrese los nombres manualmente si puede o intente de nuevo.', 'Cerrar', { duration: 4000 });
        // Permitir editar manualmente en caso de error
        this.form.get('nombre')?.enable();
        this.form.get('apellido')?.enable();
      }
    });
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const dto = {
      ...this.form.getRawValue(),
      token: this.token
    };

    this.authService.completarRegistro(dto).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.snack.open('Registro exitoso. ¡Bienvenido!', 'Cerrar', { duration: 3000 });
        this.router.navigate(['/app/pacientes']);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err.error?.mensaje || 'Error al completar el registro', 'Cerrar', { duration: 4000 });
      }
    });
  }
}
