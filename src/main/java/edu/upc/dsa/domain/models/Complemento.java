package edu.upc.dsa.domain.models;

public class Complemento implements Inventario.Comprable {
    private String nombre;
    private String tipo;
    private String efecto;
    private Integer precio;
    private Integer nivelRequerido;

    public Complemento() {}

    public Complemento(Complemento complemento) {
        this(complemento.nombre, complemento.tipo, complemento.efecto,
                complemento.precio, complemento.nivelRequerido);
    }

    public Complemento(String nombre, String tipo, String efecto, Integer precio, Integer nivelRequerido) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.efecto = efecto;
        this.precio = precio;
        this.nivelRequerido = nivelRequerido;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEfecto() { return efecto; }
    public void setEfecto(String efecto) { this.efecto = efecto; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public Integer getNivelRequerido() { return nivelRequerido; }
    public void setNivelRequerido(Integer nivelRequerido) { this.nivelRequerido = nivelRequerido; }

    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Complemento complemento && java.util.Objects.equals(nombre, complemento.nombre);
    }

    @Override
    public int hashCode() { return java.util.Objects.hashCode(nombre); }
}
