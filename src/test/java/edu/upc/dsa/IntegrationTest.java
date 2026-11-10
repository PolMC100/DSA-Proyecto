package edu.upc.dsa;

import edu.upc.dsa.domain.dtos.CompraDTO;
import edu.upc.dsa.domain.dtos.JugadorDTO;
import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.domain.repository.PartidaManagerImpl;
import edu.upc.dsa.domain.repository.ResultadosManagerImpl;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IntegrationTest {

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {

        PartidaManagerImpl.getInstance().clear();
        ResultadosManagerImpl.getInstance().limpiar();

        server = Main.startServer();
        Client c = ClientBuilder.newClient();
        target = c.target(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        Main.stopServer(server);
    }

    @Test
    public void testStatusEndpoint() {
        Response response = target.path("status").request().get();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testFlujoPartidaYResultados() {
        // 1. Crear partida
        Response rPartida = target.path("partidas/crear/J-001")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));
        assertEquals(201, rPartida.getStatus());

        Partida p = rPartida.readEntity(Partida.class);
        assertNotNull(p.getId());

        // 2. Iniciar partida
        Response rInicio = target.path("partidas/" + p.getId() + "/iniciar")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));
        assertEquals(200, rInicio.getStatus());

        // 3. Consultar marcador
        Response rScoreboard = target.path("resultados/scoreboard/" + p.getId())
                .request(MediaType.APPLICATION_JSON)
                .get();
        assertEquals(200, rScoreboard.getStatus());
    }

    @Test
    public void testServiciosJugadorYTienda() {
        // Test de JugadorService
        JugadorDTO jugador = new JugadorDTO(null, "AzaPlayer", 1, 0, 500);
        Response rJugador = target.path("jugadores")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(jugador));
        assertEquals(201, rJugador.getStatus());

        // Test de TiendaService
        CompraDTO compra = new CompraDTO("J-101", "ARMA-01");
        Response rCompra = target.path("tienda/comprar")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(compra));
        assertEquals(200, rCompra.getStatus());
    }

    @Test
    public void testMapeoExcepcion404() {
        Response response = target.path("partidas/PARTIDA_INEXISTENTE_999/iniciar")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));
        assertEquals(404, response.getStatus());
    }
}