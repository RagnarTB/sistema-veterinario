import { Component, Inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CustomValidators } from '../../shared/utils/custom-validators';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';

import { EmpleadoService } from '../../core/services/empleado.service';
import { SedeService } from '../../core/services/sede.service';
import { RolService } from '../../core/services/rol.service';
import { ExternoService } from '../../core/services/externo.service';
import { ColegiaturaService } from '../../core/services/colegiatura.service';
import { HorariosVeterinarioComponent } from './horarios-veterinario.component';
import { DiasBloqueadosComponent } from './dias-bloqueados.component';
import { HorarioService, HorarioVeterinarioResponse } from '../../core/services/horario.service';
import { DiaBloqueadoService } from '../../core/services/dia-bloqueado.service';
import { EmpleadoResponse, SedeResponse, RolResponse, ColegiaturaValidacion, DiaBloqueadoResponse } from '../../core/models/models';

export interface EmpleadoDialogData {
  empleado?: EmpleadoResponse;
}

@Component({
  selector: 'app-empleado-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatTabsModule,
    HorariosVeterinarioComponent,
    DiasBloqueadosComponent
  ],
  template: `
    @if (paso() === 'formulario') {
      <h2 mat-dialog-title class="dialog-title">
        <span class="material-icons-round text-primary">
          {{ isEdit() ? 'edit' : 'badge' }}
        </span>
        {{ isEdit() ? 'Editar Empleado' : 'Nuevo Empleado' }}
      </h2>

      <mat-dialog-content class="dialog-content-scroll">
        <mat-tab-group *ngIf="isEdit() && esVeterinarioGuardado(); else normalForm">
          <mat-tab label="Datos Personales">
            <ng-container *ngTemplateOutlet="formTemplate"></ng-container>
          </mat-tab>
          <mat-tab label="Horarios">
            <app-horarios-veterinario 
              [veterinarioId]="data.empleado?.id || 0" 
              [sedes]="sedesAsignadas()">
            </app-horarios-veterinario>
          </mat-tab>
          <mat-tab label="Días Bloqueados">
            <app-dias-bloqueados 
              [veterinarioId]="data.empleado?.id || 0">
            </app-dias-bloqueados>
          </mat-tab>
        </mat-tab-group>

        <ng-template #normalForm>
          <ng-container *ngTemplateOutlet="formTemplate"></ng-container>
        </ng-template>

        <ng-template #formTemplate>
        @if (dniEsCliente()) {
          <div class="ascenso-banner">
            <mat-icon class="banner-icon">info</mat-icon>
            <div class="banner-text">
              <strong>Este DNI ya pertenece a un cliente registrado.</strong>
              <p>Los datos personales se reutilizarán. Solo complete los datos laborales y asigne roles.</p>
            </div>
          </div>
        }

        <form [formGroup]="form" class="form-grid py-2">

          <div class="dni-search-container col-span-2">
            <mat-form-field appearance="outline" class="dni-field">
              <mat-label>DNI</mat-label>
              <mat-icon matPrefix>badge</mat-icon>
              <input matInput formControlName="dni" [readonly]="isEdit()" placeholder="Ej. 12345678" maxlength="8" (input)="form.get('dni')?.markAsTouched()" required (keydown)="soloNumeros($event)"/>
              <mat-error *ngIf="form.get('dni')?.hasError('required')">El DNI es obligatorio</mat-error>
              <mat-error *ngIf="form.get('dni')?.hasError('pattern')">Debe tener 8 dígitos</mat-error>
            </mat-form-field>

            <button mat-flat-button color="primary" type="button" class="btn-search-dni"
                    (click)="buscarDni()"
                    [disabled]="form.get('dni')?.invalid || form.get('dni')?.value?.length !== 8 || buscandoDni || isEdit()">
              <mat-icon *ngIf="!buscandoDni">search</mat-icon>
              <mat-spinner diameter="20" *ngIf="buscandoDni" style="margin-right:8px"></mat-spinner>
              {{ buscandoDni ? 'Buscando...' : 'Buscar Reniec' }}
            </button>
          </div>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Nombre</mat-label>
            <mat-icon matPrefix>person</mat-icon>
            <input matInput formControlName="nombre" [readonly]="dniEsCliente() || isEdit()" (keydown)="soloLetras($event)" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Apellido</mat-label>
            <mat-icon matPrefix>person_outline</mat-icon>
            <input matInput formControlName="apellido" [readonly]="dniEsCliente() || isEdit()" (keydown)="soloLetras($event)" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full col-span-2">
            <mat-label>Correo Electrónico</mat-label>
            <mat-icon matPrefix>mail_outline</mat-icon>
            <input matInput type="email" formControlName="email" [readonly]="dniEsCliente() || isEdit()" />
            @if (!isEdit() && !dniEsCliente()) {
              <mat-hint>Se enviará un correo para establecer su contraseña.</mat-hint>
            }
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Teléfono</mat-label>
            <mat-icon matPrefix>phone</mat-icon>
            <input matInput formControlName="telefono" maxlength="9" (input)="form.get('telefono')?.markAsTouched()" required (keydown)="soloNumeros($event)"/>
            <mat-error *ngIf="form.get('telefono')?.hasError('pattern')">Debe tener exactamente 9 dígitos numéricos</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Sueldo Base (Opcional)</mat-label>
            <mat-icon matPrefix>payments</mat-icon>
            <input matInput type="number" formControlName="sueldoBase" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Especialidad (Opcional)</mat-label>
            <mat-icon matPrefix>work_outline</mat-icon>
            <input matInput formControlName="especialidad" style="text-transform: uppercase;" (keydown)="soloLetras($event)"/>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Sedes Asignadas</mat-label>
            <mat-icon matPrefix>storefront</mat-icon>
            <mat-select formControlName="sedeIds" multiple>
              @for (sede of sedes(); track sede.id) {
                <mat-option [value]="sede.id">{{ sede.nombre }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full col-span-2">
            <mat-label>Roles Asignados</mat-label>
            <mat-icon matPrefix>shield</mat-icon>
            <mat-select formControlName="roles" multiple (selectionChange)="onRolesChange()">
              @for (rol of roles(); track rol.id) {
                <mat-option [value]="rol.nombre">{{ rol.nombre.replace('ROLE_', '') }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          @if (esVeterinario()) {
            <div class="col-span-2 vet-section">
              <div class="vet-section-title">
                <mat-icon>medical_services</mat-icon>
                <span>Datos de Veterinario</span>
              </div>

              <div class="vet-fields">
                <mat-form-field appearance="outline" class="colegiatura-field">
                  <mat-label>Número de Colegiatura</mat-label>
                  <mat-icon matPrefix>verified</mat-icon>
                  <input matInput formControlName="numeroColegiatura" placeholder="Ej. 12345" />
                </mat-form-field>

                <button mat-flat-button type="button" class="btn-validar-colegiatura"
                        [disabled]="!form.get('numeroColegiatura')?.value || validandoColegiatura()"
                        (click)="validarColegiatura()">
                  <mat-spinner diameter="18" *ngIf="validandoColegiatura()" style="margin-right:8px"></mat-spinner>
                  <mat-icon *ngIf="!validandoColegiatura()">verified_user</mat-icon>
                  {{ validandoColegiatura() ? 'Validando...' : 'Validar Colegiatura' }}
                </button>
              </div>

              @if (colegiaturaResultado()) {
                <div class="colegiatura-resultado" [class.valido]="colegiaturaResultado()?.habilitado" [class.invalido]="!colegiaturaResultado()?.habilitado">
                  <mat-icon>{{ colegiaturaResultado()?.habilitado ? 'check_circle' : 'cancel' }}</mat-icon>
                  <div>
                    @if (colegiaturaResultado()?.habilitado) {
                      <strong>HABILITADO</strong>
                      <p>{{ colegiaturaResultado()?.nombreCompleto }}</p>
                    } @else {
                      <strong>{{ colegiaturaResultado()?.error || 'NO HABILITADO' }}</strong>
                    }
                  </div>
                </div>
              }
            </div>
          }

        </form>
        </ng-template>
      </mat-dialog-content>

      <mat-dialog-actions align="end" class="p-4">
        <button mat-stroked-button mat-dialog-close>Cancelar</button>
        <button mat-flat-button color="primary" [disabled]="form.invalid || loading()" (click)="mostrarConfirmacion()">
          <mat-icon>preview</mat-icon> Revisar y Confirmar
        </button>
      </mat-dialog-actions>
    }

    @if (paso() === 'confirmacion') {
      <h2 mat-dialog-title class="dialog-title">
        <span class="material-icons-round text-primary">fact_check</span>
        Confirmar Registro de Empleado
      </h2>

      <mat-dialog-content class="dialog-content-scroll">
        <div class="resumen-container">
          <div class="resumen-seccion">
            <h3><mat-icon>person</mat-icon> Datos Personales</h3>
            <div class="resumen-grid">
              <div class="resumen-item"><span class="label">DNI:</span><span class="value">{{ form.getRawValue().dni }}</span></div>
              <div class="resumen-item"><span class="label">Nombre:</span><span class="value">{{ form.getRawValue().nombre }} {{ form.getRawValue().apellido }}</span></div>
              <div class="resumen-item"><span class="label">Email:</span><span class="value">{{ form.getRawValue().email }}</span></div>
              <div class="resumen-item"><span class="label">Teléfono:</span><span class="value">{{ form.getRawValue().telefono }}</span></div>
            </div>
          </div>

          <div class="resumen-seccion">
            <h3><mat-icon>work</mat-icon> Información Laboral</h3>
            <div class="resumen-grid">
              <div class="resumen-item"><span class="label">Especialidad:</span><span class="value">{{ form.getRawValue().especialidad || 'N/A' }}</span></div>
              <div class="resumen-item"><span class="label">Sueldo Base:</span><span class="value">{{ form.getRawValue().sueldoBase ? 'S/ ' + form.getRawValue().sueldoBase : 'N/A' }}</span></div>
              <div class="resumen-item"><span class="label">Sedes:</span><span class="value">{{ getSedesNombres() }}</span></div>
            </div>
          </div>

          <div class="resumen-seccion">
            <h3><mat-icon>shield</mat-icon> Roles</h3>
            <div class="roles-chips">
              @for (rol of form.getRawValue().roles; track rol) {
                <span class="rol-chip">{{ rol.replace('ROLE_', '') }}</span>
              }
            </div>
          </div>

          @if (esVeterinario() && form.getRawValue().numeroColegiatura) {
            <div class="resumen-seccion">
              <h3><mat-icon>verified</mat-icon> Colegiatura</h3>
              <div class="resumen-grid">
                <div class="resumen-item"><span class="label">Número:</span><span class="value">{{ form.getRawValue().numeroColegiatura }}</span></div>
                <div class="resumen-item">
                  <span class="label">Estado:</span>
                  <span class="value" [class.text-green]="colegiaturaResultado()?.habilitado" [class.text-red]="colegiaturaResultado() && !colegiaturaResultado()?.habilitado">
                    {{ colegiaturaResultado()?.habilitado ? 'HABILITADO' : (colegiaturaResultado()?.error || 'Sin validar') }}
                  </span>
                </div>
              </div>
            </div>
          }

          @if (dniEsCliente()) {
            <div class="ascenso-banner confirmacion-banner">
              <mat-icon class="banner-icon">upgrade</mat-icon>
              <div class="banner-text">
                <strong>Este registro ascenderá un cliente existente a empleado.</strong>
                <p>Se reutilizarán los datos personales del cliente y se asignarán los nuevos roles laborales.</p>
              </div>
            </div>
          }
        </div>
      </mat-dialog-content>

      <mat-dialog-actions align="end" class="p-4">
        <button mat-stroked-button (click)="paso.set('formulario')">
          <mat-icon>arrow_back</mat-icon> Volver a Editar
        </button>
        <button mat-flat-button color="primary" [disabled]="loading()" (click)="guardar()">
          <mat-spinner diameter="18" *ngIf="loading()" style="margin-right:8px; display:inline-block"></mat-spinner>
          <mat-icon *ngIf="!loading()">save</mat-icon>
          {{ isEdit() ? 'Guardar Cambios' : 'Registrar Empleado' }}
        </button>
      </mat-dialog-actions>
    }
  `,
  styles: [`
    :host { display: block; min-width: 600px; }
    .dialog-title { display: flex; align-items: center; gap: 8px; font-weight: bold; margin-bottom: 16px; }
    .dialog-content-scroll { max-height: 70vh; overflow-y: auto; padding-top: 8px; }

    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
    .col-span-2 { grid-column: span 2; }
    .w-full { width: 100%; }

    .dni-search-container { display: flex; gap: 16px; align-items: flex-start; }
    .dni-field { flex: 1; }
    .btn-search-dni { height: 52px; font-weight: 600; font-size: 1rem; border-radius: 8px; padding: 0 20px; display:flex; align-items:center; gap:8px;}

    .ascenso-banner {
      display: flex; gap: 12px; align-items: flex-start;
      background: linear-gradient(135deg, rgba(33,150,243,0.1), rgba(33,150,243,0.05));
      border: 1px solid rgba(33,150,243,0.3);
      border-radius: 12px; padding: 16px; margin-bottom: 16px;
    }
    .banner-icon { color: #2196F3; font-size: 28px; width: 28px; height: 28px; }
    .banner-text p { margin: 4px 0 0; opacity: 0.8; font-size: 0.85rem; }
    .confirmacion-banner { background: linear-gradient(135deg, rgba(76,175,80,0.1), rgba(76,175,80,0.05)); border-color: rgba(76,175,80,0.3); }
    .confirmacion-banner .banner-icon { color: #4CAF50; }

    .vet-section {
      background: rgba(156,39,176,0.05); border: 1px solid rgba(156,39,176,0.15);
      border-radius: 12px; padding: 16px; margin-top: 8px;
    }
    .vet-section-title { display: flex; align-items: center; gap: 8px; font-weight: 600; color: #9C27B0; margin-bottom: 12px; }
    .vet-fields { display: flex; gap: 12px; align-items: flex-start; }
    .colegiatura-field { flex: 1; }
    .btn-validar-colegiatura { height: 52px; border-radius: 8px; background: #9C27B0 !important; color: white !important; display: flex; align-items: center; gap: 6px; padding: 0 16px; }

    .colegiatura-resultado {
      display: flex; gap: 8px; align-items: center; padding: 10px 14px; border-radius: 8px; margin-top: 8px;
    }
    .colegiatura-resultado.valido { background: rgba(76,175,80,0.1); color: #2E7D32; }
    .colegiatura-resultado.invalido { background: rgba(244,67,54,0.1); color: #C62828; }
    .colegiatura-resultado p { margin: 2px 0 0; font-size: 0.85rem; }

    .resumen-container { display: flex; flex-direction: column; gap: 20px; }
    .resumen-seccion h3 { display: flex; align-items: center; gap: 8px; font-size: 1rem; font-weight: 600; margin: 0 0 10px; color: var(--text-primary); }
    .resumen-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
    .resumen-item { display: flex; flex-direction: column; }
    .resumen-item .label { font-size: 0.75rem; opacity: 0.6; text-transform: uppercase; letter-spacing: 0.5px; }
    .resumen-item .value { font-weight: 500; font-size: 0.95rem; }
    .roles-chips { display: flex; gap: 8px; flex-wrap: wrap; }
    .rol-chip { background: rgba(33,150,243,0.15); color: #1565C0; padding: 4px 12px; border-radius: 16px; font-size: 0.85rem; font-weight: 500; }
    .text-green { color: #2E7D32 !important; font-weight: 600; }
    .text-red { color: #C62828 !important; font-weight: 600; }

    ::ng-deep .mat-mdc-text-field-wrapper { background-color: var(--bg-card) !important; }

    @media (max-width: 600px) {
      .form-grid { grid-template-columns: 1fr; }
      .col-span-2 { grid-column: span 1; }
      .resumen-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class EmpleadoDialogComponent implements OnInit {
  isEdit = signal(false);
  loading = signal(false);
  buscandoDni = false;
  form: FormGroup;
  paso = signal<'formulario' | 'confirmacion'>('formulario');
  dniEsCliente = signal(false);
  esVeterinario = signal(false);
  esVeterinarioGuardado = signal(false);
  validandoColegiatura = signal(false);
  colegiaturaResultado = signal<ColegiaturaValidacion | null>(null);

  sedes = signal<SedeResponse[]>([]);
  sedesAsignadas = signal<SedeResponse[]>([]);
  roles = signal<RolResponse[]>([]);

  constructor(
    private dialogRef: MatDialogRef<EmpleadoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EmpleadoDialogData,
    private fb: FormBuilder,
    private empleadoService: EmpleadoService,
    private sedeService: SedeService,
    private rolService: RolService,
    private externoService: ExternoService,
    private colegiaturaService: ColegiaturaService,
    private snack: MatSnackBar
  ) {
    this.isEdit.set(!!data.empleado);

    this.form = this.fb.group({
      dni: [{value: data.empleado?.dni || '', disabled: this.isEdit()}, [Validators.required, CustomValidators.dni]],
      nombre: [{value: data.empleado?.nombre || '', disabled: this.isEdit()}, [Validators.required, CustomValidators.noWhitespace]],
      apellido: [{value: data.empleado?.apellido || '', disabled: this.isEdit()}, [Validators.required, CustomValidators.noWhitespace]],
      telefono: [data.empleado?.telefono || '', [Validators.required, CustomValidators.telefono]],
      email: [{value: data.empleado?.email || '', disabled: this.isEdit()}, [Validators.required, Validators.email]],
      especialidad: [data.empleado?.especialidad || ''],
      numeroColegiatura: [data.empleado?.numeroColegiatura || ''],
      sueldoBase: [data.empleado?.sueldoBase || null],
      sedeIds: [data.empleado?.sedeIds || [], Validators.required],
      roles: [data.empleado?.nombresRoles || [], Validators.required]
    });

    // Verificar si ya es veterinario al editar
    if (this.isEdit() && data.empleado?.nombresRoles?.includes('ROLE_VETERINARIO')) {
      this.esVeterinario.set(true);
      this.esVeterinarioGuardado.set(true);
    }
  }

  ngOnInit() {
    this.cargarDatosAdicionales();
    
    // Actualizar sedes asignadas dinámicamente
    this.form.get('sedeIds')?.valueChanges.subscribe(ids => {
      this.actualizarSedesAsignadas(ids);
    });
  }

  actualizarSedesAsignadas(ids: number[]) {
    if (!ids) ids = [];
    this.sedesAsignadas.set(this.sedes().filter(s => ids.includes(s.id)));
  }

  cargarDatosAdicionales() {
    this.sedeService.listar(0, 1000).subscribe((res: any) => {
      let s = res.content || [];

      if (this.isEdit()) {
        const sedesActuales = this.data.empleado?.sedeIds || [];
        s = s.filter((sede: SedeResponse) => sede.activo || sedesActuales.includes(sede.id));
      } else {
        s = s.filter((sede: SedeResponse) => sede.activo);
      }

      this.sedes.set(s);

      if (!this.isEdit()) {
        const chiclayo = s.find((sede: SedeResponse) => sede.nombre.toLowerCase().includes('chiclayo'));
        if (chiclayo) {
          this.form.get('sedeIds')?.setValue([chiclayo.id]);
        }
      } else {
        // Inicializar las sedes asignadas en edición
        this.actualizarSedesAsignadas(this.form.get('sedeIds')?.value);
      }
    });

    this.rolService.listarTodos().subscribe((r: RolResponse[]) => {
      // Filtrar siempre ROLE_CLIENTE para que no se asigne en Empleados
      const rolesValidos = r.filter(rol => rol.nombre !== 'ROLE_CLIENTE');
      
      if (this.isEdit()) {
        const rolesActuales = this.data.empleado?.nombresRoles || [];
        this.roles.set(rolesValidos.filter(rol => rol.activo || rolesActuales.includes(rol.nombre)));
      } else {
        this.roles.set(rolesValidos.filter(rol => rol.activo));
      }
    });
  }

  buscarDni() {
    const dni = this.form.get('dni')?.value;
    if (!dni || dni.length !== 8) return;

    this.buscandoDni = true;
    this.dniEsCliente.set(false);

    this.externoService.consultarDni(dni).subscribe({
      next: (res: any) => {
        if (res && res.first_name) {
          this.form.patchValue({
            nombre: res.first_name,
            apellido: res.first_last_name + (res.second_last_name ? ' ' + res.second_last_name : '')
          });
          this.form.get('nombre')?.disable();
          this.form.get('apellido')?.disable();

          if (res.existe_en_bd && res.email) {
            const patchData: any = { email: res.email };
            if (res.telefono) patchData.telefono = res.telefono;
            
            this.form.patchValue(patchData);
            this.form.get('email')?.disable();
            this.dniEsCliente.set(true);
            this.snack.open('DNI registrado en el sistema. Se reutilizarán sus datos.', 'Entendido', { duration: 4000 });
          } else {
            this.snack.open('DNI encontrado exitosamente', 'Cerrar', { duration: 3000 });
          }
        }
        this.buscandoDni = false;
      },
      error: () => {
        this.buscandoDni = false;
        this.snack.open('No se pudo encontrar información para este documento', 'Cerrar', { duration: 4000 });
      }
    });
  }

  onRolesChange() {
    const rolesSeleccionados: string[] = this.form.get('roles')?.value || [];
    this.esVeterinario.set(rolesSeleccionados.includes('ROLE_VETERINARIO'));

    if (!this.esVeterinario()) {
      this.form.get('numeroColegiatura')?.setValue('');
      this.colegiaturaResultado.set(null);
    }
  }

  validarColegiatura() {
    const numero = this.form.get('numeroColegiatura')?.value;
    if (!numero) return;

    this.validandoColegiatura.set(true);
    this.colegiaturaResultado.set(null);

    this.colegiaturaService.validar(numero).subscribe({
      next: (res: ColegiaturaValidacion) => {
        this.colegiaturaResultado.set(res);
        this.validandoColegiatura.set(false);
        if (res.habilitado) {
          this.snack.open('Colegiatura verificada: HABILITADO', 'Cerrar', { duration: 3000 });
        } else {
          this.snack.open(res.error || 'Colegiatura no válida', 'Cerrar', { duration: 4000 });
        }
      },
      error: (err: any) => {
        this.colegiaturaResultado.set({
          numeroColegiatura: numero,
          habilitado: false,
          error: err.error?.error || 'No se pudo validar la colegiatura'
        });
        this.validandoColegiatura.set(false);
      }
    });
  }

  getSedesNombres(): string {
    const ids: number[] = this.form.getRawValue().sedeIds || [];
    return this.sedes().filter(s => ids.includes(s.id)).map(s => s.nombre).join(', ') || 'N/A';
  }

  mostrarConfirmacion() {
    if (this.form.invalid) return;
    this.paso.set('confirmacion');
  }

  guardar() {
    if (this.form.invalid) return;

    this.loading.set(true);
    const dto = this.form.getRawValue();

    if (dto.especialidad) {
      dto.especialidad = dto.especialidad.toUpperCase().trim();
    }

    const obs = this.isEdit()
      ? this.empleadoService.actualizar(this.data.empleado!.id, dto)
      : this.empleadoService.crear(dto);

    obs.subscribe({
      next: (res: any) => {
        this.snack.open(this.isEdit() ? 'Empleado actualizado' : 'Empleado registrado y correo enviado', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(res);
      },
      error: (err: any) => {
        let msg = err.error?.mensaje || err.error?.message || 'Error al guardar';
        if (err.error?.detalles && Array.isArray(err.error.detalles)) {
          msg += ': ' + err.error.detalles.join(', ');
        }

        // Detectar si el backend dice que el DNI pertenece a un cliente
        if (msg.toLowerCase().includes('cliente') || msg.toLowerCase().includes('dni')) {
          this.dniEsCliente.set(true);
          this.paso.set('formulario');
          this.snack.open('Este DNI pertenece a un cliente existente. Se reutilizarán sus datos.', 'Entendido', { duration: 5000 });
        } else {
          this.snack.open(msg, 'Cerrar', { duration: 6000 });
        }
        this.loading.set(false);
      }
    });
  }

  soloNumeros(event: KeyboardEvent): void {
    const teclas_permitidas = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab', 'Home', 'End'];
    const patron = /^[0-9]$/;
    if (!teclas_permitidas.includes(event.key) && !patron.test(event.key)) {
      event.preventDefault();
    }
  }

  soloLetras(event: KeyboardEvent): void {
    const teclas_permitidas = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab', 'Home', 'End'];
    const patron = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]$/;
    if (!teclas_permitidas.includes(event.key) && !patron.test(event.key)) {
      event.preventDefault();
    }
  }
}