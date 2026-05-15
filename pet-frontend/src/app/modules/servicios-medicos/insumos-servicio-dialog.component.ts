import { Component, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialog, MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ServicioMedicoService } from './servicio-medico.service';
import { ProductoService, Producto } from '../farmacia/services/producto.service';
import { ServicioMedicoResponse, ServicioMedicoInsumoResponse } from '../../core/models/models';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion/modal-confirmacion.component';
import { debounceTime, distinctUntilChanged, switchMap, finalize } from 'rxjs/operators';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-insumos-servicio-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatAutocompleteModule
  ],
  template: `
    <div class="dialog-container flex flex-col h-[90vh] max-h-[850px]">
      <!-- Header Estilizado -->
      <div class="dialog-header flex justify-between items-center p-5 border-b bg-white rounded-t-2xl">
        <div class="flex items-center gap-4">
          <div class="bg-blue-100 p-3 rounded-xl shadow-sm">
            <span class="material-icons-round text-blue-600 text-2xl">inventory_2</span>
          </div>
          <div>
            <h2 class="text-xl font-bold m-0 text-[var(--text-primary)]">Plantilla de Insumos</h2>
            <p class="text-xs text-[var(--text-muted)] m-0 flex items-center gap-1">
              <span class="material-icons-round text-xs">label</span>
              Servicio: <span class="font-bold text-blue-600">{{ data.nombre }}</span>
            </p>
          </div>
        </div>
        <button mat-icon-button (click)="onClose()" class="text-gray-400 hover:text-gray-600 transition-colors">
          <span class="material-icons-round">close</span>
        </button>
      </div>

      <mat-dialog-content class="flex-1 overflow-y-auto p-6 custom-scrollbar bg-[#f8fafc]">
        <!-- Formulario para agregar insumo con mejor distribución -->
        <div class="bg-white p-6 rounded-2xl border border-gray-100 mb-8 shadow-sm">
          <div class="flex items-center justify-between mb-5">
            <h3 class="text-xs font-bold uppercase tracking-widest text-blue-500 flex items-center gap-2 m-0">
              <span class="material-icons-round text-sm">add_circle</span> Configurar Nuevo Insumo
            </h3>
            <span class="text-[10px] text-gray-400 italic">* Insumos sugeridos para la atención médica</span>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-12 gap-5 items-start">
            <!-- Buscador de Producto -->
            <div class="md:col-span-5">
              <label class="block text-[11px] font-bold text-gray-400 mb-1.5 ml-1 uppercase">Producto</label>
              <div class="relative group">
                <span class="material-icons-round absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-blue-500 transition-colors" style="font-size: 18px;">search</span>
                <input 
                  type="text" 
                  class="form-control w-full pl-10 h-[45px] border-gray-200 focus:border-blue-400 focus:ring-4 focus:ring-blue-50 shadow-sm transition-all" 
                  placeholder="Escriba nombre o marca..."
                  [matAutocomplete]="auto"
                  [(ngModel)]="searchQuery"
                  (input)="onSearchInput($event)"
                >
                @if (loadingSearch()) {
                  <div class="absolute right-3 top-1/2 -translate-y-1/2">
                    <div class="animate-spin h-4 w-4 border-2 border-primary border-t-transparent rounded-full"></div>
                  </div>
                }
              </div>
              <mat-autocomplete #auto="matAutocomplete" [displayWith]="displayFn" (optionSelected)="onProductSelected($event.option.value)">
                @for (prod of filteredProducts(); track prod.id) {
                  <mat-option [value]="prod">
                    <div class="flex flex-col py-1.5">
                      <span class="font-bold text-sm text-gray-800">{{ prod.nombre }}</span>
                      <span class="text-[10px] text-gray-400 uppercase tracking-tight flex items-center gap-1">
                        <span class="font-semibold text-blue-400">{{ prod.marca || 'GENÉRICO' }}</span> | 
                        Venta: {{ prod.unidadVentaNombre }}
                      </span>
                    </div>
                  </mat-option>
                }
              </mat-autocomplete>
            </div>

            <!-- Cantidad -->
            <div class="md:col-span-2">
              <label class="block text-[11px] font-bold text-gray-400 mb-1.5 ml-1 uppercase">Cantidad</label>
              <input type="number" class="form-control w-full h-[45px] text-center font-bold text-lg border-gray-200 focus:border-blue-400 shadow-sm" [(ngModel)]="newInsumo.cantidadEstimada" min="0.01" step="0.1">
            </div>

            <!-- Notas -->
            <div class="md:col-span-3">
              <label class="block text-[11px] font-bold text-gray-400 mb-1.5 ml-1 uppercase">Notas Adicionales</label>
              <input type="text" class="form-control w-full h-[45px] border-gray-200 focus:border-blue-400 shadow-sm" [(ngModel)]="newInsumo.notas" placeholder="Ej. Según peso...">
            </div>

            <!-- Botón Agregar -->
            <div class="md:col-span-2">
              <label class="block text-[11px] font-bold text-transparent mb-1.5 ml-1 uppercase">Acción</label>
              <button 
                class="btn btn-primary w-full h-[45px] flex items-center justify-center gap-2 shadow-lg shadow-blue-200 hover:translate-y-[-2px] transition-all" 
                (click)="addInsumo()" 
                [disabled]="!selectedProduct || newInsumo.cantidadEstimada <= 0"
              >
                <span class="material-icons-round text-lg">add</span>
                <span class="font-bold uppercase tracking-wider text-xs">Agregar</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Tabla de insumos actuales -->
        <div>
          <div class="flex items-center justify-between mb-4 px-1">
            <h3 class="text-sm font-bold text-gray-600 flex items-center gap-2">
              <span class="material-icons-round text-lg text-blue-400">format_list_bulleted</span>
              Insumos definidos en plantilla
            </h3>
            <span class="badge badge-primary px-3 py-1.5 rounded-xl font-bold uppercase tracking-wider">{{ insumos().length }} productos</span>
          </div>
          
          <div class="border border-gray-100 rounded-3xl overflow-hidden bg-white shadow-xl shadow-gray-200/50">
            <table class="table w-full">
              <thead class="bg-gray-50/50">
                <tr>
                  <th class="pl-8 py-4 text-[10px] uppercase tracking-widest text-gray-400">Insumo</th>
                  <th class="text-center py-4 text-[10px] uppercase tracking-widest text-gray-400">Cant. Sugerida</th>
                  <th class="py-4 text-[10px] uppercase tracking-widest text-gray-400">Observaciones</th>
                  <th class="text-center py-4 text-[10px] uppercase tracking-widest text-gray-400">Estado</th>
                  <th class="text-center pr-8 py-4 text-[10px] uppercase tracking-widest text-gray-400">Acciones</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                @for (insumo of insumos(); track insumo.id) {
                  <tr [class.opacity-60]="!insumo.activo" class="hover:bg-blue-50/30 transition-colors group">
                    <td class="pl-8 py-5">
                      <div class="flex flex-col">
                        <span class="font-bold text-gray-700 text-sm group-hover:text-blue-600 transition-colors">{{ insumo.productoNombre }}</span>
                        <span class="text-[10px] text-gray-400 uppercase tracking-tight">{{ insumo.unidadVentaNombre }}</span>
                      </div>
                    </td>
                    <td class="text-center py-5">
                      <span class="font-mono font-bold text-blue-600 bg-blue-50 px-4 py-1.5 rounded-xl text-sm border border-blue-100">
                        {{ insumo.cantidadEstimada }}
                      </span>
                    </td>
                    <td class="py-5">
                      <span class="text-xs text-gray-500 italic">{{ insumo.notas || '—' }}</span>
                    </td>
                    <td class="text-center py-5">
                      <span class="chip px-3 py-1 rounded-lg font-bold" [class]="insumo.activo ? 'chip-success' : 'chip-danger'">
                        {{ insumo.activo ? 'Activo' : 'Inactivo' }}
                      </span>
                    </td>
                    <td class="text-center pr-8 py-5">
                      <div class="flex justify-center gap-2">
                        <button class="btn-icon w-9 h-9 rounded-xl hover:bg-white shadow-sm transition-all" (click)="toggleEstado(insumo)" [title]="insumo.activo ? 'Desactivar' : 'Reactivar'">
                          <span class="material-icons-round text-lg" [style.color]="insumo.activo ? '#f87171' : '#4ade80'">
                            {{ insumo.activo ? 'block' : 'settings_backup_restore' }}
                          </span>
                        </button>
                        @if (!insumo.activo) {
                          <button class="btn-icon w-9 h-9 rounded-xl hover:bg-red-50 shadow-sm transition-all" (click)="eliminarInsumoPermanente(insumo)" title="Eliminar definitivamente">
                            <span class="material-icons-round text-lg text-red-600">delete_forever</span>
                          </button>
                        }
                      </div>
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="5" class="text-center py-20 text-gray-400 bg-white">
                      <div class="flex flex-col items-center opacity-30">
                        <span class="material-icons-round text-7xl mb-3">inventory_2</span>
                        <p class="m-0 font-bold uppercase tracking-widest text-xs">Plantilla vacía</p>
                        <p class="text-[10px] italic">Agregue insumos usando el buscador superior</p>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </mat-dialog-content>

      <div class="dialog-actions p-5 border-t bg-white rounded-b-2xl flex justify-end">
        <button mat-flat-button color="primary" class="h-[45px] px-8 rounded-xl font-bold" (click)="onClose()">
          Finalizar Gestión
        </button>
      </div>
    </div>
  `,
  styles: [`
    .dialog-container { min-width: 800px; }
    .badge-primary { background: #eff6ff; color: #3b82f6; font-size: 10px; padding: 2px 8px; border-radius: 6px; }
    .chip { font-size: 10px; font-weight: 800; padding: 2px 8px; border-radius: 6px; text-transform: uppercase; }
    .chip-success { background: #dcfce7; color: #166534; }
    .chip-danger { background: #fee2e2; color: #991b1b; }
    .animate-spin { animation: spin 1s linear infinite; }
    @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
    .custom-scrollbar::-webkit-scrollbar { width: 6px; }
    .custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
    .custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
    .custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #cbd5e1; }
  `]
})
export class InsumosServicioDialogComponent implements OnInit {
  insumos = signal<ServicioMedicoInsumoResponse[]>([]);
  filteredProducts = signal<Producto[]>([]);
  loadingSearch = signal(false);
  
  searchQuery = '';
  selectedProduct: Producto | null = null;
  newInsumo = {
    cantidadEstimada: 1,
    notas: ''
  };

  private searchSubject = new Subject<string>();

  constructor(
    private servicioMedicoService: ServicioMedicoService,
    private productoService: ProductoService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    public dialogRef: MatDialogRef<InsumosServicioDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ServicioMedicoResponse
  ) {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(query => {
        if (!query || query.length < 2) return [[]];
        this.loadingSearch.set(true);
        // Listar productos activos
        return this.productoService.listar(query, 0, 20).pipe(
          switchMap(res => [res.content]),
          finalize(() => this.loadingSearch.set(false))
        );
      })
    ).subscribe(prods => {
      this.filteredProducts.set(prods);
    });
  }

  ngOnInit(): void {
    this.cargarInsumos();
  }

  cargarInsumos(): void {
    this.servicioMedicoService.listarInsumos(this.data.id).subscribe(res => {
      this.insumos.set(res);
    });
  }

  onSearchInput(event: any): void {
    this.searchSubject.next(event.target.value);
  }

  displayFn(prod: Producto): string {
    return prod ? prod.nombre : '';
  }

  onProductSelected(prod: Producto): void {
    this.selectedProduct = prod;
  }

  addInsumo(): void {
    if (!this.selectedProduct) return;

    this.servicioMedicoService.agregarInsumo(this.data.id, {
      productoId: this.selectedProduct.id,
      cantidadEstimada: this.newInsumo.cantidadEstimada,
      notas: this.newInsumo.notas
    }).subscribe({
      next: () => {
        this.snackBar.open('Insumo agregado a la plantilla', 'OK', { duration: 2000 });
        this.cargarInsumos();
        this.searchQuery = '';
        this.selectedProduct = null;
        this.newInsumo = { cantidadEstimada: 1, notas: '' };
      },
      error: (err) => {
        this.snackBar.open(err.error?.message || 'Error al agregar insumo', 'Cerrar', { duration: 3000 });
      }
    });
  }

  toggleEstado(insumo: ServicioMedicoInsumoResponse): void {
    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: insumo.activo ? 'Desactivar Insumo' : 'Reactivar Insumo',
        message: `¿Está seguro de ${insumo.activo ? 'desactivar' : 'reactivar'} el insumo "${insumo.productoNombre}" de la plantilla?`,
        confirmText: insumo.activo ? 'Desactivar' : 'Reactivar',
        isDestructive: insumo.activo
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.servicioMedicoService.cambiarEstadoInsumo(this.data.id, insumo.id, !insumo.activo).subscribe(() => {
          this.cargarInsumos();
          this.snackBar.open('Estado de insumo actualizado', 'OK', { duration: 2000 });
        });
      }
    });
  }

  eliminarInsumoPermanente(insumo: ServicioMedicoInsumoResponse): void {
    const dialogRef = this.dialog.open(ModalConfirmacionComponent, {
      width: '400px',
      data: {
        title: 'Eliminar Insumo',
        message: `¿Está seguro de ELIMINAR PERMANENTEMENTE el insumo "${insumo.productoNombre}" de esta plantilla? Esta acción no se puede deshacer.`,
        confirmText: 'Eliminar',
        isDestructive: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.servicioMedicoService.eliminarInsumo(this.data.id, insumo.id).subscribe({
          next: () => {
            this.cargarInsumos();
            this.snackBar.open('Insumo eliminado definitivamente', 'OK', { duration: 2000 });
          },
          error: (err) => {
            this.snackBar.open(err.error?.message || 'Error al eliminar', 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }

  onClose(): void {
    this.dialogRef.close();
  }
}
