package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Partida;

/**
 * Patrón Observer: quien quiera enterarse de lo que pasa en las partidas implementa esta
 * interfaz y se registra con PartidaManager.addListener(...).
 * Lo usará el módulo de resultados (Tiffany) para contar bajas y repartir recompensas.
 */
public interface PartidaListener {

    /** Se llama cada vez que un participante elimina a otro. */
    void onBaja(Partida partida, String idAsesino, String idVictima);

    /** Se llama una sola vez, cuando la partida pasa a FINALIZADA. */
    void onPartidaFinalizada(Partida partida);
}
