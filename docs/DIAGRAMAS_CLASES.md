# Diagramas de clases

La estructura sigue el componente único de `DSA-Minim-Real`: servicio REST,
interfaz del gestor, implementación en memoria y modelos. Se omiten accesores,
constructores y logs. Las reglas, rutas y tareas están en el [README](../README.md).

## Componente y API

```mermaid
%%{init: {"theme":"dark"}}%%
classDiagram
    direction LR
    class Main
    class JuegoService
    class MyExceptionMapper
    class JuegoManager {
        <<interface>>
        +registrarJugador(String id, String nombre, boolean bot) Jugador
        +getJugador(String id) Jugador
        +comprar(String jugadorId, String itemId, int cantidad) Jugador
        +consumirGranada(String jugadorId, String nombre) boolean
        +crearPartida(String humano, int bots) Partida
        +iniciarPartida(String id) Partida
        +registrarBaja(String partida, String asesino, String victima) void
        +reaparecer(String partida, String jugador) void
        +finalizarPartida(String id) Partida
        +getSegundosRestantes(String id) long
        +getScoreboard(String id) Scoreboard
        +aplicarRecompensas(String id) void
        +limpiar() void
    }
    class JuegoManagerImpl {
        -JuegoManagerImpl INSTANCE$
        -Map jugadores
        -Map catalogo
        -Map partidas
        -Map marcadores
        -Map recompensasPagadas
        -LongSupplier reloj
        -int experienciaPorNivel
        -long contadorPartidas
        +getInstance() JuegoManagerImpl$
    }
    Main ..> JuegoService : registra el paquete services
    Main ..> MyExceptionMapper : registra
    JuegoService --> JuegoManager : delega
    JuegoManagerImpl ..|> JuegoManager
    style Main stroke:#a6a0ff
    style MyExceptionMapper stroke:#a6a0ff
    style JuegoService stroke:#00c9e5
    style JuegoManager stroke:#d779ed
    style JuegoManagerImpl stroke:#d779ed
```

El gestor también expone las consultas de catálogo, perfiles, partidas y progresión
y la actualización de experiencia y créditos. El contrato completo está en
`JuegoManager.java`. Las operaciones públicas de la implementación se sincronizan.
Las consultas devuelven copias, y cada compra prepara el inventario antes de
guardar descuento e inventario conjuntamente.

| Contenedor | Contenido |
| --- | --- |
| `jugadores` | `Map<String, Jugador>`: perfiles por identificador. |
| `catalogo` | `Map<String, Inventario.Comprable>`: objetos por nombre. |
| `partidas` | `Map<String, Partida>`: partidas y su estado actual. |
| `marcadores` | `Map<String, Scoreboard>`: un marcador por partida. |
| `recompensasPagadas` | `Map<String, Set<String>>`: jugadores pagados en cada partida. |

Partidas y resultados se actualizan directamente dentro del mismo gestor.
No se necesitan `PartidaListener`, gestores que deleguen entre sí ni un segundo
registro de copias de partidas para poder validar recompensas.

## Modelos del juego

```mermaid
%%{init: {"theme":"dark"}}%%
classDiagram
    direction TB
    class Jugador {
        -String idJugador
        -String nombre
        -boolean bot
        -int vida
        -int experiencia
        -int nivel
        -int creditos
    }
    class Inventario {
        -List~Arma~ armas
        -Map granadas
        -List~Complemento~ complementos
        +addObjeto(Comprable objeto, int cantidad) void
        +useGranada(String nombre) int
    }
    class Comprable {
        <<interface>>
        +getNombre() String
        +getPrecio() Integer
        +getNivelRequerido() Integer
    }
    class Arma
    class Granada
    class Complemento
    class Partida {
        -String id
        -String idJugadorHumano
        -List~Participante~ participantes
        -Estado estado
        -long inicioMillis
        -long finMillis
    }
    class Participante {
        -String idJugador
        -boolean bot
        -boolean vivo
    }
    class Estado {
        <<enumeration>>
        PENDIENTE
        EN_CURSO
        FINALIZADA
    }
    class Scoreboard {
        -String idPartida
        -List~ResultadoJugador~ clasificacion
        -boolean procesado
    }
    class ResultadoJugador {
        -String idJugador
        -String idPartida
        -int bajas
        -int muertes
        -int puntuacion
        -int creditosGanados
    }
    Jugador "1" *-- "0..1" Inventario
    Arma ..|> Comprable
    Granada ..|> Comprable
    Complemento ..|> Comprable
    Inventario "1" *-- "0..*" Arma
    Inventario "1" *-- "0..*" Complemento
    Inventario ..> Granada : cantidades por nombre
    Partida "1" *-- "1..10" Participante
    Partida --> Estado
    Participante ..> Jugador : identificador del humano
    Scoreboard "1" *-- "0..10" ResultadoJugador
    Scoreboard ..> Partida : idPartida
    ResultadoJugador ..> Participante : idJugador
    style Jugador stroke:#d779ed
    style Inventario stroke:#d779ed
    style Arma stroke:#ff9b40
    style Granada stroke:#ff9b40
    style Complemento stroke:#ff9b40
    style Comprable stroke:#ff9b40
    style Partida stroke:#00c9e5
    style Participante stroke:#00c9e5
    style Estado stroke:#00c9e5
    style Scoreboard stroke:#00c9a7
    style ResultadoJugador stroke:#00c9a7
```

`Comprable` es una interfaz dentro de `Inventario`; `Participante` y `Estado` están
dentro de `Partida`. Se muestran por separado para explicar sus relaciones,
pero no tienen archivos Java propios. Los perfiles gestionados empiezan con
inventario; el modelo conserva el setter que permite un inventario nulo por compatibilidad.

Los bots son participantes temporales. Las estadísticas se calculan para todos,
pero solo se actualiza la progresión de los humanos. Los pagos se registran
por partida y jugador; si un pago falla, el marcador permanece pendiente y puede
reintentarse. Los ids de partida no se reutilizan al limpiar el gestor.

## Representaciones JSON y errores

Las clases de transporte están agrupadas dentro de `JuegoService`:

| Clase anidada | Campos o contenido |
| --- | --- |
| `Perfil` | id, nombre, nivel, experiencia y créditos. |
| `Compra` | jugadorId, itemId y cantidad (1 por defecto). |
| `CrearPartida` | idJugadorHumano y numBots. |
| `Baja` | idAsesino e idVictima. |
| `Reaparicion` | idJugador. |
| `Tiempo` | idPartida y segundosRestantes. |
| `Catalogo` | Listas de armas, granadas y complementos. |
| `ContenidoInventario` | Compras y lista de granadas con nombre y cantidad. |
| `Status` | status. |

`MyExceptionMapper.ErrorEntity` conserva el JSON de error con `code` y `message`.
Se utilizan excepciones estándar, sin clases de excepción adicionales:

| Excepción | HTTP |
| --- | --- |
| `IllegalArgumentException`, `ArithmeticException` | 400 |
| `SecurityException` | 403 |
| `NoSuchElementException` | 404 |
| `IllegalStateException` | 409 |
| `WebApplicationException` | Conserva su código y cabeceras, con cuerpo JSON. |
| Excepción inesperada | 500 con mensaje genérico; detalles en logs. |

`Main` configura Jersey, Grizzly, MOXy, Swagger y los archivos de `public`.
El documento Swagger lo publica el recurso de la biblioteca. Las rutas y los
campos JSON se conservan aunque se hayan agrupado las clases Java.
