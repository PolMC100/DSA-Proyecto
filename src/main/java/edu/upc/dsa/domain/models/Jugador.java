package edu.upc.dsa.domain.models;

/*CLASE PROVISIONAL PARA PODER IMPLEMENTAR LA TIENDA, (añadir estos parámetros) -- Gur*/

public class Jugador {
    private String id;
    private String nombre;
    private double saldo;
    private int nivel;
    private Inventario inventario;

    public Jugador() {
    }

    public Jugador(String id, String nombre, double saldo, int nivel) {
        this.id = id;
        this.nombre = nombre;
        this.saldo = saldo;
        this.nivel = nivel;
        this.inventario = new Inventario();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }

    public Inventario getInventario() { return inventario; }
    public void setInventario(Inventario inventario) { this.inventario = inventario; }
}