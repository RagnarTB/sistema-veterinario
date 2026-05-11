import { Component, signal, computed, HostListener } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../core/services/auth.service';
import { RolNombre } from '../../core/models/models';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: RolNombre[];
}

interface NavGroup {
  label: string;
  icon: string;
  roles?: RolNombre[];
  expanded?: boolean;
  children: NavItem[];
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

        <!-- Navegación Acordeón -->
        <nav class="sidebar-nav">
          @for (group of visibleNavGroups(); track group.label) {
            <div class="nav-group">
              <button 
                class="nav-group-header" 
                [class.active]="group.expanded && !isCollapsed()"
                (click)="toggleGroup(group)"
                [matTooltip]="isCollapsed() ? group.label : ''"
                matTooltipPosition="right">
                <span class="material-icons-round nav-icon">{{ group.icon }}</span>
                @if (!isCollapsed()) {
                  <span class="nav-label">{{ group.label }}</span>
                  <span class="material-icons-round chevron" [class.rotated]="group.expanded">expand_more</span>
                }
              </button>
              
              <div class="nav-group-children" [class.open]="group.expanded && !isCollapsed()">
                @for (child of getVisibleChildren(group); track child.route) {
                  <a
                    [routerLink]="child.route"
                    routerLinkActive="active"
                    class="nav-item child-item"
                  >
                    <span class="material-icons-round nav-icon child-icon">{{ child.icon }}</span>
                    <span class="nav-label">{{ child.label }}</span>
                  </a>
                }
              </div>
            </div>
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

    /* Acordeón Styles */
    .nav-group {
      display: flex;
      flex-direction: column;
      margin-bottom: 4px;
    }

    .nav-group-header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 0.65rem 0.85rem;
      border-radius: var(--radius-md);
      color: var(--text-secondary);
      background: transparent;
      border: none;
      cursor: pointer;
      font-size: 0.875rem;
      font-weight: 600;
      transition: all var(--transition-fast);
      white-space: nowrap;
      overflow: hidden;
      width: 100%;
      text-align: left;
    }

    .nav-group-header:hover {
      background: rgba(255,255,255,0.05);
      color: var(--text-primary);
    }

    .nav-group-header.active {
      background: rgba(0,189,189,0.08);
      color: var(--color-primary-400);
    }

    .chevron {
      margin-left: auto;
      font-size: 18px;
      transition: transform var(--transition-normal);
    }

    .chevron.rotated {
      transform: rotate(180deg);
    }

    .nav-group-children {
      display: flex;
      flex-direction: column;
      gap: 2px;
      padding-left: 14px;
      margin-top: 2px;
      max-height: 0;
      overflow: hidden;
      opacity: 0;
      transition: max-height 0.3s ease-in-out, opacity 0.3s ease-in-out;
    }

    .nav-group-children.open {
      max-height: 500px;
      opacity: 1;
      padding-bottom: 6px;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 0.55rem 0.85rem;
      border-radius: var(--radius-md);
      color: var(--text-secondary);
      text-decoration: none;
      font-size: 0.85rem;
      font-weight: 500;
      transition: all var(--transition-fast);
      white-space: nowrap;
      overflow: hidden;
    }

    .child-item {
      padding-left: 1.5rem;
      position: relative;
    }

    .child-item::before {
      content: '';
      position: absolute;
      left: 10px;
      top: 50%;
      width: 6px;
      height: 1px;
      background: rgba(255,255,255,0.15);
    }

    .child-icon {
      font-size: 18px !important;
      opacity: 0.8;
    }

    .nav-item:hover {
      background: rgba(255,255,255,0.05);
      color: var(--text-primary);
    }

    .nav-item.active {
      background: rgba(0,189,189,0.12);
      color: var(--color-primary-400);
    }

    .nav-item.active .nav-icon { color: var(--color-primary-400); opacity: 1; }

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

  protected readonly navGroups: NavGroup[] = [
    {
      label: 'Panel de Control',
      icon: 'dashboard',
      roles: ['ROLE_ADMIN'],
      expanded: false,
      children: [
        { label: 'Dashboard', route: '/app/dashboard', icon: 'bar_chart' },
        { label: 'Reportes', route: '/app/reportes', icon: 'pie_chart' },
      ]
    },
    {
      label: 'Gestión Clínica',
      icon: 'medical_services',
      expanded: true,
      children: [
        { label: 'Citas', route: '/app/citas', icon: 'calendar_today' },
        { label: 'Atenciones', route: '/app/atenciones', icon: 'healing', roles: ['ROLE_ADMIN', 'ROLE_VETERINARIO'] },
        { label: 'Clientes', route: '/app/clientes', icon: 'people', roles: ['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'] },
        { label: 'Pacientes', route: '/app/pacientes', icon: 'pets' },
      ]
    },
    {
      label: 'Hospitalización',
      icon: 'local_hospital',
      roles: ['ROLE_ADMIN', 'ROLE_VETERINARIO'],
      expanded: false,
      children: [
        { label: 'Panel Internados', route: '/app/hospitalizacion', icon: 'bed' },
        { label: 'Gestión de Jaulas', route: '/app/jaulas', icon: 'grid_view' },
      ]
    },
    {
      label: 'Comercial y Logística',
      icon: 'storefront',
      expanded: false,
      children: [
        { label: 'Punto de Venta', route: '/app/farmacia', icon: 'point_of_sale' },
        { label: 'Inventario', route: '/app/inventario', icon: 'inventory_2', roles: ['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'] },
      ]
    },
    {
      label: 'Finanzas',
      icon: 'account_balance',
      roles: ['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'],
      expanded: false,
      children: [
        { label: 'Caja Diaria', route: '/app/caja', icon: 'payments' },
      ]
    },
    {
      label: 'Administración',
      icon: 'admin_panel_settings',
      roles: ['ROLE_ADMIN'],
      expanded: false,
      children: [
        { label: 'Empleados', route: '/app/empleados', icon: 'badge' },
        { label: 'Sedes', route: '/app/sedes', icon: 'location_on' },
        { label: 'Catálogo Servicios', route: '/app/servicios-medicos', icon: 'design_services' },
      ]
    }
  ];

  visibleNavGroups = computed(() => {
    return this.navGroups.filter(group => {
      // Si el grupo no tiene roles, o el usuario tiene alguno de los roles requeridos
      if (!group.roles || this.authService.hasAnyRole(...group.roles)) {
        // Filtrar también para asegurar que haya al menos 1 hijo visible
        return this.getVisibleChildren(group).length > 0;
      }
      return false;
    });
  });

  getVisibleChildren(group: NavGroup): NavItem[] {
    return group.children.filter(child => {
      if (!child.roles) return true;
      return this.authService.hasAnyRole(...child.roles);
    });
  }

  toggleGroup(group: NavGroup): void {
    if (this.isCollapsed()) {
      // Si está colapsado y hacen clic, lo expandimos globalmente y abrimos este grupo
      this.isCollapsed.set(false);
      group.expanded = true;
    } else {
      group.expanded = !group.expanded;
    }
  }

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
