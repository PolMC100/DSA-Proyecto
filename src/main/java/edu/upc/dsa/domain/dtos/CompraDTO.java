package edu.upc.dsa.domain.dtos;

public class CompraDTO {
    private String jugadorId;
    private String itemId;

    public CompraDTO() {

    }

    public CompraDTO(String jugadorId, String itemId) {
        this.jugadorId = jugadorId;
        this.itemId = itemId;
    }

    //Getters y Setters
    public String getJugadorId() {
        return jugadorId;
    }
    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }
    public String getItemId() {
        return itemId;
    }
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}

