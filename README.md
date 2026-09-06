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

### `GET /help`

Lists the chat models the orchestrator load balances across:

```bash
curl http://localhost:8080/help
```

```json
{
  "supportedModels": ["OpenAI", "Anthropic", "AWSBedrock", "OLlama"],
  "selectionStrategy": "random",
  "usage": "POST /completions?schema=true with a JSON Schema as the body. Only structured output is supported."
}
```

### `POST /completions?schema=true`

Send a JSON Schema as the body. The client does not pick a provider: on every
request the orchestrator selects one at random and that one runs the
completion. The answer is a document that complies with the schema.

```bash
curl -X POST 'http://localhost:8080/completions?schema=true' \
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

The chosen provider is logged on each request:

```
INFO  m.d.s.o.ChatModelOrchestratorImpl : Load balancer selected chat model: Anthropic
```

Every model currently talks to the same mock API, so the document is generated
the same way whichever one gets picked.

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
  "selectionStrategy": "random",
  "usage": "POST /completions?schema=true with a JSON Schema as the body. Only structured output is supported."
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
