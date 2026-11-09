package edu.upc.dsa.exceptions;

/** Se supera el máximo de 10 participantes (en la API REST se devolverá un 409 Conflict). */
public class PartidaLlenaException extends Exception {
    public PartidaLlenaException(String message) {
        super(message);
    }
}
