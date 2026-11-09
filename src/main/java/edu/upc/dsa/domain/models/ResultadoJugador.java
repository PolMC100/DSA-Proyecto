package edu.upc.dsa.domain.models;

public class ResultadoJugador {
    private String idJugador;
    private String idPartida;
    private int bajas;
    private int muertes;
    private int puntuacion;
    private int creditosGanados;

    public ResultadoJugador() {
        //constructor vacío
    }

    public ResultadoJugador(String idJugador, String idPartida) {
        this.idJugador = idJugador;
        this.idPartida = idPartida;
        this.bajas = 0;
        this.muertes = 0;
        this.puntuacion = 0;
        this.creditosGanados = 0;
    }

    public String getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(String idJugador) {
        this.idJugador = idJugador;
    }

    public String getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(String idPartida) {
        this.idPartida = idPartida;
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
