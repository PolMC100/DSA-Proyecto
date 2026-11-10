package edu.upc.dsa.domain.models;

import edu.upc.dsa.domain.repository.JugadorManager;
import edu.upc.dsa.domain.repository.JugadorManagerImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Compatibilidad del modelo común, sin depender del Singleton del catálogo. */
public class JugadorIntegracionTest {
    private Jugador jugador;
    private Arma arma;
    private Granada granada;
    private Complemento complemento;

    @Before
    public void setUp() {
        jugador = new Jugador("J1", "Cristian", false);
        arma = new Arma("Pistola", 15, 3, 25, 300, 0);
        granada = new Granada("Humo", 0, 8.0, "Humo", 15, 400, 0);
        complemento = new Complemento("Chaleco", "Defensa", "Protección", 700, 2);
        jugador.getInventario().addArma(arma);
        jugador.getInventario().addGranada(granada, 2);
        jugador.getInventario().addComplemento(complemento);
    }

    @After
    public void tearDown() {
        JugadorManagerImpl.getInstance().limpiar();
    }

    @Test
    public void identificadoresSonAliasEnAmbosSentidos() {
        Assert.assertEquals("J1", jugador.getId());
        jugador.setId("J2");
        Assert.assertEquals("J2", jugador.getIdJugador());
        jugador.setIdJugador("J3");
        Assert.assertEquals("J3", jugador.getId());
    }

    @Test
    public void saldoYCreditosSonElMismoDatoSinRedondeos() {
        Jugador tienda = new Jugador("J2", "Jugador", 500.0, 0);
        Assert.assertEquals(500, tienda.getCreditos());
        Assert.assertEquals(0, tienda.getNivel());
        tienda.setCreditos(200);
        Assert.assertEquals(200.0, tienda.getSaldo(), 0.0);
        tienda.setSaldo(100.0);
        Assert.assertEquals(100, tienda.getCreditos());
        for (double saldo : new double[]{0.5, Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, (double) Integer.MAX_VALUE + 1}) {
            Assert.assertThrows(IllegalArgumentException.class, () -> tienda.setSaldo(saldo));
            Assert.assertEquals(100, tienda.getCreditos());
        }
        tienda.setSaldo(Integer.MAX_VALUE);
        Assert.assertEquals(Integer.MAX_VALUE, tienda.getCreditos());
        Jugador copia = new Jugador(tienda);
        Assert.assertEquals(tienda.getSaldo(), copia.getSaldo(), 0.0);
    }

    @Test
    public void todosLosConstructoresInicializanInventario() {
        Assert.assertNotNull(new Jugador().getInventario());
        Assert.assertNotNull(new Jugador("bot-1", "Bot", true).getInventario());
        Assert.assertNotNull(new Jugador("J2", "Humano", 500, 3).getInventario());
    }

    @Test
    public void copiaConservaProgresionYSeparaColeccionesDelInventario() {
        jugador.setExperiencia(200);
        jugador.setNivel(3);
        jugador.setCreditos(500);
        jugador.setVida(80);
        Jugador copia = new Jugador(jugador);
        Assert.assertEquals(jugador.getIdJugador(), copia.getIdJugador());
        Assert.assertEquals(jugador.getNombre(), copia.getNombre());
        Assert.assertEquals(200, copia.getExperiencia());
        Assert.assertEquals(3, copia.getNivel());
        Assert.assertEquals(500, copia.getCreditos());
        Assert.assertEquals(80, copia.getVida());
        Assert.assertFalse(copia.isBot());
        Assert.assertNotSame(jugador.getInventario(), copia.getInventario());
        Assert.assertTrue(copia.getInventario().getArmas().contains(arma));
        Assert.assertTrue(copia.getInventario().getComplementos().contains(complemento));
        Assert.assertEquals(Integer.valueOf(2), copia.getInventario().checkGranadasQty(granada));
        copia.getInventario().getArmas().clear();
        copia.getInventario().getComplementos().clear();
        copia.getInventario().useGranada(granada);
        Assert.assertEquals(1, jugador.getInventario().getArmas().size());
        Assert.assertEquals(1, jugador.getInventario().getComplementos().size());
        Assert.assertEquals(Integer.valueOf(2), jugador.getInventario().checkGranadasQty(granada));
        jugador.getInventario().getGranadas().clear();
        Assert.assertEquals(Integer.valueOf(1), copia.getInventario().checkGranadasQty(granada));
    }

    @Test
    public void accesoresDeInventarioConservanMutabilidadEsperadaPorTienda() {
        Inventario inventario = new Inventario();
        jugador.setInventario(inventario);
        Assert.assertSame(inventario, jugador.getInventario());
        jugador.getInventario().addGranada(granada, 2);
        Assert.assertEquals(Integer.valueOf(2), inventario.checkGranadasQty(granada));
        jugador.setInventario(null);
        Assert.assertNull(new Jugador(jugador).getInventario());
    }

    @Test
    public void consultasDelGestorNoExponenColeccionesInternas() throws Exception {
        JugadorManager manager = JugadorManagerImpl.getInstance();
        manager.limpiar();
        Jugador alta = manager.registrarJugador("J1", "Cristian", false);
        alta.getInventario().addArma(arma);
        Jugador consulta = manager.getJugador("J1");
        Assert.assertTrue(consulta.getInventario().getArmas().isEmpty());
        consulta.getInventario().addGranada(granada, 2);
        manager.getJugadores().get(0).getInventario().addComplemento(complemento);
        Assert.assertTrue(manager.getJugador("J1").getInventario().getGranadas().isEmpty());
        Assert.assertTrue(manager.getJugador("J1").getInventario().getComplementos().isEmpty());
    }
}
