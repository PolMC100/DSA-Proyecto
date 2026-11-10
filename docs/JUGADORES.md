# Jugadores y progresión (Cristian)

Implementación en memoria siguiendo los modelos y gestores actuales y la guía PDF.
No añade REST, catálogo, armas ni inventario, ni modifica módulos de otros integrantes.

## Archivos

- `Jugador.java`: perfil humano o bot, con `idJugador`, `nombre`, `bot`, `vida`,
  `experiencia`, `nivel` y `creditos`. Constructor vacío para JSON, constructor de
  alta, constructor de copia, getters, setters y `toString`.
- `JugadorManager.java`: contrato público.
- `JugadorManagerImpl.java`: Singleton, validación, almacenamiento y progresión.
- Excepciones nuevas en el paquete existente `exceptions`: `JugadorNotFoundException`,
  `JugadorDuplicadoException` y `SaldoInsuficienteException`. Se reutiliza
  `DatosInvalidosException`. Todas son comprobadas, como las de partidas.
- `JugadorManagerTest.java`: 19 pruebas JUnit 4.

## Contrato del gestor

| Método | Función |
| --- | --- |
| `registrarJugador(idJugador, nombre, bot)` | Alta con XP y créditos 0, nivel 1 y vida base provisional 100; rechaza id duplicado o datos vacíos. |
| `getJugador(idJugador)` | Consulta una copia del perfil; rechaza id inválido o inexistente. |
| `getJugadores()` | Lista de copias sin orden garantizado; puede estar vacía. |
| `existeJugadorHumano(idJugador)` | Devuelve false si no existe o es bot; rechaza id vacío. |
| `sumarExperienciaYCreditos(idJugador, experiencia, creditos)` | Suma incrementos no negativos y recalcula nivel; NO deduplica. Permite actualizar solo uno pasando 0 en el otro. |
| `getNivel(idJugador)` | Consulta nivel acumulado. |
| `getCreditos(idJugador)` | Consulta saldo disponible. |
| `cumpleNivelRequerido(idJugador, nivelRequerido)` | Comprueba nivel >= requisito (mínimo 1), recibido del catálogo de Gur. |
| `descontarCreditos(idJugador, precio)` | Comprueba saldo y descuenta bajo el mismo bloqueo; lanza `SaldoInsuficienteException` sin modificar saldo cuando no alcanza. Precio 0 permitido; negativo rechazado. |
| `aplicarRecompensaPartida(idPartida, idJugador, experiencia, creditos)` | Suma una sola vez por partida y jugador; true al aplicar, false al repetir. |
| `setExperienciaPorNivel(cantidad)` | Configura el paso positivo de XP y recalcula todos los niveles existentes. |
| `getExperienciaPorNivel()` | Consulta el paso actual. |
| `limpiar()` | Borra perfiles y registro de recompensas; restablece 100 XP/nivel. No sustituye la instancia Singleton. Solo pruebas/reinicio controlado. |

`JugadorManagerImpl.getInstance()` devuelve la misma instancia de `JugadorManager`.
Los identificadores se conservan exactamente, sin generar UUID ni convertir
mayúsculas: deben coincidir con `Participante.idJugador`, `Partida.idJugadorHumano`
y `ResultadoJugador.idJugador`.

## Estructuras y seguridad de las operaciones

- `HashMap<String, Jugador>` para buscar perfiles por identificador sin recorrer listas.
- `HashMap<String, HashSet<String>>` para registrar qué jugadores cobraron cada
  partida. Un conjunto evita repeticiones; dos claves separadas evitan colisiones
  que podrían aparecer concatenando textos con un separador.
- `ArrayList<Jugador>` para devolver consultas recorribles.

El constructor es privado. Tanto `getInstance` como las operaciones públicas están
sincronizadas: dos compras concurrentes no pueden gastar el mismo saldo y dos
cierres no pueden aplicar la misma recompensa. Se validan todos los valores y los
límites de `int` antes de actualizar experiencia, créditos o nivel. Una recompensa
fallida no se marca como cobrada y se puede reintentar.

Las altas y consultas devuelven copias: cambiar sus setters o vaciar la lista
devuelta no cambia los datos almacenados. Los setters del modelo sirven para JSON;
para modificar el perfil almacenado se deben usar las operaciones del gestor.

## Reglas provisionales

El README no fija la curva de progresión. Por ahora:

```text
nivel = 1 + experienciaAcumulada / experienciaPorNivel
experienciaPorNivel inicial = 100 (división entera)
0..99 XP -> nivel 1; 100..199 -> nivel 2; 200..299 -> nivel 3
```

La XP no se consume al subir de nivel y una recompensa puede subir varios niveles.
`setExperienciaPorNivel` permite cambiar el paso; puede bajar niveles ya calculados
si el nuevo paso es mayor. Se rechaza el cambio completo si algún nivel deja de
caber en un `int`. Una curva no lineal requeriría cambiar `calcularNivel` y acordar
el contrato de configuración. No se han inventado requisitos de objetos.

`vida = 100` es también un valor base provisional de perfil. No representa el daño,
la muerte o la reaparición de una partida. `Participante.vivo` sigue siendo de Franklin.

## Tiffany: cambio propuesto, todavía NO aplicado

En `ResultadosManagerImpl.cerrarPartidaYCalcularRecompensas`, sustituir la llamada
comentada por esta operación, dentro del bucle de resultados:

```java
JugadorManagerImpl.getInstance().aplicarRecompensaPartida(
        res.getIdPartida(),
        res.getIdJugador(),
        res.getPuntuacion(),
        creditos
);
```

La llamada original de tres parámetros a `sumarExperienciaYCreditos` ya compila
con este módulo, pero no incluye información para deduplicar por partida. Para
el cierre debe usar `aplicarRecompensaPartida`, sin llamar además al método de tres
parámetros: cobraría dos veces. No cambia la fórmula de recompensas de Tiffany.

Debe ajustar también estos puntos de su flujo:

1. Acordar si los bots acumulan perfil persistente. Franklin genera `bot-1`,
   `bot-2`, etc. por partida y no los registra en el gestor. Si solo cobran humanos,
   filtrar usando `Participante.isBot()` al inicializar el marcador y conservar
   esa información para el cierre (el `ResultadoJugador` actual no tiene `bot`).
   Si cobran bots, registrar antes sus perfiles y acordar la reutilización de ids.
   Un humano inexistente debe seguir tratándose como error; no omitirlo silenciosamente.
2. Marcar `sb.setProcesado(true)` solo cuando todos los destinatarios previstos
   se hayan aplicado correctamente o hayan devuelto false por estar ya cobrados.
3. Si un destinatario falla, conservar `procesado=false` y propagar o registrar
   el error con Log4j2 para permitir el reintento. El `catch` actual imprime en
   `System.err` y luego se marca procesado de todas formas. En un reintento,
   los destinatarios anteriores devolverán false y no cobrarán otra vez.

La operación protege el par partida/jugador, pero no comprueba que una partida
exista, haya finalizado ni que ese jugador participase: esas comprobaciones son
del módulo que solicita el pago. Repetir el par con otros importes válidos devuelve
false: la primera aplicación correcta es la que cuenta. Las correcciones de
recompensas necesitan un acuerdo diferente, todavía no implementado.

## Gur: tienda e inventario

La tienda consultará `getCreditos(idJugador)`, comprobará
`cumpleNivelRequerido(idJugador, nivelDelObjeto)` y ejecutará
`descontarCreditos(idJugador, precioDelObjeto)` antes de dar por pagada la compra.
Una consulta previa de saldo es informativa: el descuento siempre vuelve a
comprobarlo para evitar que compras simultáneas dejen saldo negativo.

El gestor protege únicamente el descuento. Gur debe coordinar descuento y alta
en inventario como una única compra, con recuperación si falla el alta, y acordar
la protección frente a compras repetidas. Esa transacción entre módulos todavía
no está implementada, porque el inventario no existe y pertenece a Gur.

En `Jugador` queda señalado dónde integrar el inventario, sin crear un `Object`,
una lista de armas ni una clase `Inventario` provisional. Con Gur falta acordar
el tipo, su propietario (referencia en perfil o consulta por id en su gestor),
inicialización, serialización y copia para no exponer su estado mutable.

## Franklin: validación de humano

Antes de crear una partida puede consultar:

```java
boolean existe = JugadorManagerImpl.getInstance().existeJugadorHumano(idJugadorHumano);
```

Si devuelve false, su módulo debe rechazar la creación con el error acordado.
No se ha cambiado su lógica ni sus tests; actualmente todavía permite humanos
sin perfil. No existe un prefijo reservado para distinguir tipos: se utiliza `bot`.

## Verificación realizada

El IDE tiene asociado Temurin 21 y su compilación falla por exigir destino 25.
La consola usa Java 17. Se ha usado temporalmente el JBR/JDK 25.0.4 que incluye
IntelliJ y el Maven incluido en su plugin, sin cambiar configuración ni `pom.xml`:

```powershell
$env:JAVA_HOME = 'C:/Program Files/JetBrains/IntelliJ IDEA 2026.2.2/jbr'
$mavenCristian = 'C:/Program Files/JetBrains/IntelliJ IDEA 2026.2.2/plugins/maven-plugin/lib/maven3/bin/mvn.cmd'
& $mavenCristian compile
& $mavenCristian '-Dtest=JugadorManagerTest,PartidaManagerTest,ResultadosManagerTest' test
```

Resultado: compilación Java 25 correcta; 19 tests de jugadores, 18 de partidas y
3 de resultados, 40 en total, sin fallos, errores ni omitidos. Informes en
`target/surefire-reports`. No se ha ejecutado `ServerTest` ni se ha probado un
flujo integrado de pago real desde resultados: su llamada sigue comentada.

Las pruebas de jugadores incluyen altas, duplicados, inexistentes, validación de
datos, acumulación, umbrales, saltos de nivel, configuración, desbloqueos, saldo,
recompensas repetidas por par, reintentos, desbordamientos sin cambios parciales,
copias, Singleton, limpieza y llamadas concurrentes de compras y recompensas.

## Acuerdos que siguen pendientes

- Curva definitiva de experiencia y vida base inicial.
- Precios y niveles de objetos (Gur); tipo y propiedad del inventario.
- Transacción de compra, reintentos y recuperación del inventario (Gur).
- Destinatarios de recompensas, bots, reintentos y correcciones (Tiffany).
- Validación de humanos y política de identificadores de bots (Franklin).
- Traducción de las excepciones a HTTP (Azahara), sin crear REST aquí.
- Persistencia futura: perfiles y deduplicación se pierden al reiniciar el proceso
  o llamar a `limpiar`. Los ids de partida deben ser únicos mientras existan
  recompensas guardadas; reiniciar solo el contador de partidas puede reutilizar
  `P1` y hacer que se interprete como ya cobrada.
