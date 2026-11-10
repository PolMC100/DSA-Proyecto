package edu.upc.dsa.domain.dtos;

/** Cuerpo de POST /partidas/{id}/reapariciones: el jugador muerto que vuelve a la partida. */
public class ReaparicionDTO {
    private String idJugador;

    public ReaparicionDTO() {
    }

    public ReaparicionDTO(String idJugador) {
        this.idJugador = idJugador;
    }

    public String getIdJugador() { return idJugador; }
    public void setIdJugador(String idJugador) { this.idJugador = idJugador; }
}
