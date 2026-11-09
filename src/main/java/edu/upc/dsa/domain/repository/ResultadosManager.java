package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.domain.models.Scoreboard;

public interface ResultadosManager {
    void inicializarMarcador(Partida p);

    void registrarBajaYMuerte(String idAtacante, String idVictima, String idPartida);

    Scoreboard obtenerMarcadorActual(String idPartida);

    void cerrarPartidaYCalcularRecompensas(String idPartida);

    void limpiar();
}