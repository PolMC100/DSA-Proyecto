package edu.upc.dsa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.NoSuchElementException;

/** Convierte excepciones estándar en una respuesta JSON común. */
@Provider
public class MyExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOGGER = LogManager.getLogger(MyExceptionMapper.class);

    public static class ErrorEntity {
        private int code;
        private String message;
        public ErrorEntity() { }
        public ErrorEntity(int code, String message) { this.code = code; this.message = message; }
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @Override
    public Response toResponse(Exception exception) {
        int codigo = exception instanceof WebApplicationException web ? web.getResponse().getStatus()
                : exception instanceof NoSuchElementException ? 404
                : exception instanceof SecurityException ? 403
                : exception instanceof IllegalArgumentException || exception instanceof ArithmeticException ? 400
                : exception instanceof IllegalStateException ? 409 : 500;
        if (codigo >= 500) LOGGER.error("Error al procesar la solicitud", exception);
        String mensaje = codigo >= 500 ? "Error interno del servidor" : exception.getMessage();
        Response.ResponseBuilder respuesta = exception instanceof WebApplicationException web
                ? Response.fromResponse(web.getResponse()) : Response.status(codigo);
        return respuesta.type(MediaType.APPLICATION_JSON).entity(new ErrorEntity(codigo, mensaje)).build();
    }
}
