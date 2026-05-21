# Spring Security OAuth2 JWT Authentication Workflow

> Based on `api-gateway/src/main/java/niruthen/api_gateway/SecurityConfig.java`

---

## The Two Roles: Authorization Server vs Resource Server

The API Gateway acts as a **Resource Server** — it does NOT issue tokens, it only validates them. Keycloak is the **Authorization Server** that issues JWTs.

---

## Full Request Lifecycle

```
Client                   API Gateway (Resource Server)              Keycloak (Auth Server)
  |                               |                                         |
  |--- POST /login --------------->|                                        |
  |                               |--- POST /realms/saga-realm/token ------>|
  |                               |<-- { access_token: "eyJ..." } ----------|
  |<-- JWT returned ---------------|                                        |
  |                               |                                         |
  |--- GET /api/orders            |                                         |
  |    Authorization: Bearer eyJ  |                                         |
  |------------------------------->|                                        |
  |                       [Filter Chain Runs]                               |
  |                               |--- GET /.well-known/jwks.json --------->|
  |                               |<-- { keys: [{kid, n, e, ...}] } --------|
  |                               |  (cached after first fetch)             |
  |                      [Decode + Validate JWT]                            |
  |                               |                                         |
  |<-- 200 OK / 401 Unauthorized --|                                        |
```

---

## Step-by-Step Filter Chain Execution

### Step 1 — `BearerTokenAuthenticationFilter` intercepts the request

Spring Security registers this filter automatically when you configure:

```java
.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
```

It looks for `Authorization: Bearer <token>` in the request header. If missing, the request either passes through (for `permitAll()` routes) or gets rejected with **401**.

---

### Step 2 — JWK Set fetch (key resolution)

The decoder fetches Keycloak's public keys the **first time** a request arrives:

```java
NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
    .withJwkSetUri(jwkSetUri)   // e.g. http://keycloak:8080/realms/saga-realm/protocol/openid-connect/certs
    .build();
```

The response is a JWK Set like:

```json
{ "keys": [{ "kid": "abc123", "kty": "RSA", "n": "...", "e": "AQAB" }] }
```

The keys are **cached in memory**. The JWT's `kid` header is used to select the matching public key.

---

### Step 3 — JWT Decoding (signature verification)

Nimbus parses the JWT (3 Base64 parts: `header.payload.signature`) and verifies the **signature** using the RSA public key from Step 2. If tampered, this throws `JwtException` → **401**.

---

### Step 4 — JWT Validation (two validators)

```java
decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
    new JwtTimestampValidator(),        // checks exp, nbf, iat
    new JwtIssuerValidator(issuerUri)   // checks iss == "http://keycloak:8080/realms/saga-realm"
));
```

| Validator | What it checks | Failure result |
|---|---|---|
| `JwtTimestampValidator` | `exp` not in past, `nbf` not in future | 401 — token expired |
| `JwtIssuerValidator` | `iss` claim matches configured `issuerUri` | 401 — wrong issuer |

This prevents token reuse from other Keycloak realms or other auth servers entirely.

---

### Step 5 — `Authentication` object is created

A `JwtAuthenticationToken` is placed into the `SecurityContext` (reactive: `ReactiveSecurityContextHolder`). It wraps:

- The raw `Jwt` object (all claims)
- A collection of `GrantedAuthority` objects extracted from `scope` / `roles` claims

---

### Step 6 — Authorization rules are evaluated

```java
.authorizeExchange(exchanges -> exchanges
    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()                      // CORS preflight
    .pathMatchers("/api/auth/**", "/actuator/**", "/eureka/**").permitAll()   // public routes
    .anyExchange().authenticated()                                            // everything else needs JWT
)
```

Rules are evaluated **in order**. First match wins. If `authenticated()` check passes, the request proceeds downstream to the microservices.

---

## CORS Handling (pre-flight short-circuit)

```java
.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
```

The browser sends an `OPTIONS` preflight before the actual request. This rule lets it through without a JWT, so the CORS config can respond with allowed headers/origins and the browser can proceed with the real request.

---

## What a Decoded JWT Looks Like (Keycloak)

```json
{
  "header": { "alg": "RS256", "kid": "abc123" },
  "payload": {
    "iss": "http://keycloak:8080/realms/saga-realm",
    "sub": "user-uuid",
    "exp": 1714000000,
    "iat": 1713996400,
    "scope": "openid profile",
    "realm_access": { "roles": ["user", "admin"] }
  }
}
```

- `iss` must match your `issuerUri` config
- `exp` must be in the future
- `kid` selects the right public key from the JWK Set

---

## Key Configuration Properties

| Property | Purpose |
|---|---|
| `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` | Where to fetch Keycloak's public keys |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | Expected `iss` claim value in every JWT |
| `gateway.cors.allowed-origins` | Explicit origin allow-list (no wildcard) |

---

## Summary Flow

```
Request → BearerTokenAuthenticationFilter
        → Extract "Bearer <jwt>" from header
        → NimbusReactiveJwtDecoder.decode(jwt)
              → Fetch JWK Set (cached after first call)
              → Verify RSA signature
              → Run JwtTimestampValidator  (exp / nbf)
              → Run JwtIssuerValidator     (iss)
        → Build JwtAuthenticationToken
        → ReactiveSecurityContextHolder.setAuthentication(token)
        → AuthorizationWebFilter checks .anyExchange().authenticated()
        → Route request to downstream microservice
```

Any failure in the decode/validate chain produces a **401 Unauthorized** response. Downstream microservices never see an invalid token because the gateway rejects it first.