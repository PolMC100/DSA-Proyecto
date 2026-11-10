package edu.upc.dsa.services;


import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.domain.repository.PartidaManagerImpl;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import edu.upc.dsa.exceptions.EstadoPartidaInvalidoException;
import edu.upc.dsa.exceptions.PartidaNotFoundException;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/partidas", description = "Servicio de control de partidas")
@Path("/partidas")

public class PartidaService {
    @POST
    @Path("/crear/{jugadorId}")
    @ApiOperation(value = "Crear una partida con 1 jugador y 9 bots como máximo")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Partida creada", response = Partida.class),
            @ApiResponse(code = 400, message = "ID de jugador inválido")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearPartida(@PathParam("jugadorId") String jugadorId) throws DatosInvalidosException {
        // Objeto mock temporal para devolver respuesta 201 sin errores de compilación
        //Completar por Franklin
        Partida mock = new Partida("P-101");
        return Response.status(201).entity(mock).build();
    }

    @POST
    @Path("/{id}/iniciar")
    @ApiOperation(value = "Cambiar el estado de la partida a EN_CURSO")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Partida iniciada"),
            @ApiResponse(code = 404, message = "Partida no encontrada"),
            @ApiResponse(code = 409, message = "Estado de partida inválido")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response iniciarPartida(@PathParam("id") String id) throws PartidaNotFoundException, EstadoPartidaInvalidoException {
        PartidaManagerImpl.getInstance().iniciarPartida(id);
        return Response.status(200).entity("Partida iniciada").build();
    }
}
