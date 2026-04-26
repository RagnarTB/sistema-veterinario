import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartType } from 'chart.js';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ReporteService } from '../../core/services/reporte.service';
import { DashboardResumen, TopProducto, CitasVeterinario } from '../../core/models/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    BaseChartDirective,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="dashboard-container fade-in-up">
      <div class="dashboard-header">
        <div>
          <h2>Dashboard Analítico</h2>
          <p class="text-muted">Resumen general de tu veterinaria</p>
        </div>
      </div>

      @if (loading()) {
        <div class="loading-state">
          <mat-spinner diameter="40"></mat-spinner>
          <p>Cargando datos del dashboard...</p>
        </div>
      } @else {
        <!-- ROW 1: Tarjetas de resumen -->
        <div class="summary-cards">
          <!-- Card Ventas -->
          <div class="summary-card premium-card">
            <div class="card-icon gradient-primary">
              <span class="material-icons-round">attach_money</span>
            </div>
            <div class="card-info">
              <span class="card-title">Ventas del Mes</span>
              <span class="card-value">{{ resumen()?.totalVentasMes | currency }}</span>
            </div>
          </div>

          <!-- Card Clientes Activos -->
          <div class="summary-card premium-card">
            <div class="card-icon gradient-accent">
              <span class="material-icons-round">group</span>
            </div>
            <div class="card-info">
              <span class="card-title">Clientes Activos</span>
              <span class="card-value">{{ resumen()?.totalClientesActivos | number }}</span>
            </div>
          </div>
        </div>

        <!-- ROW 2: Gráficos y Tablas -->
        <div class="dashboard-grid">
          <!-- Columna Izquierda: Gráfico Top Productos -->
          <div class="dashboard-panel premium-card">
            <h3 class="panel-title">Top 5 Productos Más Vendidos</h3>
            
            <div class="chart-wrapper">
              <canvas
                baseChart
                [data]="chartData"
                [options]="chartOptions"
                [type]="chartType"
              >
              </canvas>
            </div>
          </div>

          <!-- Columna Derecha: Rendimiento Veterinarios -->
          <div class="dashboard-panel premium-card">
            <h3 class="panel-title">Rendimiento Veterinarios (Citas)</h3>

            <div class="table-container">
              <table mat-table [dataSource]="veterinarios()" class="custom-table">
                <!-- Nombre Column -->
                <ng-container matColumnDef="nombre">
                  <th mat-header-cell *matHeaderCellDef> Veterinario / Email </th>
                  <td mat-cell *matCellDef="let element">
                    <div class="vet-cell">
                      <div class="vet-avatar">
                        <span class="material-icons-round">person</span>
                      </div>
                      <span class="vet-name">{{element.emailVeterinario}}</span>
                    </div>
                  </td>
                </ng-container>

                <!-- Citas Column -->
                <ng-container matColumnDef="citas">
                  <th mat-header-cell *matHeaderCellDef style="text-align: right"> Citas Completadas </th>
                  <td mat-cell *matCellDef="let element" style="text-align: right"> 
                    <span class="badge-count">{{element.totalCitas}}</span>
                  </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="premium-row"></tr>
              </table>
            </div>

            @if (veterinarios().length === 0) {
              <div class="empty-state">
                <span class="material-icons-round">analytics</span>
                <p>No hay datos de citas registrados aún.</p>
              </div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: var(--space-xl);
      max-width: 1600px;
      margin: 0 auto;
    }

    .dashboard-header {
      margin-bottom: var(--space-xl);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .dashboard-header h2 {
      font-size: 1.8rem;
      font-weight: 800;
      color: var(--text-primary);
      margin: 0;
    }

    .text-muted {
      color: var(--text-muted);
      margin-top: 4px;
    }

    /* Targetas de Resumen */
    .summary-cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: var(--space-lg);
      margin-bottom: var(--space-xl);
    }

    .summary-card {
      display: flex;
      align-items: center;
      gap: 1.5rem;
      padding: 1.5rem;
    }

    .card-icon {
      width: 60px;
      height: 60px;
      border-radius: var(--radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      box-shadow: 0 8px 16px rgba(0,0,0,0.2);
    }

    .gradient-primary {
      background: linear-gradient(135deg, var(--color-primary-600), var(--color-primary-400));
      color: white;
    }
    
    .gradient-accent {
      background: linear-gradient(135deg, #8b5cf6, #c084fc);
      color: white;
    }

    .card-icon .material-icons-round {
      font-size: 32px;
    }

    .card-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .card-title {
      font-size: 0.9rem;
      font-weight: 500;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .card-value {
      font-size: 2rem;
      font-weight: 800;
      color: var(--text-primary);
    }

    /* Grillas inferior */
    .dashboard-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: var(--space-lg);
    }

    @media (max-width: 1024px) {
      .dashboard-grid {
        grid-template-columns: 1fr;
      }
    }

    .dashboard-panel {
      padding: 1.5rem;
      display: flex;
      flex-direction: column;
    }

    .panel-title {
      font-size: 1.2rem;
      font-weight: 600;
      margin-bottom: 1.5rem;
      color: var(--text-primary);
      border-bottom: 1px solid var(--border-color);
      padding-bottom: 1rem;
    }

    .chart-wrapper {
      height: 350px;
      width: 100%;
      position: relative;
    }

    /* Tabla personalizada */
    .table-container {
      overflow-x: auto;
    }

    .custom-table {
      width: 100%;
      background: transparent;
    }

    .vet-cell {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 0;
    }

    .vet-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: rgba(0, 189, 189, 0.15);
      color: var(--color-primary-400);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .vet-name {
      font-weight: 500;
      color: var(--text-primary);
    }

    .badge-count {
      background: rgba(139, 92, 246, 0.15);
      color: #c084fc;
      padding: 4px 12px;
      border-radius: 20px;
      font-weight: 700;
      font-size: 0.85rem;
    }

    .loading-state, .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem 2rem;
      color: var(--text-muted);
      gap: 1rem;
    }

    .empty-state .material-icons-round {
      font-size: 48px;
      opacity: 0.5;
    }
  `]
})
export class DashboardComponent implements OnInit {
  private reporteService = inject(ReporteService);

  loading = signal(true);
  resumen = signal<DashboardResumen | null>(null);
  topProductos = signal<TopProducto[]>([]);
  veterinarios = signal<CitasVeterinario[]>([]);

  // Configuración de la tabla
  displayedColumns: string[] = ['nombre', 'citas'];

  // Configuración del Chart
  chartType: ChartType = 'bar';
  chartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: []
  };
  
  chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(15, 23, 42, 0.9)',
        titleColor: '#fff',
        bodyColor: '#cbd5e1',
        padding: 12,
        cornerRadius: 8,
        displayColors: false
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: {
          color: 'rgba(255, 255, 255, 0.05)',
        },
        ticks: { color: '#94a3b8' }
      },
      x: {
        grid: { display: false },
        ticks: { color: '#94a3b8' }
      }
    }
  };

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos() {
    // Para simplificar, hacemos 3 llamadas concurrentes en un flujo simple sin RxJS forkJoin
    // debido al signal state
    let pending = 3;
    const checkDone = () => {
      pending--;
      if (pending === 0) this.loading.set(false);
    };

    this.reporteService.getDashboard().subscribe({
      next: (data) => {
        this.resumen.set(data);
        checkDone();
      },
      error: () => checkDone()
    });

    this.reporteService.getTopProductos().subscribe({
      next: (data) => {
        this.topProductos.set(data);
        this.actualizarGrafico(data);
        checkDone();
      },
      error: () => checkDone()
    });

    this.reporteService.getRendimientoVeterinarios().subscribe({
      next: (data) => {
        this.veterinarios.set(data);
        checkDone();
      },
      error: () => checkDone()
    });
  }

  actualizarGrafico(datos: TopProducto[]) {
    // Si no hay datos, evitamos error
    if (!datos || datos.length === 0) {
      this.chartData = { labels: [], datasets: [] };
      return;
    }

    const labels = datos.map(d => d.nombreProducto || 'Desconocido');
    const dataValues = datos.map(d => d.cantidadVendida || 0);

    // Paleta premium
    const bgColors = [
      'rgba(0, 189, 189, 0.8)',
      'rgba(139, 92, 246, 0.8)',
      'rgba(59, 130, 246, 0.8)',
      'rgba(16, 185, 129, 0.8)',
      'rgba(245, 158, 11, 0.8)'
    ];

    this.chartData = {
      labels: labels,
      datasets: [
        {
          data: dataValues,
          backgroundColor: bgColors,
          borderRadius: 8,
          borderSkipped: false,
          barPercentage: 0.6,
        }
      ]
    };
  }
}
