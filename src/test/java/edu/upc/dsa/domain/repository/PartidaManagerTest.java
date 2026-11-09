package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.EstadoPartida;
import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import edu.upc.dsa.exceptions.EstadoPartidaInvalidoException;
import edu.upc.dsa.exceptions.ParticipanteNotFoundException;
import edu.upc.dsa.exceptions.PartidaLlenaException;
import edu.upc.dsa.exceptions.PartidaNotFoundException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PartidaManagerTest {

    private PartidaManager pm;
    private long ahora;                       // reloj falso: lo adelantamos a mano
    private final List<String> eventos = new ArrayList<>();

    @Before
    public void setUp() {
        pm = PartidaManagerImpl.getInstance();
        ahora = 1_000_000L;
        pm.setReloj(() -> ahora);
        eventos.clear();
        pm.addListener(new PartidaListener() {
            @Override
            public void onBaja(Partida partida, String idAsesino, String idVictima) {
                eventos.add("baja:" + idAsesino + "->" + idVictima);
            }

            @Override
            public void onPartidaFinalizada(Partida partida) {
                eventos.add("fin:" + partida.getId());
            }
        });
    }

    @After
    public void tearDown() {
        pm.clear();
    }

    private void avanzarSegundos(long s) {
        ahora += s * 1000;
    }

    // ---------- creación y límite de participantes ----------

    @Test
    public void crearPartidaConHumanoYBots() throws Exception {
        Partida p = pm.crearPartida("franklin", 3);
        Assert.assertEquals(1, pm.size());
        Assert.assertEquals(EstadoPartida.PENDIENTE, p.getEstado());
        Assert.assertEquals(4, p.getParticipantes().size());
        Assert.assertFalse(p.getParticipantes().get(0).isBot());
        Assert.assertTrue(p.getParticipantes().get(1).isBot());
    }

    @Test
    public void crearPartidaConElMaximoDeDiezParticipantes() throws Exception {
        Partida p = pm.crearPartida("franklin", 9);
        Assert.assertEquals(Partida.MAX_PARTICIPANTES, p.getParticipantes().size());
    }

    @Test(expected = PartidaLlenaException.class)
    public void noSePuedenSuperarDiezParticipantes() throws Exception {
        pm.crearPartida("franklin", 10);
    }

    @Test(expected = DatosInvalidosException.class)
    public void crearPartidaSinHumanoFalla() throws Exception {
        pm.crearPartida("", 3);
    }

    @Test(expected = PartidaNotFoundException.class)
    public void partidaInexistente() throws Exception {
        pm.getPartida("NO_EXISTE");
    }

    // ---------- cambios de estado ----------

    @Test
    public void cicloDeEstadosCompleto() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.iniciarPartida(p.getId());
        Assert.assertEquals(EstadoPartida.EN_CURSO, p.getEstado());
        pm.finalizarPartida(p.getId());
        Assert.assertEquals(EstadoPartida.FINALIZADA, p.getEstado());
        Assert.assertEquals(1, pm.getPartidasPorEstado(EstadoPartida.FINALIZADA).size());
        Assert.assertEquals(List.of("fin:" + p.getId()), eventos);
    }

    @Test(expected = EstadoPartidaInvalidoException.class)
    public void noSePuedeIniciarDosVeces() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.iniciarPartida(p.getId());
        pm.iniciarPartida(p.getId());
    }

    @Test(expected = EstadoPartidaInvalidoException.class)
    public void noSePuedeFinalizarUnaPartidaPendiente() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.finalizarPartida(p.getId());
    }

    @Test
    public void finalizarDosVecesNoAvisaDosVeces() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.iniciarPartida(p.getId());
        pm.finalizarPartida(p.getId());
        try {
            pm.finalizarPartida(p.getId());
            Assert.fail("Debería lanzar EstadoPartidaInvalidoException");
        } catch (EstadoPartidaInvalidoException e) {
            // esperado
        }
        Assert.assertEquals(1, eventos.size());
    }

    // ---------- bajas y reapariciones ----------

    @Test
    public void bajaYReaparicion() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.iniciarPartida(p.getId());
        pm.registrarBaja(p.getId(), "franklin", "bot-1");
        Assert.assertFalse(p.buscarParticipante("bot-1").isVivo());
        Assert.assertEquals(List.of("baja:franklin->bot-1"), eventos);
        pm.reaparecer(p.getId(), "bot-1");
        Assert.assertTrue(p.buscarParticipante("bot-1").isVivo());
        // reapariciones ilimitadas: se puede volver a morir y reaparecer
        pm.registrarBaja(p.getId(), "bot-2", "bot-1");
        pm.reaparecer(p.getId(), "bot-1");
        Assert.assertEquals(2, eventos.size());
    }

    @Test(expected = EstadoPartidaInvalidoException.class)
    public void noHayBajasEnUnaPartidaPendiente() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.registrarBaja(p.getId(), "franklin", "bot-1");
    }

    @Test(expected = EstadoPartidaInvalidoException.class)
    public void noSePuedeMatarAUnMuerto() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.iniciarPartida(p.getId());
        pm.registrarBaja(p.getId(), "franklin", "bot-1");
        pm.registrarBaja(p.getId(), "bot-2", "bot-1");
    }

    @Test(expected = EstadoPartidaInvalidoException.class)
    public void unVivoNoPuedeReaparecer() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.iniciarPartida(p.getId());
        pm.reaparecer(p.getId(), "bot-1");
    }

    @Test(expected = ParticipanteNotFoundException.class)
    public void bajaDeJugadorQueNoParticipa() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.iniciarPartida(p.getId());
        pm.registrarBaja(p.getId(), "franklin", "intruso");
    }

    // ---------- control de los 5 minutos (sin esperar 5 minutos reales) ----------

    @Test
    public void tiempoRestante() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        Assert.assertEquals(300, pm.getSegundosRestantes(p.getId()));
        pm.iniciarPartida(p.getId());
        avanzarSegundos(60);
        Assert.assertEquals(240, pm.getSegundosRestantes(p.getId()));
    }

    @Test
    public void justoAntesDeLosCincoMinutosSigueEnCurso() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.iniciarPartida(p.getId());
        avanzarSegundos(299);
        Assert.assertFalse(pm.comprobarTiempo(p.getId()));
        Assert.assertEquals(EstadoPartida.EN_CURSO, p.getEstado());
    }

    @Test
    public void aLosCincoMinutosSeCierraSola() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.iniciarPartida(p.getId());
        avanzarSegundos(300);
        Assert.assertTrue(pm.comprobarTiempo(p.getId()));
        Assert.assertEquals(EstadoPartida.FINALIZADA, p.getEstado());
        Assert.assertEquals(0, pm.getSegundosRestantes(p.getId()));
        Assert.assertEquals(List.of("fin:" + p.getId()), eventos);
    }

    @Test
    public void bajaDespuesDeLosCincoMinutosSeRechaza() throws Exception {
        Partida p = pm.crearPartida("franklin", 2);
        pm.iniciarPartida(p.getId());
        avanzarSegundos(301);
        try {
            pm.registrarBaja(p.getId(), "franklin", "bot-1");
            Assert.fail("Debería lanzar EstadoPartidaInvalidoException");
        } catch (EstadoPartidaInvalidoException e) {
            // esperado: la partida se ha cerrado sola al pasar los 5 minutos
        }
        Assert.assertEquals(EstadoPartida.FINALIZADA, p.getEstado());
        Assert.assertEquals(1, eventos.size());   // solo el aviso de fin, ninguna baja
    }
}
