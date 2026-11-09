package edu.upc.dsa.exceptions;

/** La acción no se puede hacer en el estado actual de la partida (p. ej. una baja en una partida finalizada) (en la API REST se devolverá un 409 Conflict). */
public class EstadoPartidaInvalidoException extends Exception {
    public EstadoPartidaInvalidoException(String message) {
        super(message);
    }
}
