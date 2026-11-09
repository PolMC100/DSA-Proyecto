package edu.upc.dsa.domain.models;

import java.util.List;
import java.util.ArrayList;

public class Scoreboard {
    private String idPartida;
    private List<ResultadoJugador> clasificacion;
    private boolean procesado; // Evita duplicar recompensas

    public Scoreboard() {
        //Constructor vacío
    }

    public Scoreboard(String idPartida) {
        this.idPartida = idPartida;
        this.clasificacion = new ArrayList<>();
        this.procesado = false; // Al crearse, aún no se han cobrado las recompensas
    }

    public String getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(String idPartida) {
        this.idPartida = idPartida;
    }

    public List<ResultadoJugador> getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(List<ResultadoJugador> clasificacion) {
        this.clasificacion = clasificacion;
    }

    public boolean isProcesado() {
        return procesado;
    }

    public void setProcesado(boolean procesado) {
        this.procesado = procesado;
    }
}