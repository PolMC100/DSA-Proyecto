package edu.upc.dsa.services;

import edu.upc.dsa.domain.dtos.JugadorDTO;
import edu.upc.dsa.domain.models.Jugador;
import edu.upc.dsa.domain.repository.JugadorManager;
import edu.upc.dsa.domain.repository.JugadorManagerImpl;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import edu.upc.dsa.exceptions.JugadorDuplicadoException;
import edu.upc.dsa.exceptions.JugadorNotFoundException;
import edu.upc.dsa.MyExceptionMapper.ErrorEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Api(value = "/jugadores", description = "Servicio para gestionar el perfil de jugadores")
@Path("/jugadores")

public class JugadorService {
    private static final Logger logger = LogManager.getLogger(JugadorService.class);
    private final JugadorManager manager = JugadorManagerImpl.getInstance();
    @Context
    private Providers providers;

    @POST
    @ApiOperation(value = "Registrar un nuevo jugador")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Jugador creado", response = JugadorDTO.class),
            @ApiResponse(code = 400, message = "Datos no válidos"),
            @ApiResponse(code = 409, message = "Identificador duplicado")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiImplicitParams(@ApiImplicitParam(name = "jugador", paramType = "body",
            dataType = "edu.upc.dsa.domain.dtos.JugadorDTO", required = true))

    public Response crearJugador(@ApiParam(hidden = true) String cuerpo) {
        try {
            JugadorDTO jugador = leerJugador(cuerpo);
            if (jugador == null) return error(400, "Hace falta el jugador");
            String id = jugador.getId() == null ? UUID.randomUUID().toString() : jugador.getId();
            Jugador registrado = manager.registrarJugador(id, jugador.getNombre(), false);
            return Response.status(201).entity(toDTO(registrado)).build();
        } catch (DatosInvalidosException e) {
            return error(400, e.getMessage());
        } catch (JugadorDuplicadoException e) {
            return error(409, e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error al registrar el jugador", e);
            return error(500, "Error interno del servidor");
        }
    }
    @GET
    @Path("/{id}")
    @ApiOperation(value = "Consultar el perfil del jugador")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Encontrado", response = JugadorDTO.class),
            @ApiResponse(code = 404, message = "Jugador no encontrado")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJugador(@PathParam("id") String id) {
        try {
            return Response.ok(toDTO(manager.getJugador(id))).build();
        } catch (DatosInvalidosException e) {
            return error(400, e.getMessage());
        } catch (JugadorNotFoundException e) {
            return error(404, e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error al consultar el jugador", e);
            return error(500, "Error interno del servidor");
        }
    }

    /** Lee con el proveedor JSON existente para tratar el JSON inválido aquí,
     * sin modificar el mapper global ni el comportamiento de otros servicios. */
    private JugadorDTO leerJugador(String cuerpo) throws DatosInvalidosException {
        if (cuerpo == null || cuerpo.isBlank()) throw new DatosInvalidosException("Hace falta el jugador");
        Annotation[] anotaciones = new Annotation[0];
        MessageBodyReader<JugadorDTO> reader = providers.getMessageBodyReader(JugadorDTO.class,
                JugadorDTO.class, anotaciones, MediaType.APPLICATION_JSON_TYPE);
        if (reader == null) throw new IllegalStateException("No hay proveedor JSON para JugadorDTO");
        try {
            return reader.readFrom(JugadorDTO.class, JugadorDTO.class, anotaciones,
                    MediaType.APPLICATION_JSON_TYPE, new MultivaluedHashMap<>(),
                    new ByteArrayInputStream(cuerpo.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException | WebApplicationException | ProcessingException e) {
            throw new DatosInvalidosException("JSON de jugador inválido");
        }
    }

    private Response error(int codigo, String mensaje) {
        logger.error("JugadorService: " + codigo + " " + mensaje);
        return Response.status(codigo).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorEntity(codigo, mensaje)).build();
    }

    private JugadorDTO toDTO(Jugador jugador) {
        return new JugadorDTO(jugador.getIdJugador(), jugador.getNombre(), jugador.getNivel(),
                jugador.getExperiencia(), jugador.getCreditos());
    }
}
