package edu.upc.dsa.services;

import edu.upc.dsa.domain.dtos.CompraDTO;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/tienda", description = "Servicio de catálogo y compras")
@Path("/tienda")

public class TiendaService {
    @POST
    @Path("/comprar")
    @ApiOperation(value = "Comprar un arma, granada o complemento")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Compra correcta"),
            @ApiResponse(code = 400, message = "Datos de compra inválidos"),
            @ApiResponse(code = 403, message = "Saldo insuficiente o nivel bloqueado")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response comprarObjeto(CompraDTO compra) {

        // A modificar por Cristian se deja indicado

        // Invocación al gestor tras git pull:
        // TiendaManagerImpl.getInstance().comprar(compra.getJugadorId(), compra.getItemId());
        return Response.status(200).entity(compra).build();
    }
}
