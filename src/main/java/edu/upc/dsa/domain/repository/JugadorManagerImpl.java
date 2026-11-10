package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Jugador;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import edu.upc.dsa.exceptions.JugadorDuplicadoException;
import edu.upc.dsa.exceptions.JugadorNotFoundException;
import edu.upc.dsa.exceptions.SaldoInsuficienteException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/** Gestor Singleton en memoria. synchronized protege saldo y recompensas simultáneas. */
public class JugadorManagerImpl implements JugadorManager {
    private static final Logger logger = LogManager.getLogger(JugadorManagerImpl.class);
    public static final int EXPERIENCIA_POR_NIVEL_PROVISIONAL = 100;
    private static JugadorManager instance;
    private final HashMap<String, Jugador> jugadores;
    // Partida -> identificadores de los jugadores que ya cobraron. Sin concatenar claves.
    private final HashMap<String, HashSet<String>> recompensasAplicadas;
    private int experienciaPorNivel;

    private JugadorManagerImpl() {
        this.jugadores = new HashMap<>();
        this.recompensasAplicadas = new HashMap<>();
        this.experienciaPorNivel = EXPERIENCIA_POR_NIVEL_PROVISIONAL;
    }

    public static synchronized JugadorManager getInstance() {
        if (instance == null) instance = new JugadorManagerImpl();
        return instance;
    }

    @Override
    public synchronized Jugador registrarJugador(String idJugador, String nombre, boolean bot)
            throws DatosInvalidosException, JugadorDuplicadoException {
        logger.info("registrarJugador(" + idJugador + ", " + nombre + ", " + bot + ")");
        validarTexto(idJugador, "idJugador");
        validarTexto(nombre, "nombre");
        if (this.jugadores.containsKey(idJugador)) {
            logger.error("Jugador duplicado: " + idJugador);
            throw new JugadorDuplicadoException("Ya existe el jugador " + idJugador);
        }
        Jugador jugador = new Jugador(idJugador, nombre, bot);
        this.jugadores.put(idJugador, jugador);
        logger.info("registrarJugador() -> " + jugador);
        return new Jugador(jugador);
    }

    @Override
    public synchronized Jugador getJugador(String idJugador)
            throws DatosInvalidosException, JugadorNotFoundException {
        logger.info("getJugador(" + idJugador + ")");
        Jugador jugador = buscarJugador(idJugador);
        logger.info("getJugador() -> " + jugador);
        return new Jugador(jugador);
    }

    @Override
    public synchronized List<Jugador> getJugadores() {
        logger.info("getJugadores()");
        List<Jugador> lista = new ArrayList<>();
        for (Jugador jugador : this.jugadores.values()) lista.add(new Jugador(jugador));
        logger.info("getJugadores() -> " + lista.size() + " jugadores");
        return lista;
    }

    @Override
    public synchronized boolean existeJugadorHumano(String idJugador) throws DatosInvalidosException {
        logger.info("existeJugadorHumano(" + idJugador + ")");
        validarTexto(idJugador, "idJugador");
        Jugador jugador = this.jugadores.get(idJugador);
        boolean existe = jugador != null && !jugador.isBot();
        logger.info("existeJugadorHumano() -> " + existe);
        return existe;
    }

    @Override
    public synchronized void sumarExperienciaYCreditos(String idJugador, int experiencia, int creditos)
            throws DatosInvalidosException, JugadorNotFoundException {
        logger.info("sumarExperienciaYCreditos(" + idJugador + ", " + experiencia + ", " + creditos + ")");
        Jugador jugador = buscarJugador(idJugador);
        sumar(jugador, experiencia, creditos);
        logger.info("sumarExperienciaYCreditos() -> " + jugador);
    }

    @Override
    public synchronized int getNivel(String idJugador) throws DatosInvalidosException, JugadorNotFoundException {
        logger.info("getNivel(" + idJugador + ")");
        int nivel = buscarJugador(idJugador).getNivel();
        logger.info("getNivel() -> " + nivel);
        return nivel;
    }

    @Override
    public synchronized int getCreditos(String idJugador) throws DatosInvalidosException, JugadorNotFoundException {
        logger.info("getCreditos(" + idJugador + ")");
        int creditos = buscarJugador(idJugador).getCreditos();
        logger.info("getCreditos() -> " + creditos);
        return creditos;
    }

    @Override
    public synchronized boolean cumpleNivelRequerido(String idJugador, int nivelRequerido)
            throws DatosInvalidosException, JugadorNotFoundException {
        logger.info("cumpleNivelRequerido(" + idJugador + ", " + nivelRequerido + ")");
        if (nivelRequerido < 1) throw datosInvalidos("El nivel requerido debe ser >= 1");
        boolean cumple = buscarJugador(idJugador).getNivel() >= nivelRequerido;
        logger.info("cumpleNivelRequerido() -> " + cumple);
        return cumple;
    }

    @Override
    public synchronized void descontarCreditos(String idJugador, int precio)
            throws DatosInvalidosException, JugadorNotFoundException, SaldoInsuficienteException {
        logger.info("descontarCreditos(" + idJugador + ", " + precio + ")");
        if (precio < 0) throw datosInvalidos("El precio debe ser >= 0");
        Jugador jugador = buscarJugador(idJugador);
        if (jugador.getCreditos() < precio) {
            logger.error("Saldo insuficiente para " + idJugador + ": precio=" + precio);
            throw new SaldoInsuficienteException("El jugador " + idJugador + " no tiene suficientes créditos");
        }
        jugador.setCreditos(jugador.getCreditos() - precio);
        logger.info("descontarCreditos() -> saldo=" + jugador.getCreditos());
    }

    @Override
    public synchronized boolean aplicarRecompensaPartida(String idPartida, String idJugador,
                                                        int experiencia, int creditos)
            throws DatosInvalidosException, JugadorNotFoundException {
        logger.info("aplicarRecompensaPartida(" + idPartida + ", " + idJugador
                + ", " + experiencia + ", " + creditos + ")");
        validarTexto(idPartida, "idPartida");
        validarIncrementos(experiencia, creditos);
        Jugador jugador = buscarJugador(idJugador);
        HashSet<String> premiados = this.recompensasAplicadas.get(idPartida);
        if (premiados != null && premiados.contains(idJugador)) {
            logger.info("aplicarRecompensaPartida() -> false: recompensa ya aplicada");
            return false;
        }
        sumar(jugador, experiencia, creditos);
        // Solo registrar después de aplicar correctamente ambos incrementos.
        if (premiados == null) {
            premiados = new HashSet<>();
            this.recompensasAplicadas.put(idPartida, premiados);
        }
        premiados.add(idJugador);
        logger.info("aplicarRecompensaPartida() -> true: " + jugador);
        return true;
    }

    @Override
    public synchronized void setExperienciaPorNivel(int experienciaPorNivel) throws DatosInvalidosException {
        logger.info("setExperienciaPorNivel(" + experienciaPorNivel + ")");
        if (experienciaPorNivel <= 0) throw datosInvalidos("La experiencia por nivel debe ser > 0");
        // Validar todos los niveles antes de modificar configuración o perfiles.
        for (Jugador jugador : this.jugadores.values()) calcularNivel(jugador.getExperiencia(), experienciaPorNivel);
        this.experienciaPorNivel = experienciaPorNivel;
        for (Jugador jugador : this.jugadores.values()) {
            jugador.setNivel(calcularNivel(jugador.getExperiencia(), experienciaPorNivel));
        }
        logger.info("setExperienciaPorNivel() -> " + this.experienciaPorNivel);
    }

    @Override
    public synchronized int getExperienciaPorNivel() {
        logger.info("getExperienciaPorNivel()");
        logger.info("getExperienciaPorNivel() -> " + this.experienciaPorNivel);
        return this.experienciaPorNivel;
    }

    @Override
    public synchronized void limpiar() {
        logger.info("limpiar()");
        this.jugadores.clear();
        this.recompensasAplicadas.clear();
        this.experienciaPorNivel = EXPERIENCIA_POR_NIVEL_PROVISIONAL;
        logger.info("limpiar() -> sin jugadores ni recompensas; configuración restablecida");
    }

    private Jugador buscarJugador(String idJugador) throws DatosInvalidosException, JugadorNotFoundException {
        validarTexto(idJugador, "idJugador");
        Jugador jugador = this.jugadores.get(idJugador);
        if (jugador == null) {
            logger.error("Jugador no encontrado: " + idJugador);
            throw new JugadorNotFoundException("No existe el jugador " + idJugador);
        }
        return jugador;
    }

    private void validarTexto(String texto, String campo) throws DatosInvalidosException {
        if (texto == null || texto.isBlank()) throw datosInvalidos("Hace falta " + campo);
    }

    private void validarIncrementos(int experiencia, int creditos) throws DatosInvalidosException {
        if (experiencia < 0 || creditos < 0) throw datosInvalidos("Experiencia y créditos deben ser >= 0");
    }

    private int calcularNivel(int experiencia, int paso) throws DatosInvalidosException {
        long nivel = 1L + experiencia / paso;
        if (nivel > Integer.MAX_VALUE) throw datosInvalidos("El nivel supera el máximo representable");
        return (int) nivel;
    }

    private void sumar(Jugador jugador, int experiencia, int creditos) throws DatosInvalidosException {
        validarIncrementos(experiencia, creditos);
        long nuevaExperiencia = (long) jugador.getExperiencia() + experiencia;
        long nuevosCreditos = (long) jugador.getCreditos() + creditos;
        if (nuevaExperiencia > Integer.MAX_VALUE || nuevosCreditos > Integer.MAX_VALUE) {
            throw datosInvalidos("La experiencia o los créditos superan el máximo representable");
        }
        int nivel = calcularNivel((int) nuevaExperiencia, this.experienciaPorNivel);
        jugador.setExperiencia((int) nuevaExperiencia);
        jugador.setCreditos((int) nuevosCreditos);
        jugador.setNivel(nivel);
    }

    private DatosInvalidosException datosInvalidos(String mensaje) {
        logger.error(mensaje);
        return new DatosInvalidosException(mensaje);
    }
}
