# multi-ai-provider

## Setup

Requires **JDK 25**.

```bash
mvn verify   # build + tests for both modules
```

## Running

The mock API base URL is hardcoded in `CompletionController`
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

### `GET /help`

Lists the chat models the orchestrator can route to:

```bash
curl http://localhost:8080/help
```

```json
{
  "supportedModels": ["OpenAI", "Anthropic", "AWSBedrock", "OLlama"],
  "defaultModel": "mock",
  "usage": "POST /completions?model=<model> with body {\"prompt\": \"...\"}"
}
```

### `POST /completions`

Without `?model=` the request goes to the default `MockAiChatModel`:

```bash
curl -X POST http://localhost:8080/completions \
  -H 'Content-Type: application/json' \
  -d '{"prompt": "hola"}'
```

```json
{"completion": "..."}
```

With `?model=` the orchestrator resolves the name to a chat model and that one
runs the completion:

```bash
curl -X POST 'http://localhost:8080/completions?model=OpenAI' \
  -H 'Content-Type: application/json' \
  -d '{"prompt": "hola"}'
```

An unknown name answers `400` with the supported list in the message. Every
model currently talks to the same mock API, so the completion text is identical
whichever one you pick.

### Structured output

The mock API ignores the request body and answers with a fixed placeholder
unless you pass `?schema=true`, in which case the body itself is a JSON Schema
and the response is a random document that complies with it. The app's
`/completions` does not forward that flag, so call the mock API directly:

```bash
curl -X POST 'https://competions-mock-api.vercel.app/completions?schema=true' \
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

Supported schema nodes: `object`, `array`, `string` (with `format`: `date-time`,
`date`, `time`, `email`, `uri`, `uuid`, `ipv4`, `ipv6`), `integer`, `number`,
`boolean`, `null`, plus `const`, `enum`, `anyOf`, `oneOf`, `allOf` and internal
`#/$defs/<name>` refs. Nesting deeper than 20 levels is rejected.

### From code

Construct the model explicitly wherever you need it:

```java
final var chatModel = new MockAiChatModel(
    new MockAiApi("https://competions-mock-api.vercel.app"));

final var text = chatModel.call(new Prompt("hola"))
    .getResult().getOutput().getText();
```
