package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.domain.models.Participante;
import edu.upc.dsa.domain.models.ResultadoJugador;
import edu.upc.dsa.domain.models.Scoreboard;

import java.util.HashMap;
import java.util.Map;

public class ResultadosManagerImpl implements ResultadosManager, PartidaListener {

    private static ResultadosManagerImpl instance;
    private final Map<String, Scoreboard> historialMarcadores;

    private ResultadosManagerImpl() {
        this.historialMarcadores = new HashMap<>();
    }

    public static ResultadosManagerImpl getInstance() {
        if (instance == null) {
            instance = new ResultadosManagerImpl();
        }
        return instance;
    }

    public void inicializarMarcador(Partida p) {
        if (p == null || historialMarcadores.containsKey(p.getId())) return;

        Scoreboard sb = new Scoreboard(p.getId());
        for (Participante part : p.getParticipantes()) {
            ResultadoJugador res = new ResultadoJugador(part.getIdJugador(), p.getId());
            sb.getClasificacion().add(res);
        }
        historialMarcadores.put(p.getId(), sb);
    }

    @Override
    public void registrarBajaYMuerte(String idAtacante, String idVictima, String idPartida) {
        Scoreboard sb = historialMarcadores.get(idPartida);
        if (sb == null) return;

        for (ResultadoJugador res : sb.getClasificacion()) {
            if (res.getIdJugador().equals(idAtacante)) {
                res.setBajas(res.getBajas() + 1);
                res.setPuntuacion(res.getPuntuacion() + 100); // 100 pts por baja
            }
            if (res.getIdJugador().equals(idVictima)) {
                res.setMuertes(res.getMuertes() + 1);
            }
        }
    }

    @Override
    public Scoreboard obtenerMarcadorActual(String idPartida) {
        Scoreboard sb = historialMarcadores.get(idPartida);
        if (sb != null) {
            sb.getClasificacion().sort((r1, r2) -> {
                //mayor puntuación
                int compPuntuacion = Integer.compare(r2.getPuntuacion(), r1.getPuntuacion());
                if (compPuntuacion != 0) return compPuntuacion;

                // Primer desempate: mayor número de bajas
                int compBajas = Integer.compare(r2.getBajas(), r1.getBajas());
                if (compBajas != 0) return compBajas;

                //Segundo desempate: menor número de muertes
                return Integer.compare(r1.getMuertes(), r2.getMuertes());
            });
        }
        return sb;
    }

    @Override
    public void cerrarPartidaYCalcularRecompensas(String idPartida) {
        Scoreboard sb = obtenerMarcadorActual(idPartida);

        if (sb == null || sb.isProcesado()) {
            return;
        }

        for (ResultadoJugador res : sb.getClasificacion()) {
            int creditos = (res.getBajas() * 10) + 50;
            res.setCreditosGanados(creditos);

            try {
                JugadorManagerImpl.getInstance().sumarExperienciaYCreditos(
                        res.getIdJugador(),
                       res.getPuntuacion(),
                        creditos
                );
            } catch (Exception e) {
                System.err.println("No se han podido aplicar las recompensas al participante: " + res.getIdJugador());
            }
        }
        sb.setProcesado(true);
    }

    @Override
    public void limpiar() {
        this.historialMarcadores.clear();
    }

    @Override
    public void onBaja(Partida p, String idAsesino, String idVictima) {
        if (!historialMarcadores.containsKey(p.getId())) {
            inicializarMarcador(p);
        }
        this.registrarBajaYMuerte(idAsesino, idVictima, p.getId());
    }

    @Override
    public void onPartidaFinalizada(Partida p) {
        this.cerrarPartidaYCalcularRecompensas(p.getId());
    }
}