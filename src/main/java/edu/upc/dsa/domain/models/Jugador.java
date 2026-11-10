package edu.upc.dsa.domain.models;

import java.util.ArrayList;
import java.util.HashMap;

public class Jugador {
    private String idJugador;
    private String nombre;
    private boolean bot;
    // Vida base del perfil; vivo/muerto durante una partida pertenece a Participante.
    private int vida;
    private int experiencia;
    private int nivel;
    private int creditos;

    private Inventario inventario;

    public Jugador() {
        this.vida = 100; // Valor inicial provisional, pendiente de acordar con el equipo.
        this.nivel = 1;
        this.inventario = new Inventario();
    }

    public Jugador(String idJugador, String nombre, boolean bot) {
        this();
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.bot = bot;
    }

    /** Constructor usado por la tienda; conserva el nivel indicado, incluido 0. */
    public Jugador(String id, String nombre, double saldo, int nivel) {
        this(id, nombre, false);
        setSaldo(saldo);
        this.nivel = nivel;
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
        // Copiar las colecciones, manteniendo la identidad de los objetos del catálogo.
        // Gur usa contains(arma/complemento) y Granada como clave sin equals/hashCode.
        this.inventario = jugador.inventario == null ? null : new Inventario(
                new ArrayList<>(jugador.inventario.getArmas()),
                new HashMap<>(jugador.inventario.getGranadas()),
                new ArrayList<>(jugador.inventario.getComplementos()));
    }

    public String getIdJugador() { return idJugador; }
    public void setIdJugador(String idJugador) { this.idJugador = idJugador; }
    // Alias de compatibilidad con la tienda: un único identificador almacenado.
    public String getId() { return idJugador; }
    public void setId(String id) { this.idJugador = id; }
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

    /** Alias monetario de la tienda. Los créditos del proyecto son enteros. */
    public double getSaldo() { return creditos; }

    /** Mantiene la firma de Gur sin truncar ni redondear un saldo fraccionario. */
    public void setSaldo(double saldo) {
        if (!Double.isFinite(saldo) || saldo < Integer.MIN_VALUE || saldo > Integer.MAX_VALUE
                || saldo != Math.rint(saldo)) {
            throw new IllegalArgumentException("El saldo debe ser un número entero dentro del rango int");
        }
        this.creditos = (int) saldo;
    }

    /** Inventario mutable de este perfil, como requiere la tienda de Gur. */
    public Inventario getInventario() { return inventario; }
    public void setInventario(Inventario inventario) { this.inventario = inventario; }

    @Override
    public String toString() {
        return "Jugador [idJugador=" + idJugador + ", nombre=" + nombre + ", bot=" + bot
                + ", vida=" + vida + ", experiencia=" + experiencia + ", nivel=" + nivel
                + ", creditos=" + creditos + "]";
    }
}
