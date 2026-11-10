package edu.upc.dsa.services;


import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.domain.repository.PartidaManagerImpl;
import edu.upc.dsa.domain.repository.ResultadosManagerImpl;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import edu.upc.dsa.exceptions.EstadoPartidaInvalidoException;
import edu.upc.dsa.exceptions.PartidaNotFoundException;
import io.swagger.annotations.*;
import edu.upc.dsa.exceptions.PartidaLlenaException;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/partidas", description = "Servicio REST para el control de partidas")
@Path("/partidas")
public class PartidaService {

    @POST
    @Path("/crear/{jugadorId}")
    @ApiOperation(value = "Crear e iniciar una partida con 1 humano y 9 bots")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Partida creada", response = Partida.class),
            @ApiResponse(code = 400, message = "Datos inválidos"),
            @ApiResponse(code = 409, message = "Partida llena")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearPartida(@PathParam("jugadorId") String jugadorId)
            throws DatosInvalidosException, PartidaLlenaException {
        // Llama directamente a la implementación real de Franklin con 9 bots
        Partida p = PartidaManagerImpl.getInstance().crearPartida(jugadorId, 9);
        ResultadosManagerImpl.getInstance().inicializarMarcador(p);
        return Response.status(201).entity(p).build();
    }

    @POST
    @Path("/{id}/iniciar")
    @ApiOperation(value = "Cambiar estado de la partida a EN_CURSO")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Partida iniciada"),
            @ApiResponse(code = 404, message = "Partida no encontrada"),
            @ApiResponse(code = 409, message = "Estado de partida inválido")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response iniciarPartida(@PathParam("id") String idPartida)
            throws PartidaNotFoundException, EstadoPartidaInvalidoException {
        Partida p = PartidaManagerImpl.getInstance().iniciarPartida(idPartida);
        return Response.status(200).entity(p).build();
    }

    @GET
    @ApiOperation(value = "Obtener todas las partidas")
    @ApiResponse(code = 200, message = "Éxito", response = Partida.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartidas() {
        List<Partida> partidas = PartidaManagerImpl.getInstance().getPartidas();
        return Response.status(200).entity(partidas).build();
    }

    @POST
    @Path("/{id}/finalizar")
    @ApiOperation(value = "Finalizar la partida")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Partida finalizada"),
            @ApiResponse(code = 404, message = "Partida no encontrada"),
            @ApiResponse(code = 409, message = "Estado inválido")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response finalizarPartida(@PathParam("id") String idPartida)
            throws PartidaNotFoundException, EstadoPartidaInvalidoException {
        Partida p = PartidaManagerImpl.getInstance().finalizarPartida(idPartida);
        return Response.status(200).entity(p).build();
    }
}