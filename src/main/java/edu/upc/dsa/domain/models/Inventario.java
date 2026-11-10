package edu.upc.dsa.domain.models;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Inventario {
    private List<Arma> armas;
    private HashMap<Granada, Integer> granadas;
    private List<Complemento> complementos;

    /*Listas de los 3 elementos
    Luego habrá que determinar si debes elegir una arma/complemento para la partida o podrías intercambiarlas
    en mitad. En cuanto a granadas puedes tener múltiples de cada tipo, por eso el HashMap<Granada, Integer>
     */

    public Inventario() {
        this.armas = new ArrayList<>();
        this.granadas = new HashMap<>();
        this.complementos = new ArrayList<>();
    }

    public Inventario(List<Arma> armas, HashMap<Granada, Integer> granadas, List<Complemento> complementos) {
        this.armas = armas;
        this.granadas = granadas;
        this.complementos = complementos;
    }


    public List<Arma> getArmas() { return armas; }
    public void addArma(Arma arma) { this.armas.add(arma); }

    public HashMap<Granada, Integer> getGranadas() { return granadas; }
    public void addGranada(Granada granada, Integer qty) {
        if (granadas.containsKey(granada)) {
            granadas.put(granada, granadas.get(granada) + qty); //sumamos a las que ya tenía
        }
        else {
            granadas.put(granada, qty); //añadimos
        }
    }
    public int useGranada(Granada granada) {
        granadas.put(granada, granadas.get(granada) - 1);
        if (granadas.get(granada) == -1) {
            granadas.put(granada, 0);
            return -1; //no tiene más granadas de este tipo
        } else {
            return 0; //correcto
        }
    }
    public Integer checkGranadasQty(Granada granada) {
        return granadas.get(granada);
    }

    public List<Complemento> getComplementos() { return complementos; }
    public void addComplemento(Complemento complemento) { this.complementos.add(complemento); }
}
