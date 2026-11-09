package edu.upc.dsa.domain.models;

/**
 * Un jugador (humano o bot) dentro de una partida concreta.
 * Solo guarda lo que necesita la partida: quién es y si está vivo ahora mismo.
 * Las bajas y muertes acumuladas las lleva ResultadoJugador (módulo de resultados).
 */
public class Participante {

    private String idJugador;
    private boolean bot;
    private boolean vivo;

    public Participante() {
        // Constructor vacío: lo necesita Jackson/Jersey para crear el objeto desde JSON
    }

    public Participante(String idJugador, boolean bot) {
        this.idJugador = idJugador;
        this.bot = bot;
        this.vivo = true;
    }

    public String getIdJugador() { return idJugador; }
    public void setIdJugador(String idJugador) { this.idJugador = idJugador; }

    public boolean isBot() { return bot; }
    public void setBot(boolean bot) { this.bot = bot; }

    public boolean isVivo() { return vivo; }
    public void setVivo(boolean vivo) { this.vivo = vivo; }

    @Override
    public String toString() {
        return "Participante [idJugador=" + idJugador + ", bot=" + bot + ", vivo=" + vivo + "]";
    }
}
