package edu.upc.dsa.services;

import edu.upc.dsa.domain.models.*;
import edu.upc.dsa.domain.models.Partida.Estado;
import edu.upc.dsa.domain.repository.JuegoManager;
import edu.upc.dsa.domain.repository.JuegoManagerImpl;
import io.swagger.annotations.*;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/** Recurso REST único, como DroneService en la referencia. */
@Api(value = "/", description = "Jugadores, tienda, partidas y resultados")
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JuegoService {
    private final JuegoManager manager = JuegoManagerImpl.getInstance();

    @GET
    @Path("status")
    @ApiOperation(value = "Comprobar disponibilidad", response = Status.class)
    public Status getStatus() { return new Status("ok"); }

    @POST
    @Path("auth/register")
    @ApiOperation(value = "Registrar un jugador con usuario y contraseña", response = Perfil.class)
    public Response registrarJugador(Credenciales datos) {
        if (datos == null) throw new IllegalArgumentException("Hacen falta usuario y contraseña");
        return Response.status(201).entity(Perfil.from(manager.registrarJugador(datos.getUsuario(), datos.getPassword())))
                .header("Cache-Control", "no-store").build();
    }

    @POST
    @Path("auth/login")
    @ApiOperation(value = "Comprobar usuario y contraseña", response = Perfil.class)
    public Response login(Credenciales datos) {
        if (datos == null) throw new IllegalArgumentException("Hacen falta usuario y contraseña");
        try {
            return Response.ok(Perfil.from(manager.autenticarJugador(datos.getUsuario(), datos.getPassword())))
                    .header("Cache-Control", "no-store").build();
        } catch (SecurityException e) {
            throw new ClientErrorException("Usuario o contraseña incorrectos", 401);
        }
    }

    @POST
    @Path("jugadores")
    @ApiOperation(value = "Registrar un jugador humano", response = Perfil.class)
    @ApiResponses({@ApiResponse(code = 201, message = "Jugador creado"),
            @ApiResponse(code = 400, message = "Datos inválidos"), @ApiResponse(code = 409, message = "Duplicado")})
    public Response crearJugador(Perfil jugador) {
        if (jugador == null) throw new IllegalArgumentException("Hace falta el jugador");
        String id = jugador.getId() == null ? UUID.randomUUID().toString() : jugador.getId();
        return Response.status(201).entity(Perfil.from(manager.registrarJugador(id, jugador.getNombre(), false))).build();
    }

    //ISSUE #39 : Falta @GET para getJugadores, añado.

    @GET
    @Path("jugadores")
    @ApiOperation(value = "Listar todos los jugadores registrados", response = Perfil.class, responseContainer = "Lista")
    public Response getJugadores() {
        List<Perfil> perfiles = new ArrayList<>();
        for (Jugador j : manager.getJugadores()) {
            perfiles.add(Perfil.from(j));
        }
        return Response.ok(new GenericEntity<List<Perfil>>(perfiles) { }).build();
    }

    @GET
    @Path("jugadores/{id}")
    @ApiOperation(value = "Consultar perfil y progresión", response = Perfil.class)
    public Perfil getJugador(@PathParam("id") String id) { return Perfil.from(manager.getJugador(id)); }

    @GET
    @Path("jugadores/{id}/inventario")
    @ApiOperation(value = "Consultar inventario", response = ContenidoInventario.class)
    public ContenidoInventario getInventario(@PathParam("id") String id) {
        return new ContenidoInventario(manager.getJugador(id).getInventario());
    }

    @POST
    @Path("jugadores/{id}/inventario/granadas/{nombre}/usar")
    @ApiOperation(value = "Consumir una granada", response = ContenidoInventario.class)
    public ContenidoInventario usarGranada(@PathParam("id") String id, @PathParam("nombre") String nombre) {
        if (!manager.consumirGranada(id, nombre)) throw new ClientErrorException("No quedan granadas de " + nombre, 409);
        return getInventario(id);
    }

    @GET
    @Path("tienda/catalogo")
    @ApiOperation(value = "Consultar catálogo", response = Catalogo.class)
    public Catalogo getCatalogo() {
        return new Catalogo(manager.getArmas().values(), manager.getGranadas().values(), manager.getComplementos().values());
    }

    @POST
    @Path("tienda/comprar")
    @ApiOperation(value = "Comprar y guardar saldo e inventario conjuntamente", response = Perfil.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Compra guardada"),
            @ApiResponse(code = 400, message = "Cantidad inválida"), @ApiResponse(code = 403, message = "Saldo o nivel insuficiente"),
            @ApiResponse(code = 404, message = "Recurso inexistente"), @ApiResponse(code = 409, message = "Objeto ya comprado")})
    public Perfil comprarObjeto(Compra compra) {
        if (compra == null) throw new IllegalArgumentException("Hace falta la compra");
        return Perfil.from(manager.comprar(compra.getJugadorId(), compra.getItemId(), compra.getCantidad()));
    }

    @POST
    @Path("partidas")
    @ApiOperation(value = "Crear con un humano y entre cero y nueve bots", response = Partida.class)
    @ApiResponses({@ApiResponse(code = 201, message = "Partida creada"),
            @ApiResponse(code = 400, message = "Datos inválidos"), @ApiResponse(code = 404, message = "Humano inexistente"),
            @ApiResponse(code = 409, message = "Máximo diez participantes")})
    public Response crearPartida(CrearPartida datos) {
        if (datos == null) throw new IllegalArgumentException("Hacen falta idJugadorHumano y numBots");
        return Response.status(201).entity(manager.crearPartida(datos.getIdJugadorHumano(), datos.getNumBots())).build();
    }

    @POST
    @Path("partidas/crear/{jugadorId}")
    @ApiOperation(value = "Crear una partida con nueve bots", response = Partida.class)
    public Response crearPartidaRapida(@PathParam("jugadorId") String jugadorId) {
        return Response.status(201).entity(manager.crearPartida(jugadorId, Partida.MAX_PARTICIPANTES - 1)).build();
    }

    @GET
    @Path("partidas")
    @ApiOperation(value = "Listar partidas; admite filtro por estado", response = Partida.class, responseContainer = "List")
    public Response getPartidas(@QueryParam("estado") String estado) {
        List<Partida> partidas = estado == null || estado.isBlank() ? manager.getPartidas()
                : manager.getPartidasPorEstado(Estado.valueOf(estado.trim().toUpperCase(Locale.ROOT)));
        return Response.ok(new GenericEntity<List<Partida>>(partidas) { }).build();
    }

    @GET
    @Path("partidas/{id}")
    @ApiOperation(value = "Consultar partida", response = Partida.class)
    public Partida getPartida(@PathParam("id") String id) { return manager.getPartida(id); }

    @POST
    @Path("partidas/{id}/iniciar")
    @ApiOperation(value = "Iniciar partida", response = Partida.class)
    public Partida iniciarPartida(@PathParam("id") String id) { return manager.iniciarPartida(id); }

    @POST
    @Path("partidas/{id}/bajas")
    @ApiOperation(value = "Registrar una baja y actualizar el marcador")
    public Response registrarBaja(@PathParam("id") String id, Baja baja) {
        if (baja == null) throw new IllegalArgumentException("Hacen falta idAsesino e idVictima");
        manager.registrarBaja(id, baja.getIdAsesino(), baja.getIdVictima());
        return Response.status(201).build();
    }

    @POST
    @Path("partidas/{id}/reapariciones")
    @ApiOperation(value = "Reaparecer en una partida en curso")
    public Response reaparecer(@PathParam("id") String id, Reaparicion datos) {
        if (datos == null || datos.getIdJugador() == null || datos.getIdJugador().isBlank()) {
            throw new IllegalArgumentException("Hace falta idJugador");
        }
        manager.reaparecer(id, datos.getIdJugador());
        return Response.ok().build();
    }

    @POST
    @Path("partidas/{id}/finalizar")
    @ApiOperation(value = "Finalizar partida y aplicar recompensas", response = Partida.class)
    public Partida finalizarPartida(@PathParam("id") String id) { return manager.finalizarPartida(id); }

    @GET
    @Path("partidas/{id}/tiempo")
    @ApiOperation(value = "Consultar segundos restantes", response = Tiempo.class)
    public Tiempo getTiempo(@PathParam("id") String id) { return new Tiempo(id, manager.getSegundosRestantes(id)); }

    @GET
    @Path("resultados/scoreboard/{partidaId}")
    @ApiOperation(value = "Consultar clasificación y recompensas", response = Scoreboard.class)
    public Scoreboard getScoreboard(@PathParam("partidaId") String id) { return manager.getScoreboard(id); }

    @POST
    @Path("resultados/{partidaId}/recompensas")
    @ApiOperation(value = "Reintentar pagos pendientes sin duplicarlos", response = Scoreboard.class)
    public Scoreboard aplicarRecompensas(@PathParam("partidaId") String id) {
        manager.aplicarRecompensas(id);
        return manager.getScoreboard(id);
    }

    // Los cuerpos y vistas pequeños se agrupan aquí; no tienen lógica de negocio.

    public static class Credenciales {
        private String usuario;
        private String password;

        public Credenciales() { }
        public String getUsuario() { return usuario; }
        public void setUsuario(String usuario) { this.usuario = usuario; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class Perfil {
        public static Perfil from(edu.upc.dsa.domain.models.Jugador jugador) {
            return new Perfil(jugador.getIdJugador(), jugador.getNombre(), jugador.getNivel(),
                    jugador.getExperiencia(), jugador.getCreditos());
        }

        private String id;
        private String nombre;
        private int nivel;
        private int experiencia;
        private int creditos;

        public Perfil() {

        }
        public Perfil(String id, String nombre, int nivel, int experiencia, int creditos) {
            this.id = id;
            this.nombre = nombre;
            this.nivel = nivel;
            this.experiencia = experiencia;
            this.creditos = creditos;
        }
        //Getters y Setters
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getNombre() {
            return nombre;
        }
        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
        public int getNivel() {
            return nivel;
        }
        public void setNivel(int nivel) {
            this.nivel = nivel;
        }
        public int getExperiencia() {
            return experiencia;
        }
        public void setExperiencia(int experiencia) {
            this.experiencia = experiencia;
        }
        public int getCreditos() {
            return creditos;
        }
        public void setCreditos(int creditos) {
            this.creditos = creditos;
        }
    }

    public static class Compra {
        private String jugadorId;
        private String itemId;
        private int cantidad = 1;

        public Compra() {

        }

        public Compra(String jugadorId, String itemId) {
            this.jugadorId = jugadorId;
            this.itemId = itemId;
        }

        public Compra(String jugadorId, String itemId, int cantidad) {
            this(jugadorId, itemId);
            this.cantidad = cantidad;
        }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }

        //Getters y Setters
        public String getJugadorId() {
            return jugadorId;
        }
        public void setJugadorId(String jugadorId) {
            this.jugadorId = jugadorId;
        }
        public String getItemId() {
            return itemId;
        }
        public void setItemId(String itemId) {
            this.itemId = itemId;
        }
    }

    public static class CrearPartida {
        private String idJugadorHumano;
        private int numBots;

        public CrearPartida() {
        }

        public CrearPartida(String idJugadorHumano, int numBots) {
            this.idJugadorHumano = idJugadorHumano;
            this.numBots = numBots;
        }

        public String getIdJugadorHumano() { return idJugadorHumano; }
        public void setIdJugadorHumano(String idJugadorHumano) { this.idJugadorHumano = idJugadorHumano; }

        public int getNumBots() { return numBots; }
        public void setNumBots(int numBots) { this.numBots = numBots; }
    }

    public static class Baja {
        private String idAsesino;
        private String idVictima;

        public Baja() {
        }

        public Baja(String idAsesino, String idVictima) {
            this.idAsesino = idAsesino;
            this.idVictima = idVictima;
        }

        public String getIdAsesino() { return idAsesino; }
        public void setIdAsesino(String idAsesino) { this.idAsesino = idAsesino; }

        public String getIdVictima() { return idVictima; }
        public void setIdVictima(String idVictima) { this.idVictima = idVictima; }
    }

    public static class Reaparicion {
        private String idJugador;

        public Reaparicion() {
        }

        public Reaparicion(String idJugador) {
            this.idJugador = idJugador;
        }

        public String getIdJugador() { return idJugador; }
        public void setIdJugador(String idJugador) { this.idJugador = idJugador; }
    }

    public static class Tiempo {
        private String idPartida;
        private long segundosRestantes;

        public Tiempo() {
        }

        public Tiempo(String idPartida, long segundosRestantes) {
            this.idPartida = idPartida;
            this.segundosRestantes = segundosRestantes;
        }

        public String getIdPartida() { return idPartida; }
        public void setIdPartida(String idPartida) { this.idPartida = idPartida; }

        public long getSegundosRestantes() { return segundosRestantes; }
        public void setSegundosRestantes(long segundosRestantes) { this.segundosRestantes = segundosRestantes; }
    }

    public static class Catalogo {
        private List<Arma> armas;
        private List<Granada> granadas;
        private List<Complemento> complementos;
        public Catalogo() { }
        public Catalogo(Collection<Arma> armas, Collection<Granada> granadas, Collection<Complemento> complementos) {
            this.armas = new ArrayList<>(armas);
            this.granadas = new ArrayList<>(granadas);
            this.complementos = new ArrayList<>(complementos);
        }
        public List<Arma> getArmas() { return armas; }
        public void setArmas(List<Arma> armas) { this.armas = armas; }
        public List<Granada> getGranadas() { return granadas; }
        public void setGranadas(List<Granada> granadas) { this.granadas = granadas; }
        public List<Complemento> getComplementos() { return complementos; }
        public void setComplementos(List<Complemento> complementos) { this.complementos = complementos; }
    }

    public static class ContenidoInventario {
        private List<Arma> armas;
        private List<GranadaCantidad> granadas;
        private List<Complemento> complementos;

        public ContenidoInventario() { }

        public ContenidoInventario(Inventario inventario) {
            this.armas = new ArrayList<>(inventario.getArmas());
            this.complementos = new ArrayList<>(inventario.getComplementos());
            this.granadas = new ArrayList<>();
            inventario.getGranadas().forEach((nombre, cantidad) -> granadas.add(new GranadaCantidad(nombre, cantidad)));
        }

        public List<Arma> getArmas() { return armas; }
        public void setArmas(List<Arma> armas) { this.armas = armas; }
        public List<GranadaCantidad> getGranadas() { return granadas; }
        public void setGranadas(List<GranadaCantidad> granadas) { this.granadas = granadas; }
        public List<Complemento> getComplementos() { return complementos; }
        public void setComplementos(List<Complemento> complementos) { this.complementos = complementos; }

        public static class GranadaCantidad {
            private String nombre;
            private int cantidad;
            public GranadaCantidad() { }
            public GranadaCantidad(String nombre, int cantidad) { this.nombre = nombre; this.cantidad = cantidad; }
            public String getNombre() { return nombre; }
            public void setNombre(String nombre) { this.nombre = nombre; }
            public int getCantidad() { return cantidad; }
            public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        }
    }

    public static class Status {
        private String status;

        public Status() {
        }

        public Status(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
