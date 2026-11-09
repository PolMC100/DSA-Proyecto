package edu.upc.dsa.services;

import io.swagger.annotations.ApiOperation;
import io.swagger.models.Swagger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/** Publica el documento de este servidor sin usar la caché global de Swagger. */
@Path("swagger.json")
public class SwaggerService {
    public static final String MODEL_PROPERTY = "dsa.swagger.model";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Definición de la API", hidden = true)
    public Swagger getSwagger(@Context Application application) {
        return (Swagger) application.getProperties().get(MODEL_PROPERTY);
    }
}
