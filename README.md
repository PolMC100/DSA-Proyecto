# DSA-Proyecto
Proyecto Java 25 con Maven. Actualmente no tiene dependencias; las del servidor REST y las pruebas se incorporarán durante el desarrollo.

## Configuración del proyecto

### Estructura

- `src/main/java`: código Java.
- `src/main/resources`: recursos de la aplicación.
- `src/test/java`: pruebas.
- `src/test/resources`: recursos de las pruebas.

### Compilación

Requiere JDK 25 o superior y Maven.

```sh
mvn compile
```

En IntelliJ IDEA, abrir el archivo `pom.xml` como proyecto Maven.

## Idea del juego

Shooter para un jugador con bots, en modo todos contra todos (*free for all*).
El escenario es el aula de DSA y los jugadores tienen el tamaño de una hormiga.

### Partidas

- Cada partida dura **5 minutos**.
- Participan un jugador humano y bots, con un **máximo de 10 participantes en total**.
- Los jugadores pueden reaparecer un número ilimitado de veces mientras dure la partida.
- Cada partida dispone de un marcador (*scoreboard*) con los resultados de sus participantes.

### Tienda y progresión

- Los jugadores ganan créditos en las partidas y pueden gastarlos en armas, granadas y complementos.
- La experiencia permite subir de nivel y desbloquear armas, granadas y complementos.
- Los objetos se pueden comprar desde el menú o durante una partida.
- Las compras se guardan en el inventario del jugador y se pueden usar en la misma partida.
- Las granadas son consumibles: se gastan al usarlas y hay que comprarlas de nuevo.

## Modelo de clases

| Entidad | Atributos y relaciones | Tipos o ejemplos |
| --- | --- | --- |
| Jugador | Nombre, vida, inventario, experiencia, nivel y créditos. | Humano o bot. |
| Arma | Nombre, munición, cadencia y daño. | Pistola, escopeta, rifle, ametralladora y francotirador. |
| Granada | Nombre, daño, radio, efecto y duración del efecto. | Fragmentación, humo y flash. |
| Complemento | Nombre, tipo y efecto. | Chaleco, casco y jetpack. |
| Partida | Marcador, tiempo y jugadores participantes. | Duración de 5 minutos y hasta 10 participantes. |
| Scoreboard | Colección de resultados de los jugadores de una partida. | Un marcador por partida. |
| ResultadoJugador | Jugador, partida, bajas (*kills*), muertes, puntuación y créditos ganados. | Un resultado por jugador y partida. |

### Resultados de una partida

`ResultadoJugador` recoge las estadísticas de un jugador en una partida concreta:

- **Jugador:** participante al que pertenecen las estadísticas.
- **Partida:** partida en la que se han obtenido.
- **Bajas y muertes:** número de eliminaciones realizadas y veces que ha muerto.
- **Puntuación:** experiencia ganada por el jugador en esa partida.
- **Créditos ganados:** créditos obtenidos en esa partida.

El `Scoreboard` reúne los resultados de todos los participantes. Las estadísticas
individuales se guardan en `ResultadoJugador`. La experiencia y los créditos de
`Jugador` representan los valores acumulados del jugador; los de `ResultadoJugador`
representan lo ganado en una partida concreta.

## Estructura de desarrollo

El servidor utilizará Jersey y Grizzly, con Swagger para documentar
la API, Log4j2 para las trazas y JUnit para las pruebas.

```text
pom.xml
public/
└── swagger/                         # Interfaz de documentación de la API
src/
├── main/
│   ├── java/edu/upc/dsa/
│   │   ├── Main.java                # Arranque del servidor Grizzly
│   │   ├── MyExceptionMapper.java   # Conversión de excepciones a respuestas HTTP
│   │   ├── domain/
│   │   │   ├── models/              # Jugador, Arma, Granada, Partida, etc.
│   │   │   └── repository/          # Interfaces *Manager e implementaciones *ManagerImpl
│   │   ├── services/               # Recursos REST que llaman a los gestores
│   │   └── util/                   # Adaptadores y utilidades necesarios
│   └── resources/
│       └── log4j2.properties
└── test/
    ├── java/edu/upc/dsa/            # Pruebas de gestores y servicios REST
    └── resources/                  # Datos y configuración de pruebas
```

La primera versión utilizará gestores singleton y almacenamiento en memoria.

## To-do

### Reparto de responsabilidades del backend

| Responsable | Área | Entregables principales | Dependencias |
| --- | --- | --- | --- |
| Pol | Base del proyecto y arquitectura | Maven, servidor, configuración de logs, estructura de paquetes y coordinación del diagrama. | Acuerdos iniciales del equipo. |
| Cristian | Jugadores y progresión | Modelo y gestor de jugadores; perfil, experiencia, niveles y desbloqueos; pruebas del módulo. | Reglas de progresión acordadas con integrantes 3 y 5. |
| Gur | Catálogo, tienda e inventario | Armas, granadas, complementos e inventario; gestor de catálogo y compras; pruebas del módulo. | Saldo y desbloqueos del módulo de jugadores. |
| Franklin | Partidas | Modelo y gestor de partidas; participantes, estados, duración y reglas de reaparición; pruebas del módulo. | Jugadores y contrato de comunicación con el cliente. |
| Tiffany | Resultados y marcador | `ResultadoJugador`, `Scoreboard`, clasificación y cálculo de recompensas; pruebas del módulo. | Partidas y operaciones de actualización de jugadores. |
| Azahara | API REST e integración | Servicios REST, errores HTTP, Swagger y pruebas HTTP del flujo completo. | Interfaces de los gestores acordadas con integrantes 2–5 y servidor del integrante 1. |

### Pol — base del proyecto y arquitectura

- [ ] Preparar los paquetes `edu.upc.dsa` siguiendo la estructura de referencia.
- [ ] Configurar en `pom.xml` Jersey, Grizzly, serialización JSON, Swagger, Log4j2, JUnit y el arranque con Maven.
- [ ] Implementar `Main` con arranque y parada del servidor, registro de servicios y publicación de `public/swagger`.
- [ ] Configurar `log4j2.properties` y comprobar que se registran operaciones y errores.
- [ ] Coordinar el diagrama de clases con los modelos y contratos aportados por el equipo.
- [ ] Documentar los comandos de compilación, pruebas y ejecución, y comprobarlos desde una copia limpia del proyecto.

### Cristian — jugadores y progresión

- [ ] Implementar `Jugador`, distinguiendo entre humano y bot, con sus identificadores y atributos acordados.
- [ ] Definir la interfaz del gestor de jugadores e implementar su versión en memoria.
- [ ] Implementar la creación y consulta de jugadores y la actualización de sus créditos y experiencia.
- [ ] Implementar el cálculo de nivel y la consulta de objetos desbloqueados.
- [ ] Acordar con integrantes 3 y 5 las operaciones para descontar compras y aplicar recompensas sin duplicarlas.
- [ ] Probar identificadores duplicados, jugadores inexistentes, saldo insuficiente y cambios de nivel.

### Gur — catálogo, tienda e inventario

- [ ] Implementar `Arma`, `Granada`, `Complemento` e `Inventario` con las relaciones acordadas.
- [ ] Definir la interfaz del gestor de catálogo y tienda e implementar su versión en memoria.
- [ ] Preparar un catálogo inicial con precios, estadísticas y requisitos de desbloqueo.
- [ ] Implementar la consulta del catálogo y la compra desde el menú o durante una partida.
- [ ] Validar saldo y desbloqueo, descontar créditos y añadir el objeto al inventario como una única operación.
- [ ] Implementar la consulta del inventario, el equipamiento y el consumo de granadas.
- [ ] Probar compras válidas, objetos bloqueados, saldo insuficiente y consumo sin unidades disponibles.

### Franklin — partidas

- [ ] Implementar `Partida` y sus estados: pendiente, en curso y finalizada.
- [ ] Definir la interfaz del gestor de partidas e implementar su versión en memoria.
- [ ] Implementar la creación e inicio de partidas con un humano y bots, hasta 10 participantes en total.
- [ ] Implementar el control de los 5 minutos y el cierre de partida según el reparto de responsabilidades con el cliente.
- [ ] Definir y procesar los eventos de baja y reaparición necesarios para actualizar el estado de la partida.
- [ ] Notificar la finalización al módulo de resultados y rechazar acciones incompatibles con el estado de la partida.
- [ ] Probar el límite de participantes, los cambios de estado y el cierre por tiempo sin esperar cinco minutos reales en las pruebas.

### Tiffany — resultados, marcador y recompensas

- [ ] Implementar `ResultadoJugador` y `Scoreboard`, con un único resultado por jugador y partida.
- [ ] Definir la interfaz del gestor de resultados e implementar su versión en memoria.
- [ ] Actualizar bajas y muertes de los participantes y permitir consultar el marcador durante la partida.
- [ ] Ordenar la clasificación con el criterio de puntuación y desempate acordado.
- [ ] Calcular la experiencia y los créditos ganados y aplicar las recompensas al jugador una sola vez.
- [ ] Conservar los resultados de las partidas finalizadas en memoria para su consulta.
- [ ] Probar empates, resultados duplicados y la repetición del cierre de una partida sin duplicar recompensas.

### Azahara — API REST e integración

- [ ] Implementar los recursos REST de jugadores, catálogo, tienda, inventario, partidas y resultados, delegando en los gestores.
- [ ] Definir las representaciones JSON y evitar referencias circulares entre jugador, partida y resultado.
- [ ] Implementar `MyExceptionMapper` y unificar las respuestas para datos inválidos, recursos inexistentes y conflictos.
- [ ] Añadir anotaciones Swagger a las operaciones y configurar `public/swagger` para consultar y probar la API.
- [ ] Preparar datos de prueba y pruebas HTTP que arranquen y detengan el servidor de forma aislada.
- [ ] Verificar el flujo completo: crear jugador, consultar catálogo, comprar, iniciar partida, registrar resultados y consultar marcador y recompensas.
- [ ] Comprobar con el equipo que compras y cierres repetidos o simultáneos mantienen el saldo y las recompensas correctos.


### Entrega del 12/11

- [x] Idea del juego — todo el equipo.
- [ ] To-do completo
- [ ] PPT — coordina el integrante 6; cada integrante aporta la explicación de su área y todo el equipo revisa la presentación.
