package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Arma;
import edu.upc.dsa.domain.models.Complemento;
import edu.upc.dsa.domain.models.Granada;
import edu.upc.dsa.domain.models.Jugador;
import edu.upc.dsa.exceptions.DatosInvalidosException;

import java.util.HashMap;
import java.util.List;

public interface TiendaManager {

    HashMap<String, Arma> getArmas();
    HashMap<String, Granada> getGranadas();
    HashMap<String, Complemento> getComplementos();

    int comprarArma(Jugador jugador, String nombreArma) throws DatosInvalidosException;
    int comprarGranada(Jugador jugador, String nombreGranada, Integer qty) throws DatosInvalidosException;
    int comprarComplemento(Jugador jugador, String nombreComplemento) throws DatosInvalidosException;

    void clear();
}
