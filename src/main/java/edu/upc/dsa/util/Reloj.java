package edu.upc.dsa.util;

/**
 * Da la hora actual en milisegundos.
 * En el programa real se usa la hora del sistema; en los tests se usa un reloj falso
 * que podemos adelantar a mano, así probamos los 5 minutos sin esperar 5 minutos.
 */
public interface Reloj {

    long ahoraMillis();

    /** Reloj real, el que se usa por defecto. */
    Reloj SISTEMA = System::currentTimeMillis;
}
