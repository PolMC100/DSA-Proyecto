package edu.upc.dsa.exceptions;

/** El jugador no participa en esa partida (en la API REST se devolverá un 404 Not Found). */
public class ParticipanteNotFoundException extends Exception {
    public ParticipanteNotFoundException(String message) {
        super(message);
    }
}
