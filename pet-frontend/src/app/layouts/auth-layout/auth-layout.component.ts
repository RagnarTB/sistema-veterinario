import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-auth-layout',
  imports: [RouterOutlet],
  template: `
    <div class="auth-wrapper">
      <!-- Fondo decorativo con burbujas -->
      <div class="auth-bg">
        <div class="blob blob-1"></div>
        <div class="blob blob-2"></div>
        <div class="blob blob-3"></div>
      </div>

      <!-- Branding lateral -->
      <div class="auth-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <span class="material-icons-round">pets</span>
          </div>
          <h1 class="brand-name">VetCare</h1>
          <p class="brand-tagline">Sistema Integral de Gestión Veterinaria</p>

          <div class="brand-features">
            <div class="feature-item">
              <span class="material-icons-round">event_available</span>
              <span>Gestión de citas online</span>
            </div>
            <div class="feature-item">
              <span class="material-icons-round">medical_services</span>
              <span>Historiales clínicos digitales</span>
            </div>
            <div class="feature-item">
              <span class="material-icons-round">local_pharmacy</span>
              <span>Control de farmacia e inventario</span>
            </div>
            <div class="feature-item">
              <span class="material-icons-round">bar_chart</span>
              <span>Reportes y estadísticas en tiempo real</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Área del formulario -->
      <div class="auth-form-area">
        <router-outlet />
      </div>
    </div>
  `,
  styles: [`
    .auth-wrapper {
      min-height: 100vh;
      display: grid;
      grid-template-columns: 1fr 1fr;
      position: relative;
      overflow: hidden;
    }

    /* Fondo animado */
    .auth-bg {
      position: fixed;
      inset: 0;
      pointer-events: none;
      z-index: 0;
    }

    .blob {
      position: absolute;
      border-radius: 50%;
      filter: blur(80px);
      opacity: 0.12;
      animation: floatBlob 8s ease-in-out infinite;
    }

    .blob-1 {
      width: 500px; height: 500px;
      background: var(--color-primary-600);
      top: -100px; left: -100px;
      animation-delay: 0s;
    }

    .blob-2 {
      width: 400px; height: 400px;
      background: var(--color-primary-400);
      bottom: -80px; right: -80px;
      animation-delay: -3s;
    }

    .blob-3 {
      width: 300px; height: 300px;
      background: #8b5cf6;
      top: 40%; left: 30%;
      animation-delay: -5s;
    }

    @keyframes floatBlob {
      0%, 100% { transform: translate(0, 0) scale(1); }
      33%       { transform: translate(20px, -20px) scale(1.05); }
      66%       { transform: translate(-15px, 15px) scale(0.97); }
    }

    /* Branding lateral izquierdo */
    .auth-brand {
      background: linear-gradient(160deg, rgba(13,115,119,0.15) 0%, rgba(11,17,32,0.6) 100%);
      border-right: 1px solid var(--border-color);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 3rem;
      position: relative;
      z-index: 1;
    }

    .brand-content {
      max-width: 380px;
    }

    .brand-logo {
      width: 72px; height: 72px;
      background: linear-gradient(135deg, var(--color-primary-800), var(--color-primary-500));
      border-radius: var(--radius-xl);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 1.5rem;
      box-shadow: 0 8px 32px rgba(0,189,189,0.3);
    }

    .brand-logo .material-icons-round {
      font-size: 36px;
      color: white;
    }

    .brand-name {
      font-size: 2.5rem;
      font-weight: 800;
      background: linear-gradient(135deg, var(--color-primary-300), #fff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 0.5rem;
    }

    .brand-tagline {
      font-size: 1rem;
      color: var(--text-secondary);
      margin-bottom: 3rem;
    }

    .brand-features {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }

    .feature-item {
      display: flex;
      align-items: center;
      gap: 0.875rem;
      font-size: 0.9375rem;
      color: var(--text-secondary);
    }

    .feature-item .material-icons-round {
      font-size: 22px;
      color: var(--color-primary-400);
      flex-shrink: 0;
    }

    /* Área del formulario */
    .auth-form-area {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
      position: relative;
      z-index: 1;
    }

    /* Responsive */
    @media (max-width: 900px) {
      .auth-wrapper { grid-template-columns: 1fr; }
      .auth-brand { display: none; }
      .auth-form-area { padding: 1.5rem; }
    }
  `],
})
export class AuthLayoutComponent {}
