package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.domain.models.Participante;
import edu.upc.dsa.domain.models.ResultadoJugador;
import edu.upc.dsa.domain.models.Scoreboard;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ResultadosManagerTest {

    private ResultadosManager manager;

    @Before
    public void setUp() {
        manager = ResultadosManagerImpl.getInstance();
        manager.limpiar();
    }

    @After
    public void tearDown() {
        manager.limpiar();
    }

    @Test
    public void testRegistroBajasYMuertes() {
        Partida p = new Partida("P1", "jugador1");
        p.addParticipante(new Participante("jugador1", false));
        p.addParticipante(new Participante("jugador2", false));

        manager.inicializarMarcador(p);
        manager.registrarBajaYMuerte("jugador1", "jugador2", "P1");

        Scoreboard sb = manager.obtenerMarcadorActual("P1");
        assertNotNull(sb);

        ResultadoJugador res1 = sb.getClasificacion().stream()
                .filter(r -> r.getIdJugador().equals("jugador1"))
                .findFirst().orElse(null);

        assertNotNull(res1);
        assertEquals(1, res1.getBajas());
        assertEquals(100, res1.getPuntuacion());
    }

    @Test
    public void testOrdenacionYDesempate() {
        Partida p = new Partida("P2", "jugadorA");
        p.addParticipante(new Participante("jugadorA", false));
        p.addParticipante(new Participante("jugadorB", false));
        manager.inicializarMarcador(p);

        manager.registrarBajaYMuerte("jugadorA", "jugadorB", "P2");
        manager.registrarBajaYMuerte("jugadorB", "jugadorA", "P2");
        manager.registrarBajaYMuerte("jugadorB", "jugadorA", "P2");

        Scoreboard sb = manager.obtenerMarcadorActual("P2");
        assertEquals("jugadorB", sb.getClasificacion().get(0).getIdJugador());
    }

    @Test
    public void testEvitarDuplicarRecompensas() {
        Partida p = new Partida("P3", "jugador1");
        p.addParticipante(new Participante("jugador1", false));
        manager.inicializarMarcador(p);

        manager.registrarBajaYMuerte("jugador1", "bot-1", "P3");

        manager.cerrarPartidaYCalcularRecompensas("P3");
        Scoreboard sb = manager.obtenerMarcadorActual("P3");
        ResultadoJugador res = sb.getClasificacion().get(0);
        int creditosPrimeraVez = res.getCreditosGanados();

        manager.cerrarPartidaYCalcularRecompensas("P3");
        int creditosSegundaVez = res.getCreditosGanados();

        assertEquals(creditosPrimeraVez, creditosSegundaVez);
        assertTrue(sb.isProcesado());
    }
}