package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.EstadoPartida;
import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import edu.upc.dsa.exceptions.EstadoPartidaInvalidoException;
import edu.upc.dsa.exceptions.ParticipanteNotFoundException;
import edu.upc.dsa.exceptions.PartidaLlenaException;
import edu.upc.dsa.exceptions.PartidaNotFoundException;
import edu.upc.dsa.util.Reloj;

import java.util.List;

/**
 * Gestor de partidas (lógica de negocio). Lo usarán los servicios REST de Azahara.
 */
public interface PartidaManager {

    /** Crea una partida PENDIENTE con el jugador humano y numBots bots (máximo 10 en total). */
    Partida crearPartida(String idJugadorHumano, int numBots)
            throws DatosInvalidosException, PartidaLlenaException;

    Partida getPartida(String idPartida) throws PartidaNotFoundException;

    List<Partida> getPartidas();

    List<Partida> getPartidasPorEstado(EstadoPartida estado);

    /** PENDIENTE -> EN_CURSO. Empieza a contar el tiempo. */
    Partida iniciarPartida(String idPartida)
            throws PartidaNotFoundException, EstadoPartidaInvalidoException;

    /** Un participante elimina a otro. La víctima queda muerta hasta que reaparezca. */
    void registrarBaja(String idPartida, String idAsesino, String idVictima)
            throws PartidaNotFoundException, ParticipanteNotFoundException, EstadoPartidaInvalidoException;

    /** Un participante muerto vuelve a la partida (reapariciones ilimitadas). */
    void reaparecer(String idPartida, String idJugador)
            throws PartidaNotFoundException, ParticipanteNotFoundException, EstadoPartidaInvalidoException;

    /** EN_CURSO -> FINALIZADA (por ejemplo, cuando el cliente avisa de que ha acabado). */
    Partida finalizarPartida(String idPartida)
            throws PartidaNotFoundException, EstadoPartidaInvalidoException;

    /** Segundos que le quedan a la partida (300 si no ha empezado, 0 si ha acabado). */
    long getSegundosRestantes(String idPartida) throws PartidaNotFoundException;

    /** Si han pasado los 5 minutos, finaliza la partida. Devuelve true si está finalizada. */
    boolean comprobarTiempo(String idPartida) throws PartidaNotFoundException;

    void addListener(PartidaListener listener);

    /** Permite cambiar el reloj (en los tests se usa uno falso). */
    void setReloj(Reloj reloj);

    int size();

    void clear();
}
