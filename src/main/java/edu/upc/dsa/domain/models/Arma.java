package edu.upc.dsa.domain.models;

public class Arma {
    private String nombre;
    private Integer munición;
    private Integer cadencia;
    private Integer daño;
    private Integer precio;
    private Integer nivelRequerido;

    public Arma() {}

    public Arma(String nombre, Integer munición, Integer cadencia, Integer daño, Integer precio, Integer nivelRequerido) {
        this.nombre = nombre;
        this.munición = munición;
        this.cadencia = cadencia;
        this.daño = daño;
        this.precio = precio;
        this.nivelRequerido = nivelRequerido;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getMunición() { return munición; }
    public void setMunición(Integer munición) { this.munición = munición; }

    public Integer getCadencia() { return cadencia; }
    public void setCadencia(Integer cadencia) { this.cadencia = cadencia; }

    public Integer getDaño() { return daño; }
    public void setDaño(Integer daño) { this.daño = daño; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public Integer getNivelRequerido() { return nivelRequerido; }
    public void setNivelRequerido(Integer precio) { this.nivelRequerido = nivelRequerido; }
}