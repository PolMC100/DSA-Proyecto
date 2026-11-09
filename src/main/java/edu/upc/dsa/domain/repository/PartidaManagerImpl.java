package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.EstadoPartida;
import edu.upc.dsa.domain.models.Participante;
import edu.upc.dsa.domain.models.Partida;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import edu.upc.dsa.exceptions.EstadoPartidaInvalidoException;
import edu.upc.dsa.exceptions.ParticipanteNotFoundException;
import edu.upc.dsa.exceptions.PartidaLlenaException;
import edu.upc.dsa.exceptions.PartidaNotFoundException;
import edu.upc.dsa.util.Reloj;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implementación en memoria del gestor de partidas (Singleton).
 */
public class PartidaManagerImpl implements PartidaManager {

    private static final Logger logger = LogManager.getLogger(PartidaManagerImpl.class);
    private static PartidaManager instance;

    private final HashMap<String, Partida> partidas;     // búsqueda por id en O(1)
    private final List<PartidaListener> listeners;       // Observer: módulo de resultados, etc.
    private Reloj reloj;
    private int contador;                                // para generar ids "P1", "P2"...

    private PartidaManagerImpl() {
        this.partidas = new HashMap<>();
        this.listeners = new ArrayList<>();
        this.reloj = Reloj.SISTEMA;
        this.contador = 0;
    }

    public static PartidaManager getInstance() {
        if (instance == null) instance = new PartidaManagerImpl();
        return instance;
    }

    @Override
    public Partida crearPartida(String idJugadorHumano, int numBots)
            throws DatosInvalidosException, PartidaLlenaException {
        logger.info("crearPartida(" + idJugadorHumano + ", " + numBots + ")");
        if (idJugadorHumano == null || idJugadorHumano.isBlank() || numBots < 0) {
            logger.error("crearPartida: datos inválidos");
            throw new DatosInvalidosException("Hace falta un jugador humano y un número de bots >= 0");
        }
        if (1 + numBots > Partida.MAX_PARTICIPANTES) {
            logger.error("crearPartida: " + (1 + numBots) + " participantes, máximo " + Partida.MAX_PARTICIPANTES);
            throw new PartidaLlenaException("Máximo " + Partida.MAX_PARTICIPANTES + " participantes (1 humano + "
                    + (Partida.MAX_PARTICIPANTES - 1) + " bots)");
        }
        this.contador++;
        Partida p = new Partida("P" + this.contador, idJugadorHumano);
        p.addParticipante(new Participante(idJugadorHumano, false));
        for (int i = 1; i <= numBots; i++) {
            p.addParticipante(new Participante("bot-" + i, true));
        }
        this.partidas.put(p.getId(), p);
        logger.info("crearPartida() -> " + p);
        return p;
    }

    @Override
    public Partida getPartida(String idPartida) throws PartidaNotFoundException {
        logger.info("getPartida(" + idPartida + ")");
        Partida p = this.partidas.get(idPartida);
        if (p == null) {
            logger.error("Partida no encontrada: " + idPartida);
            throw new PartidaNotFoundException("No existe la partida " + idPartida);
        }
        logger.info("getPartida() -> " + p);
        return p;
    }

    @Override
    public List<Partida> getPartidas() {
        logger.info("getPartidas()");
        List<Partida> lista = new ArrayList<>(this.partidas.values());
        logger.info("getPartidas() -> " + lista.size() + " partidas");
        return lista;
    }

    @Override
    public List<Partida> getPartidasPorEstado(EstadoPartida estado) {
        logger.info("getPartidasPorEstado(" + estado + ")");
        List<Partida> lista = new ArrayList<>();
        for (Partida p : this.partidas.values()) {
            actualizarSiHaCaducado(p);
            if (p.getEstado() == estado) lista.add(p);
        }
        logger.info("getPartidasPorEstado() -> " + lista.size() + " partidas");
        return lista;
    }

    @Override
    public Partida iniciarPartida(String idPartida)
            throws PartidaNotFoundException, EstadoPartidaInvalidoException {
        logger.info("iniciarPartida(" + idPartida + ")");
        Partida p = getPartida(idPartida);
        exigirEstado(p, EstadoPartida.PENDIENTE);
        p.setEstado(EstadoPartida.EN_CURSO);
        p.setInicioMillis(this.reloj.ahoraMillis());
        logger.info("iniciarPartida() -> " + p);
        return p;
    }

    @Override
    public void registrarBaja(String idPartida, String idAsesino, String idVictima)
            throws PartidaNotFoundException, ParticipanteNotFoundException, EstadoPartidaInvalidoException {
        logger.info("registrarBaja(" + idPartida + ", " + idAsesino + ", " + idVictima + ")");
        Partida p = getPartida(idPartida);
        actualizarSiHaCaducado(p);
        exigirEstado(p, EstadoPartida.EN_CURSO);
        buscarParticipante(p, idAsesino);
        Participante victima = buscarParticipante(p, idVictima);
        if (!victima.isVivo()) {
            logger.error("registrarBaja: " + idVictima + " ya está muerto");
            throw new EstadoPartidaInvalidoException(idVictima + " ya está muerto, tiene que reaparecer antes");
        }
        victima.setVivo(false);
        for (PartidaListener l : this.listeners) l.onBaja(p, idAsesino, idVictima);
        logger.info("registrarBaja() -> " + victima);
    }

    @Override
    public void reaparecer(String idPartida, String idJugador)
            throws PartidaNotFoundException, ParticipanteNotFoundException, EstadoPartidaInvalidoException {
        logger.info("reaparecer(" + idPartida + ", " + idJugador + ")");
        Partida p = getPartida(idPartida);
        actualizarSiHaCaducado(p);
        exigirEstado(p, EstadoPartida.EN_CURSO);
        Participante part = buscarParticipante(p, idJugador);
        if (part.isVivo()) {
            logger.error("reaparecer: " + idJugador + " ya está vivo");
            throw new EstadoPartidaInvalidoException(idJugador + " está vivo, no puede reaparecer");
        }
        part.setVivo(true);
        logger.info("reaparecer() -> " + part);
    }

    @Override
    public Partida finalizarPartida(String idPartida)
            throws PartidaNotFoundException, EstadoPartidaInvalidoException {
        logger.info("finalizarPartida(" + idPartida + ")");
        Partida p = getPartida(idPartida);
        actualizarSiHaCaducado(p);
        exigirEstado(p, EstadoPartida.EN_CURSO);
        finalizar(p, this.reloj.ahoraMillis());
        logger.info("finalizarPartida() -> " + p);
        return p;
    }

    @Override
    public long getSegundosRestantes(String idPartida) throws PartidaNotFoundException {
        logger.info("getSegundosRestantes(" + idPartida + ")");
        Partida p = getPartida(idPartida);
        actualizarSiHaCaducado(p);
        long restantes;
        if (p.getEstado() == EstadoPartida.PENDIENTE) restantes = Partida.DURACION_SEGUNDOS;
        else if (p.getEstado() == EstadoPartida.FINALIZADA) restantes = 0;
        else restantes = (finPrevistoMillis(p) - this.reloj.ahoraMillis()) / 1000;
        logger.info("getSegundosRestantes() -> " + restantes);
        return restantes;
    }

    @Override
    public boolean comprobarTiempo(String idPartida) throws PartidaNotFoundException {
        logger.info("comprobarTiempo(" + idPartida + ")");
        Partida p = getPartida(idPartida);
        actualizarSiHaCaducado(p);
        boolean finalizada = p.getEstado() == EstadoPartida.FINALIZADA;
        logger.info("comprobarTiempo() -> finalizada=" + finalizada);
        return finalizada;
    }

    @Override
    public void addListener(PartidaListener listener) {
        logger.info("addListener(" + listener + ")");
        this.listeners.add(listener);
    }

    @Override
    public void setReloj(Reloj reloj) {
        this.reloj = reloj;
    }

    @Override
    public int size() {
        return this.partidas.size();
    }

    @Override
    public void clear() {
        logger.info("clear()");
        this.partidas.clear();
        this.listeners.clear();
        this.reloj = Reloj.SISTEMA;
        this.contador = 0;
    }

    // ---------- métodos privados de ayuda ----------

    private long finPrevistoMillis(Partida p) {
        return p.getInicioMillis() + Partida.DURACION_SEGUNDOS * 1000L;
    }

    /** Si la partida está en curso y ya han pasado los 5 minutos, la cierra. */
    private void actualizarSiHaCaducado(Partida p) {
        if (p.getEstado() == EstadoPartida.EN_CURSO && this.reloj.ahoraMillis() >= finPrevistoMillis(p)) {
            logger.warn("La partida " + p.getId() + " ha llegado a los 5 minutos: se finaliza");
            finalizar(p, finPrevistoMillis(p));
        }
    }

    /** Pasa la partida a FINALIZADA y avisa a los listeners (solo puede pasar una vez). */
    private void finalizar(Partida p, long momento) {
        p.setEstado(EstadoPartida.FINALIZADA);
        p.setFinMillis(momento);
        for (PartidaListener l : this.listeners) l.onPartidaFinalizada(p);
    }

    private void exigirEstado(Partida p, EstadoPartida esperado) throws EstadoPartidaInvalidoException {
        if (p.getEstado() != esperado) {
            logger.error("Partida " + p.getId() + " en estado " + p.getEstado() + ", se esperaba " + esperado);
            throw new EstadoPartidaInvalidoException("La partida " + p.getId() + " está " + p.getEstado()
                    + " y para esta acción tiene que estar " + esperado);
        }
    }

    private Participante buscarParticipante(Partida p, String idJugador) throws ParticipanteNotFoundException {
        Participante part = p.buscarParticipante(idJugador);
        if (part == null) {
            logger.error("El jugador " + idJugador + " no participa en la partida " + p.getId());
            throw new ParticipanteNotFoundException("El jugador " + idJugador + " no participa en la partida " + p.getId());
        }
        return part;
    }
}
