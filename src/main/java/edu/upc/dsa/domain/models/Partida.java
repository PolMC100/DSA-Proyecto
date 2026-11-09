package edu.upc.dsa.domain.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Una partida free for all: 1 jugador humano + bots, máximo 10 participantes, 5 minutos.
 */
public class Partida {

    public static final int MAX_PARTICIPANTES = 10;
    public static final int DURACION_SEGUNDOS = 5 * 60;

    private String id;
    private String idJugadorHumano;
    private List<Participante> participantes;
    private EstadoPartida estado;
    private long inicioMillis;   // momento en que empezó (0 si aún no ha empezado)
    private long finMillis;      // momento en que acabó (0 si aún no ha acabado)

    public Partida() {
        // Constructor vacío: lo necesita Jackson/Jersey para crear el objeto desde JSON
        this.participantes = new ArrayList<>();
        this.estado = EstadoPartida.PENDIENTE;
    }

    public Partida(String id, String idJugadorHumano) {
        this();
        this.id = id;
        this.idJugadorHumano = idJugadorHumano;
    }

    /** Busca un participante por su id; devuelve null si no está en la partida. */
    public Participante buscarParticipante(String idJugador) {
        for (Participante p : this.participantes) {
            if (p.getIdJugador().equals(idJugador)) return p;
        }
        return null;
    }

    public void addParticipante(Participante p) {
        this.participantes.add(p);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdJugadorHumano() { return idJugadorHumano; }
    public void setIdJugadorHumano(String idJugadorHumano) { this.idJugadorHumano = idJugadorHumano; }

    public List<Participante> getParticipantes() { return participantes; }
    public void setParticipantes(List<Participante> participantes) { this.participantes = participantes; }

    public EstadoPartida getEstado() { return estado; }
    public void setEstado(EstadoPartida estado) { this.estado = estado; }

    public long getInicioMillis() { return inicioMillis; }
    public void setInicioMillis(long inicioMillis) { this.inicioMillis = inicioMillis; }

    public long getFinMillis() { return finMillis; }
    public void setFinMillis(long finMillis) { this.finMillis = finMillis; }

    @Override
    public String toString() {
        return "Partida [id=" + id + ", humano=" + idJugadorHumano + ", estado=" + estado
                + ", participantes=" + participantes.size() + "]";
    }
}
