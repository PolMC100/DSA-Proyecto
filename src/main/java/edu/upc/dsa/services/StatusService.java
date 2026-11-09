package edu.upc.dsa.services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/** Comprobación de infraestructura independiente de los módulos del juego. */
@Api(tags = "Estado del servidor")
@Path("status")
@Produces(MediaType.APPLICATION_JSON)
public class StatusService {
    private static final Logger LOGGER = LogManager.getLogger(StatusService.class);

    @GET
    @ApiOperation(value = "Comprobar que el servidor está disponible", response = Status.class)
    public Status getStatus() {
        LOGGER.info("Consulta del estado del servidor");
        return new Status("ok");
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
