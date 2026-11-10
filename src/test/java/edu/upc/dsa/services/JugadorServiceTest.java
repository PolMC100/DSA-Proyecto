package edu.upc.dsa.services;

import edu.upc.dsa.Main;
import edu.upc.dsa.MyExceptionMapper.ErrorEntity;
import edu.upc.dsa.domain.dtos.JugadorDTO;
import edu.upc.dsa.domain.repository.JugadorManager;
import edu.upc.dsa.domain.repository.JugadorManagerImpl;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;

/** Pruebas HTTP exclusivas del módulo de jugadores; no modifican otros gestores. */
public class JugadorServiceTest {
    private HttpServer server;
    private Client client;
    private WebTarget target;
    private JugadorManager manager;

    @Before
    public void setUp() throws Exception {
        manager = JugadorManagerImpl.getInstance();
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
        try (Response response = registrar(new JugadorDTO("J1", "Cristian", 99, 999, 999))) {
            Assert.assertEquals(201, response.getStatus());
            JugadorDTO dto = response.readEntity(JugadorDTO.class);
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
            JugadorDTO dto = response.readEntity(JugadorDTO.class);
            Assert.assertEquals(2, dto.getNivel());
            Assert.assertEquals(100, dto.getExperiencia());
            Assert.assertEquals(50, dto.getCreditos());
        }
    }

    @Test
    public void registroSinIdentificadorGeneraIdsDistintos() throws Exception {
        String primero;
        try (Response response = registrar(new JugadorDTO(null, "Uno", 0, 0, 0))) {
            Assert.assertEquals(201, response.getStatus());
            primero = response.readEntity(JugadorDTO.class).getId();
            Assert.assertNotNull(primero);
        }
        try (Response response = registrar(new JugadorDTO(null, "Dos", 0, 0, 0))) {
            Assert.assertEquals(201, response.getStatus());
            String segundo = response.readEntity(JugadorDTO.class).getId();
            Assert.assertNotEquals(primero, segundo);
            Assert.assertNotNull(manager.getJugador(segundo));
        }
        Assert.assertNotNull(manager.getJugador(primero));
    }

    @Test
    public void duplicadoDevuelve409SinSobrescribirPerfil() throws Exception {
        manager.registrarJugador("J1", "Cristian", false);
        manager.sumarExperienciaYCreditos("J1", 100, 50);
        try (Response response = registrar(new JugadorDTO("J1", "Otro", 0, 0, 0))) {
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
    public void cuerposInvalidosDevuelven400SinModificarMapperGlobal() {
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
        try (Response response = new JugadorService().getJugador(" ")) {
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals(400, ((ErrorEntity) response.getEntity()).getCode());
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
            JugadorDTO dto = response.readEntity(JugadorDTO.class);
            Assert.assertEquals(3, dto.getNivel());
            Assert.assertEquals(200, dto.getExperiencia());
            Assert.assertEquals(70, dto.getCreditos());
        }
    }
}
