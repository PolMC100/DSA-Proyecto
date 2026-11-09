package edu.upc.dsa.domain.models;

/**
 * Estados por los que pasa una partida.
 * PENDIENTE -> EN_CURSO -> FINALIZADA (no se puede volver atrás).
 */
public enum EstadoPartida {
    PENDIENTE,
    EN_CURSO,
    FINALIZADA
}
