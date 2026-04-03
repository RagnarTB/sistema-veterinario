package com.veterinaria.modelos.Enums;

public enum EstadoCita {
    AGENDADA, // El cliente reservó por internet o por teléfono
    CONFIRMADA, // La recepcionista llamó para confirmar la asistencia
    EN_SALA_ESPERA, // El paciente llegó a la clínica y está esperando
    EN_CONSULTORIO, // El doctor llamó al paciente y está siendo atendido
    COMPLETADA, // La atención médica terminó
    CANCELADA, // El cliente o la clínica canceló antes de la fecha
    NO_ASISTIO // El cliente nunca llegó (importante para reportes de morosidad)
}