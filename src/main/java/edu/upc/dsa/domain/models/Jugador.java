package edu.upc.dsa.domain.models;

public class Jugador {
    private String idJugador;
    private String nombre;
    private boolean bot;
    // Vida base del perfil; vivo/muerto durante una partida pertenece a Participante.
    private int vida;
    private int experiencia;
    private int nivel;
    private int creditos;

    // Pendiente con Gur: añadir Inventario y sus accesores cuando defina su clase.
    // No se usa Object ni una colección provisional incompatible con su modelo.

    public Jugador() {
        this.vida = 100; // Valor inicial provisional, pendiente de acordar con el equipo.
        this.nivel = 1;
    }

    public Jugador(String idJugador, String nombre, boolean bot) {
        this();
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.bot = bot;
    }

    /** Copia del perfil para no exponer el estado interno del gestor. */
    public Jugador(Jugador jugador) {
        this.idJugador = jugador.idJugador;
        this.nombre = jugador.nombre;
        this.bot = jugador.bot;
        this.vida = jugador.vida;
        this.experiencia = jugador.experiencia;
        this.nivel = jugador.nivel;
        this.creditos = jugador.creditos;
    }

    public String getIdJugador() { return idJugador; }
    public void setIdJugador(String idJugador) { this.idJugador = idJugador; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public boolean isBot() { return bot; }
    public void setBot(boolean bot) { this.bot = bot; }
    public int getVida() { return vida; }
    public void setVida(int vida) { this.vida = vida; }
    public int getExperiencia() { return experiencia; }
    public void setExperiencia(int experiencia) { this.experiencia = experiencia; }
    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
    public int getCreditos() { return creditos; }
    public void setCreditos(int creditos) { this.creditos = creditos; }

    @Override
    public String toString() {
        return "Jugador [idJugador=" + idJugador + ", nombre=" + nombre + ", bot=" + bot
                + ", vida=" + vida + ", experiencia=" + experiencia + ", nivel=" + nivel
                + ", creditos=" + creditos + "]";
    }
}
