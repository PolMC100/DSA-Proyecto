package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.*;
import edu.upc.dsa.domain.models.Inventario.Comprable;
import edu.upc.dsa.domain.models.Partida.Estado;
import java.util.List;
import java.util.Map;

/** Operaciones del juego, siguiendo el componente único de DSA-Minim-Real. */
public interface JuegoManager {
    Jugador registrarJugador(String usuario, String password);
    Jugador autenticarJugador(String usuario, String password);
    Jugador registrarJugador(String id, String nombre, boolean bot);
    Jugador getJugador(String id);
    List<Jugador> getJugadores();
    boolean existeJugadorHumano(String id);
    void sumarExperienciaYCreditos(String id, int experiencia, int creditos);
    int getNivel(String id);
    int getCreditos(String id);
    boolean cumpleNivelRequerido(String id, int requisito);
    void descontarCreditos(String id, int precio);
    boolean aplicarRecompensaPartida(String partida, String jugador, int experiencia, int creditos);
    void setExperienciaPorNivel(int paso);
    int getExperienciaPorNivel();

    Map<String, Arma> getArmas();
    Map<String, Granada> getGranadas();
    Map<String, Complemento> getComplementos();
    void addObjeto(Comprable objeto);
    void cargarCatalogoInicial();
    Jugador comprar(String jugadorId, String itemId, int cantidad);
    boolean consumirGranada(String jugadorId, String nombre);

    Partida crearPartida(String humano, int bots);
    Partida getPartida(String id);
    List<Partida> getPartidas();
    List<Partida> getPartidasPorEstado(Estado estado);
    Partida iniciarPartida(String id);
    void registrarBaja(String partida, String asesino, String victima);
    void reaparecer(String partida, String jugador);
    Partida finalizarPartida(String id);
    long getSegundosRestantes(String id);
    Scoreboard getScoreboard(String id);
    void aplicarRecompensas(String id);

    /** Restablece los datos y el catálogo; no reutiliza identificadores de partida. */
    void limpiar();
    void cargarDatosPrueba(); // Se crea una partida pre cargada para hacer pruebas y test hasta que esté todo funcionando
}
