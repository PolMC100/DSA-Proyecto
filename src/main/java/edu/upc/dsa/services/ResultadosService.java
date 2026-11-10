package edu.upc.dsa.services;
import edu.upc.dsa.domain.models.Scoreboard;
import edu.upc.dsa.domain.repository.ResultadosManagerImpl;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/resultados", description = "Servicio de marcadores y clasi")
@Path("/resultados")

public class ResultadosService {
    @GET
    @Path("/scoreboard/{partidaId}")
    @ApiOperation(value = "Obtener el marcador de una partida por su ID")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Éxito", response = Scoreboard.class),
            @ApiResponse(code = 404, message = "Marcador no encontrado")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScoreboard(@PathParam("partidaId") String partidaId) {
        Scoreboard sb = ResultadosManagerImpl.getInstance().obtenerMarcadorActual(partidaId);
        if (sb == null) {
            return Response.status(404).entity("Marcador no encontrado para la partida " + partidaId).build();
        }
        return Response.status(200).entity(sb).build();
    }
}
