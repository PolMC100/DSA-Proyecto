# Jugadores y progresión (Cristian)

## Alcance actual

Este módulo contiene Jugador, JugadorManager/JugadorManagerImpl, sus excepciones,
JugadorService y las pruebas de jugadores. La integración interna de tienda,
partidas, resultados y el mapper HTTP común pertenece a sus responsables.
Los cambios recientes de esos módulos se han retirado sin revertir sus commits.

## Modelo y almacenamiento

Jugador conserva idJugador, nombre, bot, vida, experiencia, nivel, créditos e
Inventario. Los alias id/idJugador utilizan el mismo identificador; saldo/creditos
utilizan el mismo entero. setSaldo(double) conserva la firma de Gur y rechaza
fracciones, NaN, infinitos y valores fuera de int sin redondearlos.

Los constructores de alta inicializan Inventario. La copia del perfil crea listas
y mapa independientes, conservando las referencias a los objetos del catálogo
porque Gur los compara por identidad. Esto separa pertenencia y cantidades, pero
no los atributos mutables de los objetos compartidos del catálogo.
Vivo/muerto durante una partida sigue perteneciendo a Participante.

JugadorManagerImpl sigue siendo un Singleton con constructor privado y HashMap
por identificador. Las operaciones están sincronizadas. Altas y consultas devuelven
copias, por lo que sus setters no alteran los perfiles guardados.
Un HashMap de HashSet registra quién cobró cada partida, sin concatenar claves.

## Contrato público

| Método | Función |
| --- | --- |
| registrarJugador(id, nombre, bot) | Alta validada; rechaza identificador duplicado. |
| getJugador(id), getJugadores() | Consultas de copias del perfil. |
| existeJugadorHumano(id) | Comprueba existencia y que no sea bot. |
| sumarExperienciaYCreditos(id, xp, créditos) | Suma incrementos no negativos y recalcula nivel; no deduplica por partida. |
| getNivel(id), getCreditos(id) | Consulta nivel y saldo. |
| cumpleNivelRequerido(id, requisito) | Comprueba nivel >= requisito; el contrato actual exige requisito >= 1. |
| descontarCreditos(id, precio) | Comprueba y descuenta bajo el mismo bloqueo; rechaza saldo insuficiente. |
| aplicarRecompensaPartida(partida, jugador, xp, créditos) | Aplica una vez por par partida/jugador; devuelve false si ya estaba cobrada. |
| setExperienciaPorNivel(paso), getExperienciaPorNivel() | Configuran/consultan el paso y recalculan niveles existentes. |
| limpiar() | Borra perfiles, registro de pagos y restablece la configuración provisional. |

Se ha retirado comprarObjeto del gestor: la coordinación de las compras con el
inventario queda pendiente del módulo de Gur. Se conservan descuento, consulta
de créditos y requisitos para que pueda utilizar esos contratos.

La progresión provisional sigue siendo nivel = 1 + XP acumulada / paso, con paso
inicial 100. La XP no se consume al subir y una recompensa puede subir varios
niveles. La vida inicial 100 es también provisional. Las operaciones validan
incrementos y desbordamientos antes de modificar los datos.

JugadorDuplicadoException y DatosInvalidosException son comprobadas.
JugadorNotFoundException y SaldoInsuficienteException extienden RuntimeException
por los cambios previamente integrados del equipo; no se han alterado ahora.

## Servicio REST de jugadores

Se conservan POST /jugadores y GET /jugadores/{id}, con JugadorDTO como formato.
No quedan identificadores ni perfiles ficticios.

POST registra humanos mediante registrarJugador. Si id se omite/null, genera
un UUID; si se indica, lo conserva y el gestor controla duplicados. El gestor
establece nivel 1, XP 0 y créditos 0: el cliente no puede asignarse progresión
mediante los campos de entrada del DTO.

GET obtiene el perfil del gestor y lo convierte a JugadorDTO, mostrando los
valores actuales de experiencia, nivel y créditos.

El servicio trata sus propios errores: 400 para datos/cuerpo/JSON inválidos,
404 para jugador inexistente, 409 para identificador duplicado y 500 para un
fallo interno inesperado. Reutiliza la representación ErrorEntity existente
(code y message) sin modificar MyExceptionMapper. Los errores internos se
registran con Log4j2 y no exponen sus detalles al cliente.

Para que el JSON inválido pueda recibir 400 sin cambiar el mapper global,
el endpoint recibe el cuerpo como texto y lo lee con el MessageBodyReader de
JugadorDTO proporcionado por Jersey/MOXy. No añade un parser ni duplica las
validaciones del gestor. Swagger mantiene el esquema de cuerpo JugadorDTO.
Esto no corrige errores de rutas/métodos o formatos de otros servicios: son del
mapper común y quedan para su responsable.

## Pruebas

- JugadorManagerTest: 19 pruebas de registro, validaciones, acumulación, niveles,
  desbloqueos, descuentos, deduplicación, copias, Singleton y concurrencia.
- JugadorIntegracionTest: 6 pruebas de alias y compatibilidad/copia del modelo.
- JugadorServiceTest: 7 pruebas propias de HTTP, sin manipular otros gestores.
- IntegrationTest conserva exactamente su contenido previo, con sus 4 pruebas;
  contiene aún comprobaciones de mocks de otros módulos, no un flujo completo real.

La ejecución focalizada de esas clases pasa: 36 pruebas sin fallos ni errores,
compiladas con Java 25. Se usa temporalmente el JDK 25 incluido en IntelliJ y su
Maven, sin modificar pom.xml ni la configuración del proyecto.

La ejecución completa posterior suma 62 pruebas: 58 correctas, 2 fallos y 2 errores.
Las cuatro incidencias corresponden a los tests de catálogo/tienda indicados abajo.

Las cuatro incidencias conocidas de catálogo/tienda son previas: sus @After
vacían el Singleton y sus @Before no lo recargan. No se modifican esos gestores
ni las expectativas de sus tests. Los logs e informes se guardan en target/,
que está ignorado por Git.

## Integraciones pendientes de otros responsables

- Gur: quitar el mock de TiendaService y coordinar descuento e inventario sobre
  el perfil real. El gestor devuelve copias; modificar una consulta no persiste
  una compra. Acordar requisitos de nivel 0 (catálogo) frente a >= 1 (consulta).
- Tiffany: conectar aplicarRecompensaPartida, permitir reintentos y marcar
  procesado solo al completar destinatarios; decidir con Franklin los bots.
- Franklin: usar existeJugadorHumano y registrar el listener de resultados.
- Responsable REST: completar el mapper común para excepciones del framework
  y conflictos, y los servicios de los demás módulos.
- Equipo: confirmar curva de XP, vida inicial y persistencia futura. Los datos
  y la deduplicación son en memoria; no reutilizar ids de partida mientras
  existan pagos guardados con esos identificadores.
