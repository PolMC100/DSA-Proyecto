# DSA-Proyecto
Proyecto Java 25 con Maven, sin dependencias.

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

## Tareas para el 12/11

- [x] Tarea 1: Idea del juego
- [ ] Tarea 2: Diagrama de clases
- [ ] Tarea 3: PPT
- [ ] Tarea 4: 
