package edu.upc.dsa.domain.dtos;

public class JugadorDTO {
    private String id;
    private String nombre;
    private int nivel;
    private int experiencia;
    private int creditos;

    public JugadorDTO() {

    }
    public JugadorDTO(String id, String nombre, int nivel, int experiencia, int creditos) {
        this.id = id;
        this.nombre = nombre;
        this.nivel = nivel;
        this.experiencia = experiencia;
        this.creditos = creditos;
    }
    //Getters y Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public int getNivel() {
        return nivel;
    }
    public void setNivel(int nivel) {
        this.nivel = nivel;
    }
    public int getExperiencia() {
        return experiencia;
    }
    public void setExperiencia(int experiencia) {
        this.experiencia = experiencia;
    }
    public int getCreditos() {
        return creditos;
    }
    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }
}

