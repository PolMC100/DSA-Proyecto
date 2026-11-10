package edu.upc.dsa.services;

import edu.upc.dsa.domain.dtos.JugadorDTO;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/jugadores", description = "Servicio para gestionar el perfil de jugadores")
@Path("/jugadores")

public class JugadorService {
    @POST
    @ApiOperation(value = "Registrar un nuevo jugador")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Jugador creado", response = JugadorDTO.class),
            @ApiResponse(code = 400, message = "Datos no válidos")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    public Response crearJugador(JugadorDTO jugador) {
        if (jugador.getNombre() == null || jugador.getNombre().isEmpty()) {
            return Response.status(400).entity("Escribir un nombre").build();
        }
        //Dejo indicaco aqui lo que Cristian tiene que meter, ademas una prueba a ver si funciona, esto se cambia.
        // Invocación al gestor tras git pull:
        // Jugador j = JugadorManagerImpl.getInstance().addJugador(jugador.getNombre());

        jugador.setId("J-101");
        return Response.status(201).entity(jugador).build();
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

        //Dejo indicaco aqui lo que Cristian tiene que meter, ademas una prueba a ver si funciona, esto se cambia.
        // Invocación al gestor tras git pull:
        // Jugador j = JugadorManagerImpl.getInstance().getJugador(id);
        JugadorDTO mock = new JugadorDTO(id, "JugadorPrueba", 1, 0, 500);
        return Response.status(200).entity(mock).build();
    }
}