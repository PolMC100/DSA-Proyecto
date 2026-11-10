package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Jugador;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import edu.upc.dsa.exceptions.JugadorDuplicadoException;
import edu.upc.dsa.exceptions.JugadorNotFoundException;
import edu.upc.dsa.exceptions.SaldoInsuficienteException;
import java.util.List;

/** Contrato de perfiles y progresión. Las consultas devuelven copias del perfil. */
public interface JugadorManager {
    /** Alta con experiencia y créditos cero, nivel 1 y vida base provisional 100. */
    Jugador registrarJugador(String idJugador, String nombre, boolean bot)
            throws DatosInvalidosException, JugadorDuplicadoException;

    Jugador getJugador(String idJugador) throws DatosInvalidosException, JugadorNotFoundException;
    List<Jugador> getJugadores();

    /** false si no existe o es un bot; un identificador vacío es inválido. */
    boolean existeJugadorHumano(String idJugador) throws DatosInvalidosException;

    /** Incrementos >= 0. Compatible con la llamada comentada de Tiffany.
     * No deduplica: para recompensas de partidas usar aplicarRecompensaPartida. */
    void sumarExperienciaYCreditos(String idJugador, int experiencia, int creditos)
            throws DatosInvalidosException, JugadorNotFoundException;

    int getNivel(String idJugador) throws DatosInvalidosException, JugadorNotFoundException;
    int getCreditos(String idJugador) throws DatosInvalidosException, JugadorNotFoundException;

    /** Gur proporciona el requisito del objeto (>= 1); no se mantiene un catálogo aquí. */
    boolean cumpleNivelRequerido(String idJugador, int nivelRequerido)
            throws DatosInvalidosException, JugadorNotFoundException;

    /** Precio >= 0. Comprueba y descuenta en una misma operación. */
    void descontarCreditos(String idJugador, int precio)
            throws DatosInvalidosException, JugadorNotFoundException, SaldoInsuficienteException;

    /** true al aplicar; false si ya se aplicó a ese jugador en esa partida.
     * Una repetición no cambia importes, aunque reciba otros valores válidos.
     * El llamante valida que la partida finalizó y que el jugador participó. */
    boolean aplicarRecompensaPartida(String idPartida, String idJugador, int experiencia, int creditos)
            throws DatosInvalidosException, JugadorNotFoundException;

    /** Regla PROVISIONAL: nivel = 1 + experiencia / experienciaPorNivel.
     * Cambiarla recalcula los niveles existentes. */
    void setExperienciaPorNivel(int experienciaPorNivel) throws DatosInvalidosException;
    int getExperienciaPorNivel();

    /** Borra perfiles y recompensas y restablece la configuración provisional. */
    void limpiar();
}
