package edu.upc.dsa;

import edu.upc.dsa.domain.models.*;
import edu.upc.dsa.domain.models.Partida.Estado;
import edu.upc.dsa.domain.repository.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.experimental.runners.Enclosed;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class JuegoManagerTest {

    public static class Jugadores {
        private JuegoManager manager;

        @Before
        public void setUp() throws Exception {
            manager = JuegoManagerImpl.getInstance();
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

        @Test
        public void credencialesDelJugadorSeConservanSinExponerElEstadoInterno() {
            Jugador registrado = manager.registrarJugador(" Tester ", "password123");
            String id = registrado.getIdJugador();
            Assert.assertEquals("tester", registrado.getUsuario());
            Assert.assertEquals("password123", manager.getJugador(id).getPassword());
            Assert.assertTrue(manager.getJugadores().stream()
                    .anyMatch(jugador -> "tester".equals(jugador.getUsuario())));
            registrado.setUsuario("otro");
            registrado.setPassword("otraPassword");
            Assert.assertEquals(id, manager.autenticarJugador("TESTER", "password123").getIdJugador());
            Assert.assertThrows(SecurityException.class, () -> manager.autenticarJugador("tester", "otraPassword"));
            Assert.assertThrows(SecurityException.class, () -> manager.autenticarJugador("otro", "otraPassword"));
            Assert.assertNull(manager.getJugador("cristian").getPassword());
        }

        @Test(expected = IllegalStateException.class)
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
            Assert.assertThrows(NoSuchElementException.class, () -> manager.getJugador("nadie"));
            Assert.assertThrows(NoSuchElementException.class, () -> manager.getNivel("nadie"));
            Assert.assertThrows(NoSuchElementException.class, () -> manager.getCreditos("nadie"));
            Assert.assertThrows(NoSuchElementException.class, () -> manager.cumpleNivelRequerido("nadie", 1));
            Assert.assertThrows(NoSuchElementException.class, () -> manager.descontarCreditos("nadie", 0));
            Assert.assertThrows(NoSuchElementException.class, () -> manager.sumarExperienciaYCreditos("nadie", 1, 1));
            Assert.assertThrows(NoSuchElementException.class, () -> manager.aplicarRecompensaPartida("P1", "nadie", 1, 1));
        }

        @Test
        public void validarIdentificadoresYNombres() {
            for (String id : new String[]{null, "", "  "}) {
                Assert.assertThrows(IllegalArgumentException.class, () -> manager.registrarJugador(id, "Nombre", false));
                Assert.assertThrows(IllegalArgumentException.class, () -> manager.getJugador(id));
                Assert.assertThrows(IllegalArgumentException.class, () -> manager.existeJugadorHumano(id));
                Assert.assertThrows(IllegalArgumentException.class, () -> manager.aplicarRecompensaPartida(id, "cristian", 1, 1));
                Assert.assertThrows(IllegalArgumentException.class, () -> manager.registrarJugador("nuevo", id, false));
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
            Assert.assertThrows(SecurityException.class, () -> manager.descontarCreditos("cristian", 21));
            Assert.assertEquals(20, manager.getCreditos("cristian"));
            Assert.assertEquals(10, manager.getJugador("cristian").getExperiencia());
        }

        @Test
        public void rechazarValoresNegativosYConfiguracionInvalida() throws Exception {
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.sumarExperienciaYCreditos("cristian", -1, 10));
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.sumarExperienciaYCreditos("cristian", 10, -1));
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.descontarCreditos("cristian", -1));
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.cumpleNivelRequerido("cristian", -1));
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.setExperienciaPorNivel(0));
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.setExperienciaPorNivel(-1));
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.aplicarRecompensaPartida("P1", "cristian", -1, 0));
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.aplicarRecompensaPartida("P1", "cristian", 0, -1));
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
            Assert.assertThrows(NoSuchElementException.class, () -> manager.aplicarRecompensaPartida("P1", "otro", 1, 1));
            manager.registrarJugador("otro", "Otro", false);
            Assert.assertTrue(manager.aplicarRecompensaPartida("P1", "otro", 1, 1));
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.aplicarRecompensaPartida("P2", "otro", -1, 1));
            Assert.assertTrue(manager.aplicarRecompensaPartida("P2", "otro", 1, 1));
        }

        @Test
        public void evitarDesbordamientoSinCambiosParciales() throws Exception {
            manager.sumarExperienciaYCreditos("cristian", 0, Integer.MAX_VALUE);
            Assert.assertThrows(IllegalArgumentException.class,
                    () -> manager.aplicarRecompensaPartida("P1", "cristian", 100, 1));
            Assert.assertEquals(0, manager.getJugador("cristian").getExperiencia());
            manager.descontarCreditos("cristian", 1);
            Assert.assertTrue(manager.aplicarRecompensaPartida("P1", "cristian", 100, 1));
            manager.sumarExperienciaYCreditos("cristian", Integer.MAX_VALUE - 100, 0);
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.sumarExperienciaYCreditos("cristian", 1, 0));
            int nivel = manager.getNivel("cristian");
            Assert.assertThrows(IllegalArgumentException.class, () -> manager.setExperienciaPorNivel(1));
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
            Assert.assertSame(manager, JuegoManagerImpl.getInstance());
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
                } catch (SecurityException e) {
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

    public static class Catalogo {
        private JuegoManager cm;

        @Before
        public void setUp() {
            cm = new JuegoManagerImpl();
        }

        @After
        public void tearDown() {
            cm.limpiar();
        }

        @Test
        public void testCargarCatalogoInicial() {
            Assert.assertEquals(5, cm.getArmas().size());
            Assert.assertEquals(3, cm.getGranadas().size());
            Assert.assertEquals(3, cm.getComplementos().size());
        }

        @Test
        public void testAdd() {
            Arma arma = new Arma("ExampleArma", 15, 3, 25, 300, 0);
            cm.addObjeto(arma);
            Assert.assertEquals(6, cm.getArmas().size());
            Assert.assertTrue(cm.getArmas().containsKey(arma.getNombre()));

            Granada granada = new Granada("ExampleGranada", 0, 5.0, "", 15, 400, 0);
            cm.addObjeto(granada);
            Assert.assertEquals(4, cm.getGranadas().size());
            Assert.assertTrue(cm.getGranadas().containsKey(granada.getNombre()));

            Complemento complemento = new Complemento("ExampleComplemento", "", "", 2, 0);
            cm.addObjeto(complemento);
            Assert.assertEquals(4, cm.getComplementos().size());
            Assert.assertTrue(cm.getComplementos().containsKey(complemento.getNombre()));
        }
    }

    public static class Compras {
        private JuegoManager catalogo;
        private JuegoManager jugadores;
        private JuegoManager tienda;

        @Before
        public void setUp() throws Exception {
            catalogo = new JuegoManagerImpl();
            jugadores = catalogo;
            jugadores.registrarJugador("J1", "Jugador", false);
            jugadores.sumarExperienciaYCreditos("J1", 500, 3000);
            tienda = catalogo;
        }

        @Test
        public void comprarGuardaSaldoEInventarioYDevuelveUnaCopia() throws Exception {
            Jugador compra = tienda.comprar("J1", "Pistola", 1);
            assertEquals(2700, compra.getCreditos());
            compra.setCreditos(9999);
            compra.getInventario().getArmas().get(0).setPrecio(0);
            compra.getInventario().getArmas().clear();
            Jugador guardado = jugadores.getJugador("J1");
            assertEquals(2700, guardado.getCreditos());
            assertEquals(1, guardado.getInventario().getArmas().size());
            assertEquals(Integer.valueOf(300), guardado.getInventario().getArmas().get(0).getPrecio());
        }

        @Test
        public void granadasSeIdentificanPorNombreYSeConsumenSinNegativos() throws Exception {
            tienda.comprar("J1", "Humo", 2);
            assertEquals(Integer.valueOf(2), jugadores.getJugador("J1").getInventario()
                    .checkGranadasQty(new Granada("Humo", 0, 1.0, "", 1, 1, 0)));
            assertTrue(jugadores.consumirGranada("J1", "Humo"));
            assertTrue(jugadores.consumirGranada("J1", "Humo"));
            assertFalse(jugadores.consumirGranada("J1", "Humo"));
            assertFalse(jugadores.consumirGranada("J1", "Flash"));
            assertEquals(Integer.valueOf(0), jugadores.getJugador("J1").getInventario().getGranadas().get("Humo"));
        }

        @Test
        public void cantidadesInvalidasYDesbordamientosNoModificanElPerfil() throws Exception {
            for (int cantidad : new int[]{-2, 0, Integer.MAX_VALUE}) {
                assertThrows(IllegalArgumentException.class, () -> tienda.comprar("J1", "Humo", cantidad));
            }
            assertThrows(IllegalArgumentException.class, () -> tienda.comprar("J1", "Pistola", 2));
            assertEquals(3000, jugadores.getCreditos("J1"));
            assertTrue(jugadores.getJugador("J1").getInventario().getGranadas().isEmpty());
        }

        @Test
        public void seRechazanDuplicadosSinCobrarOtraVez() throws Exception {
            tienda.comprar("J1", "Pistola", 1);
            // Una consulta nueva del catálogo devuelve otra instancia del mismo objeto.
            assertThrows(IllegalStateException.class, () -> tienda.comprar("J1", "Pistola", 1));
            tienda.comprar("J1", "Chaleco", 1);
            assertThrows(IllegalStateException.class, () -> tienda.comprar("J1", "Chaleco", 1));
            assertEquals(2000, jugadores.getCreditos("J1"));
        }

        @Test
        public void saldoNivelYRecursosInexistentesSeValidan() throws Exception {
            jugadores.registrarJugador("J2", "Nuevo", false);
            assertThrows(SecurityException.class, () -> tienda.comprar("J2", "Jetpack", 1));
            assertThrows(SecurityException.class, () -> tienda.comprar("J2", "Pistola", 1));
            assertThrows(NoSuchElementException.class, () -> tienda.comprar("nadie", "Pistola", 1));
            assertThrows(NoSuchElementException.class, () -> tienda.comprar("J1", "No existe", 1));
            assertTrue(jugadores.cumpleNivelRequerido("J2", 0));
            assertTrue(jugadores.getJugador("J2").getInventario().getArmas().isEmpty());
        }

        @Test
        public void consultasNoPermitenCambiarPreciosNiVaciarElCatalogo() throws Exception {
            tienda.getArmas().get("Pistola").setPrecio(0);
            tienda.getArmas().clear();
            tienda.comprar("J1", "Pistola", 1);
            assertEquals(2700, jugadores.getCreditos("J1"));
            assertEquals(5, tienda.getArmas().size());
        }

        @Test
        public void comprasSimultaneasNoDuplicanArmasNiDescuentos() throws Exception {
            Callable<Boolean> comprar = () -> {
                try { tienda.comprar("J1", "Pistola", 1); return true; }
                catch (IllegalStateException e) { return false; }
            };
            try (var executor = Executors.newFixedThreadPool(2)) {
                var resultados = executor.invokeAll(List.of(comprar, comprar));
                assertNotEquals(resultados.get(0).get(), resultados.get(1).get());
            }
            assertEquals(2700, jugadores.getCreditos("J1"));
            assertEquals(1, jugadores.getJugador("J1").getInventario().getArmas().size());
        }

        @Test
        public void comprasSimultaneasNoSuperanElSaldoDisponible() throws Exception {
            jugadores.descontarCreditos("J1", 2500);
            Callable<Boolean> comprar = () -> {
                try { tienda.comprar("J1", "Fragmentación", 1); return true; }
                catch (SecurityException e) { return false; }
            };
            try (var executor = Executors.newFixedThreadPool(2)) {
                var resultados = executor.invokeAll(List.of(comprar, comprar));
                assertNotEquals(resultados.get(0).get(), resultados.get(1).get());
            }
            assertEquals(0, jugadores.getCreditos("J1"));
            assertEquals(Integer.valueOf(1), jugadores.getJugador("J1").getInventario().getGranadas().get("Fragmentación"));
        }

        @Test
        public void desbordarLasUnidadesNoDejaUnaCompraParcial() throws Exception {
            catalogo.addObjeto(new Granada("Gratis", 1, 1.0, "", 0, 0, 0));
            tienda.comprar("J1", "Gratis", Integer.MAX_VALUE);
            assertThrows(IllegalArgumentException.class, () -> tienda.comprar("J1", "Gratis", 1));
            assertEquals(Integer.valueOf(Integer.MAX_VALUE), jugadores.getJugador("J1").getInventario().getGranadas().get("Gratis"));
            assertEquals(3000, jugadores.getCreditos("J1"));
        }
    }

    public static class Partidas {

        private JuegoManager pm;
        private long ahora;                       // reloj falso: lo adelantamos a mano

        @Before
        public void setUp() {
            ahora = 1_000_000L;
            pm = new JuegoManagerImpl(() -> ahora);
            pm.registrarJugador("franklin", "Franklin", false);
            pm.registrarJugador("robot", "Robot", true);
        }

        @After
        public void tearDown() { pm.limpiar(); }

        private void avanzarSegundos(long s) {
            ahora += s * 1000;
        }

        // ---------- creación y límite de participantes ----------

        @Test
        public void crearPartidaConHumanoYBots() throws Exception {
            Partida p = pm.crearPartida("franklin", 3);
            Assert.assertEquals(1, pm.getPartidas().size());
            Assert.assertEquals(Estado.PENDIENTE, pm.getPartida(p.getId()).getEstado());
            Assert.assertEquals(4, p.getParticipantes().size());
            Assert.assertFalse(p.getParticipantes().get(0).isBot());
            Assert.assertTrue(p.getParticipantes().get(1).isBot());
        }

        @Test
        public void crearPartidaConElMaximoDeDiezParticipantes() throws Exception {
            Partida p = pm.crearPartida("franklin", 9);
            Assert.assertEquals(Partida.MAX_PARTICIPANTES, p.getParticipantes().size());
        }

        @Test(expected = IllegalStateException.class)
        public void noSePuedenSuperarDiezParticipantes() throws Exception {
            pm.crearPartida("franklin", 10);
        }

        @Test(expected = IllegalArgumentException.class)
        public void crearPartidaSinHumanoFalla() throws Exception {
            pm.crearPartida("", 3);
        }

        @Test(expected = NoSuchElementException.class)
        public void partidaInexistente() throws Exception {
            pm.getPartida("NO_EXISTE");
        }

        // ---------- cambios de estado ----------

        @Test
        public void cicloDeEstadosCompleto() throws Exception {
            Partida p = pm.crearPartida("franklin", 2);
            pm.iniciarPartida(p.getId());
            Assert.assertEquals(Estado.EN_CURSO, pm.getPartida(p.getId()).getEstado());
            pm.finalizarPartida(p.getId());
            Assert.assertEquals(Estado.FINALIZADA, pm.getPartida(p.getId()).getEstado());
            Assert.assertEquals(1, pm.getPartidasPorEstado(Estado.FINALIZADA).size());
            Assert.assertTrue(pm.getScoreboard(p.getId()).isProcesado());
        }

        @Test(expected = IllegalStateException.class)
        public void noSePuedeIniciarDosVeces() throws Exception {
            Partida p = pm.crearPartida("franklin", 2);
            pm.iniciarPartida(p.getId());
            pm.iniciarPartida(p.getId());
        }

        @Test(expected = IllegalStateException.class)
        public void noSePuedeFinalizarUnaPartidaPendiente() throws Exception {
            Partida p = pm.crearPartida("franklin", 2);
            pm.finalizarPartida(p.getId());
        }

        @Test
        public void finalizarDosVecesNoDuplicaRecompensas() throws Exception {
            Partida p = pm.crearPartida("franklin", 2);
            pm.iniciarPartida(p.getId());
            pm.finalizarPartida(p.getId());
            try {
                pm.finalizarPartida(p.getId());
                Assert.fail("Debería lanzar IllegalStateException");
            } catch (IllegalStateException e) {
                // esperado
            }
            Assert.assertEquals(50, pm.getCreditos("franklin"));
        }

        // ---------- bajas y reapariciones ----------

        @Test
        public void bajaYReaparicion() throws Exception {
            Partida p = pm.crearPartida("franklin", 2);
            pm.iniciarPartida(p.getId());
            pm.registrarBaja(p.getId(), "franklin", "bot-1");
            Assert.assertFalse(pm.getPartida(p.getId()).buscarParticipante("bot-1").isVivo());
            Assert.assertEquals(1, pm.getScoreboard(p.getId()).getClasificacion().get(0).getBajas());
            pm.reaparecer(p.getId(), "bot-1");
            Assert.assertTrue(pm.getPartida(p.getId()).buscarParticipante("bot-1").isVivo());
            // reapariciones ilimitadas: se puede volver a morir y reaparecer
            pm.registrarBaja(p.getId(), "bot-2", "bot-1");
            pm.reaparecer(p.getId(), "bot-1");
            Assert.assertEquals(2, pm.getScoreboard(p.getId()).getClasificacion().stream().mapToInt(ResultadoJugador::getBajas).sum());
        }

        @Test(expected = IllegalStateException.class)
        public void noHayBajasEnUnaPartidaPendiente() throws Exception {
            Partida p = pm.crearPartida("franklin", 2);
            pm.registrarBaja(p.getId(), "franklin", "bot-1");
        }

        @Test(expected = IllegalStateException.class)
        public void noSePuedeMatarAUnMuerto() throws Exception {
            Partida p = pm.crearPartida("franklin", 2);
            pm.iniciarPartida(p.getId());
            pm.registrarBaja(p.getId(), "franklin", "bot-1");
            pm.registrarBaja(p.getId(), "bot-2", "bot-1");
        }

        @Test(expected = IllegalStateException.class)
        public void unVivoNoPuedeReaparecer() throws Exception {
            Partida p = pm.crearPartida("franklin", 2);
            pm.iniciarPartida(p.getId());
            pm.reaparecer(p.getId(), "bot-1");
        }

        @Test(expected = NoSuchElementException.class)
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
            Assert.assertFalse(pm.getPartida(p.getId()).getEstado() == Estado.FINALIZADA);
            Assert.assertEquals(Estado.EN_CURSO, pm.getPartida(p.getId()).getEstado());
        }

        @Test
        public void aLosCincoMinutosSeCierraSola() throws Exception {
            Partida p = pm.crearPartida("franklin", 2);
            pm.iniciarPartida(p.getId());
            avanzarSegundos(300);
            Assert.assertTrue(pm.getPartida(p.getId()).getEstado() == Estado.FINALIZADA);
            Assert.assertEquals(Estado.FINALIZADA, pm.getPartida(p.getId()).getEstado());
            Assert.assertEquals(0, pm.getSegundosRestantes(p.getId()));
            Assert.assertTrue(pm.getScoreboard(p.getId()).isProcesado());
        }

        @Test
        public void bajaDespuesDeLosCincoMinutosSeRechaza() throws Exception {
            Partida p = pm.crearPartida("franklin", 2);
            pm.iniciarPartida(p.getId());
            avanzarSegundos(301);
            try {
                pm.registrarBaja(p.getId(), "franklin", "bot-1");
                Assert.fail("Debería lanzar IllegalStateException");
            } catch (IllegalStateException e) {
                // esperado: la partida se ha cerrado sola al pasar los 5 minutos
            }
            Assert.assertEquals(Estado.FINALIZADA, pm.getPartida(p.getId()).getEstado());
            Assert.assertEquals(50, pm.getCreditos("franklin"));   // solo la recompensa base, ninguna baja
        }

        // ---------- integración entre partidas, perfiles y resultados ----------

        @Test
        public void consultasNoPermitenSaltarEstadosNiCambiarParticipantes() throws Exception {
            Partida copia = pm.crearPartida("franklin", 1);
            copia.setEstado(Estado.FINALIZADA);
            copia.getParticipantes().clear();
            Assert.assertEquals(Estado.PENDIENTE, pm.getPartida(copia.getId()).getEstado());
            Assert.assertEquals(2, pm.getPartida(copia.getId()).getParticipantes().size());
        }

        @Test
        public void listarSinFiltroTambienCierraLasPartidasCaducadas() throws Exception {
            Partida partida = pm.crearPartida("franklin", 0);
            pm.iniciarPartida(partida.getId());
            avanzarSegundos(300);
            Assert.assertEquals(Estado.FINALIZADA, pm.getPartidas().get(0).getEstado());
            Assert.assertEquals(50, pm.getCreditos("franklin"));
        }

        @Test
        public void limiteDeBotsNoSePuedeEvitarConUnDesbordamiento() {
            Assert.assertThrows(IllegalStateException.class, () -> pm.crearPartida("franklin", Integer.MAX_VALUE));
        }

        @Test
        public void humanoConNombreDeBotNoDuplicaIdentificadores() throws Exception {
            pm.registrarJugador("bot-1", "Humano", false);
            Partida partida = pm.crearPartida("bot-1", 2);
            Assert.assertFalse(partida.buscarParticipante("bot-1").isBot());
            Assert.assertTrue(partida.buscarParticipante("bot-2").isBot());
            Assert.assertTrue(partida.buscarParticipante("bot-3").isBot());
        }

        @Test(expected = NoSuchElementException.class)
        public void crearPartidaConHumanoQueNoExisteFalla() throws Exception {
            pm.crearPartida("fantasma", 3);
        }

        @Test(expected = NoSuchElementException.class)
        public void unBotNoPuedeSerElHumanoDeLaPartida() throws Exception {
            pm.crearPartida("robot", 3);
        }

        @Test
        public void unaBajaActualizaUnaVezElMarcadorYLaRecompensa() {
            Partida p = pm.crearPartida("franklin", 1);
            pm.iniciarPartida(p.getId());
            pm.registrarBaja(p.getId(), "franklin", "bot-1");
            pm.finalizarPartida(p.getId());
            pm.aplicarRecompensas(p.getId());
            Assert.assertEquals(1, pm.getScoreboard(p.getId()).getClasificacion().get(0).getBajas());
            Assert.assertEquals(60, pm.getCreditos("franklin"));
        }

        @Test
        public void partidasActualizanResultadosSinObservador() {
            Partida p = pm.crearPartida("franklin", 2);
            pm.iniciarPartida(p.getId());
            pm.registrarBaja(p.getId(), "franklin", "bot-1");
            pm.reaparecer(p.getId(), "bot-1");
            pm.registrarBaja(p.getId(), "franklin", "bot-1");
            pm.finalizarPartida(p.getId());
            Scoreboard marcador = pm.getScoreboard(p.getId());
            Assert.assertEquals("franklin", marcador.getClasificacion().get(0).getIdJugador());
            Assert.assertEquals(2, marcador.getClasificacion().get(0).getBajas());
            Assert.assertTrue(marcador.isProcesado());
            Assert.assertEquals(70, pm.getCreditos("franklin"));
        }
    }

    public static class Resultados {
        private JuegoManager juego;
        private Partida partida;

        @Before
        public void setUp() {
            juego = new JuegoManagerImpl();
            juego.registrarJugador("A", "Jugador A", false);
            partida = juego.crearPartida("A", 2);
            juego.iniciarPartida(partida.getId());
        }

        @Test
        public void registraBajasMuertesYOrdenaElMarcador() {
            juego.registrarBaja(partida.getId(), "bot-1", "A");
            juego.reaparecer(partida.getId(), "A");
            juego.registrarBaja(partida.getId(), "bot-1", "A");
            Scoreboard marcador = juego.getScoreboard(partida.getId());
            assertEquals("bot-1", marcador.getClasificacion().get(0).getIdJugador());
            assertEquals(200, marcador.getClasificacion().get(0).getPuntuacion());
            assertEquals(2, marcador.getClasificacion().stream().filter(r -> r.getIdJugador().equals("A")).findFirst().orElseThrow().getMuertes());
        }

        @Test
        public void elDesempateFavoreceMenosMuertes() {
            juego.registrarBaja(partida.getId(), "A", "bot-2");
            juego.registrarBaja(partida.getId(), "bot-1", "A");
            assertEquals("bot-1", juego.getScoreboard(partida.getId()).getClasificacion().get(0).getIdJugador());
        }

        @Test
        public void reintentarNoDuplicaRecompensasNiBuscaPerfilesDeBots() {
            juego.registrarBaja(partida.getId(), "A", "bot-1");
            juego.finalizarPartida(partida.getId());
            juego.aplicarRecompensas(partida.getId());
            assertEquals(60, juego.getCreditos("A"));
            assertEquals(100, juego.getJugador("A").getExperiencia());
            assertTrue(juego.getScoreboard(partida.getId()).isProcesado());
        }

        @Test
        public void pagoFallidoSePuedeReintentar() {
            juego.sumarExperienciaYCreditos("A", 0, Integer.MAX_VALUE);
            juego.finalizarPartida(partida.getId());
            assertFalse(juego.getScoreboard(partida.getId()).isProcesado());
            juego.descontarCreditos("A", 50);
            juego.aplicarRecompensas(partida.getId());
            assertEquals(Integer.MAX_VALUE, juego.getCreditos("A"));
            assertTrue(juego.getScoreboard(partida.getId()).isProcesado());
        }

        @Test
        public void noSePagaAntesDelFinalNiSeActualizanResultadosDespues() {
            assertThrows(IllegalStateException.class, () -> juego.aplicarRecompensas(partida.getId()));
            juego.finalizarPartida(partida.getId());
            assertThrows(IllegalStateException.class, () -> juego.registrarBaja(partida.getId(), "A", "bot-1"));
        }

        @Test
        public void unaConsultaNoPermiteAlterarLasRecompensas() {
            juego.getScoreboard(partida.getId()).getClasificacion().get(0).setPuntuacion(9999);
            juego.getScoreboard(partida.getId()).setProcesado(true);
            juego.finalizarPartida(partida.getId());
            assertEquals(0, juego.getJugador("A").getExperiencia());
            assertEquals(50, juego.getCreditos("A"));
        }

        @Test
        public void reintentosSimultaneosPaganUnaSolaVez() throws Exception {
            juego.sumarExperienciaYCreditos("A", 0, Integer.MAX_VALUE);
            juego.finalizarPartida(partida.getId());
            juego.descontarCreditos("A", 50);
            Callable<Void> pagar = () -> { juego.aplicarRecompensas(partida.getId()); return null; };
            try (var executor = Executors.newFixedThreadPool(2)) {
                for (var resultado : executor.invokeAll(List.of(pagar, pagar))) resultado.get();
            }
            assertEquals(Integer.MAX_VALUE, juego.getCreditos("A"));
        }

        @Test
        public void partidaSinBajasTieneMarcadorYRecompensaBase() {
            assertNotNull(juego.getScoreboard(partida.getId()));
            juego.finalizarPartida(partida.getId());
            assertTrue(juego.getScoreboard(partida.getId()).isProcesado());
            assertEquals(50, juego.getCreditos("A"));
        }
    }
}
