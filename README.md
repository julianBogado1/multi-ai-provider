# multi-ai-provider

## Setup

Requires **JDK 25**.

```bash
mvn verify   # build + tests for both modules
```

## Running

The mock API base URL is hardcoded in `MockAiApi`
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

Copy-paste this once the app is running — it sends a JSON Schema and prints a
document that complies with it:

```bash
curl -s -X POST 'http://localhost:8080/completions?schema=true' \
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

### `GET /help`

Lists the chat model behind the endpoint and how to call it:

```bash
curl http://localhost:8080/help
```

```json
{
  "supportedModels": ["mock"],
  "defaultModel": "mock",
  "usage": "POST /completions?schema=true with a JSON Schema as the body. Only structured output is supported."
}
```

### `POST /completions?schema=true`

Send a JSON Schema as the body. `MockAiChatModel` forwards it to the mock API
and the answer is a document that complies with the schema — the call shown in
[Run the use case](#run-the-use-case).

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
  "supportedModels": ["mock"],
  "defaultModel": "mock",
  "usage": "POST /completions?schema=true with a JSON Schema as the body. Only structured output is supported."
}
```

### From code

The schema is carried as the prompt, so the model is driven through the plain
Spring AI `ChatModel` contract — one method, no schema-specific overload:

```java
final var chatModel = new MockAiChatModel(
    new MockAiApi("https://competions-mock-api.vercel.app"));

final var document = chatModel.call(new Prompt("""
        {"type": "object", "properties": {"name": {"type": "string"}}}
        """))
    .getResult().getOutput().getText();
```
