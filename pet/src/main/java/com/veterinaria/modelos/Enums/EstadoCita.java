package com.veterinaria.modelos.Enums;

public enum EstadoCita {
    AGENDADA {
        @Override
        public boolean puedeTransitarA(EstadoCita nuevo) {
            return nuevo == CONFIRMADA || nuevo == EN_SALA_ESPERA || nuevo == CANCELADA || nuevo == NO_ASISTIO;
        }
    },
    CONFIRMADA {
        @Override
        public boolean puedeTransitarA(EstadoCita nuevo) {
            return nuevo == EN_SALA_ESPERA || nuevo == CANCELADA || nuevo == NO_ASISTIO;
        }
    },
    EN_SALA_ESPERA {
        @Override
        public boolean puedeTransitarA(EstadoCita nuevo) {
            return nuevo == EN_CONSULTORIO || nuevo == CANCELADA || nuevo == NO_ASISTIO;
        }
    },
    EN_CONSULTORIO {
        @Override
        public boolean puedeTransitarA(EstadoCita nuevo) {
            return nuevo == COMPLETADA;
        }
    },
    COMPLETADA {
        @Override
        public boolean puedeTransitarA(EstadoCita nuevo) {
            return false;
        }
    },
    CANCELADA {
        @Override
        public boolean puedeTransitarA(EstadoCita nuevo) {
            return false;
        }
    },
    NO_ASISTIO {
        @Override
        public boolean puedeTransitarA(EstadoCita nuevo) {
            return false;
        }
    };

    public abstract boolean puedeTransitarA(EstadoCita nuevo);
}