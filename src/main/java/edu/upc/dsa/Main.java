package edu.upc.dsa;

import edu.upc.dsa.domain.repository.PartidaManagerImpl;
import edu.upc.dsa.domain.repository.ResultadosManagerImpl;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerContextService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import java.io.IOException;
import java.net.URI;

public final class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    public static final String BASE_URI = "http://localhost:8080/dsaApp/";

    private Main() {
    }

    public static HttpServer startServer() throws IOException {
        return startServer(URI.create(BASE_URI));
    }

    public static HttpServer startServer(URI uri) throws IOException {
        // Observer: el módulo de resultados escucha las bajas y el final de cada partida.
        // addListener ignora repeticiones, así reiniciar el servidor no duplica los avisos.
        PartidaManagerImpl.getInstance().addListener(ResultadosManagerImpl.getInstance());

        ResourceConfig config = new ResourceConfig().packages("edu.upc.dsa")
                .register(MoxyJsonFeature.class)
                .register(io.swagger.jaxrs.listing.ApiListingResource.class)
                .register(io.swagger.jaxrs.listing.SwaggerSerializers.class)
                .property(ServerProperties.WADL_FEATURE_DISABLE, true);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config, false);
        StaticHttpHandler staticFiles = new StaticHttpHandler("./public/");
        staticFiles.setFileCacheEnabled(false);
        server.getServerConfiguration().addHttpHandler(staticFiles, "/");

        try {
            server.start();
            int port = server.getListeners().iterator().next().getPort();
            BeanConfig swagger = new BeanConfig();
            swagger.setHost(uri.getHost() + ":" + port);
            swagger.setBasePath(uri.getPath().replaceAll("/$", ""));
            swagger.setSchemes(new String[]{"http"});
            swagger.setResourcePackage("edu.upc.dsa.services");
            swagger.setTitle("DSA-Proyecto API");
            swagger.setDescription("API del shooter del aula de DSA");
            swagger.setVersion("1.0-SNAPSHOT");
            swagger.setScan(true);
            // Swagger mantiene registros estáticos: renovar el contexto al reiniciar
            // evita publicar el puerto y la configuración del primer servidor.
            new SwaggerContextService().withSwaggerConfig(swagger).withScanner(swagger)
                    .initConfig(swagger.getSwagger()).initScanner();

            LOGGER.info("Servidor iniciado en http://{}:{}{}", uri.getHost(), port, uri.getPath());
            LOGGER.info("Swagger UI: http://{}:{}/swagger/", uri.getHost(), port);
            return server;
        } catch (IOException | RuntimeException e) {
            server.shutdownNow();
            LOGGER.error("No se ha podido iniciar el servidor en {}", uri, e);
            throw e;
        }
    }

    public static void stopServer(HttpServer server) {
        server.shutdownNow();
        LOGGER.info("Servidor detenido");
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = startServer();
        Thread shutdownHook = new Thread(() -> stopServer(server), "dsa-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        LOGGER.info("Pulsa Enter o Ctrl+C para detener el servidor");
        try {
            System.in.read();
        } finally {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
                stopServer(server);
            } catch (IllegalStateException ignored) {
            }
        }
    }
}
