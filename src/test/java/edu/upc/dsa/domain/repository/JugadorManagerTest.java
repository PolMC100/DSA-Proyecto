package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Jugador;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import edu.upc.dsa.exceptions.JugadorDuplicadoException;
import edu.upc.dsa.exceptions.JugadorNotFoundException;
import edu.upc.dsa.exceptions.SaldoInsuficienteException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JugadorManagerTest {
    private JugadorManager manager;

    @Before
    public void setUp() throws Exception {
        manager = JugadorManagerImpl.getInstance();
        manager.limpiar();
        manager.registrarJugador("cristian", "Cristian", false);
    }

    @After
    public void tearDown() {
        manager.limpiar();
    }

    @Test
    public void registrarYConsultarHumanosYBots() throws Exception {
        manager.registrarJugador("bot-1", "Bot 1", true);
        Jugador jugador = manager.getJugador("cristian");
        Assert.assertEquals("cristian", jugador.getIdJugador());
        Assert.assertEquals("Cristian", jugador.getNombre());
        Assert.assertFalse(jugador.isBot());
        Assert.assertEquals(100, jugador.getVida());
        Assert.assertEquals(0, jugador.getExperiencia());
        Assert.assertEquals(0, jugador.getCreditos());
        Assert.assertEquals(1, jugador.getNivel());
        Assert.assertTrue(manager.getJugador("bot-1").isBot());
        Assert.assertEquals(2, manager.getJugadores().size());
    }

    @Test(expected = JugadorDuplicadoException.class)
    public void rechazarIdentificadorDuplicado() throws Exception {
        manager.registrarJugador("cristian", "Otro nombre", true);
    }

    @Test
    public void validarExistenciaDeHumano() throws Exception {
        manager.registrarJugador("bot-1", "Bot", true);
        Assert.assertTrue(manager.existeJugadorHumano("cristian"));
        Assert.assertFalse(manager.existeJugadorHumano("bot-1"));
        Assert.assertFalse(manager.existeJugadorHumano("no-existe"));
    }

    @Test
    public void rechazarJugadoresInexistentesEnTodasLasOperaciones() {
        Assert.assertThrows(JugadorNotFoundException.class, () -> manager.getJugador("nadie"));
        Assert.assertThrows(JugadorNotFoundException.class, () -> manager.getNivel("nadie"));
        Assert.assertThrows(JugadorNotFoundException.class, () -> manager.getCreditos("nadie"));
        Assert.assertThrows(JugadorNotFoundException.class, () -> manager.cumpleNivelRequerido("nadie", 1));
        Assert.assertThrows(JugadorNotFoundException.class, () -> manager.descontarCreditos("nadie", 0));
        Assert.assertThrows(JugadorNotFoundException.class, () -> manager.sumarExperienciaYCreditos("nadie", 1, 1));
        Assert.assertThrows(JugadorNotFoundException.class, () -> manager.aplicarRecompensaPartida("P1", "nadie", 1, 1));
    }

    @Test
    public void validarIdentificadoresYNombres() {
        for (String id : new String[]{null, "", "  "}) {
            Assert.assertThrows(DatosInvalidosException.class, () -> manager.registrarJugador(id, "Nombre", false));
            Assert.assertThrows(DatosInvalidosException.class, () -> manager.getJugador(id));
            Assert.assertThrows(DatosInvalidosException.class, () -> manager.existeJugadorHumano(id));
            Assert.assertThrows(DatosInvalidosException.class, () -> manager.aplicarRecompensaPartida(id, "cristian", 1, 1));
            Assert.assertThrows(DatosInvalidosException.class, () -> manager.registrarJugador("nuevo", id, false));
        }
        Assert.assertEquals(1, manager.getJugadores().size());
    }

    @Test
    public void acumularExperienciaYCreditos() throws Exception {
        manager.sumarExperienciaYCreditos("cristian", 40, 20);
        manager.sumarExperienciaYCreditos("cristian", 50, 30);
        Assert.assertEquals(90, manager.getJugador("cristian").getExperiencia());
        Assert.assertEquals(50, manager.getCreditos("cristian"));
        Assert.assertEquals(1, manager.getNivel("cristian"));
    }

    @Test
    public void subirJustoEnElUmbralYSaltarVariosNiveles() throws Exception {
        int paso = manager.getExperienciaPorNivel();
        manager.sumarExperienciaYCreditos("cristian", paso - 1, 0);
        Assert.assertEquals(1, manager.getNivel("cristian"));
        manager.sumarExperienciaYCreditos("cristian", 1, 0);
        Assert.assertEquals(2, manager.getNivel("cristian"));
        manager.sumarExperienciaYCreditos("cristian", 3 * paso, 0);
        Assert.assertEquals(5, manager.getNivel("cristian"));
        Assert.assertEquals(4 * paso, manager.getJugador("cristian").getExperiencia());
    }

    @Test
    public void configurarProgresionRecalculaNiveles() throws Exception {
        manager.sumarExperienciaYCreditos("cristian", 200, 0);
        manager.setExperienciaPorNivel(50);
        Assert.assertEquals(50, manager.getExperienciaPorNivel());
        Assert.assertEquals(5, manager.getNivel("cristian"));
        manager.setExperienciaPorNivel(200);
        Assert.assertEquals(2, manager.getNivel("cristian"));
    }

    @Test
    public void comprobarDesbloqueosPorNivel() throws Exception {
        Assert.assertTrue(manager.cumpleNivelRequerido("cristian", 1));
        Assert.assertFalse(manager.cumpleNivelRequerido("cristian", 2));
        manager.sumarExperienciaYCreditos("cristian", manager.getExperienciaPorNivel(), 0);
        Assert.assertTrue(manager.cumpleNivelRequerido("cristian", 2));
        Assert.assertFalse(manager.cumpleNivelRequerido("cristian", 3));
    }

    @Test
    public void descontarHastaAgotarSaldoYPermitirPrecioCero() throws Exception {
        manager.sumarExperienciaYCreditos("cristian", 0, 50);
        manager.descontarCreditos("cristian", 20);
        Assert.assertEquals(30, manager.getCreditos("cristian"));
        manager.descontarCreditos("cristian", 30);
        manager.descontarCreditos("cristian", 0);
        Assert.assertEquals(0, manager.getCreditos("cristian"));
    }

    @Test
    public void compraSinSaldoNoModificaPerfil() throws Exception {
        manager.sumarExperienciaYCreditos("cristian", 10, 20);
        Assert.assertThrows(SaldoInsuficienteException.class, () -> manager.descontarCreditos("cristian", 21));
        Assert.assertEquals(20, manager.getCreditos("cristian"));
        Assert.assertEquals(10, manager.getJugador("cristian").getExperiencia());
    }

    @Test
    public void rechazarValoresNegativosYConfiguracionInvalida() throws Exception {
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.sumarExperienciaYCreditos("cristian", -1, 10));
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.sumarExperienciaYCreditos("cristian", 10, -1));
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.descontarCreditos("cristian", -1));
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.cumpleNivelRequerido("cristian", 0));
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.setExperienciaPorNivel(0));
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.setExperienciaPorNivel(-1));
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.aplicarRecompensaPartida("P1", "cristian", -1, 0));
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.aplicarRecompensaPartida("P1", "cristian", 0, -1));
        Assert.assertEquals(0, manager.getCreditos("cristian"));
        Assert.assertEquals(0, manager.getJugador("cristian").getExperiencia());
    }

    @Test
    public void recompensaUnicaPorJugadorYPartida() throws Exception {
        manager.registrarJugador("otro", "Otro", false);
        Assert.assertTrue(manager.aplicarRecompensaPartida("P1", "cristian", 100, 60));
        Assert.assertFalse(manager.aplicarRecompensaPartida("P1", "cristian", 999, 999));
        Assert.assertTrue(manager.aplicarRecompensaPartida("P1", "otro", 100, 60));
        Assert.assertTrue(manager.aplicarRecompensaPartida("P2", "cristian", 50, 20));
        Assert.assertEquals(150, manager.getJugador("cristian").getExperiencia());
        Assert.assertEquals(80, manager.getCreditos("cristian"));
        Assert.assertEquals(60, manager.getCreditos("otro"));
    }

    @Test
    public void recompensaFallidaSePuedeReintentar() throws Exception {
        Assert.assertThrows(JugadorNotFoundException.class, () -> manager.aplicarRecompensaPartida("P1", "otro", 1, 1));
        manager.registrarJugador("otro", "Otro", false);
        Assert.assertTrue(manager.aplicarRecompensaPartida("P1", "otro", 1, 1));
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.aplicarRecompensaPartida("P2", "otro", -1, 1));
        Assert.assertTrue(manager.aplicarRecompensaPartida("P2", "otro", 1, 1));
    }

    @Test
    public void evitarDesbordamientoSinCambiosParciales() throws Exception {
        manager.sumarExperienciaYCreditos("cristian", 0, Integer.MAX_VALUE);
        Assert.assertThrows(DatosInvalidosException.class,
                () -> manager.aplicarRecompensaPartida("P1", "cristian", 100, 1));
        Assert.assertEquals(0, manager.getJugador("cristian").getExperiencia());
        manager.descontarCreditos("cristian", 1);
        Assert.assertTrue(manager.aplicarRecompensaPartida("P1", "cristian", 100, 1));
        manager.sumarExperienciaYCreditos("cristian", Integer.MAX_VALUE - 100, 0);
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.sumarExperienciaYCreditos("cristian", 1, 0));
        int nivel = manager.getNivel("cristian");
        Assert.assertThrows(DatosInvalidosException.class, () -> manager.setExperienciaPorNivel(1));
        Assert.assertEquals(nivel, manager.getNivel("cristian"));
        Assert.assertEquals(100, manager.getExperienciaPorNivel());
    }

    @Test
    public void consultasYAltasNoExponenEstadoInterno() throws Exception {
        Jugador alta = manager.registrarJugador("otro", "Otro", false);
        alta.setCreditos(-100);
        alta.setIdJugador("cambiado");
        Jugador consulta = manager.getJugador("cristian");
        consulta.setCreditos(-1);
        consulta.setNivel(999);
        List<Jugador> lista = manager.getJugadores();
        for (Jugador jugador : lista) jugador.setExperiencia(-1);
        lista.clear();
        Assert.assertEquals(2, manager.getJugadores().size());
        Assert.assertEquals(0, manager.getCreditos("otro"));
        Assert.assertEquals(0, manager.getCreditos("cristian"));
        Assert.assertEquals(0, manager.getJugador("cristian").getExperiencia());
        Assert.assertEquals(1, manager.getNivel("cristian"));
    }

    @Test
    public void singletonYLimpiezaCompleta() throws Exception {
        Assert.assertSame(manager, JugadorManagerImpl.getInstance());
        manager.setExperienciaPorNivel(50);
        manager.aplicarRecompensaPartida("P1", "cristian", 100, 50);
        manager.limpiar();
        Assert.assertTrue(manager.getJugadores().isEmpty());
        Assert.assertEquals(100, manager.getExperienciaPorNivel());
        manager.registrarJugador("cristian", "Cristian", false);
        Assert.assertTrue(manager.aplicarRecompensaPartida("P1", "cristian", 100, 50));
    }

    @Test
    public void comprasSimultaneasNoDejanSaldoNegativo() throws Exception {
        manager.sumarExperienciaYCreditos("cristian", 0, 50);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> compra = () -> {
            try {
                manager.descontarCreditos("cristian", 40);
                return true;
            } catch (SaldoInsuficienteException e) {
                return false;
            }
        };
        try {
            List<Future<Boolean>> resultados = executor.invokeAll(List.of(compra, compra));
            Assert.assertNotEquals(resultados.get(0).get(), resultados.get(1).get());
            Assert.assertEquals(10, manager.getCreditos("cristian"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void recompensasSimultaneasSoloSeAplicanUnaVez() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> recompensa = () -> manager.aplicarRecompensaPartida("P1", "cristian", 100, 60);
        try {
            List<Future<Boolean>> resultados = executor.invokeAll(List.of(recompensa, recompensa));
            Assert.assertNotEquals(resultados.get(0).get(), resultados.get(1).get());
            Assert.assertEquals(100, manager.getJugador("cristian").getExperiencia());
            Assert.assertEquals(60, manager.getCreditos("cristian"));
        } finally {
            executor.shutdownNow();
        }
    }
}
