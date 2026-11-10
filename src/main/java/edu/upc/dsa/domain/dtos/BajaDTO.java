package edu.upc.dsa.domain.dtos;

/** Cuerpo de POST /partidas/{id}/bajas: quién ha eliminado a quién. */
public class BajaDTO {
    private String idAsesino;
    private String idVictima;

    public BajaDTO() {
    }

    public BajaDTO(String idAsesino, String idVictima) {
        this.idAsesino = idAsesino;
        this.idVictima = idVictima;
    }

    public String getIdAsesino() { return idAsesino; }
    public void setIdAsesino(String idAsesino) { this.idAsesino = idAsesino; }

    public String getIdVictima() { return idVictima; }
    public void setIdVictima(String idVictima) { this.idVictima = idVictima; }
}
