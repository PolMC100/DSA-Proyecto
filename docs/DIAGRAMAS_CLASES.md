# Diagramas de clases

Diagramas Mermaid del backend de DSA-Proyecto, basados en el
[README](../README.md) y en las clases de [`src/main/java/edu/upc/dsa`](../src/main/java/edu/upc/dsa).
Representan la implementación actual; las diferencias respecto al diseño previsto
en el README se recogen al final.

Se omiten constructores, getters, setters, trazas de Log4j2 y métodos auxiliares
privados para facilitar la lectura. Las operaciones de las interfaces también
están implementadas por sus clases `*ManagerImpl`.

## Notación

| Símbolo | Significado |
| --- | --- |
| `+`, `-`, `#` | Miembro público, privado o protegido. |
| `$` al final de un miembro | Miembro estático. |
| `*--` | Composición: el objeto forma parte de otro. |
| `o--` | Agregación: agrupa objetos que pueden compartirse. |
| `-->` | Asociación con una referencia a otro objeto. |
| `..>` | Dependencia de uso o referencia mediante un identificador, según la etiqueta. |
| `..\|>` | Implementación de una interfaz. |
| `--\|>` | Herencia de una clase. |
| `0..*`, `0..1`, `1..10` | Multiplicidad de una relación. |

Los mapas se muestran como `HashMap` o `Map`, con sus claves y valores explicados
junto a cada diagrama. Las listas usan la sintaxis genérica de Mermaid (`List~Tipo~`).

### Colores

La paleta sigue el estilo de la imagen de referencia: fondo oscuro, texto claro,
bordes de colores y notas amarillas. Cada clase conserva su color entre diagramas.

| Color | Clases |
| --- | --- |
| Morado | Jugadores e inventario. |
| Naranja | Catálogo, tienda y objetos comprables. |
| Cian | Partidas, participantes y reloj. |
| Turquesa | Resultados y marcador. |
| Lavanda | Arranque y servicios de infraestructura. |
| Coral | Excepciones del juego. |
| Gris | Clases e interfaces externas. |

Las vistas se organizan en una proporción aproximada de **16:9**. Las secciones
con más clases se dividen en varios diagramas para mantener legibles los miembros
y las relaciones.

## 1. Modelo del juego: jugadores, partidas y resultados

Paquete: [`domain.models`](../src/main/java/edu/upc/dsa/domain/models).
Esta vista reúne las relaciones entre las entidades; el contenido del inventario
se detalla en el siguiente diagrama.

```mermaid
%%{init: {"theme":"base","themeVariables":{"darkMode":true,"background":"#1b1b1f","primaryColor":"#0d111a","primaryTextColor":"#e6edf3","primaryBorderColor":"#d779ed","secondaryColor":"#0d111a","secondaryTextColor":"#e6edf3","tertiaryColor":"#0d111a","tertiaryTextColor":"#e6edf3","textColor":"#e6edf3","classText":"#e6edf3","lineColor":"#b8bfcc","edgeLabelBackground":"#1b1b1f","noteBkgColor":"#fff7be","noteBorderColor":"#f3c600","noteTextColor":"#26210a"},"themeCSS":"background-color: #1b1b1f; .noteText, .noteText tspan, .node[id^=\"note\"] .nodeLabel, .node[id^=\"note\"] .nodeLabel p { color: #26210a !important; fill: #26210a !important; }","layout":"dagre","class":{"nodeSpacing":60,"rankSpacing":132,"diagramPadding":24,"hideEmptyMembersBox":true},"flowchart":{"nodeSpacing":60,"rankSpacing":132,"curve":"linear"}}}%%
classDiagram
    direction TB

    class Jugador {
        -String id
        -String nombre
        -double saldo
        -int nivel
        -Inventario inventario
    }

    class Inventario

    class Arma

    class Granada

    class Complemento

    class Partida {
        +int MAX_PARTICIPANTES$
        +int DURACION_SEGUNDOS$
        -String id
        -String idJugadorHumano
        -List~Participante~ participantes
        -EstadoPartida estado
        -long inicioMillis
        -long finMillis
        +buscarParticipante(String idJugador) Participante
        +addParticipante(Participante p) void
    }

    class Participante {
        -String idJugador
        -boolean bot
        -boolean vivo
    }

    class EstadoPartida {
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

    Jugador "1" --> "0..1" Inventario : inventario
    Inventario "0..*" o-- "0..*" Arma : armas
    Inventario "0..*" o-- "0..*" Granada : granadas y cantidades
    Inventario "0..*" o-- "0..*" Complemento : complementos
    Partida "1" *-- "1..10" Participante : participantes
    EstadoPartida <-- Partida : estado
    Jugador <.. Partida : idJugadorHumano
    Jugador <.. Participante : idJugador del humano
    ResultadoJugador "0..10" --* "1" Scoreboard : clasificacion
    Partida <.. Scoreboard : idPartida
    ResultadoJugador ..> Partida : idPartida
    ResultadoJugador ..> Participante : idJugador en esa partida

    note for Partida "MAX_PARTICIPANTES = 10; DURACION_SEGUNDOS = 300"
    note for Participante "El estado vivo y la marca bot pertenecen a la<br/>participación en una partida."
    style Jugador fill:#0d111a,stroke:#d779ed,stroke-width:2px,color:#e6edf3
    style Inventario fill:#0d111a,stroke:#d779ed,stroke-width:2px,color:#e6edf3
    style Arma fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style Granada fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style Complemento fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style Partida fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style Participante fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style EstadoPartida fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style Scoreboard fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
    style ResultadoJugador fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
```

- Una partida creada mediante `PartidaManager` tiene un humano y entre cero y
  nueve bots. El constructor vacío de `Partida` permite una lista vacía antes de
  inicializarla; `1..10` expresa la regla aplicada por el gestor.
- Las referencias `idJugador`, `idJugadorHumano` e `idPartida` son cadenas, sin
  referencias Java a los objetos identificados. Los bots se crean como
  `Participante`; no se crea un `Jugador` por cada bot.
- `Jugador` puede tener el inventario a `null` si se usa el constructor vacío.
  El constructor con parámetros crea un inventario.
- El módulo de resultados mantiene como máximo un `Scoreboard` por identificador
  de partida y crea un `ResultadoJugador` por participante al inicializarlo.
  `Partida` no contiene un atributo `Scoreboard`.

## 2. Inventario y objetos del catálogo

Paquete: [`domain.models`](../src/main/java/edu/upc/dsa/domain/models).

```mermaid
%%{init: {"theme":"base","themeVariables":{"darkMode":true,"background":"#1b1b1f","primaryColor":"#0d111a","primaryTextColor":"#e6edf3","primaryBorderColor":"#d779ed","secondaryColor":"#0d111a","secondaryTextColor":"#e6edf3","tertiaryColor":"#0d111a","tertiaryTextColor":"#e6edf3","textColor":"#e6edf3","classText":"#e6edf3","lineColor":"#b8bfcc","edgeLabelBackground":"#1b1b1f","noteBkgColor":"#fff7be","noteBorderColor":"#f3c600","noteTextColor":"#26210a"},"themeCSS":"background-color: #1b1b1f; .noteText, .noteText tspan, .node[id^=\"note\"] .nodeLabel, .node[id^=\"note\"] .nodeLabel p { color: #26210a !important; fill: #26210a !important; }","layout":"dagre","class":{"nodeSpacing":60,"rankSpacing":180,"diagramPadding":24,"hideEmptyMembersBox":true},"flowchart":{"nodeSpacing":60,"rankSpacing":180,"curve":"linear"}}}%%
classDiagram
    direction LR

    class Inventario {
        -List~Arma~ armas
        -HashMap granadas
        -List~Complemento~ complementos
        +addArma(Arma arma) void
        +addGranada(Granada granada, Integer qty) void
        +useGranada(Granada granada) int
        +checkGranadasQty(Granada granada) Integer
        +addComplemento(Complemento complemento) void
    }

    class Arma {
        -String nombre
        -Integer munición
        -Integer cadencia
        -Integer daño
        -Integer precio
        -Integer nivelRequerido
    }

    class Granada {
        -String nombre
        -Integer daño
        -Double radio
        -String efecto
        -Integer duracionEfecto
        -Integer precio
        -Integer nivelRequerido
    }

    class Complemento {
        -String nombre
        -String tipo
        -String efecto
        -Integer precio
        -Integer nivelRequerido
    }

    Inventario "0..*" o-- "0..*" Arma : armas
    Inventario "0..*" o-- "0..*" Granada : granadas
    Inventario "0..*" o-- "0..*" Complemento : complementos

    note for Inventario "granadas: HashMap con clave Granada y valor<br/>Integer, cantidad disponible."
    style Inventario fill:#0d111a,stroke:#d779ed,stroke-width:2px,color:#e6edf3
    style Arma fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style Granada fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style Complemento fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
```

Las compras añaden al inventario las referencias a los objetos del catálogo; por
eso se usa agregación. Las granadas tienen una cantidad por tipo, que disminuye
con `useGranada`. `Arma`, `Granada` y `Complemento` son clases independientes:
actualmente no existe una superclase común de objeto comprable.

## 3. Gestores de catálogo y tienda

Paquete: [`domain.repository`](../src/main/java/edu/upc/dsa/domain/repository).

```mermaid
%%{init: {"theme":"base","themeVariables":{"darkMode":true,"background":"#1b1b1f","primaryColor":"#0d111a","primaryTextColor":"#e6edf3","primaryBorderColor":"#d779ed","secondaryColor":"#0d111a","secondaryTextColor":"#e6edf3","tertiaryColor":"#0d111a","tertiaryTextColor":"#e6edf3","textColor":"#e6edf3","classText":"#e6edf3","lineColor":"#b8bfcc","edgeLabelBackground":"#1b1b1f","noteBkgColor":"#fff7be","noteBorderColor":"#f3c600","noteTextColor":"#26210a"},"themeCSS":"background-color: #1b1b1f; .noteText, .noteText tspan, .node[id^=\"note\"] .nodeLabel, .node[id^=\"note\"] .nodeLabel p { color: #26210a !important; fill: #26210a !important; }","layout":"dagre","class":{"nodeSpacing":150,"rankSpacing":100,"diagramPadding":24,"hideEmptyMembersBox":true},"flowchart":{"nodeSpacing":150,"rankSpacing":100,"curve":"linear"}}}%%
classDiagram
    direction TB

    class CatalogoManager {
        <<interface>>
        +getArmasCatalogo() HashMap
        +getGranadasCatalogo() HashMap
        +getComplementosCatalogo() HashMap
        +addArmaCatalogo(Arma arma) void
        +addGranadaCatalogo(Granada granada) void
        +addComplementoCatalogo(Complemento complemento) void
        +clear() void
    }

    class CatalogoManagerImpl {
        -CatalogoManager instance$
        #HashMap armasCatalogo
        #HashMap granadasCatalogo
        #HashMap complementosCatalogo
        +getInstance() CatalogoManager$
        +cargarCatalogoInicial() void
    }

    class TiendaManager {
        <<interface>>
        +getArmas() HashMap
        +getGranadas() HashMap
        +getComplementos() HashMap
        +comprarArma(Jugador jugador, String nombreArma) int
        +comprarGranada(Jugador jugador, String nombreGranada, Integer qty) int
        +comprarComplemento(Jugador jugador, String nombreComplemento) int
        +clear() void
    }

    class TiendaManagerImpl {
        -TiendaManager instance$
        -CatalogoManager cm
        +getInstance() TiendaManager$
    }

    class Jugador

    class Inventario

    class Arma

    class Granada

    class Complemento

    class DatosInvalidosException

    CatalogoManager <|.. CatalogoManagerImpl
    TiendaManager <|.. TiendaManagerImpl
    CatalogoManager <-- TiendaManagerImpl : cm
    CatalogoManagerImpl "1" o-- "0..*" Arma : armasCatalogo
    CatalogoManagerImpl "1" o-- "0..*" Granada : granadasCatalogo
    CatalogoManagerImpl "1" o-- "0..*" Complemento : complementosCatalogo
    TiendaManagerImpl ..> Jugador : consulta nivel y modifica<br/>saldo
    TiendaManagerImpl ..> Inventario : añade la compra
    TiendaManager ..> DatosInvalidosException : declara

    style CatalogoManager fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style CatalogoManagerImpl fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style TiendaManager fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style TiendaManagerImpl fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style Jugador fill:#0d111a,stroke:#d779ed,stroke-width:2px,color:#e6edf3
    style Inventario fill:#0d111a,stroke:#d779ed,stroke-width:2px,color:#e6edf3
    style Arma fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style Granada fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style Complemento fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style DatosInvalidosException fill:#0d111a,stroke:#ff7b86,stroke-width:2px,color:#e6edf3
```

Los tres mapas del catálogo y los devueltos por la tienda tienen claves `String`
(nombre del objeto) y valores `Arma`, `Granada` o `Complemento`, respectivamente.
Ambas implementaciones usan el patrón Singleton.

Las compras devuelven `0` si se completan, `1` por nivel insuficiente, `2` por saldo
insuficiente y `3` si el arma o complemento ya está en el inventario. Un jugador
nulo o un nombre de objeto inexistente provoca `DatosInvalidosException`.

## 4. Gestión de partidas y notificación de eventos

Paquetes: [`domain.repository`](../src/main/java/edu/upc/dsa/domain/repository)
y [`util`](../src/main/java/edu/upc/dsa/util).

```mermaid
%%{init: {"theme":"base","themeVariables":{"darkMode":true,"background":"#1b1b1f","primaryColor":"#0d111a","primaryTextColor":"#e6edf3","primaryBorderColor":"#d779ed","secondaryColor":"#0d111a","secondaryTextColor":"#e6edf3","tertiaryColor":"#0d111a","tertiaryTextColor":"#e6edf3","textColor":"#e6edf3","classText":"#e6edf3","lineColor":"#b8bfcc","edgeLabelBackground":"#1b1b1f","noteBkgColor":"#fff7be","noteBorderColor":"#f3c600","noteTextColor":"#26210a"},"themeCSS":"background-color: #1b1b1f; .noteText, .noteText tspan, .node[id^=\"note\"] .nodeLabel, .node[id^=\"note\"] .nodeLabel p { color: #26210a !important; fill: #26210a !important; }","layout":"dagre","class":{"nodeSpacing":60,"rankSpacing":169,"diagramPadding":24,"hideEmptyMembersBox":true},"flowchart":{"nodeSpacing":60,"rankSpacing":169,"curve":"linear"}}}%%
classDiagram
    direction TB

    class PartidaManager {
        <<interface>>
        +crearPartida(String idJugadorHumano, int numBots) Partida
        +getPartida(String idPartida) Partida
        +getPartidas() List~Partida~
        +getPartidasPorEstado(EstadoPartida estado) List~Partida~
        +iniciarPartida(String idPartida) Partida
        +registrarBaja(String idPartida, String idAsesino, String idVictima) void
        +reaparecer(String idPartida, String idJugador) void
        +finalizarPartida(String idPartida) Partida
        +getSegundosRestantes(String idPartida) long
        +comprobarTiempo(String idPartida) boolean
        +addListener(PartidaListener listener) void
        +setReloj(Reloj reloj) void
        +size() int
        +clear() void
    }

    class PartidaManagerImpl {
        -PartidaManager instance$
        -HashMap partidas
        -List~PartidaListener~ listeners
        -Reloj reloj
        -int contador
        +getInstance() PartidaManager$
    }

    class PartidaListener {
        <<interface>>
        +onBaja(Partida partida, String idAsesino, String idVictima) void
        +onPartidaFinalizada(Partida partida) void
    }

    class Reloj {
        <<interface>>
        +Reloj SISTEMA$
        +ahoraMillis() long
    }

    class Partida

    class Participante

    class EstadoPartida {
        <<enumeration>>
        PENDIENTE
        EN_CURSO
        FINALIZADA
    }

    class ResultadosManagerImpl

    PartidaManagerImpl ..|> PartidaManager
    PartidaManagerImpl "1" o-- "0..*" Partida : partidas por id
    PartidaManagerImpl "1" --> "0..*" PartidaListener : listeners registrados
    PartidaManagerImpl --> Reloj : reloj sustituible
    PartidaManagerImpl ..> Participante : crea y actualiza
    PartidaManagerImpl ..> EstadoPartida : valida transiciones
    PartidaListener ..> Partida : recibe eventos
    ResultadosManagerImpl ..|> PartidaListener

    style PartidaManager fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style PartidaManagerImpl fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style PartidaListener fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style Reloj fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style Partida fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style Participante fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style EstadoPartida fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style ResultadosManagerImpl fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
```

`partidas` es un `HashMap<String, Partida>`. El gestor usa Singleton y conserva
las partidas en memoria. Los listeners siguen el patrón Observer y reciben
avisos de bajas y finalización.

La duración se comprueba al ejecutar determinadas operaciones del gestor;
no existe un temporizador en segundo plano. `Reloj.SISTEMA` usa la hora del
sistema y `setReloj` permite sustituirla en las pruebas.

## 5. Gestión de resultados, clasificación y recompensas

Paquetes: [`domain.repository`](../src/main/java/edu/upc/dsa/domain/repository)
y [`domain.models`](../src/main/java/edu/upc/dsa/domain/models).

```mermaid
%%{init: {"theme":"base","themeVariables":{"darkMode":true,"background":"#1b1b1f","primaryColor":"#0d111a","primaryTextColor":"#e6edf3","primaryBorderColor":"#d779ed","secondaryColor":"#0d111a","secondaryTextColor":"#e6edf3","tertiaryColor":"#0d111a","tertiaryTextColor":"#e6edf3","textColor":"#e6edf3","classText":"#e6edf3","lineColor":"#b8bfcc","edgeLabelBackground":"#1b1b1f","noteBkgColor":"#fff7be","noteBorderColor":"#f3c600","noteTextColor":"#26210a"},"themeCSS":"background-color: #1b1b1f; .noteText, .noteText tspan, .node[id^=\"note\"] .nodeLabel, .node[id^=\"note\"] .nodeLabel p { color: #26210a !important; fill: #26210a !important; }","layout":"dagre","class":{"nodeSpacing":60,"rankSpacing":117,"diagramPadding":24,"hideEmptyMembersBox":true},"flowchart":{"nodeSpacing":60,"rankSpacing":117,"curve":"linear"}}}%%
classDiagram
    direction TB

    class ResultadosManager {
        <<interface>>
        +inicializarMarcador(Partida p) void
        +registrarBajaYMuerte(String idAtacante, String idVictima, String idPartida) void
        +obtenerMarcadorActual(String idPartida) Scoreboard
        +cerrarPartidaYCalcularRecompensas(String idPartida) void
        +limpiar() void
    }

    class ResultadosManagerImpl {
        -ResultadosManagerImpl instance$
        -Map historialMarcadores
        +getInstance() ResultadosManagerImpl$
        +onBaja(Partida p, String idAsesino, String idVictima) void
        +onPartidaFinalizada(Partida p) void
    }

    class PartidaListener {
        <<interface>>
        +onBaja(Partida partida, String idAsesino, String idVictima) void
        +onPartidaFinalizada(Partida partida) void
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

    class Partida

    class Participante

    ResultadosManagerImpl ..|> ResultadosManager
    ResultadosManagerImpl ..|> PartidaListener
    ResultadosManagerImpl "1" o-- "0..*" Scoreboard : historialMarcadores por<br/>idPartida
    ResultadosManagerImpl ..> Partida : inicializa desde sus<br/>participantes
    ResultadosManagerImpl ..> ResultadoJugador : actualiza estadísticas y<br/>recompensas
    Scoreboard "1" *-- "0..10" ResultadoJugador : clasificacion
    Scoreboard ..> Partida : idPartida
    ResultadoJugador ..> Partida : idPartida
    ResultadoJugador ..> Participante : idJugador

    note for Scoreboard "procesado evita recalcular las recompensas al<br/>repetir el cierre."
    style ResultadosManager fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
    style ResultadosManagerImpl fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
    style PartidaListener fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style Scoreboard fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
    style ResultadoJugador fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
    style Partida fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style Participante fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
```

`historialMarcadores` es un `Map<String, Scoreboard>`. La implementación es
Singleton. Cada baja suma 100 puntos; al cerrar se calculan `10 * bajas + 50`
créditos por participante. La clasificación se ordena por puntuación descendente,
bajas descendentes y muertes ascendentes.

## 6. Servicios REST y objetos de transferencia

Paquetes: [`services`](../src/main/java/edu/upc/dsa/services)
y [`domain.dtos`](../src/main/java/edu/upc/dsa/domain/dtos).
Se muestran las dependencias usadas actualmente por los servicios del juego.

### 6.1. Jugadores, compras y DTO

```mermaid
%%{init: {"theme":"base","themeVariables":{"darkMode":true,"background":"#1b1b1f","primaryColor":"#0d111a","primaryTextColor":"#e6edf3","primaryBorderColor":"#d779ed","secondaryColor":"#0d111a","secondaryTextColor":"#e6edf3","tertiaryColor":"#0d111a","tertiaryTextColor":"#e6edf3","textColor":"#e6edf3","classText":"#e6edf3","lineColor":"#b8bfcc","edgeLabelBackground":"#1b1b1f","noteBkgColor":"#fff7be","noteBorderColor":"#f3c600","noteTextColor":"#26210a"},"themeCSS":"background-color: #1b1b1f; .noteText, .noteText tspan, .node[id^=\"note\"] .nodeLabel, .node[id^=\"note\"] .nodeLabel p { color: #26210a !important; fill: #26210a !important; }","layout":"dagre","class":{"nodeSpacing":103,"rankSpacing":100,"diagramPadding":24,"hideEmptyMembersBox":true},"flowchart":{"nodeSpacing":103,"rankSpacing":100,"curve":"linear"}}}%%
classDiagram
    direction TB

    class JugadorService {
        +crearJugador(JugadorDTO jugador) Response
        +getJugador(String id) Response
    }

    class TiendaService {
        +comprarObjeto(CompraDTO compra) Response
    }

    class JugadorDTO {
        -String id
        -String nombre
        -int nivel
        -int experiencia
        -int creditos
    }

    class CompraDTO {
        -String jugadorId
        -String itemId
    }

    class ResultadoJugadorDTO {
        -String jugadorId
        -String partidaId
        -int bajas
        -int muertes
        -int puntuacion
        -int creditosGanados
    }

    JugadorService ..> JugadorDTO : recibe y devuelve
    TiendaService ..> CompraDTO : recibe y devuelve

    note for JugadorService "Implementación provisional con datos simulados."
    note for TiendaService "Devuelve la solicitud; todavía no ejecuta la<br/>compra."
    note for ResultadoJugadorDTO "Definido, pero aún no utilizado por los<br/>servicios."
    style JugadorService fill:#0d111a,stroke:#d779ed,stroke-width:2px,color:#e6edf3
    style TiendaService fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style JugadorDTO fill:#0d111a,stroke:#d779ed,stroke-width:2px,color:#e6edf3
    style CompraDTO fill:#0d111a,stroke:#ff9b40,stroke-width:2px,color:#e6edf3
    style ResultadoJugadorDTO fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
```

### 6.2. Servicios de partidas y resultados

```mermaid
%%{init: {"theme":"base","themeVariables":{"darkMode":true,"background":"#1b1b1f","primaryColor":"#0d111a","primaryTextColor":"#e6edf3","primaryBorderColor":"#d779ed","secondaryColor":"#0d111a","secondaryTextColor":"#e6edf3","tertiaryColor":"#0d111a","tertiaryTextColor":"#e6edf3","textColor":"#e6edf3","classText":"#e6edf3","lineColor":"#b8bfcc","edgeLabelBackground":"#1b1b1f","noteBkgColor":"#fff7be","noteBorderColor":"#f3c600","noteTextColor":"#26210a"},"themeCSS":"background-color: #1b1b1f; .noteText, .noteText tspan, .node[id^=\"note\"] .nodeLabel, .node[id^=\"note\"] .nodeLabel p { color: #26210a !important; fill: #26210a !important; }","layout":"dagre","class":{"nodeSpacing":120,"rankSpacing":240,"diagramPadding":24,"hideEmptyMembersBox":true},"flowchart":{"nodeSpacing":120,"rankSpacing":240,"curve":"linear"}}}%%
classDiagram
    direction LR

    class PartidaService {
        +crearPartida(String jugadorId) Response
        +iniciarPartida(String idPartida) Response
        +getPartidas() Response
        +finalizarPartida(String idPartida) Response
    }

    class ResultadosService {
        +getScoreboard(String partidaId) Response
    }

    class PartidaManagerImpl

    class ResultadosManagerImpl

    class Partida

    class Scoreboard

    PartidaService ..> PartidaManagerImpl : obtiene el gestor<br/>Singleton
    PartidaService ..> ResultadosManagerImpl : inicializarMarcador
    PartidaService ..> Partida : devuelve en Response
    ResultadosService ..> ResultadosManagerImpl : obtenerMarcadorActual
    ResultadosService ..> Scoreboard : devuelve en Response

    style PartidaService fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style ResultadosService fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
    style PartidaManagerImpl fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style ResultadosManagerImpl fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
    style Partida fill:#0d111a,stroke:#00c9e5,stroke-width:2px,color:#e6edf3
    style Scoreboard fill:#0d111a,stroke:#00c9a7,stroke-width:2px,color:#e6edf3
```

`PartidaService.crearPartida` crea una partida pendiente con un humano y nueve
bots e inicializa su marcador. El inicio se ejecuta mediante `iniciarPartida`.
Los servicios obtienen los gestores mediante llamadas estáticas a `getInstance`;
no los conservan en atributos.

## 7. Arranque, servicios de infraestructura y errores

Paquetes: [`edu.upc.dsa`](../src/main/java/edu/upc/dsa),
[`services`](../src/main/java/edu/upc/dsa/services)
y [`exceptions`](../src/main/java/edu/upc/dsa/exceptions).
`HttpServer`, `Swagger`, `ExceptionMapper`, `Throwable`, `Exception` y
`RuntimeException` pertenecen a las bibliotecas o al JDK.

### 7.1. Arranque y servicios de infraestructura

```mermaid
%%{init: {"theme":"base","themeVariables":{"darkMode":true,"background":"#1b1b1f","primaryColor":"#0d111a","primaryTextColor":"#e6edf3","primaryBorderColor":"#d779ed","secondaryColor":"#0d111a","secondaryTextColor":"#e6edf3","tertiaryColor":"#0d111a","tertiaryTextColor":"#e6edf3","textColor":"#e6edf3","classText":"#e6edf3","lineColor":"#b8bfcc","edgeLabelBackground":"#1b1b1f","noteBkgColor":"#fff7be","noteBorderColor":"#f3c600","noteTextColor":"#26210a"},"themeCSS":"background-color: #1b1b1f; .noteText, .noteText tspan, .node[id^=\"note\"] .nodeLabel, .node[id^=\"note\"] .nodeLabel p { color: #26210a !important; fill: #26210a !important; }","layout":"dagre","class":{"nodeSpacing":60,"rankSpacing":214,"diagramPadding":24,"hideEmptyMembersBox":true},"flowchart":{"nodeSpacing":60,"rankSpacing":214,"curve":"linear"}}}%%
classDiagram
    direction TB

    class Main {
        +String BASE_URI$
        +startServer() HttpServer$
        +startServer(URI uri) HttpServer$
        +stopServer(HttpServer server) void$
        +main(String[] args) void$
    }

    class HttpServer

    class StatusService {
        +getStatus() Status
    }

    class Status {
        -String status
    }

    class SwaggerService {
        +String MODEL_PROPERTY$
        +getSwagger(Application application) Swagger
    }

    class Swagger

    Main ..> HttpServer : crea y detiene
    StatusService ..> Status : crea la respuesta
    SwaggerService ..> Swagger : consulta el modelo de la<br/>aplicación

    style Main fill:#0d111a,stroke:#a6a0ff,stroke-width:2px,color:#e6edf3
    style HttpServer fill:#0d111a,stroke:#b8bfcc,stroke-width:2px,color:#e6edf3
    style StatusService fill:#0d111a,stroke:#a6a0ff,stroke-width:2px,color:#e6edf3
    style Status fill:#0d111a,stroke:#a6a0ff,stroke-width:2px,color:#e6edf3
    style SwaggerService fill:#0d111a,stroke:#a6a0ff,stroke-width:2px,color:#e6edf3
    style Swagger fill:#0d111a,stroke:#b8bfcc,stroke-width:2px,color:#e6edf3
```

### 7.2. Adaptador de excepciones y respuesta de error

```mermaid
%%{init: {"theme":"base","themeVariables":{"darkMode":true,"background":"#1b1b1f","primaryColor":"#0d111a","primaryTextColor":"#e6edf3","primaryBorderColor":"#d779ed","secondaryColor":"#0d111a","secondaryTextColor":"#e6edf3","tertiaryColor":"#0d111a","tertiaryTextColor":"#e6edf3","textColor":"#e6edf3","classText":"#e6edf3","lineColor":"#b8bfcc","edgeLabelBackground":"#1b1b1f","noteBkgColor":"#fff7be","noteBorderColor":"#f3c600","noteTextColor":"#26210a"},"themeCSS":"background-color: #1b1b1f; .noteText, .noteText tspan, .node[id^=\"note\"] .nodeLabel, .node[id^=\"note\"] .nodeLabel p { color: #26210a !important; fill: #26210a !important; }","layout":"dagre","class":{"nodeSpacing":60,"rankSpacing":131,"diagramPadding":24,"hideEmptyMembersBox":true},"flowchart":{"nodeSpacing":60,"rankSpacing":131,"curve":"linear"}}}%%
classDiagram
    direction TB

    class ExceptionMapper {
        <<interface>>
        +toResponse(Throwable exception) Response
    }

    class MyExceptionMapper {
        +toResponse(Throwable exception) Response
    }

    class ErrorEntity {
        -int code
        -String message
    }

    class Throwable

    MyExceptionMapper ..|> ExceptionMapper : ExceptionMapper de<br/>Throwable
    MyExceptionMapper ..> Throwable : recibe y clasifica
    MyExceptionMapper ..> ErrorEntity : crea la respuesta JSON

    style ExceptionMapper fill:#0d111a,stroke:#b8bfcc,stroke-width:2px,color:#e6edf3
    style MyExceptionMapper fill:#0d111a,stroke:#a6a0ff,stroke-width:2px,color:#e6edf3
    style ErrorEntity fill:#0d111a,stroke:#a6a0ff,stroke-width:2px,color:#e6edf3
    style Throwable fill:#0d111a,stroke:#b8bfcc,stroke-width:2px,color:#e6edf3
```

### 7.3. Jerarquía de excepciones

```mermaid
%%{init: {"theme":"base","themeVariables":{"darkMode":true,"background":"#1b1b1f","primaryColor":"#0d111a","primaryTextColor":"#e6edf3","primaryBorderColor":"#d779ed","secondaryColor":"#0d111a","secondaryTextColor":"#e6edf3","tertiaryColor":"#0d111a","tertiaryTextColor":"#e6edf3","textColor":"#e6edf3","classText":"#e6edf3","lineColor":"#b8bfcc","edgeLabelBackground":"#1b1b1f","noteBkgColor":"#fff7be","noteBorderColor":"#f3c600","noteTextColor":"#26210a"},"themeCSS":"background-color: #1b1b1f; .noteText, .noteText tspan, .node[id^=\"note\"] .nodeLabel, .node[id^=\"note\"] .nodeLabel p { color: #26210a !important; fill: #26210a !important; }","layout":"dagre","class":{"nodeSpacing":60,"rankSpacing":154,"diagramPadding":24,"hideEmptyMembersBox":true},"flowchart":{"nodeSpacing":60,"rankSpacing":154,"curve":"linear"}}}%%
classDiagram
    direction LR

    class Throwable

    class Exception

    class RuntimeException

    class DatosInvalidosException

    class EstadoPartidaInvalidoException

    class ParticipanteNotFoundException

    class PartidaLlenaException

    class PartidaNotFoundException

    class JugadorNotFoundException

    class NivelBloqueadoException

    class SaldoInsuficienteException

    Exception --|> Throwable
    RuntimeException --|> Exception
    DatosInvalidosException --|> Exception
    EstadoPartidaInvalidoException --|> Exception
    ParticipanteNotFoundException --|> Exception
    PartidaLlenaException --|> Exception
    PartidaNotFoundException --|> Exception
    JugadorNotFoundException --|> RuntimeException
    NivelBloqueadoException --|> RuntimeException
    SaldoInsuficienteException --|> RuntimeException

    style Throwable fill:#0d111a,stroke:#b8bfcc,stroke-width:2px,color:#e6edf3
    style Exception fill:#0d111a,stroke:#b8bfcc,stroke-width:2px,color:#e6edf3
    style RuntimeException fill:#0d111a,stroke:#b8bfcc,stroke-width:2px,color:#e6edf3
    style DatosInvalidosException fill:#0d111a,stroke:#ff7b86,stroke-width:2px,color:#e6edf3
    style EstadoPartidaInvalidoException fill:#0d111a,stroke:#ff7b86,stroke-width:2px,color:#e6edf3
    style ParticipanteNotFoundException fill:#0d111a,stroke:#ff7b86,stroke-width:2px,color:#e6edf3
    style PartidaLlenaException fill:#0d111a,stroke:#ff7b86,stroke-width:2px,color:#e6edf3
    style PartidaNotFoundException fill:#0d111a,stroke:#ff7b86,stroke-width:2px,color:#e6edf3
    style JugadorNotFoundException fill:#0d111a,stroke:#ff7b86,stroke-width:2px,color:#e6edf3
    style NivelBloqueadoException fill:#0d111a,stroke:#ff7b86,stroke-width:2px,color:#e6edf3
    style SaldoInsuficienteException fill:#0d111a,stroke:#ff7b86,stroke-width:2px,color:#e6edf3
```

`Status` y `ErrorEntity` son clases anidadas estáticas:
`StatusService.Status` y `MyExceptionMapper.ErrorEntity`, respectivamente.
`Main` configura Jersey y Grizzly, el descubrimiento de recursos del paquete
`edu.upc.dsa`, la serialización JSON, Swagger y la publicación de archivos
estáticos de `public`.

El adaptador `MyExceptionMapper` convierte las excepciones en respuestas JSON:

| Excepción | Código HTTP |
| --- | --- |
| `DatosInvalidosException` | 400 |
| `JugadorNotFoundException`, `ParticipanteNotFoundException`, `PartidaNotFoundException` | 404 |
| `EstadoPartidaInvalidoException`, `PartidaLlenaException` | 409 |
| `NivelBloqueadoException`, `SaldoInsuficienteException` | 403 |
| Cualquier otra excepción | 500 |

## Diferencias respecto al diseño previsto en el README

- `Jugador` es provisional: tiene `saldo` e `inventario`, pero todavía no incluye
  `vida`, `experiencia` ni un atributo llamado `creditos`. La distinción entre
  humano y bot está en `Participante`.
- Aún no existen `JugadorManager` ni `JugadorManagerImpl`. `JugadorDTO` ya incluye
  experiencia y créditos, pero no hay una conversión implementada con `Jugador`.
- La tienda permite comprar objetos y el inventario permite consumir granadas;
  todavía no hay operaciones ni atributos para equipar armas o complementos.
- `ResultadosManagerImpl` implementa `PartidaListener`, pero ningún código de
  producción lo registra con `PartidaManager.addListener`. Los avisos de bajas y
  finalización necesitan ese registro para llegar al módulo de resultados.
- Las recompensas se calculan en `ResultadoJugador`; su aplicación al saldo y
  la experiencia del jugador permanece comentada en el gestor de resultados.
- Los servicios de jugadores y tienda son provisionales. `ResultadoJugadorDTO`
  aún no se utiliza y parte de las operaciones de `PartidaManager` no tiene un
  recurso REST asociado.
