# Módulo de Partidas (Franklin): explicado paso a paso

Este documento es para que entiendas y puedas **defender** tu parte en la presentación del 12/11.

## 1. Qué hace el módulo
Gestiona la vida de una partida del shooter:
- Se **crea** con 1 jugador humano y entre 0 y 9 bots (máximo **10 participantes**).
- Se **inicia** y empieza a contar el tiempo.
- Durante la partida llegan **bajas** (alguien mata a alguien) y **reapariciones** (el muerto vuelve; ilimitadas).
- A los **5 minutos** se cierra sola, o antes si el cliente (Unity) avisa con "finalizar".
- Al cerrarse **avisa al módulo de resultados** (Tiffany) para que reparta créditos y experiencia.

## 2. Clases (diagrama de clases)
| Clase | Qué es | Atributos |
|---|---|---|
| `Partida` | Una partida concreta | `id`, `idJugadorHumano`, `participantes` (lista), `estado`, `inicioMillis`, `finMillis`. Constantes `MAX_PARTICIPANTES = 10`, `DURACION_SEGUNDOS = 300` |
| `Participante` | Un jugador **dentro** de una partida | `idJugador`, `bot` (sí/no), `vivo` (sí/no) |
| `EstadoPartida` (enum) | Estados posibles | `PENDIENTE`, `EN_CURSO`, `FINALIZADA` |
| `PartidaManager` (interfaz) | Qué operaciones ofrece el módulo | crear, consultar, iniciar, baja, reaparecer, finalizar, tiempo restante |
| `PartidaManagerImpl` | La implementación en memoria, **Singleton** | `HashMap<String, Partida>`, lista de listeners, reloj |
| `PartidaListener` (interfaz) | Patrón **Observer** para avisar a otros módulos | `onBaja(...)`, `onPartidaFinalizada(...)` |

Relaciones:
- `Partida` **1 → 1..10** `Participante` (composición: los participantes solo existen dentro de su partida).
- `Participante` → `Jugador` (de Cristian) por `idJugador`. Guardamos el id y no el objeto entero para no crear referencias circulares en el JSON (lo pide la tarea de Azahara).
- `Partida` **1 → 1** `Scoreboard` (de Tiffany). Lo crea y rellena el módulo de resultados cuando recibe los avisos.

**¿Por qué `Participante` y no usar `Jugador` directamente?** Porque "estar vivo en esta partida" no es algo del jugador, es algo del jugador **en esa partida**. Así el módulo de Cristian no tiene que saber nada de partidas.

## 3. Estados
```
PENDIENTE --iniciar--> EN_CURSO --finalizar / 5 minutos--> FINALIZADA
```
- Solo se puede iniciar una partida PENDIENTE.
- Bajas, reapariciones y finalizar solo en EN_CURSO.
- No se vuelve atrás. Si algo no encaja con el estado, error **409 Conflict**.

## 4. Reglas que controla
- Más de 10 participantes → `PartidaLlenaException` (409).
- Humano vacío o bots negativos → `DatosInvalidosException` (400).
- Matar a alguien que ya está muerto → 409. Reaparecer estando vivo → 409.
- Jugador que no está en la partida → `ParticipanteNotFoundException` (404).
- Partida inexistente → `PartidaNotFoundException` (404).
- Cada vez que se toca una partida en curso se mira el reloj: si han pasado 5 minutos, se finaliza sola. Finalizar avisa **una sola vez** a los listeners, así no se duplican recompensas.

## 5. API REST de partidas (contrato para Azahara)
Base: `/dsaApp/partidas`. Todo en JSON.

| Método | Ruta | Cuerpo | Respuesta OK | Errores |
|---|---|---|---|---|
| POST | `/partidas` | `{"idJugadorHumano":"franklin","numBots":5}` | **201** + Partida | 400 datos inválidos, 404 jugador no existe, 409 más de 10 |
| GET | `/partidas` | | **200** lista de partidas | |
| GET | `/partidas?estado=EN_CURSO` | | **200** lista filtrada | 400 estado no válido |
| GET | `/partidas/{id}` | | **200** Partida | 404 |
| PUT | `/partidas/{id}/iniciar` | | **200** Partida (EN_CURSO) | 404, 409 si no está PENDIENTE |
| POST | `/partidas/{id}/bajas` | `{"idAsesino":"franklin","idVictima":"bot-2"}` | **201** | 404 partida o jugador, 409 no está en curso o víctima ya muerta |
| POST | `/partidas/{id}/reapariciones` | `{"idJugador":"bot-2"}` | **200** | 404, 409 no está en curso o el jugador está vivo |
| PUT | `/partidas/{id}/finalizar` | | **200** Partida (FINALIZADA) | 404, 409 si no está EN_CURSO |
| GET | `/partidas/{id}/tiempo` | | **200** `{"segundosRestantes":240}` | 404 |

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

## 6. Decisiones de diseño (preguntas que te pueden hacer)
- **¿Por qué Singleton?** Porque todos los servicios REST tienen que ver las mismas partidas. Si cada uno hiciera `new`, cada uno tendría su propio `HashMap` vacío.
- **¿Por qué HashMap?** Buscar una partida por id es O(1).
- **¿Por qué Observer (`PartidaListener`)?** Para que Partidas no dependa del código de Tiffany: ella se registra como "oyente" y recibe los avisos de baja y de fin. Es uno de los patrones de la Unidad 4.
- **¿Cómo probáis los 5 minutos sin esperar?** El manager usa una interfaz `Reloj`. En el programa es la hora del sistema; en los tests es un reloj falso que adelantamos 300 segundos de golpe.
- **¿Quién decide que acaba la partida?** El servidor es la autoridad: aunque el cliente no avise, a los 5 minutos se cierra y no acepta más bajas.

## 7. Tests (JUnit 4): 18 pruebas, todas en verde
Creación con humano y bots, máximo 10, más de 10 falla, datos inválidos, partida inexistente, ciclo de estados completo, no iniciar dos veces, no finalizar una pendiente, finalizar dos veces no avisa dos veces, baja y reaparición (repetidas), no bajas en pendiente, no matar a un muerto, un vivo no reaparece, jugador que no participa, tiempo restante, a los 299 s sigue en curso, a los 300 s se cierra sola, baja después de 5 minutos rechazada.

## 8. Pendiente de acordar con el equipo
- **Cristian**: comprobar que el jugador humano existe al crear la partida (cuando exista su `JugadorManager`).
- **Tiffany**: su gestor de resultados implementa `PartidaListener` y se registra con `PartidaManagerImpl.getInstance().addListener(...)`.
- **Azahara**: el servicio REST `PartidaService` con las rutas de arriba y mapear las excepciones a 400/404/409.
- **Pol**: he añadido al `pom.xml` solo Log4j2 y JUnit 4, y un `log4j2.properties` básico. Él puede ampliarlos.
