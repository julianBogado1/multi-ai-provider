# multi-ai-provider

## Setup

Requires **JDK 25**.

```bash
mvn verify   # build + tests for both modules
```

## Running

The mock API base URL is hardcoded in `AICompletionProviderImpl`
(`https://competions-mock-api.vercel.app`), so nothing needs to run locally. To
hit a local mock instead, change the literal there and rebuild.

```bash
mvn -q package -DskipTests
java -jar boot/target/boot-1.0-SNAPSHOT.jar
```

To run the sibling `competions-mock-api` (FastAPI, port 8000) locally:

```bash
cd ../competions-mock-api
.venv/bin/uvicorn main:app --port 8000
```

## Usage

The app listens on `http://localhost:8080`.

**Only structured output is supported.** Every completion is a JSON Schema sent
as the request body; there is no plain-prompt mode.

### Run the use case

Copy-paste this once the app is running — it sends a JSON Schema to a named
provider and prints a document that complies with it:

```bash
curl -s -X POST 'http://localhost:8080/completions?schema=true&model=OpenAI' \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "object",
    "properties": {
      "name": {"type": "string"},
      "email": {"type": "string", "format": "email"},
      "plan_interest": {"type": "string", "enum": ["basic", "pro", "enterprise"]},
      "address": {"$ref": "#/$defs/Address"}
    },
    "required": ["name", "email", "plan_interest"],
    "$defs": {
      "Address": {
        "type": "object",
        "properties": {
          "city": {"type": "string"},
          "zip_code": {"type": "integer"}
        }
      }
    }
  }'
```

```json
{
  "name": "srtlmnspdp",
  "email": "nketojmh@example.com",
  "plan_interest": "enterprise",
  "address": {"city": "blfhlhtguq", "zip_code": 465}
}
```

Drop `&model=OpenAI` and the same call runs on the default `MockAiChatModel`.

### `GET /help`

Lists the chat models the orchestrator can route to:

```bash
curl http://localhost:8080/help
```

```json
{
  "supportedModels": ["OpenAI", "Anthropic", "AWSBedrock", "OLlama"],
  "defaultModel": "mock",
  "usage": "POST /completions?schema=true&model=<model> with a JSON Schema as the body. Only structured output is supported."
}
```

### `POST /completions?schema=true`

Send a JSON Schema as the body. Without `&model=` the request goes to the
default `MockAiChatModel`; with it the orchestrator resolves the name to a chat
model and that one runs the completion — the call shown in
[Run the use case](#run-the-use-case).

An unknown name answers `400` with the supported list in the message. Every
model currently talks to the same mock API, so the document is generated the
same way whichever one you pick.

The same call against the mock API directly, skipping the app:

```bash
curl -X POST 'https://competions-mock-api.vercel.app/completions?schema=true' \
  -H 'Content-Type: application/json' \
  -d '{"type": "object", "properties": {"name": {"type": "string"}}}'
```

Supported schema nodes: `object`, `array`, `string` (with `format`: `date-time`,
`date`, `time`, `email`, `uri`, `uuid`, `ipv4`, `ipv6`), `integer`, `number`,
`boolean`, `null`, plus `const`, `enum`, `anyOf`, `oneOf`, `allOf` and internal
`#/$defs/<name>` refs. Nesting deeper than 20 levels is rejected.

### `POST /completions` without `schema=true`

Structured output is the only supported mode, so a request that omits the query
param — or sends `?schema=false` or `?schema=` — is rejected with `400` and the
usage instructions:

```bash
curl -i -X POST http://localhost:8080/completions \
  -H 'Content-Type: application/json' \
  -d '{"prompt": "hola"}'
```

```
HTTP/1.1 400 Bad Request
```

```json
{
  "supportedModels": ["OpenAI", "Anthropic", "AWSBedrock", "OLlama"],
  "defaultModel": "mock",
  "usage": "POST /completions?schema=true&model=<model> with a JSON Schema as the body. Only structured output is supported."
}
```

### From code

The schema is carried as the prompt, so a `DomainChatModel` is driven through
the plain Spring AI `ChatModel` contract — one method, no schema-specific
overload:

```java
final var chatModel = new OpenAIChatModelImpl(
    new AICompletionProviderImpl("https://competions-mock-api.vercel.app"));

final var document = chatModel.call(new Prompt("""
        {"type": "object", "properties": {"name": {"type": "string"}}}
        """))
    .getResult().getOutput().getText();
```
