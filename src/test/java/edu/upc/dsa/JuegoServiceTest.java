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

import edu.upc.dsa.services.JuegoService.*;
import edu.upc.dsa.MyExceptionMapper.ErrorEntity;
import org.glassfish.grizzly.http.server.HttpServer;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.net.URI;

@RunWith(Enclosed.class)
public class JuegoServiceTest {

    public static class FlujoCompleto {
        private HttpServer server;
        private Client client;
        private WebTarget target;

        @Before
        public void setUp() throws Exception {
            JuegoManagerImpl.getInstance().limpiar();
            JuegoManagerImpl.getInstance().cargarCatalogoInicial();
            server = Main.startServer(URI.create("http://localhost:0/dsaApp/"));
            int port = server.getListeners().iterator().next().getPort();
            client = ClientBuilder.newClient();
            target = client.target("http://localhost:" + port + "/dsaApp/");
        }

        @After
        public void tearDown() {
            if (client != null) client.close();
            if (server != null) Main.stopServer(server);
            JuegoManagerImpl.getInstance().limpiar();
        }

        private Response post(String ruta, Object cuerpo) {
            return target.path(ruta).request().post(Entity.json(cuerpo));
        }

        private void postStatus(String ruta, Object cuerpo, int codigo) {
            try (Response response = post(ruta, cuerpo)) { assertEquals(codigo, response.getStatus()); }
        }

        private void prepararJugador() throws Exception {
            JuegoManager jugadores = JuegoManagerImpl.getInstance();
            jugadores.registrarJugador("J1", "Tester", false);
            jugadores.sumarExperienciaYCreditos("J1", 500, 3000);
        }

        private Credenciales credenciales(String usuario, String password) {
            Credenciales datos = new Credenciales();
            datos.setUsuario(usuario);
            datos.setPassword(password);
            return datos;
        }

        @Test
        public void registroYLoginDevuelvenElMismoJugadorParaLaTienda() {
            String id;
            try (Response response = post("auth/register", credenciales("Tester", "1234"))) {
                assertEquals(201, response.getStatus());
                assertEquals("no-store", response.getHeaderString("Cache-Control"));
                String json = response.readEntity(String.class);
                assertFalse(json.contains("password"));
                assertFalse(json.contains("\"1234\""));
            }
            try (Response response = post("auth/login", credenciales(" tester ", "1234"))) {
                assertEquals(200, response.getStatus());
                Perfil perfil = response.readEntity(Perfil.class);
                id = perfil.getId();
                assertNotNull(id);
                assertEquals("Tester", perfil.getNombre());
                assertEquals(1, perfil.getNivel());
                assertEquals(0, perfil.getCreditos());
            }
            JuegoManagerImpl.getInstance().sumarExperienciaYCreditos(id, 0, 300);
            try (Response response = post("auth/login", credenciales("TESTER", "1234"))) {
                Perfil perfil = response.readEntity(Perfil.class);
                assertEquals(id, perfil.getId());
                assertEquals(300, perfil.getCreditos());
            }
            postStatus("tienda/comprar", new Compra(id, "Pistola"), 200);
            assertEquals(1, JuegoManagerImpl.getInstance().getJugadores().size());
            assertEquals(0, JuegoManagerImpl.getInstance().getCreditos(id));
        }

        @Test
        public void registroDuplicadoYLoginIncorrectoSeRechazan() {
            postStatus("auth/register", credenciales("Tester", "password123"), 201);
            postStatus("auth/register", credenciales(" tester ", "otraPassword"), 409);
            postStatus("auth/login", credenciales("Tester", "password456"), 401);
            postStatus("auth/login", credenciales("NoExiste", "password123"), 401);
            assertEquals(1, JuegoManagerImpl.getInstance().getJugadores().size());
            JuegoManagerImpl.getInstance().limpiar();
            postStatus("auth/login", credenciales("Tester", "password123"), 401);
        }

        @Test
        public void credencialesIncompletasNoCreanJugadores() {
            for (String ruta : new String[]{"auth/register", "auth/login"}) {
                postStatus(ruta, "null", 400);
                postStatus(ruta, credenciales(null, "password123"), 400);
                postStatus(ruta, credenciales("ab", "password123"), 400);
                postStatus(ruta, credenciales("Tester", null), 400);
                postStatus(ruta, credenciales("Tester", ""), 400);
                postStatus(ruta, credenciales("Tester", "   "), 400);
            }
            assertTrue(JuegoManagerImpl.getInstance().getJugadores().isEmpty());
        }

        @Test
        public void paginaDeAccesoSePublicaEnLaRaiz() {
            try (Response response = client.target(target.getUri().resolve("/")).request().get()) {
                assertEquals(200, response.getStatus());
                assertTrue(response.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE));
                String html = response.readEntity(String.class);
                assertTrue(html.contains("id=\"login-form\""));
                assertTrue(html.contains("id=\"register-form\""));
                assertTrue(html.contains("/dsaApp/auth/"));
            }
        }

        @Test
        public void estadoYSwaggerEstanDisponibles() {
            try (Response response = target.path("status").request().get()) { assertEquals(200, response.getStatus()); }
            try (Response response = target.path("swagger.json").request().get()) {
                assertEquals(200, response.getStatus());
                String spec = response.readEntity(String.class);
                assertTrue(spec.contains("/tienda/comprar"));
                assertTrue(spec.contains("/auth/login"));
            }
            try (Response response = client.target(target.getUri().resolve("/swagger/")).request().get()) {
                assertEquals(200, response.getStatus());
                assertTrue(response.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE));
                String html = response.readEntity(String.class);
                assertTrue(html.contains("/dsaApp/swagger.json"));
                assertTrue(html.contains("swagger-ui-bundle.js"));
            }
        }

        @Test
        public void flujoCompletoConRecompensasYCompraGuardada() throws Exception {
            try (Response response = post("jugadores", new Perfil("J1", "Tester", 99, 999, 999))) {
                assertEquals(201, response.getStatus());
                assertEquals(0, response.readEntity(Perfil.class).getCreditos());
            }
            Catalogo catalogo = target.path("tienda/catalogo").request().get(Catalogo.class);
            assertEquals(5, catalogo.getArmas().size());
            Partida partida;
            try (Response response = post("partidas", new CrearPartida("J1", 1))) {
                assertEquals(201, response.getStatus());
                partida = response.readEntity(Partida.class);
            }
            String ruta = "partidas/" + partida.getId();
            postStatus(ruta + "/iniciar", "", 200);
            for (int i = 0; i < 25; i++) {
                postStatus(ruta + "/bajas", new Baja("J1", "bot-1"), 201);
                if (i < 24) postStatus(ruta + "/reapariciones", new Reaparicion("bot-1"), 200);
            }
            postStatus(ruta + "/finalizar", "", 200);
            Scoreboard scoreboard = target.path("resultados/scoreboard/" + partida.getId()).request().get(Scoreboard.class);
            assertTrue(scoreboard.isProcesado());
            assertEquals(25, scoreboard.getClasificacion().get(0).getBajas());
            Perfil perfil = target.path("jugadores/J1").request().get(Perfil.class);
            assertEquals(2500, perfil.getExperiencia());
            assertEquals(300, perfil.getCreditos());
            postStatus("resultados/" + partida.getId() + "/recompensas", "", 200);
            assertEquals(300, target.path("jugadores/J1").request().get(Perfil.class).getCreditos());
            try (Response response = post("tienda/comprar", new Compra("J1", "Pistola"))) {
                assertEquals(200, response.getStatus());
                assertEquals(0, response.readEntity(Perfil.class).getCreditos());
            }
            assertEquals(1, JuegoManagerImpl.getInstance().getJugador("J1").getInventario().getArmas().size());
            try (Response response = target.path("jugadores/J1/inventario").request().get()) {
                assertEquals(200, response.getStatus());
                assertTrue(response.readEntity(String.class).contains("Pistola"));
            }
        }

        @Test
        public void erroresDeCompraSonRealesYNoModificanSaldo() throws Exception {
            prepararJugador();
            postStatus("tienda/comprar", new Compra("J1", "Humo", -2), 400);
            postStatus("tienda/comprar", new Compra("J1", "Humo", Integer.MAX_VALUE), 400);
            postStatus("tienda/comprar", new Compra("nadie", "Pistola"), 404);
            postStatus("tienda/comprar", new Compra("J1", "ARMA-01"), 404);
            assertEquals(3000, JuegoManagerImpl.getInstance().getCreditos("J1"));
            postStatus("tienda/comprar", new Compra("J1", "Pistola"), 200);
            postStatus("tienda/comprar", new Compra("J1", "Pistola"), 409);
            postStatus("tienda/comprar", new Compra("J1", "Francotirador"), 200);
            postStatus("tienda/comprar", new Compra("J1", "Humo"), 403);
            JuegoManagerImpl.getInstance().registrarJugador("J2", "Nuevo", false);
            postStatus("tienda/comprar", new Compra("J2", "Jetpack"), 403);
        }

        @Test
        public void consumoDeGranadasPersisteYNoPermiteUnidadesNegativas() throws Exception {
            prepararJugador();
            postStatus("tienda/comprar", new Compra("J1", "Humo", 2), 200);
            ContenidoInventario inventario = target.path("jugadores/J1/inventario").request().get(ContenidoInventario.class);
            assertEquals("Humo", inventario.getGranadas().get(0).getNombre());
            assertEquals(2, inventario.getGranadas().get(0).getCantidad());
            postStatus("jugadores/J1/inventario/granadas/Humo/usar", "", 200);
            postStatus("jugadores/J1/inventario/granadas/Humo/usar", "", 200);
            postStatus("jugadores/J1/inventario/granadas/Humo/usar", "", 409);
            assertEquals(Integer.valueOf(0), JuegoManagerImpl.getInstance().getJugador("J1").getInventario().getGranadas().get("Humo"));
        }

        @Test
        public void erroresDeJsonYDelFrameworkTienenFormatoComun() {
            for (String ruta : new String[]{"jugadores", "partidas", "tienda/comprar"}) {
                try (Response response = post(ruta, "{invalido")) {
                    assertEquals(ruta, 400, response.getStatus());
                    assertEquals(400, response.readEntity(ErrorEntity.class).getCode());
                }
            }
            try (Response response = target.path("no-existe").request().get()) {
                assertEquals(404, response.getStatus());
                assertEquals(404, response.readEntity(ErrorEntity.class).getCode());
            }
            try (Response response = target.path("status").request().put(Entity.json(""))) {
                assertEquals(405, response.getStatus());
                assertNotNull(response.getHeaderString("Allow"));
                assertEquals(405, response.readEntity(ErrorEntity.class).getCode());
            }
            try (Response response = target.path("jugadores").request().post(Entity.entity("texto", MediaType.TEXT_PLAIN_TYPE))) {
                assertEquals(415, response.getStatus());
                assertEquals(415, response.readEntity(ErrorEntity.class).getCode());
            }
            try (Response response = target.path("resultados/scoreboard/no-existe").request().get()) {
                assertEquals(404, response.getStatus());
                assertEquals(404, response.readEntity(ErrorEntity.class).getCode());
            }
        }
    }

    public static class JugadoresHttp {
        private HttpServer server;
        private Client client;
        private WebTarget target;
        private JuegoManager manager;

        @Before
        public void setUp() throws Exception {
            manager = JuegoManagerImpl.getInstance();
            manager.limpiar();
            server = Main.startServer(URI.create("http://localhost:0/dsaApp/"));
            int port = server.getListeners().iterator().next().getPort();
            client = ClientBuilder.newClient();
            target = client.target("http://localhost:" + port + "/dsaApp/jugadores");
        }

        @After
        public void tearDown() {
            if (client != null) client.close();
            if (server != null) Main.stopServer(server);
            manager.limpiar();
        }

        private Response registrar(Object cuerpo) {
            return target.request().post(Entity.json(cuerpo));
        }

        @Test
        public void registrarYConsultarPerfilRealSinAceptarProgresionDelCliente() throws Exception {
            try (Response response = registrar(new Perfil("J1", "Cristian", 99, 999, 999))) {
                Assert.assertEquals(201, response.getStatus());
                Perfil dto = response.readEntity(Perfil.class);
                Assert.assertEquals("J1", dto.getId());
                Assert.assertEquals("Cristian", dto.getNombre());
                Assert.assertEquals(1, dto.getNivel());
                Assert.assertEquals(0, dto.getExperiencia());
                Assert.assertEquals(0, dto.getCreditos());
            }
            Assert.assertTrue(manager.existeJugadorHumano("J1"));
            manager.sumarExperienciaYCreditos("J1", 100, 50);
            try (Response response = target.path("J1").request().get()) {
                Assert.assertEquals(200, response.getStatus());
                Perfil dto = response.readEntity(Perfil.class);
                Assert.assertEquals(2, dto.getNivel());
                Assert.assertEquals(100, dto.getExperiencia());
                Assert.assertEquals(50, dto.getCreditos());
            }
        }

        @Test
        public void registroSinIdentificadorGeneraIdsDistintos() throws Exception {
            String primero;
            try (Response response = registrar(new Perfil(null, "Uno", 0, 0, 0))) {
                Assert.assertEquals(201, response.getStatus());
                primero = response.readEntity(Perfil.class).getId();
                Assert.assertNotNull(primero);
            }
            try (Response response = registrar(new Perfil(null, "Dos", 0, 0, 0))) {
                Assert.assertEquals(201, response.getStatus());
                String segundo = response.readEntity(Perfil.class).getId();
                Assert.assertNotEquals(primero, segundo);
                Assert.assertNotNull(manager.getJugador(segundo));
            }
            Assert.assertNotNull(manager.getJugador(primero));
        }

        @Test
        public void duplicadoDevuelve409SinSobrescribirPerfil() throws Exception {
            manager.registrarJugador("J1", "Cristian", false);
            manager.sumarExperienciaYCreditos("J1", 100, 50);
            try (Response response = registrar(new Perfil("J1", "Otro", 0, 0, 0))) {
                Assert.assertEquals(409, response.getStatus());
                Assert.assertEquals(409, response.readEntity(ErrorEntity.class).getCode());
            }
            Assert.assertEquals("Cristian", manager.getJugador("J1").getNombre());
            Assert.assertEquals(50, manager.getCreditos("J1"));
            Assert.assertEquals(1, manager.getJugadores().size());
        }

        @Test
        public void inexistenteDevuelve404Json() {
            try (Response response = target.path("NO_EXISTE").request().get()) {
                Assert.assertEquals(404, response.getStatus());
                Assert.assertEquals(404, response.readEntity(ErrorEntity.class).getCode());
            }
        }

        @Test
        public void cuerposInvalidosDevuelven400Json() {
            for (String cuerpo : new String[]{"", "null", "{}", "{invalido",
                    "{\"nombre\":\"   \"}", "{\"id\":\"\",\"nombre\":\"Cristian\"}"}) {
                try (Response response = registrar(cuerpo)) {
                    Assert.assertEquals(cuerpo, 400, response.getStatus());
                    Assert.assertEquals(400, response.readEntity(ErrorEntity.class).getCode());
                }
            }
            Assert.assertTrue(manager.getJugadores().isEmpty());
        }

        @Test
        public void consultaInvalidaDevuelve400EnElServicio() {
            try (Response response = target.path(" ").request().get()) {
                Assert.assertEquals(400, response.getStatus());
                Assert.assertEquals(400, response.readEntity(ErrorEntity.class).getCode());
            }
        }

        @Test
        public void consultaReflejaDescuentoYRecompensaUnicaDelGestor() throws Exception {
            manager.registrarJugador("J1", "Cristian", false);
            Assert.assertTrue(manager.aplicarRecompensaPartida("P1", "J1", 200, 100));
            Assert.assertFalse(manager.aplicarRecompensaPartida("P1", "J1", 200, 100));
            manager.descontarCreditos("J1", 30);
            try (Response response = target.path("J1").request().get()) {
                Assert.assertEquals(200, response.getStatus());
                Perfil dto = response.readEntity(Perfil.class);
                Assert.assertEquals(3, dto.getNivel());
                Assert.assertEquals(200, dto.getExperiencia());
                Assert.assertEquals(70, dto.getCreditos());
            }
        }
    }

    public static class PartidasHttp {

        private HttpServer server;
        private Client client;
        private WebTarget partidas;

        @Before
        public void setUp() throws Exception {
            JuegoManagerImpl.getInstance().limpiar();
            JuegoManagerImpl.getInstance().registrarJugador("franklin", "Franklin", false);

            server = Main.startServer(URI.create("http://localhost:0/dsaApp/"));
            int port = server.getListeners().iterator().next().getPort();
            client = ClientBuilder.newClient();
            partidas = client.target("http://localhost:" + port + "/dsaApp/partidas");
        }

        @After
        public void tearDown() {
            if (client != null) client.close();
            if (server != null) Main.stopServer(server);
            JuegoManagerImpl.getInstance().limpiar();
        }

        private Partida crear(String humano, int bots) {
            Response r = partidas.request().post(Entity.json(new CrearPartida(humano, bots)));
            Assert.assertEquals(201, r.getStatus());
            return r.readEntity(Partida.class);
        }

        private int post(String ruta, Object cuerpo) {
            try (Response r = partidas.path(ruta).request().post(Entity.json(cuerpo))) {
                return r.getStatus();
            }
        }

        @Test
        public void flujoCompletoCrearIniciarBajaReaparecerYFinalizar() {
            Partida p = crear("franklin", 3);
            Assert.assertEquals(Estado.PENDIENTE, p.getEstado());
            Assert.assertEquals(4, p.getParticipantes().size());

            Assert.assertEquals(200, post(p.getId() + "/iniciar", ""));
            Assert.assertEquals(201, post(p.getId() + "/bajas", new Baja("franklin", "bot-1")));
            Assert.assertEquals(200, post(p.getId() + "/reapariciones", new Reaparicion("bot-1")));
            Assert.assertEquals(201, post(p.getId() + "/bajas", new Baja("franklin", "bot-1")));

            Tiempo tiempo = partidas.path(p.getId() + "/tiempo").request().get(Tiempo.class);
            Assert.assertTrue(tiempo.getSegundosRestantes() > 290);

            Assert.assertEquals(200, post(p.getId() + "/finalizar", ""));
            Partida fin = partidas.path(p.getId()).request().get(Partida.class);
            Assert.assertEquals(Estado.FINALIZADA, fin.getEstado());

            // El gestor ha actualizado el marcador una sola vez por baja
            ResultadoJugador primero = JuegoManagerImpl.getInstance()
                    .getScoreboard(p.getId()).getClasificacion().get(0);
            Assert.assertEquals("franklin", primero.getIdJugador());
            Assert.assertEquals(2, primero.getBajas());
        }

        @Test
        public void listarPartidasYFiltrarPorEstado() {
            Partida p1 = crear("franklin", 1);
            crear("franklin", 2);
            post(p1.getId() + "/iniciar", "");

            List<Partida> todas = partidas.request().get(new GenericType<List<Partida>>() {});
            Assert.assertEquals(2, todas.size());
            List<Partida> enCurso = partidas.queryParam("estado", "EN_CURSO").request()
                    .get(new GenericType<List<Partida>>() {});
            Assert.assertEquals(1, enCurso.size());
            Assert.assertEquals(p1.getId(), enCurso.get(0).getId());

            try (Response r = partidas.queryParam("estado", "PAUSADA").request().get()) {
                Assert.assertEquals(400, r.getStatus());
            }
        }

        @Test
        public void erroresDeCreacion() {
            try (Response r = partidas.request().post(Entity.json(new CrearPartida("franklin", 10)))) {
                Assert.assertEquals(409, r.getStatus());   // 11 participantes
            }
            try (Response r = partidas.request().post(Entity.json(new CrearPartida("fantasma", 3)))) {
                Assert.assertEquals(404, r.getStatus());   // el humano no existe
            }
            try (Response r = partidas.request().post(Entity.json(new CrearPartida("franklin", -1)))) {
                Assert.assertEquals(400, r.getStatus());
            }
        }

        @Test
        public void erroresDeEstadoYDeParticipantes() {
            Partida p = crear("franklin", 2);
            // Baja en una partida que aún no ha empezado
            Assert.assertEquals(409, post(p.getId() + "/bajas", new Baja("franklin", "bot-1")));
            post(p.getId() + "/iniciar", "");
            // Iniciar dos veces
            Assert.assertEquals(409, post(p.getId() + "/iniciar", ""));
            // Jugador que no participa
            Assert.assertEquals(404, post(p.getId() + "/bajas", new Baja("franklin", "bot-9")));
            // Suicidio y cuerpo incompleto
            Assert.assertEquals(400, post(p.getId() + "/bajas", new Baja("franklin", "franklin")));
            Assert.assertEquals(400, post(p.getId() + "/bajas", new Baja("franklin", null)));
            // Reaparecer estando vivo
            Assert.assertEquals(409, post(p.getId() + "/reapariciones", new Reaparicion("bot-1")));
            // Partida inexistente
            Assert.assertEquals(404, post("P999/iniciar", ""));
            try (Response r = partidas.path("P999/tiempo").request().get()) {
                Assert.assertEquals(404, r.getStatus());
            }
        }
    }

    public static class Errores {
        @Test
        public void erroresInternosNoExponenDetallesAlCliente() {
            try (Response response = new MyExceptionMapper().toResponse(new RuntimeException("detalle privado"))) {
                assertEquals(500, response.getStatus());
                assertEquals("Error interno del servidor", ((MyExceptionMapper.ErrorEntity) response.getEntity()).getMessage());
            }
        }
    }
}
