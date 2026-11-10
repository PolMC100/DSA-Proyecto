package edu.upc.dsa.domain.models;

import io.swagger.models.auth.In;

public class Granada {
    private String nombre;
    private Integer daño;
    private Double radio;
    private String efecto;
    private Integer duracionEfecto;
    private Integer precio;
    private Integer nivelRequerido;

    public Granada() {}

    public Granada(String nombre, Integer daño, Double radio, String efecto, Integer duracionEfecto, Integer precio, Integer nivelRequerido) {
        this.nombre = nombre;
        this.daño = daño;
        this.radio = radio;
        this.efecto = efecto;
        this.duracionEfecto = duracionEfecto;
        this.precio = precio;
        this.nivelRequerido = nivelRequerido;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getDaño() { return daño; }
    public void setDaño(Integer daño) { this.daño = daño; }

    public Double getRadio() { return radio; }
    public void setRadio(Double radio) { this.radio = radio; }

    public String getEfecto() { return efecto; }
    public void setEfecto(String efecto) { this.efecto = efecto; }

    public Integer getDuracionEfecto() { return duracionEfecto; }
    public void setDuracionEfecto(Integer duracionEfecto) { this.duracionEfecto = duracionEfecto; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public Integer getNivelRequerido() { return nivelRequerido; }
    public void setNivelRequerido(Integer precio) { this.nivelRequerido = nivelRequerido; }
}
