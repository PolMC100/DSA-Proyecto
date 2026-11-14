# DSA-Proyecto

Proyecto Java 25 con Maven, Jersey y Grizzly, documentación Swagger, trazas Log4j2 y pruebas JUnit.

## Compilación, pruebas y ejecución

Requiere JDK 25 o superior y Maven. En IntelliJ IDEA, abrir `pom.xml` como proyecto Maven.

```sh
mvn compile
mvn clean test
mvn exec:java
```

- API: `http://localhost:8080/dsaApp/`
- Estado: `http://localhost:8080/dsaApp/status`
- Swagger JSON: `http://localhost:8080/dsaApp/swagger.json`
- Swagger UI: `http://localhost:8080/swagger/`
- Para detener el servidor, pulsar Enter o Ctrl+C.

Los datos se conservan en memoria durante la ejecución. Las pruebas HTTP utilizan
puertos libres y cierran sus clientes y servidores. Las pruebas de catálogo y
compras crean gestores independientes para evitar compartir datos entre casos.

## Idea del juego

Shooter para un jugador con bots, en modo todos contra todos (*free for all*).
El escenario es el aula de DSA y los jugadores tienen el tamaño de una hormiga.

### Partidas

- Cada partida dura **5 minutos** y admite un humano y entre cero y nueve bots.
- El humano debe estar registrado. Los bots se crean como participantes de esa partida.
- Las reapariciones son ilimitadas mientras la partida esté en curso.
- Los estados son `PENDIENTE`, `EN_CURSO` y `FINALIZADA`.
- Cada partida tiene un marcador, incluso si finaliza sin ninguna baja.
- El tiempo se comprueba al consultar o ejecutar acciones; no hay un temporizador en segundo plano.

### Tienda y progresión

- El alta crea un perfil de nivel 1, experiencia 0, créditos 0 y vida base 100.
- La regla provisional es `nivel = 1 + experiencia / 100`, con división entera.
- Cada baja suma 100 puntos de experiencia. Al finalizar, los créditos ganados son `10 * bajas + 50`.
- Solo los participantes humanos reciben progresión persistida; los bots aparecen en el marcador.
- Una recompensa se aplica una sola vez por partida y jugador. Un fallo permite reintentar sin pagar dos veces.
- Los créditos son enteros. Las compras comprueban nivel, saldo y cantidad antes de actualizar saldo e inventario conjuntamente.
- El nombre del objeto es su identificador en el catálogo; por ejemplo, `Pistola`, `Humo` o `Chaleco`.
- Un requisito de nivel 0 significa que no hace falta desbloquear el objeto.
- Las armas y complementos se compran una vez. Las granadas admiten varias unidades y se consumen al usarlas.
- Se puede comprar desde el menú o durante una partida: la compra pertenece al perfil y no depende del estado de una partida.

## Estructura simplificada

Se sigue el esquema de `DSA-Minim-Real`: un servicio REST, una interfaz de gestor,
una implementación en memoria y los modelos del dominio. El código de producción
se ha reducido de 49 a **13 archivos Java**.

```text
src/main/java/edu/upc/dsa/
├── Main.java
├── MyExceptionMapper.java
├── domain/
│   ├── models/
│   │   ├── Jugador.java
│   │   ├── Inventario.java
│   │   ├── Arma.java
│   │   ├── Granada.java
│   │   ├── Complemento.java
│   │   ├── Partida.java
│   │   ├── Scoreboard.java
│   │   └── ResultadoJugador.java
│   └── repository/
│       ├── JuegoManager.java
│       └── JuegoManagerImpl.java
└── services/
    └── JuegoService.java
src/test/java/edu/upc/dsa/
├── JuegoManagerTest.java
├── JuegoServiceTest.java
└── ModelosTest.java
```

- `JuegoManagerImpl` guarda perfiles, catálogo, partidas, marcadores y pagos. Las compras y recompensas se coordinan dentro del mismo componente, sin gestores intermedios ni observadores.
- `JuegoService` conserva las rutas HTTP y agrupa sus cuerpos y respuestas pequeños: `Perfil`, `Compra`, `CrearPartida`, `Baja`, `Reaparicion`, `Tiempo`, `Catalogo` y `ContenidoInventario`. No necesitan archivos DTO separados.
- `Partida` contiene sus tipos `Estado` y `Participante`; `Inventario` contiene el contrato `Comprable` que implementan los tres tipos de objeto.
- `MyExceptionMapper` convierte excepciones estándar en JSON; no hay un archivo por excepción de negocio.
- `getInstance()` proporciona el gestor compartido. Los constructores permiten gestores independientes para las pruebas; el reloj se puede sustituir mediante `LongSupplier` de Java.
- Los ocho archivos de modelos corresponden a entidades del juego. Sus consultas devuelven copias para proteger el estado guardado.

Por ejemplo, la respuesta antes definida en `TiempoDTO.java` está ahora en
`JuegoService.Tiempo` y conserva el JSON `{idPartida, segundosRestantes}`.
Las granadas del inventario se representan como una lista `{nombre, cantidad}`.
Los nombres de las clases Java y del esquema Swagger cambian al agruparse, pero
se mantienen las rutas, los campos JSON y los códigos HTTP.

El [diagrama de clases](docs/DIAGRAMAS_CLASES.md) describe esta estructura.
Las pruebas se agrupan por comportamiento en tres archivos y cubren modelos,
progresión, compras, partidas, resultados, HTTP, copias, errores y concurrencia.

## API disponible

Todas las rutas siguientes son relativas a `/dsaApp`. Los cuerpos y respuestas
utilizan JSON. Los errores tienen el formato `{"code": 400, "message": "..."}`.

| Método | Ruta | Operación |
| --- | --- | --- |
| GET | `/status` | Comprobar el servidor. |
| POST | `/jugadores` | Registrar un humano; genera un UUID si se omite `id`. |
| GET | `/jugadores/{id}` | Consultar perfil y progresión. |
| GET | `/jugadores/{id}/inventario` | Consultar las compras guardadas. |
| POST | `/jugadores/{id}/inventario/granadas/{nombre}/usar` | Consumir una unidad; 409 si no quedan. |
| GET | `/tienda/catalogo` | Consultar armas, granadas y complementos. |
| POST | `/tienda/comprar` | Comprar y devolver el perfil actualizado. |
| POST | `/partidas` | Crear con `idJugadorHumano` y `numBots`. |
| POST | `/partidas/crear/{jugadorId}` | Atajo de creación con nueve bots. |
| GET | `/partidas` | Listar; admite `?estado=EN_CURSO`. |
| GET | `/partidas/{id}` | Consultar una partida. |
| POST | `/partidas/{id}/iniciar` | Iniciar. |
| POST | `/partidas/{id}/bajas` | Registrar `idAsesino` e `idVictima`. |
| POST | `/partidas/{id}/reapariciones` | Reaparecer mediante `idJugador`. |
| POST | `/partidas/{id}/finalizar` | Finalizar antes de tiempo. |
| GET | `/partidas/{id}/tiempo` | Consultar segundos restantes. |
| GET | `/resultados/scoreboard/{partidaId}` | Consultar clasificación y recompensas. |
| POST | `/resultados/{partidaId}/recompensas` | Reintentar pagos de una partida finalizada. |

Ejemplo de alta:

```json
{"id": "J1", "nombre": "Jugador 1"}
```

El cliente no puede asignarse experiencia, nivel ni créditos mediante el alta.
Una vez ganados los créditos necesarios, este cuerpo compra dos granadas de humo:

```json
{"jugadorId": "J1", "itemId": "Humo", "cantidad": 2}
```

Si se omite `cantidad`, vale 1. Las armas y complementos requieren exactamente
una unidad. Cantidades no positivas o costes fuera del rango de `int` devuelven
400; nivel o saldo insuficientes, 403; recursos inexistentes, 404; armas o
complementos repetidos, 409. Una compra rechazada no modifica el perfil.

## To-do

### Reparto de responsabilidades del backend

| Responsable | Área | Entregables principales |
| --- | --- | --- |
| Pol | Base y arquitectura | Maven, servidor, logs, paquetes y diagrama. |
| Cristian | Jugadores y progresión | Perfiles, experiencia, niveles, créditos y operaciones atómicas. |
| Gur | Catálogo, tienda e inventario | Objetos, catálogo, compras y consumo de granadas. |
| Franklin | Partidas | Participantes, estados, duración, bajas y reapariciones. |
| Tiffany | Resultados | Marcador, clasificación, cálculo y aplicación de recompensas. |
| Azahara | API REST e integración | Recursos, errores JSON, Swagger y pruebas HTTP. |

### Pol — base del proyecto y arquitectura

- [x] Configurar Java 25, Maven, Jersey, Grizzly, JSON, Swagger, Log4j2 y JUnit.
- [x] Implementar arranque, parada y publicación de `public/swagger`.
- [x] Mantener la estructura de paquetes y el diagrama de clases actualizado.
- [x] Documentar compilación, pruebas y ejecución.

### Cristian — jugadores y progresión

- [x] Implementar humanos y bots, altas y consultas de perfiles.
- [x] Calcular experiencia, niveles y requisitos de desbloqueo, incluido el requisito 0.
- [x] Actualizar créditos y experiencia sin exponer los perfiles internos.
- [x] Coordinar descuento e inventario como una compra atómica.
- [x] Deduplicar recompensas por partida y jugador.
- [x] Probar errores, copias, desbordamientos y operaciones simultáneas.

### Gur — catálogo, tienda e inventario

- [x] Implementar objetos, inventario y catálogo inicial.
- [x] Compartir los datos de compra mediante `Inventario.Comprable` y comprar por identificador.
- [x] Validar cantidad, nivel, saldo y duplicados antes de guardar la compra.
- [x] Consultar el inventario y consumir granadas sin cantidades negativas.
- [x] Probar compras válidas, rechazos, concurrencia y aislamiento del catálogo.
- [ ] Acordar e implementar el equipamiento de armas y complementos.

### Franklin — partidas

- [x] Gestionar un humano y hasta nueve bots, estados y duración de cinco minutos.
- [x] Validar humano existente, bajas y reapariciones.
- [x] Inicializar el marcador y actualizar resultados y recompensas desde el gestor único.
- [x] Devolver copias y serializar las operaciones simultáneas.
- [x] Probar límites, transiciones y caducidad con un reloj controlado en las pruebas.

### Tiffany — resultados, marcador y recompensas

- [x] Mantener un marcador por partida y un resultado por participante.
- [x] Actualizar y ordenar por puntos descendentes, bajas descendentes y muertes ascendentes.
- [x] Aplicar progresión humana mediante `aplicarRecompensaPartida`.
- [x] Permitir reintentos sin duplicar pagos completados ni cerrar pagos fallidos.
- [x] Conservar resultados finalizados y devolver copias para consulta.
- [x] Probar desempates, cierre sin bajas, pagos fallidos y reintentos simultáneos.

### Azahara — API REST e integración

- [x] Conectar las rutas de jugadores, tienda, partidas y resultados con `JuegoManager`.
- [x] Definir JSON sin referencias circulares ni claves de objetos Java.
- [x] Centralizar errores de negocio, JSON inválido, rutas, métodos y formatos HTTP.
- [x] Publicar Swagger y comprobar su contenido por HTTP.
- [x] Probar el flujo real: alta, partida, recompensa, catálogo, compra e inventario.
- [x] Usar puertos libres y cerrar clientes y servidores en las pruebas.

### Entrega del 12/11

- [x] Idea del juego — todo el equipo.
- [ ] To-do completo: queda pendiente el equipamiento.
- [ ] PPT — coordina el integrante 6; cada integrante aporta su área y el equipo revisa la presentación.
