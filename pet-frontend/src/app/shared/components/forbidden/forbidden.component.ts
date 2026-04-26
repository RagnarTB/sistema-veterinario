import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  imports: [RouterLink],
  template: `
    <div class="forbidden-page fade-in-up">
      <div class="forbidden-icon">
        <span class="material-icons-round">gpp_bad</span>
      </div>
      <h2>Acceso denegado</h2>
      <p>No tienes permisos para acceder a esta sección.</p>
      <a routerLink="/app/citas" class="btn btn-primary">
        <span class="material-icons-round">home</span>
        Ir al inicio
      </a>
    </div>
  `,
  styles: [`
    .forbidden-page {
      min-height: calc(100vh - var(--header-height));
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 1rem;
      text-align: center;
      padding: 2rem;
    }
    .forbidden-icon {
      width: 80px; height: 80px;
      background: rgba(239,68,68,0.1);
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      margin-bottom: 1rem;
    }
    .forbidden-icon .material-icons-round { font-size: 40px; color: #ef4444; }
    h2 { font-size: 1.75rem; }
    p { color: var(--text-muted); margin-bottom: 1rem; }
  `],
})
export class ForbiddenComponent {}
