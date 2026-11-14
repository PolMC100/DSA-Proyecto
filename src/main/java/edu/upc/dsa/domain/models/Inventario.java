package edu.upc.dsa.domain.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inventario {
    private final List<Arma> armas;
    private final Map<String, Integer> granadas;
    private final List<Complemento> complementos;

    public Inventario() {
        this.armas = new ArrayList<>();
        this.granadas = new HashMap<>();
        this.complementos = new ArrayList<>();
    }

    public Inventario(List<Arma> armas, Map<String, Integer> granadas, List<Complemento> complementos) {
        this();
        armas.forEach(arma -> this.armas.add(new Arma(arma)));
        this.granadas.putAll(granadas);
        complementos.forEach(complemento -> this.complementos.add(new Complemento(complemento)));
    }

    public Inventario(Inventario inventario) {
        this(inventario.armas, inventario.granadas, inventario.complementos);
    }

    public void addObjeto(Comprable objeto, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser > 0");
        if (!(objeto instanceof Granada) && cantidad != 1) {
            throw new IllegalArgumentException("Las armas y complementos se compran de uno en uno");
        }
        switch (objeto) {
            case Arma arma -> addArma(arma);
            case Granada granada -> addGranada(granada, cantidad);
            case Complemento complemento -> addComplemento(complemento);
            default -> throw new IllegalArgumentException("Tipo de objeto no comprable");
        }
    }

    public List<Arma> getArmas() { return armas; }

    public void addArma(Arma arma) {
        validarNombre(arma == null ? null : arma.getNombre());
        if (armas.contains(arma)) throw new IllegalStateException("El jugador ya tiene el arma " + arma.getNombre());
        armas.add(new Arma(arma));
    }

    public Map<String, Integer> getGranadas() { return granadas; }

    public void addGranada(Granada granada, Integer cantidad) {
        String nombre = validarNombre(granada == null ? null : granada.getNombre());
        if (cantidad == null || cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser > 0");
        long total = (long) granadas.getOrDefault(nombre, 0) + cantidad;
        if (total > Integer.MAX_VALUE) throw new IllegalArgumentException("Demasiadas granadas en el inventario");
        granadas.put(nombre, (int) total);
    }

    /** Devuelve -1 sin unidades y 0 al consumir una; nunca deja cantidades negativas. */
    public int useGranada(Granada granada) {
        return useGranada(validarNombre(granada == null ? null : granada.getNombre()));
    }

    public int useGranada(String nombre) {
        validarNombre(nombre);
        int cantidad = granadas.getOrDefault(nombre, 0);
        if (cantidad == 0) return -1;
        granadas.put(nombre, cantidad - 1);
        return 0;
    }

    public Integer checkGranadasQty(Granada granada) {
        return granadas.getOrDefault(validarNombre(granada == null ? null : granada.getNombre()), 0);
    }

    public List<Complemento> getComplementos() { return complementos; }

    public void addComplemento(Complemento complemento) {
        validarNombre(complemento == null ? null : complemento.getNombre());
        if (complementos.contains(complemento)) {
            throw new IllegalStateException("El jugador ya tiene el complemento " + complemento.getNombre());
        }
        complementos.add(new Complemento(complemento));
    }

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Hace falta el nombre del objeto");
        return nombre;
    }

    public interface Comprable {
        String getNombre();
        Integer getPrecio();
        Integer getNivelRequerido();
    }
}
