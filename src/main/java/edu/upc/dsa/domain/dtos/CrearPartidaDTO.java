package edu.upc.dsa.domain.dtos;

/** Cuerpo de POST /partidas: quién juega y cuántos bots quiere (0 a 9). */
public class CrearPartidaDTO {
    private String idJugadorHumano;
    private int numBots;

    public CrearPartidaDTO() {
    }

    public CrearPartidaDTO(String idJugadorHumano, int numBots) {
        this.idJugadorHumano = idJugadorHumano;
        this.numBots = numBots;
    }

    public String getIdJugadorHumano() { return idJugadorHumano; }
    public void setIdJugadorHumano(String idJugadorHumano) { this.idJugadorHumano = idJugadorHumano; }

    public int getNumBots() { return numBots; }
    public void setNumBots(int numBots) { this.numBots = numBots; }
}
