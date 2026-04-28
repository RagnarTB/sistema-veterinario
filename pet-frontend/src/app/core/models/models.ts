// =============================================
// ENUMS
// =============================================
export type EstadoCita =
  | 'AGENDADA'
  | 'CONFIRMADA'
  | 'EN_SALA_ESPERA'
  | 'EN_CONSULTORIO'
  | 'COMPLETADA'
  | 'CANCELADA'
  | 'NO_ASISTIO';

export type EstadoVenta = 'ACTIVA' | 'ANULADA';
export type MetodoPago = 'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA';
export type TipoMovimiento = 'INGRESO' | 'EGRESO';

export type RolNombre =
  | 'ROLE_ADMIN'
  | 'ROLE_VETERINARIO'
  | 'ROLE_RECEPCIONISTA'
  | 'ROLE_CLIENTE';

// =============================================
// AUTH
// =============================================
export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  email: string;
  roles: string[];      // ROLE_ADMIN, ROLE_VETERINARIO, etc.
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface CambiarPasswordRequest {
  passwordActual: string;
  passwordNueva: string;
  confirmarPassword: string;
}

export interface RegistroClienteDTO {
  email: string;
  password: string;
  nombre: string;
  apellido: string;
  dni: string;
  telefono: string;
  direccion?: string;
}

export interface MensajeResponse {
  mensaje: string;
}

// =============================================
// SEDE
// =============================================
export interface SedeRequest {
  nombre: string;
  direccion: string;
  telefono: string;
}

export interface SedeResponse {
  id: number;
  nombre: string;
  direccion: string;
  telefono: string;
  activo: boolean;
}

// =============================================
// ESPECIE
// =============================================
export interface EspecieRequest {
  nombre: string;
}

export interface EspecieResponse {
  id: number;
  nombre: string;
  activo: boolean;
}

// =============================================
// CLIENTE
// =============================================
export interface ClienteRequest {
  nombre: string;
  apellido: string;
  dni: string;
  telefono: string;
  email: string;
  direccion?: string;
}

export interface ClienteResponse {
  id: number;
  nombre: string;
  apellido: string;
  dni: string;
  telefono: string;
  email: string;
  activo: boolean;
  verificado: boolean;
}

// =============================================
// PACIENTE
// =============================================
export interface PacienteRequest {
  nombre: string;
  especieId: number;
  raza?: string;
  fechaNacimiento?: string;
  sexo?: string;
  color?: string;
  clienteId: number;
}

export interface PacienteResponse {
  id: number;
  nombre: string;
  especie: string;
  raza?: string;
  fechaNacimiento?: string;
  sexo?: string;
  color?: string;
  clienteId?: number;
  clienteNombre: string;
  activo: boolean;
}

// =============================================
// ROL
// =============================================
export interface RolRequest {
  nombre: string;
}

export interface RolResponse {
  id: number;
  nombre: string;
  activo: boolean;
}

// =============================================
// EMPLEADO
// =============================================
export interface EmpleadoRequest {
  email: string;
  roles: string[];
  nombre: string;
  apellido: string;
  dni: string;
  telefono: string;
  especialidad?: string;
  sueldoBase?: number;
  sedeIds: number[];
}

export interface EmpleadoResponse {
  id: number;
  usuarioId: number;
  email: string;
  nombresRoles: string[];
  nombre: string;
  apellido: string;
  dni: string;
  telefono: string;
  especialidad?: string;
  sueldoBase?: number;
  activo: boolean;
  sedeIds: number[];
  sedeNombres: string[];
}

// =============================================
// SERVICIO MÉDICO
// =============================================
export interface ServicioMedicoRequest {
  nombre: string;
  descripcion?: string;
  duracionMinutos: number;
  precio: number;
  activo?: boolean;
}

export interface ServicioMedicoResponse {
  id: number;
  nombre: string;
  descripcion?: string;
  duracionMinutos: number;
  precio: number;
  activo: boolean;
}

// =============================================
// HORARIO VETERINARIO
// =============================================
export interface HorarioVeterinarioRequest {
  veterinarioId: number;
  diaSemana: string;
  horaInicio: string;
  horaFin: string;
  sedeId: number;
}

export interface HorarioVeterinarioResponse {
  id: number;
  veterinarioNombre: string;
  diaSemana: string;
  horaInicio: string;
  horaFin: string;
  sedeNombre: string;
}

// =============================================
// DÍA BLOQUEADO
// =============================================
export interface DiaBloqueadoRequest {
  veterinarioId: number;
  fecha: string;
  motivo?: string;
}

export interface DiaBloqueadoResponse {
  id: number;
  veterinarioNombre: string;
  fecha: string;
  motivo?: string;
}

// =============================================
// CITA
// =============================================
export interface CitaRequest {
  fecha: string;
  horaInicio: string;
  servicioId: number;
  veterinarioId: number;
  motivo: string;
  pacienteIds: number[];
  sedeId: number;
}

export interface CitaResponse {
  id: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: EstadoCita;
  motivo: string;
  veterinarioNombre: string;
  servicioNombre: string;
  sedeNombre: string;
  pacientes: PacienteResponse[];
}

export interface SlotDisponibilidad {
  horaInicio: string;
  horaFin: string;
  disponible: boolean;
}

// =============================================
// ATENCIÓN MÉDICA
// =============================================
export interface AtencionMedicaRequest {
  citaId: number;
  diagnostico: string;
  tratamiento: string;
  observaciones?: string;
  examenesIds?: number[];
}

export interface AtencionMedicaResponse {
  id: number;
  citaId: number;
  diagnostico: string;
  tratamiento: string;
  observaciones?: string;
  fecha: string;
}

// =============================================
// EXAMEN MÉDICO
// =============================================
export interface ExamenMedicoRequest {
  pacienteId: number;
  tipo: string;
  resultado?: string;
  fecha: string;
}

export interface ExamenMedicoResponse {
  id: number;
  pacienteNombre: string;
  tipo: string;
  resultado?: string;
  fecha: string;
}

// =============================================
// RECETA
// =============================================
export interface DetalleRecetaRequest {
  productoId: number;
  cantidad: number;
  indicaciones: string;
}

export interface DetalleRecetaResponse {
  productoNombre: string;
  cantidad: number;
  indicaciones: string;
}

export interface RecetaRequest {
  atencionId: number;
  detalles: DetalleRecetaRequest[];
}

export interface RecetaResponse {
  id: number;
  atencionId: number;
  detalles: DetalleRecetaResponse[];
}

// =============================================
// VACUNA
// =============================================
export interface VacunaRequest {
  pacienteId: number;
  nombre: string;
  lote?: string;
  fechaAplicacion: string;
  proximaDosis?: string;
  veterinarioId: number;
}

export interface VacunaResponse {
  id: number;
  pacienteNombre: string;
  nombre: string;
  lote?: string;
  fechaAplicacion: string;
  proximaDosis?: string;
  veterinarioNombre: string;
}

// =============================================
// DESPARASITACIÓN
// =============================================
export interface DesparasitacionRequest {
  pacienteId: number;
  producto: string;
  dosis?: string;
  fechaAplicacion: string;
  proximaDosis?: string;
  veterinarioId: number;
}

export interface DesparasitacionResponse {
  id: number;
  pacienteNombre: string;
  producto: string;
  dosis?: string;
  fechaAplicacion: string;
  proximaDosis?: string;
  veterinarioNombre: string;
}

// =============================================
// CIRUGÍA
// =============================================
export interface CirugiaRequest {
  pacienteId: number;
  tipoCirugia: string;
  veterinarioId: number;
  fecha: string;
  descripcion?: string;
  resultado?: string;
}

export interface CirugiaResponse {
  id: number;
  pacienteNombre: string;
  tipoCirugia: string;
  veterinarioNombre: string;
  fecha: string;
  descripcion?: string;
  resultado?: string;
}

// =============================================
// CONSENTIMIENTO INFORMADO
// =============================================
export interface ConsentimientoRequest {
  citaId: number;
  clienteId: number;
}

export interface ConsentimientoResponse {
  id: number;
  citaId: number;
  clienteNombre: string;
  fechaGeneracion: string;
}

// =============================================
// HOSPITALIZACIÓN
// =============================================
export interface HospitalizacionRequest {
  pacienteId: number;
  jaulaId: number;
  motivoIngreso: string;
  veterinarioId: number;
}

export interface HospitalizacionResponse {
  id: number;
  pacienteNombre: string;
  jaulaNombre: string;
  motivoIngreso: string;
  veterinarioNombre: string;
  fechaIngreso: string;
  fechaAlta?: string;
  estado: string;
}

export interface TrasladoHospitalizacionDTO {
  nuevaJaulaId: number;
}

// =============================================
// JAULA
// =============================================
export interface JaulaRequest {
  nombre: string;
  descripcion?: string;
  sedeId: number;
}

export interface JaulaResponse {
  id: number;
  nombre: string;
  disponible: boolean;
}

// =============================================
// MONITOREO HOSPITALIZACIÓN
// =============================================
export interface MonitoreoHospitalizacionRequest {
  hospitalizacionId: number;
  temperatura?: number;
  frecuenciaCardiaca?: number;
  frecuenciaRespiratoria?: number;
  observaciones?: string;
}

export interface MonitoreoHospitalizacionResponse {
  id: number;
  fecha: string;
  temperatura?: number;
  frecuenciaCardiaca?: number;
  frecuenciaRespiratoria?: number;
  observaciones?: string;
}

// =============================================
// PRODUCTO
// =============================================
export interface ProductoRequest {
  nombre: string;
  descripcion?: string;
  precio: number;
  stockMinimo: number;
  activo?: boolean;
}

export interface ProductoResponse {
  id: number;
  nombre: string;
  descripcion?: string;
  precio: number;
  stockMinimo: number;
  activo: boolean;
}

// =============================================
// INVENTARIO
// =============================================
export interface InventarioRequest {
  productoId: number;
  sedeId: number;
  cantidad: number;
}

// =============================================
// VENTA
// =============================================
export interface DetalleVentaRequest {
  productoId: number;
  cantidad: number;
}

export interface DetalleVentaResponse {
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface VentaRequest {
  sedeId: number;
  clienteId?: number;
  metodoPago: MetodoPago;
  detalles: DetalleVentaRequest[];
}

export interface VentaResponse {
  id: number;
  fecha: string;
  total: number;
  estado: EstadoVenta;
  metodoPago: MetodoPago;
  empleadoNombre: string;
  clienteNombre?: string;
  detalles: DetalleVentaResponse[];
}

// =============================================
// CAJA
// =============================================
export interface CajaRequest {
  sedeId: number;
  montoInicial: number;
}

export interface CierreCajaResponse {
  totalVentas: number;
  totalEfectivo: number;
  totalTarjeta: number;
  totalTransferencia: number;
}

// =============================================
// REPORTES / DASHBOARD
// =============================================
export interface DashboardResumen {
  totalVentasMes: number;
  totalClientesActivos: number;
}

export interface TopProducto {
  nombreProducto: string;
  cantidadVendida: number;
}

export interface CitasVeterinario {
  emailVeterinario: string;
  totalCitas: number;
}

// =============================================
// PAGINACIÓN (estructura de Page<T> de Spring)
// =============================================
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;     // página actual (0-indexed)
  size: number;
  first: boolean;
  last: boolean;
}

// =============================================
// ERROR RESPONSE
// =============================================
export interface ErrorResponse {
  mensaje: string;
  errores?: Record<string, string>;
  timestamp?: string;
  status?: number;
}
