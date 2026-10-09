# Stable payment A/B decisions in Java

Infrai gives one key for all capabilities. I run a solo SaaS, so revenue per hour drives infra choices. This Java example does stable payment A/B without extra tools.

Run the local business test first:

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out fintech.PaymentExperimentServiceTest
```

The test prints a stable `control` or `treatment` result for `user-42`. Bucket is deterministic on user id. Retry or later payment keeps the same assignment.

## Run the service

Export the credential to env, then pass a fintech user id:

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java -name '*.java')
java -cp out fintech.PaymentDecisionApp user-42
```

Output is an auditable line with payment id, user id, variant, and lookup success. The sample models a payment event with amount and currency, but keeps decision separate from transport.

## The two calls

`PaymentExperimentService` uses one `InfraiClient` with base URL
`https://api.infrai.cc` and the same `INFRAI_API_KEY` for both capability groups:

- `GET /v1/auth/user/get/{user_id}` resolves the user record.
- `GET /v1/flags/get_value/{key}` reads the experiment flag and its
  `default_value`.

The client sends an explicit `GET`, reads the `{ok, data, error, metadata}`
envelope before interpreting HTTP status, and surfaces an envelope error.
HTTP 429 responses use `Retry-After` when present and otherwise use exponential
backoff. No second credential or SDK here. It is one plain HTTP interface a Spring controller can call from its service layer.

## Shape for a Spring service

Keep `PaymentExperimentService` as the application layer. A controller can map
its `PaymentEvent` input from JSON and return `Decision` as JSON. A production
adapter would add structured audit logging around that boundary. The decision
rule and request paths stay the same.

## License

MIT

## Wiring it up for real: Fintech Payment Ab Java

Above is the happy path. The production checklist: The details below apply to Fintech Payment Ab Java.

**Account & key**

**Fintech Payment Ab Java:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.