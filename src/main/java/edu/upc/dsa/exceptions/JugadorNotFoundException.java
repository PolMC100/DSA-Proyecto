package edu.upc.dsa.exceptions;

/** No existe un perfil con ese identificador. */
public class JugadorNotFoundException extends Exception {
    public JugadorNotFoundException(String message) { super(message); }
}
