# multi-ai-provider

Aplicación Spring Boot de dos módulos: `ai` es una librería que implementa el
`ChatModel` de Spring AI contra una API mock de completions, y `boot` es la
raíz de composición que la consume. El sistema solo hace requests salientes —
no expone ningún endpoint HTTP propio.

```
ai      librería Spring AI — MockAiChatModel + auto-configuración
boot    raíz de composición — el único artefacto ejecutable
```

El módulo `ai` no se escanea por componentes: aporta sus beans con una
`@AutoConfiguration` registrada en
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
Depender del jar *es* la integración completa. Los beans que publica:

| Bean | Qué es |
| --- | --- |
| `MockAiProperties` | configuración (`ai.mock.base-url`) |
| `MockAiApi` | cliente HTTP de la mock API (`POST /completions`) |
| `MockAiChatModel` | la implementación de `ChatModel` |
| `ChatClient` | la API fluida de Spring AI sobre ese `ChatModel` |

Todos son `@ConditionalOnMissingBean`: declarar un `ChatModel` propio en la
aplicación lo reemplaza sin tocar el módulo.

## Setup

Requiere **JDK 25** (el build lo verifica con `maven-enforcer-plugin` y falla
si `JAVA_HOME` apunta a otra versión).

```bash
mvn verify   # build + tests de los dos módulos
```

## Entornos y correr

La app hace una llamada de prueba al arrancar (en el `main`). Contra qué mock
API la hace lo decide el perfil de Spring, exportado como variable de entorno:

| Entorno | `SPRING_PROFILES_ACTIVE` | Mock API |
| --- | --- | --- |
| prod (default) | — (o `prod`) | `https://competions-mock-api.vercel.app` |
| local | `local` | `http://localhost:8000` |

**Prod (default)** — pega a la mock API hosteada en Vercel, no requiere nada
corriendo local:

```bash
mvn -q package -DskipTests
java -jar boot/target/boot-1.0-SNAPSHOT.jar
```

**Local** — primero levantá la mock API del repo hermano `competions-mock-api`
(FastAPI, puerto 8000) y después la app con el perfil `local`:

```bash
cd ../competions-mock-api
.venv/bin/uvicorn main:app --port 8000 &
cd ../multi-ai-provider

export SPRING_PROFILES_ACTIVE=local
java -jar boot/target/boot-1.0-SNAPSHOT.jar
```

Para apuntar a cualquier otra URL sin tocar perfiles, la variable de entorno
`MOCK_AI_BASE_URL` pisa el default del perfil activo:

```bash
export MOCK_AI_BASE_URL=https://otra-instancia.example.com
java -jar boot/target/boot-1.0-SNAPSHOT.jar
```

## Usar

No hay controllers: se consume inyectando el `ChatClient` (o el `ChatModel`)
auto-configurado en cualquier bean de la aplicación:

```java
@Component
public class MyService {

    private final ChatClient chatClient;

    public MyService(final ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String ask(final String prompt) {
        return this.chatClient.prompt().user(prompt).call().content();
    }
}
```

La URL de la mock API se configura con `ai.mock.base-url`:
`boot/src/main/resources/application.yml` trae el default de prod (Vercel) y
`application-local.yml` el del perfil `local` (localhost).
