package edu.upc.dsa.domain.models;

public class Arma implements Inventario.Comprable {
    private String nombre;
    private Integer ammo;
    private Integer cadencia;
    private Integer damage;
    private Integer precio;
    private Integer nivelRequerido;

    public Arma() {}

    public Arma(Arma arma) {
        this(arma.nombre, arma.ammo, arma.cadencia, arma.damage, arma.precio, arma.nivelRequerido);
    }

    public Arma(String nombre, Integer ammo, Integer cadencia, Integer damage, Integer precio, Integer nivelRequerido) {
        this.nombre = nombre;
        this.ammo = ammo;
        this.cadencia = cadencia;
        this.damage = damage;
        this.precio = precio;
        this.nivelRequerido = nivelRequerido;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getAmmo() { return ammo; }
    public void setAmmo(Integer ammo) { this.ammo = ammo; }

    public Integer getCadencia() { return cadencia; }
    public void setCadencia(Integer cadencia) { this.cadencia = cadencia; }

    public Integer getDamage() { return damage; }
    public void setDamage(Integer damage) { this.damage = damage; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public Integer getNivelRequerido() { return nivelRequerido; }
    public void setNivelRequerido(Integer nivelRequerido) { this.nivelRequerido = nivelRequerido; }

    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Arma arma && java.util.Objects.equals(nombre, arma.nombre);
    }

    @Override
    public int hashCode() { return java.util.Objects.hashCode(nombre); }
}
