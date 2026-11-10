package edu.upc.dsa.exceptions;

/** La compra supera los créditos disponibles. */
public class SaldoInsuficienteException extends Exception {
    public SaldoInsuficienteException(String message) { super(message); }
}
