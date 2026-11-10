# Partidas (Franklin)

## Alcance

Este módulo gestiona el ciclo de vida de una partida: creación con un jugador
humano y bots, inicio, bajas, reapariciones y cierre a los 5 minutos o cuando el
cliente lo indica. Contiene Partida, Participante, EstadoPartida,
PartidaManager/PartidaManagerImpl, PartidaListener, Reloj, las excepciones de
partidas, PartidaService con sus DTOs y las pruebas del módulo.

## Reglas

- Una partida tiene 1 jugador humano y entre 0 y 9 bots: como máximo 10 participantes.
- El humano tiene que existir en el módulo de jugadores y no puede ser un bot.
- La partida dura 5 minutos (`DURACION_SEGUNDOS = 300`) desde que se inicia.
- Las reapariciones son ilimitadas mientras la partida está en curso.
- Al finalizar se avisa una sola vez al módulo de resultados.

## Modelo

| Clase | Función | Atributos principales |
| --- | --- | --- |
| Partida | Una partida concreta. | id, idJugadorHumano, participantes, estado, inicioMillis, finMillis. Constantes MAX_PARTICIPANTES = 10 y DURACION_SEGUNDOS = 300. |
| Participante | Un jugador dentro de una partida. | idJugador, bot, vivo. |
| EstadoPartida | Enumerado de estados. | PENDIENTE, EN_CURSO, FINALIZADA. |
| PartidaManager | Contrato del gestor. | Crear, consultar, iniciar, registrar bajas y reapariciones, finalizar, tiempo restante y oyentes. |
| PartidaManagerImpl | Implementación en memoria, Singleton. | HashMap por id de partida, lista de PartidaListener, Reloj y contador de ids (P1, P2...). |
| PartidaListener | Interfaz del patrón Observer. | onBaja(partida, asesino, víctima) y onPartidaFinalizada(partida). |
| Reloj | Fuente de la hora actual. | ahoraMillis(). Hora del sistema en ejecución; reloj controlado en las pruebas. |

Relaciones:

- Partida 1 → 1..10 Participante, por composición: un participante solo existe dentro de su partida.
- Participante → Jugador por idJugador. Se guarda el identificador, no el objeto, para evitar referencias circulares en el JSON.
- Partida 1 → 1 Scoreboard. Lo crea y actualiza el módulo de resultados.

Participante es una clase propia porque estar vivo o muerto depende de la
partida, no del jugador. Así Jugador no necesita conocer las partidas.

## Estados

```text
PENDIENTE --iniciar--> EN_CURSO --finalizar / 5 minutos--> FINALIZADA
```

- Solo se puede iniciar una partida PENDIENTE.
- Las bajas, las reapariciones y la finalización solo se aceptan EN_CURSO.
- Los estados no retroceden. Una acción incompatible con el estado lanza
  EstadoPartidaInvalidoException (409).

## Funcionamiento del gestor

| Operación | Comportamiento |
| --- | --- |
| crearPartida(humano, numBots) | Valida los datos (400), el máximo de 10 participantes (409) y que el humano exista y no sea un bot (404). Crea la partida PENDIENTE con el humano y los bots bot-1 ... bot-N. |
| iniciarPartida(id) | Exige PENDIENTE, pasa a EN_CURSO y guarda la hora de inicio. |
| registrarBaja(id, asesino, víctima) | Exige EN_CURSO y que ambos participen. Rechaza una víctima ya muerta (409). Marca la víctima como muerta y avisa a los oyentes con onBaja. |
| reaparecer(id, jugador) | Exige EN_CURSO y que el jugador esté muerto (409 si está vivo). Lo vuelve a marcar como vivo. |
| finalizarPartida(id) | Exige EN_CURSO, pasa a FINALIZADA y avisa a los oyentes con onPartidaFinalizada. |
| getSegundosRestantes(id) | 300 si está PENDIENTE, 0 si está FINALIZADA y el tiempo que queda si está EN_CURSO. |
| addListener(oyente) | Registra un oyente. Si ya estaba registrado, lo ignora para no duplicar avisos. |

El cierre por tiempo no usa temporizadores. Cada operación sobre una partida en
curso comprueba el reloj y, si han pasado 5 minutos, la finaliza en ese momento
con `finMillis = inicio + 300 s`. El servidor es la autoridad: pasado ese tiempo
no acepta más bajas aunque el cliente no haya avisado.

Como solo se puede finalizar una partida EN_CURSO, el aviso de fin se emite una
única vez y las recompensas no se reparten dos veces.

## Integración con otros módulos

| Módulo | Integración |
| --- | --- |
| Jugadores | crearPartida consulta `JugadorManagerImpl.existeJugadorHumano(...)`. |
| Resultados | ResultadosManagerImpl implementa PartidaListener. `Main.startServer` lo registra con `addListener`. Cada baja actualiza el marcador y el fin de la partida reparte experiencia y créditos. |
| API REST | PartidaService delega en el gestor. MyExceptionMapper traduce las excepciones a 400, 404 y 409. |

## API REST

Base: `/dsaApp/partidas`. Todas las respuestas son JSON.

| Método | Ruta | Cuerpo | Respuesta OK | Errores |
|---|---|---|---|---|
| POST | `/partidas` | `{"idJugadorHumano":"franklin","numBots":5}` | **201** + Partida | 400 datos inválidos, 404 el humano no existe o es bot, 409 más de 10 |
| POST | `/partidas/crear/{jugadorId}` | | **201** + Partida con 9 bots | 400, 404 |
| GET | `/partidas` | | **200** lista de partidas | |
| GET | `/partidas?estado=EN_CURSO` | | **200** lista filtrada | 400 estado no válido |
| GET | `/partidas/{id}` | | **200** Partida | 404 |
| POST | `/partidas/{id}/iniciar` | | **200** Partida (EN_CURSO) | 404, 409 si no está PENDIENTE |
| POST | `/partidas/{id}/bajas` | `{"idAsesino":"franklin","idVictima":"bot-2"}` | **201** | 400 faltan datos o se elimina a sí mismo, 404 partida o jugador, 409 no está en curso o víctima ya muerta |
| POST | `/partidas/{id}/reapariciones` | `{"idJugador":"bot-2"}` | **200** | 400, 404, 409 no está en curso o el jugador está vivo |
| POST | `/partidas/{id}/finalizar` | | **200** Partida (FINALIZADA) | 404, 409 si no está EN_CURSO |
| GET | `/partidas/{id}/tiempo` | | **200** `{"idPartida":"P1","segundosRestantes":240}` | 404 |

Ejemplo de respuesta de `POST /partidas` (201):

```json
{
  "id": "P1",
  "idJugadorHumano": "franklin",
  "estado": "PENDIENTE",
  "inicioMillis": 0,
  "finMillis": 0,
  "participantes": [
    { "idJugador": "franklin", "bot": false, "vivo": true },
    { "idJugador": "bot-1", "bot": true, "vivo": true },
    { "idJugador": "bot-2", "bot": true, "vivo": true }
  ]
}
```

## Decisiones de diseño

- **Singleton:** todos los servicios REST comparten las mismas partidas en memoria.
- **HashMap por id:** la búsqueda de una partida es O(1) y es la operación más frecuente.
- **Observer (PartidaListener):** el módulo de partidas avisa de bajas y finales sin depender del código de resultados. Otros módulos pueden registrarse sin modificarlo.
- **Reloj inyectable:** permite probar el cierre a los 5 minutos sin esperar.
- **DTOs:** el cliente solo envía los datos necesarios y no puede fijar el estado, las horas ni los participantes.

## Pruebas

- `PartidaManagerTest` (22): creación y límite de 10 participantes, datos inválidos,
  partida inexistente, ciclo de estados, inicio y finalización repetidos, aviso de
  fin único, bajas y reapariciones, víctima ya muerta, reaparición estando vivo,
  jugador que no participa, tiempo restante, cierre exacto a los 300 s, baja
  después del cierre, humano inexistente, bot como humano, oyente repetido y
  actualización del marcador de resultados por el Observer.
- `PartidaServiceTest` (4, HTTP): flujo completo con marcador actualizado, listado
  y filtro por estado, errores de creación y errores de estado y participantes.

El tiempo se controla con un reloj falso (`pm.setReloj(() -> ahora)`): avanzar la
variable `ahora` 300 segundos equivale a esperar los 5 minutos.
