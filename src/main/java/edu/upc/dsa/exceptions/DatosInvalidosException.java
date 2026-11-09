package edu.upc.dsa.exceptions;

/** Faltan datos o son incorrectos (en la API REST se devolverá un 400 Bad Request). */
public class DatosInvalidosException extends Exception {
    public DatosInvalidosException(String message) {
        super(message);
    }
}
