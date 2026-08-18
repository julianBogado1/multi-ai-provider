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

La mock API vive en el repo hermano `competions-mock-api` (FastAPI). Se
levanta en el puerto 8000:

```bash
cd ../competions-mock-api
.venv/bin/uvicorn main:app --port 8000
```

## Correr

```bash
mvn verify   # build + tests de los dos módulos
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

La URL de la mock API se configura con `ai.mock.base-url` en
`boot/src/main/resources/application.yml` (o la variable de entorno
`MOCK_AI_BASE_URL`).
