package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.*;
import edu.upc.dsa.domain.models.Inventario.Comprable;
import edu.upc.dsa.domain.models.Partida.Estado;
import edu.upc.dsa.domain.models.Partida.Participante;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.function.LongSupplier;

/** Un componente en memoria para perfiles, catálogo, compras, partidas y resultados. */
public class JuegoManagerImpl implements JuegoManager {
    private static final Logger LOGGER = LogManager.getLogger(JuegoManagerImpl.class);
    private static final JuegoManagerImpl INSTANCE = new JuegoManagerImpl();
    private static final Comparator<ResultadoJugador> CLASIFICACION =
            Comparator.comparingInt(ResultadoJugador::getPuntuacion).reversed()
                    .thenComparing(Comparator.comparingInt(ResultadoJugador::getBajas).reversed())
                    .thenComparingInt(ResultadoJugador::getMuertes);

    private final Map<String, Jugador> jugadores = new HashMap<>();
    private final Map<String, Comprable> catalogo = new HashMap<>();
    private final Map<String, Partida> partidas = new HashMap<>();
    private final Map<String, Scoreboard> marcadores = new HashMap<>();
    private final Map<String, Set<String>> recompensasPagadas = new HashMap<>();
    private final LongSupplier reloj;
    private int experienciaPorNivel = 100;
    private long contadorPartidas;

    public JuegoManagerImpl() { this(System::currentTimeMillis); }

    /** Constructor para pruebas con una hora controlada. */
    public JuegoManagerImpl(LongSupplier reloj) {
        this.reloj = Objects.requireNonNull(reloj);
        cargarCatalogoInicial();
    }

    public static JuegoManagerImpl getInstance() { return INSTANCE; }

    // Perfiles y progresión

    @Override
    public synchronized Jugador registrarJugador(String usuario, String password) {
        String clave = validarCredenciales(usuario, password);
        if (buscarJugadorPorUsuario(clave) != null) throw new IllegalStateException("El usuario ya está registrado");
        Jugador jugador = new Jugador(UUID.randomUUID().toString(), usuario.trim(), false);
        jugador.setUsuario(clave);
        jugador.setPassword(password);
        jugadores.put(jugador.getIdJugador(), jugador);
        LOGGER.info("Jugador registrado: {}", jugador.getIdJugador());
        return new Jugador(jugador);
    }

    @Override
    public synchronized Jugador autenticarJugador(String usuario, String password) {
        String clave = validarCredenciales(usuario, password);
        Jugador jugador = buscarJugadorPorUsuario(clave);
        if (jugador == null || jugador.isBot() || !password.equals(jugador.getPassword())) {
            throw new SecurityException("Usuario o contraseña incorrectos");
        }
        return new Jugador(jugador);
    }

    private Jugador buscarJugadorPorUsuario(String usuario) {
        return jugadores.values().stream().filter(jugador -> usuario.equals(jugador.getUsuario())).findFirst().orElse(null);
    }

    private static String validarCredenciales(String usuario, String password) {
        validarTexto(usuario, "usuario");
        if (usuario.trim().length() < 3 || usuario.trim().length() > 30) {
            throw new IllegalArgumentException("El usuario debe tener entre 3 y 30 caracteres");
        }
        validarTexto(password, "contraseña");
        return usuario.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public synchronized Jugador registrarJugador(String id, String nombre, boolean bot) {
        validarTexto(id, "id");
        validarTexto(nombre, "nombre");
        if (jugadores.containsKey(id)) throw new IllegalStateException("Ya existe el jugador " + id);
        Jugador jugador = new Jugador(id, nombre, bot);
        jugadores.put(id, jugador);
        LOGGER.info("Jugador registrado: {}", id);
        return new Jugador(jugador);
    }

    private Jugador buscarJugador(String id) {
        validarTexto(id, "idJugador");
        Jugador jugador = jugadores.get(id);
        if (jugador == null) throw new NoSuchElementException("No existe el jugador " + id);
        return jugador;
    }

    @Override
    public synchronized Jugador getJugador(String id) { return new Jugador(buscarJugador(id)); }

    @Override
    public synchronized List<Jugador> getJugadores() {
        return new ArrayList<>(jugadores.values().stream().map(Jugador::new).toList());
    }

    @Override
    public synchronized boolean existeJugadorHumano(String id) {
        validarTexto(id, "idJugador");
        Jugador jugador = jugadores.get(id);
        return jugador != null && !jugador.isBot();
    }

    @Override
    public synchronized int getNivel(String id) { return buscarJugador(id).getNivel(); }
    @Override
    public synchronized int getCreditos(String id) { return buscarJugador(id).getCreditos(); }

    @Override
    public synchronized boolean cumpleNivelRequerido(String id, int requisito) {
        if (requisito < 0) throw new IllegalArgumentException("El requisito debe ser >= 0");
        return buscarJugador(id).getNivel() >= requisito;
    }

    @Override
    public synchronized void sumarExperienciaYCreditos(String id, int experiencia, int creditos) {
        sumar(buscarJugador(id), experiencia, creditos);
    }

    private void sumar(Jugador jugador, int experiencia, int creditos) {
        validarIncrementos(experiencia, creditos);
        long nuevaExperiencia = (long) jugador.getExperiencia() + experiencia;
        long nuevosCreditos = (long) jugador.getCreditos() + creditos;
        if (nuevaExperiencia > Integer.MAX_VALUE || nuevosCreditos > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("La experiencia o los créditos superan el máximo representable");
        }
        int nivel = calcularNivel((int) nuevaExperiencia, experienciaPorNivel);
        jugador.setExperiencia((int) nuevaExperiencia);
        jugador.setCreditos((int) nuevosCreditos);
        jugador.setNivel(nivel);
    }

    @Override
    public synchronized void descontarCreditos(String id, int precio) {
        if (precio < 0) throw new IllegalArgumentException("El precio debe ser >= 0");
        Jugador jugador = buscarJugador(id);
        if (jugador.getCreditos() < precio) throw new SecurityException("Créditos insuficientes");
        jugador.setCreditos(jugador.getCreditos() - precio);
    }

    @Override
    public synchronized boolean aplicarRecompensaPartida(String partida, String jugador, int experiencia, int creditos) {
        validarTexto(partida, "idPartida");
        validarIncrementos(experiencia, creditos);
        Jugador perfil = buscarJugador(jugador);
        Set<String> pagados = recompensasPagadas.get(partida);
        if (pagados != null && pagados.contains(jugador)) return false;
        sumar(perfil, experiencia, creditos);
        recompensasPagadas.computeIfAbsent(partida, id -> new HashSet<>()).add(jugador);
        return true;
    }

    @Override
    public synchronized void setExperienciaPorNivel(int paso) {
        if (paso <= 0) throw new IllegalArgumentException("La experiencia por nivel debe ser > 0");
        for (Jugador jugador : jugadores.values()) calcularNivel(jugador.getExperiencia(), paso);
        experienciaPorNivel = paso;
        for (Jugador jugador : jugadores.values()) jugador.setNivel(calcularNivel(jugador.getExperiencia(), paso));
    }

    @Override
    public synchronized int getExperienciaPorNivel() { return experienciaPorNivel; }

    private static int calcularNivel(int experiencia, int paso) {
        long nivel = 1L + experiencia / paso;
        if (nivel > Integer.MAX_VALUE) throw new IllegalArgumentException("El nivel supera el máximo representable");
        return (int) nivel;
    }

    private static void validarIncrementos(int experiencia, int creditos) {
        if (experiencia < 0 || creditos < 0) throw new IllegalArgumentException("Experiencia y créditos deben ser >= 0");
    }

    // Catálogo, compras e inventario

    @Override
    public synchronized Map<String, Arma> getArmas() { return objetosDeTipo(Arma.class); }
    @Override
    public synchronized Map<String, Granada> getGranadas() { return objetosDeTipo(Granada.class); }
    @Override
    public synchronized Map<String, Complemento> getComplementos() { return objetosDeTipo(Complemento.class); }

    private <T extends Comprable> Map<String, T> objetosDeTipo(Class<T> tipo) {
        Map<String, T> copia = new HashMap<>();
        catalogo.forEach((nombre, objeto) -> {
            if (tipo.isInstance(objeto)) copia.put(nombre, tipo.cast(copiarObjeto(objeto)));
        });
        return copia;
    }

    private static Comprable copiarObjeto(Comprable objeto) {
        return switch (objeto) {
            case Arma arma -> new Arma(arma);
            case Granada granada -> new Granada(granada);
            case Complemento complemento -> new Complemento(complemento);
            default -> throw new IllegalArgumentException("Tipo de objeto desconocido");
        };
    }

    @Override
    public synchronized void addObjeto(Comprable objeto) {
        if (objeto == null || objeto.getNombre() == null || objeto.getNombre().isBlank()
                || objeto.getPrecio() == null || objeto.getPrecio() < 0
                || objeto.getNivelRequerido() == null || objeto.getNivelRequerido() < 0) {
            throw new IllegalArgumentException("El objeto requiere nombre, precio >= 0 y nivel requerido >= 0");
        }
        Comprable anterior = catalogo.get(objeto.getNombre());
        if (anterior != null && anterior.getClass() != objeto.getClass()) {
            throw new IllegalArgumentException("El nombre ya identifica otro tipo de objeto");
        }
        catalogo.put(objeto.getNombre(), copiarObjeto(objeto));
    }

    @Override
    public synchronized Jugador comprar(String jugadorId, String itemId, int cantidad) {
        Jugador jugador = buscarJugador(jugadorId);
        validarTexto(itemId, "itemId");
        Comprable objeto = catalogo.get(itemId);
        if (objeto == null) throw new NoSuchElementException("No existe el objeto " + itemId);
        if (cantidad <= 0 || (!(objeto instanceof Granada) && cantidad != 1)) {
            throw new IllegalArgumentException("La cantidad debe ser positiva y las armas y complementos requieren una unidad");
        }
        long coste = (long) objeto.getPrecio() * cantidad;
        if (coste > Integer.MAX_VALUE) throw new IllegalArgumentException("El coste supera el máximo representable");
        if (jugador.getNivel() < objeto.getNivelRequerido()) throw new SecurityException("Nivel insuficiente");
        if (jugador.getCreditos() < coste) throw new SecurityException("Créditos insuficientes");
        Inventario inventario = new Inventario(jugador.getInventario());
        inventario.addObjeto(objeto, cantidad);
        jugador.setInventario(inventario);
        jugador.setCreditos(jugador.getCreditos() - (int) coste);
        LOGGER.info("Compra guardada: jugador={}, objeto={}, cantidad={}", jugadorId, itemId, cantidad);
        return new Jugador(jugador);
    }

    @Override
    public synchronized boolean consumirGranada(String jugadorId, String nombre) {
        validarTexto(nombre, "nombre de la granada");
        return buscarJugador(jugadorId).getInventario().useGranada(nombre) == 0;
    }

    @Override
    public synchronized void cargarCatalogoInicial() {
        catalogo.clear();
        addObjeto(new Arma("Pistola", 15, 3, 25, 300, 0));
        addObjeto(new Arma("Escopeta", 8, 1, 80, 800, 2));
        addObjeto(new Arma("Rifle", 30, 5, 45, 1200, 1));
        addObjeto(new Arma("Ametralladora", 100, 10, 15, 2000, 3));
        addObjeto(new Arma("Francotirador", 5, 1, 100, 2500, 4));
        addObjeto(new Granada("Fragmentación", 100, 5.0, "Explosión", 0, 500, 1));
        addObjeto(new Granada("Humo", 0, 8.0, "Reduce la visibilidad", 15, 400, 0));
        addObjeto(new Granada("Flash", 0, 6.0, "Ceguera temporal", 5, 600, 1));
        addObjeto(new Complemento("Chaleco", "Defensa", "Reduce el daño recibido", 700, 2));
        addObjeto(new Complemento("Casco", "Protección", "Reduce el daño recibido en la cabeza", 500, 1));
        addObjeto(new Complemento("Jetpack", "Movilidad", "Permite realizar saltos más altos y prolongados", 2000, 5));
    }

    // Partidas y resultados: el mismo gestor coordina ambos, sin listeners ni copias de estado paralelas.

    @Override
    public synchronized Partida crearPartida(String humano, int bots) {
        validarTexto(humano, "idJugadorHumano");
        if (bots < 0) throw new IllegalArgumentException("El número de bots debe ser >= 0");
        if (bots >= Partida.MAX_PARTICIPANTES) throw new IllegalStateException("Máximo diez participantes");
        if (!existeJugadorHumano(humano)) throw new NoSuchElementException("No existe el jugador humano " + humano);
        Partida partida = new Partida("P" + ++contadorPartidas, humano);
        partida.addParticipante(new Participante(humano, false));
        int indice = 1;
        for (int i = 0; i < bots; i++) {
            String id;
            do { id = "bot-" + indice++; } while (id.equals(humano));
            partida.addParticipante(new Participante(id, true));
        }
        Scoreboard marcador = new Scoreboard(partida.getId());
        for (Participante participante : partida.getParticipantes()) {
            marcador.getClasificacion().add(new ResultadoJugador(participante.getIdJugador(), partida.getId()));
        }
        partidas.put(partida.getId(), partida);
        marcadores.put(partida.getId(), marcador);
        LOGGER.info("Partida creada: {}", partida.getId());
        return new Partida(partida);
    }

    private Partida buscarPartida(String id) {
        Partida partida = partidas.get(id);
        if (partida == null) throw new NoSuchElementException("No existe la partida " + id);
        return partida;
    }

    @Override
    public synchronized Partida getPartida(String id) {
        Partida partida = buscarPartida(id);
        actualizarTiempo(partida);
        return new Partida(partida);
    }

    @Override
    public synchronized List<Partida> getPartidas() {
        List<Partida> copia = new ArrayList<>();
        for (Partida partida : partidas.values()) {
            actualizarTiempo(partida);
            copia.add(new Partida(partida));
        }
        return copia;
    }

    @Override
    public synchronized List<Partida> getPartidasPorEstado(Estado estado) {
        return getPartidas().stream().filter(p -> p.getEstado() == estado).toList();
    }

    @Override
    public synchronized Partida iniciarPartida(String id) {
        Partida partida = buscarPartida(id);
        exigirEstado(partida, Estado.PENDIENTE);
        partida.setEstado(Estado.EN_CURSO);
        partida.setInicioMillis(reloj.getAsLong());
        return new Partida(partida);
    }

    @Override
    public synchronized void registrarBaja(String id, String asesino, String victima) {
        validarTexto(asesino, "idAsesino");
        validarTexto(victima, "idVictima");
        if (asesino.equals(victima)) throw new IllegalArgumentException("Un participante no puede eliminarse a sí mismo");
        Partida partida = buscarPartida(id);
        actualizarTiempo(partida);
        exigirEstado(partida, Estado.EN_CURSO);
        Participante atacante = buscarParticipante(partida, asesino);
        Participante objetivo = buscarParticipante(partida, victima);
        if (!atacante.isVivo() || !objetivo.isVivo()) throw new IllegalStateException("Atacante y víctima deben estar vivos");
        Scoreboard marcador = marcadores.get(id);
        ResultadoJugador resultadoAtacante = buscarResultado(marcador, asesino);
        ResultadoJugador resultadoVictima = buscarResultado(marcador, victima);
        int bajas = Math.addExact(resultadoAtacante.getBajas(), 1);
        int puntos = Math.addExact(resultadoAtacante.getPuntuacion(), 100);
        int muertes = Math.addExact(resultadoVictima.getMuertes(), 1);
        resultadoAtacante.setBajas(bajas);
        resultadoAtacante.setPuntuacion(puntos);
        resultadoVictima.setMuertes(muertes);
        objetivo.setVivo(false);
    }

    @Override
    public synchronized void reaparecer(String id, String jugador) {
        Partida partida = buscarPartida(id);
        actualizarTiempo(partida);
        exigirEstado(partida, Estado.EN_CURSO);
        Participante participante = buscarParticipante(partida, jugador);
        if (participante.isVivo()) throw new IllegalStateException(jugador + " ya está vivo");
        participante.setVivo(true);
    }

    @Override
    public synchronized Partida finalizarPartida(String id) {
        Partida partida = buscarPartida(id);
        actualizarTiempo(partida);
        exigirEstado(partida, Estado.EN_CURSO);
        finalizar(partida, reloj.getAsLong());
        return new Partida(partida);
    }

    @Override
    public synchronized long getSegundosRestantes(String id) {
        Partida partida = buscarPartida(id);
        actualizarTiempo(partida);
        return switch (partida.getEstado()) {
            case PENDIENTE -> Partida.DURACION_SEGUNDOS;
            case FINALIZADA -> 0;
            case EN_CURSO -> Math.max(0, (finPrevisto(partida) - reloj.getAsLong()) / 1000);
        };
    }

    @Override
    public synchronized Scoreboard getScoreboard(String id) {
        actualizarTiempo(buscarPartida(id));
        Scoreboard copia = new Scoreboard(marcadores.get(id));
        copia.getClasificacion().sort(CLASIFICACION);
        return copia;
    }

    @Override
    public synchronized void aplicarRecompensas(String id) {
        Partida partida = buscarPartida(id);
        actualizarTiempo(partida);
        exigirEstado(partida, Estado.FINALIZADA);
        Scoreboard marcador = marcadores.get(id);
        if (marcador.isProcesado()) return;
        for (ResultadoJugador resultado : marcador.getClasificacion()) {
            int creditos = Math.toIntExact(10L * resultado.getBajas() + 50);
            resultado.setCreditosGanados(creditos);
            if (!buscarParticipante(partida, resultado.getIdJugador()).isBot()) {
                aplicarRecompensaPartida(id, resultado.getIdJugador(), resultado.getPuntuacion(), creditos);
            }
        }
        marcador.setProcesado(true);
    }

    private void finalizar(Partida partida, long momento) {
        partida.setEstado(Estado.FINALIZADA);
        partida.setFinMillis(momento);
        try {
            aplicarRecompensas(partida.getId());
        } catch (IllegalArgumentException | NoSuchElementException | ArithmeticException e) {
            LOGGER.warn("Recompensas pendientes de {}: {}", partida.getId(), e.getMessage());
        }
    }

    private void actualizarTiempo(Partida partida) {
        if (partida.getEstado() == Estado.EN_CURSO && reloj.getAsLong() >= finPrevisto(partida)) {
            finalizar(partida, finPrevisto(partida));
        }
    }

    private static long finPrevisto(Partida partida) {
        return partida.getInicioMillis() + Partida.DURACION_SEGUNDOS * 1000L;
    }

    private static void exigirEstado(Partida partida, Estado esperado) {
        if (partida.getEstado() != esperado) {
            throw new IllegalStateException("La partida está " + partida.getEstado() + "; se esperaba " + esperado);
        }
    }

    private static Participante buscarParticipante(Partida partida, String id) {
        Participante participante = partida.buscarParticipante(id);
        if (participante == null) throw new NoSuchElementException("No participa el jugador " + id);
        return participante;
    }

    private static ResultadoJugador buscarResultado(Scoreboard marcador, String id) {
        return marcador.getClasificacion().stream().filter(r -> r.getIdJugador().equals(id)).findFirst().orElseThrow();
    }

    private static void validarTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException("Hace falta " + campo);
    }

    @Override
    public synchronized void limpiar() {
        jugadores.clear();
        partidas.clear();
        marcadores.clear();
        recompensasPagadas.clear();
        experienciaPorNivel = 100;
        cargarCatalogoInicial();
    }
}
