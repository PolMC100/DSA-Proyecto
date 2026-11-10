package edu.upc.dsa.domain.dtos;

public class ResultadoJugadorDTO {
    private String jugadorId;
    private String partidaId;
    private int bajas;
    private int muertes;
    private int puntuacion;
    private int creditosGanados;

    public ResultadoJugadorDTO() {
    }

    public ResultadoJugadorDTO(String jugadorId, String partidaId, int bajas, int muertes, int puntuacion, int creditosGanados) {
        this.jugadorId = jugadorId;
        this.partidaId = partidaId;
        this.bajas = bajas;
        this.muertes = muertes;
        this.puntuacion = puntuacion;
        this.creditosGanados = creditosGanados;
    }
    //Getters y Setters
    public String getJugadorId() {
        return jugadorId;
    }
    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }
    public String getPartidaId() {
        return partidaId;
    }
    public void setPartidaId(String partidaId) {
        this.partidaId = partidaId;
    }
    public int getBajas() {
        return bajas;
    }
    public void setBajas(int bajas) {
        this.bajas = bajas;
    }
    public int getMuertes() {
        return muertes;
    }
    public void setMuertes(int muertes) {
        this.muertes = muertes;
    }
    public int getPuntuacion() {
        return puntuacion;
    }
    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }
    public int getCreditosGanados() {
        return creditosGanados;
    }
    public void setCreditosGanados(int creditosGanados) {
        this.creditosGanados = creditosGanados;
    }
}



