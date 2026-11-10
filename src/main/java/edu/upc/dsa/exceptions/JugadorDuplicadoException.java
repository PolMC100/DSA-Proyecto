package edu.upc.dsa.exceptions;

/** El identificador ya está registrado. */
public class JugadorDuplicadoException extends Exception {
    public JugadorDuplicadoException(String message) { super(message); }
}
