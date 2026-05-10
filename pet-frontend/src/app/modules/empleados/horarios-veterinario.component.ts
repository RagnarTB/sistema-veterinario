import { Component, Input, OnInit, OnChanges, SimpleChanges, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';

import { HorarioService, HorarioVeterinarioRequest, HorarioVeterinarioResponse } from '../../core/services/horario.service';
import { SedeResponse } from '../../core/models/models';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion/modal-confirmacion.component';

@Component({
  selector: 'app-horarios-veterinario',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTabsModule
  ],
  template: `
    <div class="p-4">
      <h3 class="text-lg font-semibold mb-4 text-[var(--text-primary)]">Horarios del Veterinario</h3>

      <form [formGroup]="form" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
        <mat-form-field appearance="outline">
          <mat-label>Sede</mat-label>
          <mat-select formControlName="sedeId">
            @for (sede of sedes; track sede.id) {
              <mat-option [value]="sede.id">{{ sede.nombre }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Día de la Semana</mat-label>
          <mat-select formControlName="diaSemana">
            <mat-option value="MONDAY">Lunes</mat-option>
            <mat-option value="TUESDAY">Martes</mat-option>
            <mat-option value="WEDNESDAY">Miércoles</mat-option>
            <mat-option value="THURSDAY">Jueves</mat-option>
            <mat-option value="FRIDAY">Viernes</mat-option>
            <mat-option value="SATURDAY">Sábado</mat-option>
            <mat-option value="SUNDAY">Domingo</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Hora Entrada</mat-label>
          <mat-select formControlName="horaEntrada" required>
            @for (h of horasDisponibles; track h) {
              <mat-option [value]="h">{{ formatHora(h) }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Hora Salida</mat-label>
          <mat-select formControlName="horaSalida" required>
            @for (h of horasDisponibles; track h) {
              <mat-option [value]="h">{{ formatHora(h) }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Inicio Refrigerio (Opcional)</mat-label>
          <mat-select formControlName="inicioRefrigerio">
            <mat-option [value]="null">Sin refrigerio</mat-option>
            @for (h of horasDisponibles; track h) {
              <mat-option [value]="h">{{ formatHora(h) }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Fin Refrigerio (Opcional)</mat-label>
          <mat-select formControlName="finRefrigerio">
            <mat-option [value]="null">Sin refrigerio</mat-option>
            @for (h of horasDisponibles; track h) {
              <mat-option [value]="h">{{ formatHora(h) }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <div class="col-span-1 md:col-span-2 lg:col-span-4 flex justify-end">
          <button mat-flat-button color="primary" [disabled]="form.invalid || loading()" (click)="agregarHorario()">
            <mat-icon>add</mat-icon> Agregar Horario
          </button>
        </div>
      </form>

      @if (sedes.length > 1) {
        <mat-tab-group (selectedTabChange)="sedeFiltroId.set(sedes[$event.index].id)" class="mb-4">
          @for (sede of sedes; track sede.id) {
            <mat-tab [label]="sede.nombre"></mat-tab>
          }
        </mat-tab-group>
      }

      <table mat-table [dataSource]="horariosFiltrados()" class="w-full">
        <ng-container matColumnDef="sede">
          <th mat-header-cell *matHeaderCellDef> Sede </th>
          <td mat-cell *matCellDef="let element"> {{ getSedeNombre(element.sedeId) }} </td>
        </ng-container>

        <ng-container matColumnDef="dia">
          <th mat-header-cell *matHeaderCellDef> Día </th>
          <td mat-cell *matCellDef="let element"> {{ traducirDia(element.diaSemana) }} </td>
        </ng-container>

        <ng-container matColumnDef="horas">
          <th mat-header-cell *matHeaderCellDef> Horario </th>
          <td mat-cell *matCellDef="let element"> {{ element.horaEntrada }} - {{ element.horaSalida }} </td>
        </ng-container>

        <ng-container matColumnDef="refrigerio">
          <th mat-header-cell *matHeaderCellDef> Refrigerio </th>
          <td mat-cell *matCellDef="let element"> 
            {{ element.inicioRefrigerio ? element.inicioRefrigerio + ' - ' + element.finRefrigerio : 'Sin refrigerio' }} 
          </td>
        </ng-container>

        <ng-container matColumnDef="acciones">
          <th mat-header-cell *matHeaderCellDef> Acciones </th>
          <td mat-cell *matCellDef="let element">
            <button mat-icon-button color="warn" (click)="eliminar(element.id)" title="Eliminar horario">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        
        <tr class="mat-row" *matNoDataRow>
          <td class="mat-cell p-4 text-center text-gray-500" colspan="5">
            No hay horarios registrados para este veterinario.
          </td>
        </tr>
      </table>
    </div>
  `
})
export class HorariosVeterinarioComponent implements OnInit, OnChanges {
  @Input() veterinarioId!: number;
  @Input() sedes: SedeResponse[] = [];
  
  form: FormGroup;
  loading = signal(false);
  horarios = signal<HorarioVeterinarioResponse[]>([]);
  sedeFiltroId = signal<number>(0);
  
  horariosFiltrados = computed(() => {
    const todos = this.horarios();
    if (this.sedes.length === 1) return todos.filter(h => h.sedeId === this.sedes[0].id);
    const filtro = this.sedeFiltroId();
    if (filtro > 0) return todos.filter(h => h.sedeId === filtro);
    return this.sedes.length > 0 ? todos.filter(h => h.sedeId === this.sedes[0].id) : todos;
  });

  displayedColumns: string[] = ['sede', 'dia', 'horas', 'refrigerio', 'acciones'];
  horasDisponibles: string[] = [];

  constructor(
    private fb: FormBuilder,
    private horarioService: HorarioService,
    private snack: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.form = this.fb.group({
      sedeId: ['', Validators.required],
      diaSemana: ['', Validators.required],
      horaEntrada: ['', Validators.required],
      horaSalida: ['', Validators.required],
      inicioRefrigerio: [''],
      finRefrigerio: ['']
    });
  }

  ngOnInit() {
    this.generarHoras();
    if (this.sedes.length > 0) {
      this.form.get('sedeId')?.setValue(this.sedes[0].id);
      this.sedeFiltroId.set(this.sedes[0].id);
    }
    this.cargarHorarios();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['sedes'] && this.sedes.length > 0) {
      const currentSedeId = this.form.get('sedeId')?.value;
      if (!this.sedes.find(s => s.id === currentSedeId)) {
        this.form.get('sedeId')?.setValue(this.sedes[0].id);
      }
      if (!this.sedes.find(s => s.id === this.sedeFiltroId())) {
        this.sedeFiltroId.set(this.sedes[0].id);
      }
    }
  }

  generarHoras() {
    for (let i = 0; i < 24; i++) {
      for (let j = 0; j < 60; j += 30) {
        const hh = i.toString().padStart(2, '0');
        const mm = j.toString().padStart(2, '0');
        this.horasDisponibles.push(`${hh}:${mm}`);
      }
    }
  }

  formatHora(hora: string): string {
    if (!hora) return '';
    const partes = hora.split(':');
    let h = parseInt(partes[0], 10);
    const m = partes[1];
    const ampm = h >= 12 ? 'PM' : 'AM';
    h = h % 12;
    if (h === 0) h = 12;
    return `${h.toString().padStart(2, '0')}:${m} ${ampm}`;
  }

  cargarHorarios() {
    this.loading.set(true);
    this.horarioService.listar().subscribe({
      next: (res) => {
        // En un caso real, el backend debería filtrar por veterinario. 
        // Si no lo hace, filtramos en frontend:
        const delVet = res.filter(h => h.veterinarioId === this.veterinarioId);
        this.horarios.set(delVet);
        this.loading.set(false);
      },
      error: () => {
        this.snack.open('Error al cargar horarios', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  agregarHorario() {
    if (this.form.invalid) return;
    
    const v = this.form.value;
    
    // Validaciones lógicas de tiempo
    if (v.horaEntrada >= v.horaSalida) {
      this.snack.open('La hora de salida debe ser posterior a la de entrada', 'Cerrar', { duration: 4000 });
      return;
    }
    if (v.inicioRefrigerio && v.finRefrigerio) {
      if (v.inicioRefrigerio >= v.finRefrigerio) {
        this.snack.open('El fin de refrigerio debe ser posterior al inicio', 'Cerrar', { duration: 4000 });
        return;
      }
      if (v.inicioRefrigerio < v.horaEntrada || v.finRefrigerio > v.horaSalida) {
        this.snack.open('El refrigerio debe estar dentro del horario laboral', 'Cerrar', { duration: 4000 });
        return;
      }
    } else if (v.inicioRefrigerio || v.finRefrigerio) {
      this.snack.open('Debe especificar tanto el inicio como el fin del refrigerio', 'Cerrar', { duration: 4000 });
      return;
    }

    // Comprobar si ya existe un horario para ese día en NINGUNA sede
    const existe = this.horarios().find(h => h.diaSemana === v.diaSemana);
    if (existe) {
      const msj = existe.sedeId === v.sedeId 
        ? `El veterinario ya tiene un horario registrado para este día en esta sede. ¿Desea reemplazarlo con el nuevo horario?`
        : `El veterinario ya trabaja el ${this.traducirDia(v.diaSemana)} en la sede ${this.getSedeNombre(existe.sedeId)}. Solo puede trabajar en una sede por día. ¿Desea reemplazar su sede y horario para este día?`;

      const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
        width: '400px',
        data: {
          title: 'Reemplazar Horario',
          message: msj,
          confirmText: 'Sí, Reemplazar',
          isDestructive: false
        }
      });

      dialogRef.afterClosed().subscribe(confirmed => {
        if (confirmed) {
          this.loading.set(true);
          this.horarioService.eliminar(existe.id).subscribe({
            next: () => this.guardarNuevo(),
            error: () => {
              this.snack.open('Error al reemplazar horario existente', 'Cerrar', { duration: 3000 });
              this.loading.set(false);
            }
          });
        }
      });
      return;
    }

    this.guardarNuevo();
  }

  private guardarNuevo() {
    this.loading.set(true);
    const dto: HorarioVeterinarioRequest = {
      veterinarioId: this.veterinarioId,
      ...this.form.value
    };

    if (dto.horaEntrada.length === 5) dto.horaEntrada += ':00';
    if (dto.horaSalida.length === 5) dto.horaSalida += ':00';
    if (dto.inicioRefrigerio && dto.inicioRefrigerio.length === 5) dto.inicioRefrigerio += ':00';
    if (dto.finRefrigerio && dto.finRefrigerio.length === 5) dto.finRefrigerio += ':00';

    this.horarioService.crear(dto).subscribe({
      next: () => {
        this.snack.open('Horario agregado', 'Cerrar', { duration: 3000 });
        this.cargarHorarios();
        // Reset partial form
        this.form.patchValue({
          horaEntrada: '',
          horaSalida: '',
          inicioRefrigerio: '',
          finRefrigerio: ''
        });
      },
      error: (err) => {
        this.snack.open(err.error?.mensaje || 'Error al agregar horario', 'Cerrar', { duration: 4000 });
        this.loading.set(false);
      }
    });
  }

  eliminar(id: number) {
    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: 'Eliminar Horario',
        message: '¿Estás seguro de que deseas eliminar este horario? Esta acción no se puede deshacer.',
        confirmText: 'Sí, Eliminar',
        isDestructive: true
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);
        this.horarioService.eliminar(id).subscribe({
          next: () => {
            this.snack.open('Horario eliminado', 'Cerrar', { duration: 3000 });
            this.cargarHorarios();
          },
          error: () => {
            this.snack.open('Error al eliminar', 'Cerrar', { duration: 3000 });
            this.loading.set(false);
          }
        });
      }
    });
  }

  getSedeNombre(sedeId: number): string {
    const s = this.sedes.find(x => x.id === sedeId);
    return s ? s.nombre : 'Desconocida';
  }

  traducirDia(dia: string): string {
    const dias: any = {
      'MONDAY': 'Lunes',
      'TUESDAY': 'Martes',
      'WEDNESDAY': 'Miércoles',
      'THURSDAY': 'Jueves',
      'FRIDAY': 'Viernes',
      'SATURDAY': 'Sábado',
      'SUNDAY': 'Domingo'
    };
    return dias[dia] || dia;
  }
}
