package edu.upc.dsa.services;

import edu.upc.dsa.domain.dtos.BajaDTO;
import edu.upc.dsa.domain.dtos.CrearPartidaDTO;
import edu.upc.dsa.domain.dtos.ReaparicionDTO;
import edu.upc.dsa.domain.dtos.TiempoDTO;
import edu.upc.dsa.domain.models.EstadoPartida;
import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.domain.repository.PartidaManager;
import edu.upc.dsa.domain.repository.PartidaManagerImpl;
import edu.upc.dsa.domain.repository.ResultadosManagerImpl;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import edu.upc.dsa.exceptions.EstadoPartidaInvalidoException;
import edu.upc.dsa.exceptions.ParticipanteNotFoundException;
import edu.upc.dsa.exceptions.PartidaLlenaException;
import edu.upc.dsa.exceptions.PartidaNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Servicio REST de partidas. No tiene lógica propia: valida la entrada y delega en PartidaManager.
 * Los errores los convierte MyExceptionMapper: 400 datos inválidos, 404 no existe, 409 conflicto de estado.
 */
@Api(value = "/partidas", description = "Servicio REST para el control de partidas")
@Path("/partidas")
public class PartidaService {

    private final PartidaManager pm = PartidaManagerImpl.getInstance();

    @POST
    @ApiOperation(value = "Crear una partida PENDIENTE con 1 humano y entre 0 y 9 bots")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Partida creada", response = Partida.class),
            @ApiResponse(code = 400, message = "Datos inválidos"),
            @ApiResponse(code = 404, message = "El jugador humano no existe"),
            @ApiResponse(code = 409, message = "Más de 10 participantes")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearPartida(CrearPartidaDTO datos)
            throws DatosInvalidosException, PartidaLlenaException {
        if (datos == null) throw new DatosInvalidosException("Falta el cuerpo con idJugadorHumano y numBots");
        Partida p = pm.crearPartida(datos.getIdJugadorHumano(), datos.getNumBots());
        ResultadosManagerImpl.getInstance().inicializarMarcador(p);
        return Response.status(201).entity(p).build();
    }

    @POST
    @Path("/crear/{jugadorId}")
    @ApiOperation(value = "Atajo: crear una partida PENDIENTE con el humano indicado y 9 bots")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Partida creada", response = Partida.class),
            @ApiResponse(code = 400, message = "Datos inválidos"),
            @ApiResponse(code = 404, message = "El jugador humano no existe")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearPartidaRapida(@PathParam("jugadorId") String jugadorId)
            throws DatosInvalidosException, PartidaLlenaException {
        Partida p = pm.crearPartida(jugadorId, Partida.MAX_PARTICIPANTES - 1);
        ResultadosManagerImpl.getInstance().inicializarMarcador(p);
        return Response.status(201).entity(p).build();
    }

    @GET
    @ApiOperation(value = "Obtener todas las partidas, o solo las de un estado (?estado=EN_CURSO)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Éxito", response = Partida.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Estado no válido")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartidas(@QueryParam("estado") String estado) throws DatosInvalidosException {
        List<Partida> partidas;
        if (estado == null || estado.isBlank()) {
            partidas = pm.getPartidas();
        } else {
            partidas = pm.getPartidasPorEstado(parsearEstado(estado));
        }
        GenericEntity<List<Partida>> entity = new GenericEntity<List<Partida>>(partidas) {};
        return Response.status(200).entity(entity).build();
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Obtener una partida por su id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Éxito", response = Partida.class),
            @ApiResponse(code = 404, message = "Partida no encontrada")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartida(@PathParam("id") String idPartida) throws PartidaNotFoundException {
        pm.comprobarTiempo(idPartida);   // si ya han pasado los 5 minutos, se devuelve FINALIZADA
        return Response.status(200).entity(pm.getPartida(idPartida)).build();
    }

    @POST
    @Path("/{id}/iniciar")
    @ApiOperation(value = "Iniciar la partida: PENDIENTE -> EN_CURSO, empieza a contar el tiempo")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Partida iniciada", response = Partida.class),
            @ApiResponse(code = 404, message = "Partida no encontrada"),
            @ApiResponse(code = 409, message = "La partida no está PENDIENTE")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response iniciarPartida(@PathParam("id") String idPartida)
            throws PartidaNotFoundException, EstadoPartidaInvalidoException {
        Partida p = pm.iniciarPartida(idPartida);
        return Response.status(200).entity(p).build();
    }

    @POST
    @Path("/{id}/bajas")
    @ApiOperation(value = "Registrar una baja: un participante elimina a otro")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Baja registrada"),
            @ApiResponse(code = 400, message = "Datos inválidos"),
            @ApiResponse(code = 404, message = "Partida o participante no encontrado"),
            @ApiResponse(code = 409, message = "La partida no está EN_CURSO o la víctima ya está muerta")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarBaja(@PathParam("id") String idPartida, BajaDTO baja)
            throws DatosInvalidosException, PartidaNotFoundException,
            ParticipanteNotFoundException, EstadoPartidaInvalidoException {
        if (baja == null || vacio(baja.getIdAsesino()) || vacio(baja.getIdVictima())) {
            throw new DatosInvalidosException("Hacen falta idAsesino e idVictima");
        }
        if (baja.getIdAsesino().equals(baja.getIdVictima())) {
            throw new DatosInvalidosException("Un participante no puede eliminarse a sí mismo");
        }
        pm.registrarBaja(idPartida, baja.getIdAsesino(), baja.getIdVictima());
        return Response.status(201).build();
    }

    @POST
    @Path("/{id}/reapariciones")
    @ApiOperation(value = "Reaparecer: un participante muerto vuelve a la partida (ilimitado)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Reaparición registrada"),
            @ApiResponse(code = 400, message = "Datos inválidos"),
            @ApiResponse(code = 404, message = "Partida o participante no encontrado"),
            @ApiResponse(code = 409, message = "La partida no está EN_CURSO o el jugador está vivo")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reaparecer(@PathParam("id") String idPartida, ReaparicionDTO reaparicion)
            throws DatosInvalidosException, PartidaNotFoundException,
            ParticipanteNotFoundException, EstadoPartidaInvalidoException {
        if (reaparicion == null || vacio(reaparicion.getIdJugador())) {
            throw new DatosInvalidosException("Hace falta idJugador");
        }
        pm.reaparecer(idPartida, reaparicion.getIdJugador());
        return Response.status(200).build();
    }

    @POST
    @Path("/{id}/finalizar")
    @ApiOperation(value = "Finalizar la partida antes de tiempo: EN_CURSO -> FINALIZADA")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Partida finalizada", response = Partida.class),
            @ApiResponse(code = 404, message = "Partida no encontrada"),
            @ApiResponse(code = 409, message = "La partida no está EN_CURSO")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response finalizarPartida(@PathParam("id") String idPartida)
            throws PartidaNotFoundException, EstadoPartidaInvalidoException {
        Partida p = pm.finalizarPartida(idPartida);
        return Response.status(200).entity(p).build();
    }

    @GET
    @Path("/{id}/tiempo")
    @ApiOperation(value = "Segundos que le quedan a la partida (300 si no ha empezado, 0 si ha acabado)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Éxito", response = TiempoDTO.class),
            @ApiResponse(code = 404, message = "Partida no encontrada")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTiempo(@PathParam("id") String idPartida) throws PartidaNotFoundException {
        long segundos = pm.getSegundosRestantes(idPartida);
        return Response.status(200).entity(new TiempoDTO(idPartida, segundos)).build();
    }

    // ---------- ayudas ----------

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private static EstadoPartida parsearEstado(String estado) throws DatosInvalidosException {
        try {
            return EstadoPartida.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DatosInvalidosException("Estado no válido: " + estado
                    + " (usa PENDIENTE, EN_CURSO o FINALIZADA)");
        }
    }
}
