import { Component, signal, computed, HostListener } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
  children?: NavItem[];
}

@Component({
  selector: 'app-main-layout',
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatTooltipModule,
    MatMenuModule,
    MatButtonModule,
    MatDividerModule,
  ],
  template: `
    <div class="layout-wrapper" [class.sidebar-collapsed]="isCollapsed()">

      <!-- ─────────────────────────────────────────────────
           SIDEBAR
      ───────────────────────────────────────────────── -->
      <aside class="sidebar" [class.collapsed]="isCollapsed()">

        <!-- Logo -->
        <div class="sidebar-logo" (click)="toggleSidebar()">
          <div class="logo-icon">
            <span class="material-icons-round">pets</span>
          </div>
          @if (!isCollapsed()) {
            <div class="logo-text">
              <span class="logo-name">VetCare</span>
              <span class="logo-sub">Gestión Veterinaria</span>
            </div>
          }
          <button class="collapse-btn" [matTooltip]="isCollapsed() ? 'Expandir' : 'Colapsar'">
            <span class="material-icons-round">
              {{ isCollapsed() ? 'chevron_right' : 'chevron_left' }}
            </span>
          </button>
        </div>

        <!-- Navegación -->
        <nav class="sidebar-nav">
          @for (item of visibleNavItems(); track item.route) {
            <a
              [routerLink]="item.route"
              routerLinkActive="active"
              class="nav-item"
              [matTooltip]="isCollapsed() ? item.label : ''"
              matTooltipPosition="right"
            >
              <span class="material-icons-round nav-icon">{{ item.icon }}</span>
              @if (!isCollapsed()) {
                <span class="nav-label">{{ item.label }}</span>
              }
            </a>
          }
        </nav>

        <!-- Perfil en la base del sidebar -->
        <div class="sidebar-profile">
          <div class="profile-avatar">
            <span class="material-icons-round">person</span>
          </div>
          @if (!isCollapsed()) {
            <div class="profile-info">
              <span class="profile-email">{{ authService.currentEmail() }}</span>
              <span class="profile-role">{{ getRolLabel() }}</span>
            </div>
          }
        </div>
      </aside>

      <!-- ─────────────────────────────────────────────────
           CONTENIDO PRINCIPAL
      ───────────────────────────────────────────────── -->
      <div class="main-area">

        <!-- Header top -->
        <header class="top-header">
          <!-- Breadcrumb / Título de sección se puede agregar aquí -->
          <div class="header-left">
            <button class="mobile-menu-btn" (click)="toggleSidebar()">
              <span class="material-icons-round">menu</span>
            </button>
          </div>

          <div class="header-right">
            <!-- Notificaciones -->
            <button class="icon-btn" matTooltip="Notificaciones">
              <span class="material-icons-round">notifications_none</span>
            </button>

            <!-- Menú de usuario -->
            <button class="user-menu-btn" [matMenuTriggerFor]="userMenu">
              <div class="user-avatar">
                <span class="material-icons-round">person</span>
              </div>
              <span class="user-email">{{ authService.currentEmail() }}</span>
              <span class="material-icons-round">expand_more</span>
            </button>

            <mat-menu #userMenu="matMenu" class="user-dropdown">
              <button mat-menu-item routerLink="/app/citas">
                <span class="material-icons-round">calendar_month</span>
                <span>Mis Citas</span>
              </button>
              <button mat-menu-item>
                <span class="material-icons-round">lock</span>
                <span>Cambiar contraseña</span>
              </button>
              <mat-divider />
              <button mat-menu-item (click)="logout()" class="logout-item">
                <span class="material-icons-round">logout</span>
                <span>Cerrar sesión</span>
              </button>
            </mat-menu>
          </div>
        </header>

        <!-- Router Outlet -->
        <main class="content-area">
          <router-outlet />
        </main>
      </div>
    </div>

    <!-- Overlay para móvil -->
    @if (showMobileOverlay()) {
      <div class="mobile-overlay" (click)="closeMobileSidebar()"></div>
    }
  `,
  styles: [`
    /* ── Layout principal ── */
    .layout-wrapper {
      display: grid;
      grid-template-columns: var(--sidebar-width) 1fr;
      min-height: 100vh;
      transition: grid-template-columns var(--transition-normal);
    }

    .layout-wrapper.sidebar-collapsed {
      grid-template-columns: var(--sidebar-collapsed-width) 1fr;
    }

    /* ── Sidebar ── */
    .sidebar {
      background: var(--bg-sidebar);
      border-right: 1px solid var(--border-color);
      display: flex;
      flex-direction: column;
      height: 100vh;
      position: sticky;
      top: 0;
      overflow: hidden;
      transition: width var(--transition-normal);
      width: var(--sidebar-width);
      z-index: 100;
    }

    .sidebar.collapsed { width: var(--sidebar-collapsed-width); }

    /* Logo */
    .sidebar-logo {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 1.25rem 1rem;
      border-bottom: 1px solid var(--border-color);
      cursor: pointer;
      min-height: var(--header-height);
      position: relative;
    }

    .logo-icon {
      width: 38px; height: 38px;
      background: linear-gradient(135deg, var(--color-primary-800), var(--color-primary-500));
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      box-shadow: 0 4px 12px rgba(0,189,189,0.25);
    }

    .logo-icon .material-icons-round { font-size: 20px; color: white; }

    .logo-text {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .logo-name {
      font-size: 1rem;
      font-weight: 700;
      color: var(--text-primary);
      white-space: nowrap;
    }

    .logo-sub {
      font-size: 0.7rem;
      color: var(--text-muted);
      white-space: nowrap;
    }

    .collapse-btn {
      width: 28px; height: 28px;
      background: rgba(255,255,255,0.05);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-sm);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: var(--text-muted);
      transition: all var(--transition-fast);
      flex-shrink: 0;
    }

    .collapse-btn:hover {
      background: rgba(255,255,255,0.1);
      color: var(--text-primary);
    }

    .collapse-btn .material-icons-round { font-size: 16px; }

    /* Nav */
    .sidebar-nav {
      flex: 1;
      padding: 0.75rem 0.5rem;
      display: flex;
      flex-direction: column;
      gap: 2px;
      overflow-y: auto;
      overflow-x: hidden;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 0.65rem 0.85rem;
      border-radius: var(--radius-md);
      color: var(--text-secondary);
      text-decoration: none;
      font-size: 0.875rem;
      font-weight: 500;
      transition: all var(--transition-fast);
      white-space: nowrap;
      overflow: hidden;
    }

    .nav-item:hover {
      background: rgba(255,255,255,0.05);
      color: var(--text-primary);
    }

    .nav-item.active {
      background: rgba(0,189,189,0.12);
      color: var(--color-primary-400);
    }

    .nav-item.active .nav-icon { color: var(--color-primary-400); }

    .nav-icon {
      font-size: 20px;
      flex-shrink: 0;
      transition: color var(--transition-fast);
    }

    .nav-label { flex: 1; overflow: hidden; text-overflow: ellipsis; }

    /* Profile footer */
    .sidebar-profile {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 1rem;
      border-top: 1px solid var(--border-color);
      overflow: hidden;
    }

    .profile-avatar {
      width: 34px; height: 34px;
      background: rgba(0,189,189,0.15);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      color: var(--color-primary-400);
    }

    .profile-avatar .material-icons-round { font-size: 18px; }

    .profile-info {
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .profile-email {
      font-size: 0.78rem;
      color: var(--text-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .profile-role {
      font-size: 0.7rem;
      color: var(--color-primary-400);
      font-weight: 600;
    }

    /* ── Main area ── */
    .main-area {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
      overflow: hidden;
    }

    /* ── Header ── */
    .top-header {
      height: var(--header-height);
      background: var(--bg-surface);
      border-bottom: 1px solid var(--border-color);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 var(--space-lg);
      position: sticky;
      top: 0;
      z-index: 50;
    }

    .header-left { display: flex; align-items: center; gap: var(--space-sm); }
    .header-right { display: flex; align-items: center; gap: var(--space-sm); }

    .mobile-menu-btn, .icon-btn {
      width: 38px; height: 38px;
      background: transparent;
      border: none;
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: var(--text-secondary);
      transition: all var(--transition-fast);
    }

    .mobile-menu-btn:hover, .icon-btn:hover {
      background: rgba(255,255,255,0.06);
      color: var(--text-primary);
    }

    .mobile-menu-btn { display: none; }

    .user-menu-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      background: rgba(255,255,255,0.04);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-pill);
      padding: 0.4rem 0.85rem 0.4rem 0.5rem;
      cursor: pointer;
      color: var(--text-secondary);
      font-size: 0.8rem;
      transition: all var(--transition-fast);
    }

    .user-menu-btn:hover {
      background: rgba(255,255,255,0.08);
      color: var(--text-primary);
    }

    .user-avatar {
      width: 28px; height: 28px;
      background: rgba(0,189,189,0.15);
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      color: var(--color-primary-400);
    }
    .user-avatar .material-icons-round { font-size: 16px; }

    .user-email { max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

    /* ── Content Area ── */
    .content-area {
      flex: 1;
      overflow-y: auto;
      background: var(--bg-base);
    }

    /* ── Mobile overlay ── */
    .mobile-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.5);
      z-index: 99;
      backdrop-filter: blur(2px);
    }

    /* ── Responsive ── */
    @media (max-width: 768px) {
      .layout-wrapper { grid-template-columns: 1fr; }
      .sidebar {
        position: fixed;
        left: -100%;
        height: 100%;
        transition: left var(--transition-normal), width var(--transition-normal);
      }
      .sidebar.mobile-open { left: 0; width: var(--sidebar-width); }
      .mobile-menu-btn { display: flex; }
      .user-email { display: none; }
    }
  `],
})
export class MainLayoutComponent {
  isCollapsed = signal(false);
  isMobileOpen = signal(false);
  showMobileOverlay = computed(() => this.isMobileOpen());

  protected readonly navItems: NavItem[] = [
    { label: 'Dashboard',       icon: 'dashboard',        route: '/app/dashboard',      roles: ['ROLE_ADMIN'] },
    { label: 'Citas',           icon: 'calendar_month',   route: '/app/citas' },
    { label: 'Clientes',        icon: 'group',            route: '/app/clientes',        roles: ['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'] },
    { label: 'Pacientes',       icon: 'pets',             route: '/app/pacientes' },
    { label: 'Atenciones',      icon: 'medical_services', route: '/app/atenciones',     roles: ['ROLE_ADMIN', 'ROLE_VETERINARIO'] },
    { label: 'Hospitalización', icon: 'local_hospital',   route: '/app/hospitalizacion', roles: ['ROLE_ADMIN', 'ROLE_VETERINARIO'] },
    { label: 'Farmacia',        icon: 'local_pharmacy',   route: '/app/farmacia' },
    { label: 'Caja',            icon: 'point_of_sale',    route: '/app/caja',            roles: ['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'] },
    { label: 'Empleados',       icon: 'badge',            route: '/app/empleados',       roles: ['ROLE_ADMIN'] },
    { label: 'Sedes',           icon: 'location_on',      route: '/app/sedes',           roles: ['ROLE_ADMIN'] },
  ];

  visibleNavItems = computed(() => {
    return this.navItems.filter(item => {
      if (!item.roles) return true;
      return this.authService.hasAnyRole(...(item.roles as any[]));
    });
  });

  constructor(public authService: AuthService) {}

  toggleSidebar(): void {
    if (window.innerWidth <= 768) {
      this.isMobileOpen.update(v => !v);
    } else {
      this.isCollapsed.update(v => !v);
    }
  }

  closeMobileSidebar(): void {
    this.isMobileOpen.set(false);
  }

  logout(): void {
    this.authService.logout();
  }

  getRolLabel(): string {
    const roles = this.authService.currentRoles();
    if (roles.includes('ROLE_ADMIN')) return 'Administrador';
    if (roles.includes('ROLE_VETERINARIO')) return 'Veterinario';
    if (roles.includes('ROLE_RECEPCIONISTA')) return 'Recepcionista';
    if (roles.includes('ROLE_CLIENTE')) return 'Cliente';
    return '';
  }

  @HostListener('window:resize')
  onResize(): void {
    if (window.innerWidth > 768) {
      this.isMobileOpen.set(false);
    }
  }
}
