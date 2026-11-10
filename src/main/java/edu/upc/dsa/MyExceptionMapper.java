package edu.upc.dsa;
import edu.upc.dsa.exceptions.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


//Unificador de excepciones a código de respuesta HTTP común con formato JSON para facilitar las cosas
@Provider
public class MyExceptionMapper implements ExceptionMapper<Throwable> {

    public static class ErrorEntity {
        private int code;
        private String message;

        public ErrorEntity() {
        }

        public ErrorEntity(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }

    }

    // Se plantean inicialmente los errores mas comunes:
    // 404 (NOT FOUND)
    // 400 (BAD REQUEST)
    // 409 (CONFLICT)
    // 403 (FORBIDDEN) --> Cuando jugador no tiene saldo e intenta comprar o no tiene nivel para algo.
    // 500 si el error no es idenficiado
    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof PartidaNotFoundException ||
                exception instanceof ParticipanteNotFoundException ||
                exception instanceof JugadorNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorEntity(404, exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        else if (exception instanceof DatosInvalidosException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorEntity(400, exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        else if (exception instanceof EstadoPartidaInvalidoException || exception instanceof PartidaLlenaException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorEntity(409, exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        else if (exception instanceof SaldoInsuficienteException || exception instanceof NivelBloqueadoException) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorEntity(403, exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorEntity(500, "Error del servidor: " + exception.getMessage()))
                .type(MediaType.APPLICATION_JSON).build();
    }
}
