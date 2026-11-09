package edu.upc.dsa.exceptions;

/** La partida pedida no existe (en la API REST se devolverá un 404 Not Found). */
public class PartidaNotFoundException extends Exception {
    public PartidaNotFoundException(String message) {
        super(message);
    }
}
