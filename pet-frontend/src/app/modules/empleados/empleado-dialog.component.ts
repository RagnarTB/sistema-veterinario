import { Component, Inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { EmpleadoService } from '../../core/services/empleado.service';
import { SedeService } from '../../core/services/sede.service';
import { RolService } from '../../core/services/rol.service';
import { ExternoService } from '../../core/services/externo.service';
import { EmpleadoResponse, SedeResponse, RolResponse } from '../../core/models/models';

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
    MatInputModule
  ],
  template: `
    <h2 mat-dialog-title class="dialog-title">
      <span class="material-icons-round text-primary">
        {{ isEdit() ? 'edit' : 'badge' }}
      </span>
      {{ isEdit() ? 'Editar Empleado' : 'Nuevo Empleado' }}
    </h2>
    
    <mat-dialog-content class="dialog-content-scroll">
      <form [formGroup]="form" class="form-grid py-2">
        
        <!-- DNI y Buscador RENIEC -->
        <div class="dni-search-container col-span-2">
          <mat-form-field appearance="outline" class="dni-field">
            <mat-label>DNI</mat-label>
            <mat-icon matPrefix>badge</mat-icon>
            <input matInput formControlName="dni" [readonly]="isEdit()" placeholder="Ej. 12345678" />
            <mat-error *ngIf="form.get('dni')?.hasError('required')">El DNI es obligatorio</mat-error>
            <mat-error *ngIf="form.get('dni')?.hasError('pattern')">Debe tener 8 dígitos</mat-error>
          </mat-form-field>
          
          <button mat-flat-button color="primary" type="button" class="btn-search-dni"
                  (click)="buscarDni()" 
                  [disabled]="form.get('dni')?.invalid || form.get('dni')?.value.length !== 8 || buscandoDni || isEdit()">
            <mat-icon *ngIf="!buscandoDni">search</mat-icon>
            <mat-spinner diameter="20" *ngIf="buscandoDni" style="margin-right:8px"></mat-spinner>
            {{ buscandoDni ? 'Buscando...' : 'Buscar Reniec' }}
          </button>
        </div>

        <!-- Nombre -->
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Nombre</mat-label>
          <mat-icon matPrefix>person</mat-icon>
          <input matInput formControlName="nombre" [readonly]="isEdit()" />
        </mat-form-field>
        
        <!-- Apellido -->
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Apellido</mat-label>
          <mat-icon matPrefix>person_outline</mat-icon>
          <input matInput formControlName="apellido" [readonly]="isEdit()" />
        </mat-form-field>

        <!-- Correo Electrónico -->
        <mat-form-field appearance="outline" class="w-full col-span-2">
          <mat-label>Correo Electrónico</mat-label>
          <mat-icon matPrefix>mail_outline</mat-icon>
          <input matInput type="email" formControlName="email" [readonly]="isEdit()" />
          @if (!isEdit()) {
            <mat-hint>Se enviará un correo para establecer su contraseña.</mat-hint>
          }
        </mat-form-field>

        <!-- Teléfono -->
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Teléfono</mat-label>
          <mat-icon matPrefix>phone</mat-icon>
          <input matInput formControlName="telefono" />
        </mat-form-field>

        <!-- Sueldo Base -->
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Sueldo Base (Opcional)</mat-label>
          <mat-icon matPrefix>payments</mat-icon>
          <input matInput type="number" formControlName="sueldoBase" />
        </mat-form-field>

        <!-- Especialidad -->
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Especialidad (Opcional)</mat-label>
          <mat-icon matPrefix>work_outline</mat-icon>
          <input matInput formControlName="especialidad" style="text-transform: uppercase;" />
        </mat-form-field>
        
        <!-- Sedes Asignadas -->
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Sedes Asignadas</mat-label>
          <mat-icon matPrefix>storefront</mat-icon>
          <mat-select formControlName="sedeIds" multiple>
            @for (sede of sedes(); track sede.id) {
              <mat-option [value]="sede.id">{{ sede.nombre }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <!-- Roles Asignados -->
        <mat-form-field appearance="outline" class="w-full col-span-2">
          <mat-label>Roles Asignados</mat-label>
          <mat-icon matPrefix>shield</mat-icon>
          <mat-select formControlName="roles" multiple>
            @for (rol of roles(); track rol.id) {
              <mat-option [value]="rol.nombre">{{ rol.nombre.replace('ROLE_', '') }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end" class="p-4">
      <button mat-stroked-button mat-dialog-close>Cancelar</button>
      <button mat-flat-button color="primary" [disabled]="form.invalid || loading()" (click)="guardar()">
        <mat-icon>save</mat-icon> {{ isEdit() ? 'Guardar Cambios' : 'Registrar Empleado' }}
      </button>
    </mat-dialog-actions>
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
    
    ::ng-deep .mat-mdc-text-field-wrapper { background-color: var(--bg-card) !important; }
    
    @media (max-width: 600px) {
      .form-grid { grid-template-columns: 1fr; }
      .col-span-2 { grid-column: span 1; }
    }
  `]
})
export class EmpleadoDialogComponent implements OnInit {
  isEdit = signal(false);
  loading = signal(false);
  buscandoDni = false;
  form: FormGroup;
  
  sedes = signal<SedeResponse[]>([]);
  roles = signal<RolResponse[]>([]);

  constructor(
    private dialogRef: MatDialogRef<EmpleadoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EmpleadoDialogData,
    private fb: FormBuilder,
    private empleadoService: EmpleadoService,
    private sedeService: SedeService,
    private rolService: RolService,
    private externoService: ExternoService,
    private snack: MatSnackBar
  ) {
    this.isEdit.set(!!data.empleado);
    
    this.form = this.fb.group({
      dni: [{value: data.empleado?.dni || '', disabled: this.isEdit()}, [Validators.required, Validators.pattern('^[0-9]{8}$')]],
      nombre: [{value: data.empleado?.nombre || '', disabled: this.isEdit()}, Validators.required],
      apellido: [{value: data.empleado?.apellido || '', disabled: this.isEdit()}, Validators.required],
      telefono: [data.empleado?.telefono || '', Validators.required],
      email: [{value: data.empleado?.email || '', disabled: this.isEdit()}, [Validators.required, Validators.email]],
      especialidad: [{value: data.empleado?.especialidad || '', disabled: this.isEdit()}],
      sueldoBase: [{value: data.empleado?.sueldoBase || null, disabled: this.isEdit()}],
      sedeIds: [data.empleado?.sedeIds || [], Validators.required],
      roles: [data.empleado?.nombresRoles || [], Validators.required]
    });
  }

  ngOnInit() {
    this.cargarDatosAdicionales();
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
      
      // Auto select Chiclayo for new employees
      if (!this.isEdit()) {
        const chiclayo = s.find((sede: SedeResponse) => sede.nombre.toLowerCase().includes('chiclayo'));
        if (chiclayo) {
          this.form.get('sedeIds')?.setValue([chiclayo.id]);
        }
      }
    });

    this.rolService.listarTodos().subscribe((r: RolResponse[]) => {
      // Filtrar roles activos para nuevos empleados
      if (this.isEdit()) {
        const rolesActuales = this.data.empleado?.nombresRoles || [];
        this.roles.set(r.filter(rol => rol.activo || rolesActuales.includes(rol.nombre)));
      } else {
        this.roles.set(r.filter(rol => rol.activo));
      }
    });
  }

  buscarDni() {
    const dni = this.form.get('dni')?.value;
    if (!dni || dni.length !== 8) return;

    this.buscandoDni = true;
    this.externoService.consultarDni(dni).subscribe({
      next: (res: any) => {
        if (res && res.first_name) {
          this.form.patchValue({
            nombre: res.first_name,
            apellido: `${res.first_last_name} ${res.second_last_name}`.trim()
          });
          this.snack.open('DNI encontrado exitosamente', 'Cerrar', { duration: 3000 });
        }
        this.buscandoDni = false;
      },
      error: () => {
        this.buscandoDni = false;
        this.snack.open('No se pudo encontrar información para este documento', 'Cerrar', { duration: 4000 });
      }
    });
  }

  guardar() {
    if (this.form.invalid) return;

    this.loading.set(true);
    const dto = this.form.getRawValue();
    
    // Convertir especialidad a mayusculas
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
        this.snack.open(err.error?.mensaje || 'Error al guardar', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }
}
