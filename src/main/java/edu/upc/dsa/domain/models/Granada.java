package edu.upc.dsa.domain.models;

public class Granada implements Inventario.Comprable {
    private String nombre;
    private Integer damage;
    private Double radio;
    private String efecto;
    private Integer duracionEfecto;
    private Integer precio;
    private Integer nivelRequerido;

    public Granada() {}

    public Granada(Granada granada) {
        this(granada.nombre, granada.damage, granada.radio, granada.efecto,
                granada.duracionEfecto, granada.precio, granada.nivelRequerido);
    }

    public Granada(String nombre, Integer damage, Double radio, String efecto, Integer duracionEfecto, Integer precio, Integer nivelRequerido) {
        this.nombre = nombre;
        this.damage = damage;
        this.radio = radio;
        this.efecto = efecto;
        this.duracionEfecto = duracionEfecto;
        this.precio = precio;
        this.nivelRequerido = nivelRequerido;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getDamage() { return damage; }
    public void setDamage(Integer damage) { this.damage = damage; }

    public Double getRadio() { return radio; }
    public void setRadio(Double radio) { this.radio = radio; }

    public String getEfecto() { return efecto; }
    public void setEfecto(String efecto) { this.efecto = efecto; }

    public Integer getDuracionEfecto() { return duracionEfecto; }
    public void setDuracionEfecto(Integer duracionEfecto) { this.duracionEfecto = duracionEfecto; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public Integer getNivelRequerido() { return nivelRequerido; }
    public void setNivelRequerido(Integer nivelRequerido) { this.nivelRequerido = nivelRequerido; }
}
