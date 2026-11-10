package edu.upc.dsa.services;

import edu.upc.dsa.Main;
import edu.upc.dsa.domain.dtos.BajaDTO;
import edu.upc.dsa.domain.dtos.CrearPartidaDTO;
import edu.upc.dsa.domain.dtos.ReaparicionDTO;
import edu.upc.dsa.domain.dtos.TiempoDTO;
import edu.upc.dsa.domain.models.EstadoPartida;
import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.domain.models.ResultadoJugador;
import edu.upc.dsa.domain.repository.JugadorManagerImpl;
import edu.upc.dsa.domain.repository.PartidaManagerImpl;
import edu.upc.dsa.domain.repository.ResultadosManagerImpl;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/** Pruebas HTTP del servicio de partidas: arrancan un servidor propio en un puerto libre. */
public class PartidaServiceTest {

    private HttpServer server;
    private Client client;
    private WebTarget partidas;

    @Before
    public void setUp() throws Exception {
        PartidaManagerImpl.getInstance().clear();
        ResultadosManagerImpl.getInstance().limpiar();
        JugadorManagerImpl.getInstance().limpiar();
        JugadorManagerImpl.getInstance().registrarJugador("franklin", "Franklin", false);

        server = Main.startServer(URI.create("http://localhost:0/dsaApp/"));
        int port = server.getListeners().iterator().next().getPort();
        client = ClientBuilder.newClient();
        partidas = client.target("http://localhost:" + port + "/dsaApp/partidas");
    }

    @After
    public void tearDown() {
        if (client != null) client.close();
        if (server != null) Main.stopServer(server);
        PartidaManagerImpl.getInstance().clear();
        ResultadosManagerImpl.getInstance().limpiar();
        JugadorManagerImpl.getInstance().limpiar();
    }

    private Partida crear(String humano, int bots) {
        Response r = partidas.request().post(Entity.json(new CrearPartidaDTO(humano, bots)));
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
        Assert.assertEquals(EstadoPartida.PENDIENTE, p.getEstado());
        Assert.assertEquals(4, p.getParticipantes().size());

        Assert.assertEquals(200, post(p.getId() + "/iniciar", ""));
        Assert.assertEquals(201, post(p.getId() + "/bajas", new BajaDTO("franklin", "bot-1")));
        Assert.assertEquals(200, post(p.getId() + "/reapariciones", new ReaparicionDTO("bot-1")));
        Assert.assertEquals(201, post(p.getId() + "/bajas", new BajaDTO("franklin", "bot-1")));

        TiempoDTO tiempo = partidas.path(p.getId() + "/tiempo").request().get(TiempoDTO.class);
        Assert.assertTrue(tiempo.getSegundosRestantes() > 290);

        Assert.assertEquals(200, post(p.getId() + "/finalizar", ""));
        Partida fin = partidas.path(p.getId()).request().get(Partida.class);
        Assert.assertEquals(EstadoPartida.FINALIZADA, fin.getEstado());

        // El Observer registrado en Main ha llevado las bajas al marcador de resultados (una sola vez)
        ResultadoJugador primero = ResultadosManagerImpl.getInstance()
                .obtenerMarcadorActual(p.getId()).getClasificacion().get(0);
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
        try (Response r = partidas.request().post(Entity.json(new CrearPartidaDTO("franklin", 10)))) {
            Assert.assertEquals(409, r.getStatus());   // 11 participantes
        }
        try (Response r = partidas.request().post(Entity.json(new CrearPartidaDTO("fantasma", 3)))) {
            Assert.assertEquals(404, r.getStatus());   // el humano no existe
        }
        try (Response r = partidas.request().post(Entity.json(new CrearPartidaDTO("franklin", -1)))) {
            Assert.assertEquals(400, r.getStatus());
        }
    }

    @Test
    public void erroresDeEstadoYDeParticipantes() {
        Partida p = crear("franklin", 2);
        // Baja en una partida que aún no ha empezado
        Assert.assertEquals(409, post(p.getId() + "/bajas", new BajaDTO("franklin", "bot-1")));
        post(p.getId() + "/iniciar", "");
        // Iniciar dos veces
        Assert.assertEquals(409, post(p.getId() + "/iniciar", ""));
        // Jugador que no participa
        Assert.assertEquals(404, post(p.getId() + "/bajas", new BajaDTO("franklin", "bot-9")));
        // Suicidio y cuerpo incompleto
        Assert.assertEquals(400, post(p.getId() + "/bajas", new BajaDTO("franklin", "franklin")));
        Assert.assertEquals(400, post(p.getId() + "/bajas", new BajaDTO("franklin", null)));
        // Reaparecer estando vivo
        Assert.assertEquals(409, post(p.getId() + "/reapariciones", new ReaparicionDTO("bot-1")));
        // Partida inexistente
        Assert.assertEquals(404, post("P999/iniciar", ""));
        try (Response r = partidas.path("P999/tiempo").request().get()) {
            Assert.assertEquals(404, r.getStatus());
        }
    }
}
