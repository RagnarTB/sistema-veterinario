import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginRequest,
  AuthResponse,
  CambiarPasswordRequest,
  MensajeResponse,
  RefreshTokenRequest,
  RegistroClienteDTO,
  RolNombre,
} from '../models/models';

const TOKEN_KEY   = 'vet_token';
const REFRESH_KEY = 'vet_refresh_token';
const EMAIL_KEY   = 'vet_email';
const ROLES_KEY   = 'vet_roles';   // <-- guardamos roles directamente

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  // ── Signals reactivos ───────────────────────────────────────────────
  private _token  = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private _email  = signal<string | null>(localStorage.getItem(EMAIL_KEY));
  private _roles  = signal<RolNombre[]>(this.loadRolesFromStorage());

  readonly isAuthenticated = computed(() => !!this._token());
  readonly currentEmail    = computed(() => this._email());
  readonly currentRoles    = computed(() => this._roles());

  constructor(private http: HttpClient, private router: Router) {}

  // ── Auth endpoints ──────────────────────────────────────────────────

  login(dto: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, dto).pipe(
      tap((res) => this.guardarSesion(res))
    );
  }

  registro(dto: RegistroClienteDTO): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${this.apiUrl}/registro`, dto);
  }

  cambiarPassword(dto: CambiarPasswordRequest): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${this.apiUrl}/cambiar-password`, dto);
  }

  confirmarCuenta(token: string, password: string): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${this.apiUrl}/confirmar-token`, { token, password });
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = localStorage.getItem(REFRESH_KEY) ?? '';
    const req: RefreshTokenRequest = { refreshToken };
    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, req).pipe(
      tap((res) => this.guardarSesion(res))
    );
  }

  logout(): void {
    const refreshToken = localStorage.getItem(REFRESH_KEY) ?? '';
    const req: RefreshTokenRequest = { refreshToken };
    this.http.post(`${this.apiUrl}/logout`, req).subscribe({
      complete: () => this.limpiarSesion(),
      error:    () => this.limpiarSesion(),
    });
  }

  // ── Helpers de roles ────────────────────────────────────────────────

  hasRole(rol: RolNombre): boolean {
    return this._roles().includes(rol);
  }

  hasAnyRole(...roles: RolNombre[]): boolean {
    return roles.some((r) => this._roles().includes(r));
  }

  isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }

  // ── Helpers internos ────────────────────────────────────────────────

  getToken(): string | null {
    return this._token();
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_KEY);
  }

  private guardarSesion(res: AuthResponse): void {
    const roles = (res.roles ?? []) as RolNombre[];
    localStorage.setItem(TOKEN_KEY,   res.token);
    localStorage.setItem(REFRESH_KEY, res.refreshToken);
    localStorage.setItem(EMAIL_KEY,   res.email);
    localStorage.setItem(ROLES_KEY,   JSON.stringify(roles));
    this._token.set(res.token);
    this._email.set(res.email);
    this._roles.set(roles);
  }

  private limpiarSesion(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(EMAIL_KEY);
    localStorage.removeItem(ROLES_KEY);
    this._token.set(null);
    this._email.set(null);
    this._roles.set([]);
    this.router.navigate(['/login']);
  }

  private loadRolesFromStorage(): RolNombre[] {
    try {
      const raw = localStorage.getItem(ROLES_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }
}
