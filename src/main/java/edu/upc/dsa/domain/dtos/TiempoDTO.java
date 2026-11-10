package edu.upc.dsa.domain.dtos;

/** Respuesta de GET /partidas/{id}/tiempo: segundos que le quedan a la partida. */
public class TiempoDTO {
    private String idPartida;
    private long segundosRestantes;

    public TiempoDTO() {
    }

    public TiempoDTO(String idPartida, long segundosRestantes) {
        this.idPartida = idPartida;
        this.segundosRestantes = segundosRestantes;
    }

    public String getIdPartida() { return idPartida; }
    public void setIdPartida(String idPartida) { this.idPartida = idPartida; }

    public long getSegundosRestantes() { return segundosRestantes; }
    public void setSegundosRestantes(long segundosRestantes) { this.segundosRestantes = segundosRestantes; }
}
