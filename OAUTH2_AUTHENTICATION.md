# OAuth2 Authentication Guide

## Overview

The adapter platform supports OAuth2 Client Credentials flow with automatic token management:
- **Automatic token fetching** from OAuth2 token endpoint
- **Token caching** per service (clientId + tokenEndpoint)
- **Auto-refresh** at 55 minutes (5-minute buffer before 60-minute expiry)
- **Thread-safe** for concurrent API calls
- **Independent tokens** for multiple REST APIs

---

## Configuration

### Basic OAuth2 Setup

```yaml
adapter:
  services:
    my-rest-api:
      serviceName: "my-rest-api"
      protocol: "REST_JSON"
      endpointUrl: "https://api.example.com/v1/resource"
      httpMethod: "POST"
      auth:
        type: "OAUTH2"
        tokenEndpoint: "https://auth.example.com/oauth/token"
        clientId: "your-client-id"
        clientSecret: "your-client-secret"
        scope: "read write"  # Optional
```

### Multiple APIs with Different OAuth2 Credentials

```yaml
adapter:
  services:
    payment-api:
      protocol: "REST_JSON"
      endpointUrl: "https://api.payment.com/transactions"
      auth:
        type: "OAUTH2"
        tokenEndpoint: "https://auth.payment.com/token"
        clientId: "payment-client-123"
        clientSecret: "payment-secret-abc"
        scope: "payment.write"
    
    customer-api:
      protocol: "REST_JSON"
      endpointUrl: "https://api.customer.com/customers"
      auth:
        type: "OAUTH2"
        tokenEndpoint: "https://auth.customer.com/token"
        clientId: "customer-client-456"
        clientSecret: "customer-secret-xyz"
        scope: "customer.read"
```

Each service maintains its own token independently.

---

## How It Works

### Token Lifecycle

1. **First API Call**
   - No cached token exists
   - Fetches token from `tokenEndpoint` using client credentials
   - Caches token with expiry timestamp (actual expiry - 5 minutes)
   - Adds `Authorization: Bearer {token}` header
   - Executes API call

2. **Subsequent Calls (within 55 minutes)**
   - Uses cached token
   - No additional token fetch
   - Fast execution

3. **After 55 Minutes**
   - Token marked as expired (5-minute buffer)
   - Automatically fetches new token
   - Updates cache
   - Continues API call with new token

### Token Request Format

The adapter sends OAuth2 token requests as:

```http
POST https://auth.example.com/oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=your-client-id
&client_secret=your-client-secret
&scope=read write
```

### Expected Token Response

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

---

## Usage in Microservices

```java
@Autowired
AdapterGatewayClient adapterClient;

// OAuth2 is handled automatically
StandardResponse<?> response = adapterClient.invoke(
    "payment-api", 
    Map.of("amount", 100.00, "currency", "USD")
);
```

No manual token management needed!

---

## Token Cache Strategy

**Cache Key Format:** `{tokenEndpoint}:{clientId}`

**Examples:**
- `https://auth.payment.com/token:payment-client-123`
- `https://auth.customer.com/token:customer-client-456`

This ensures:
- Different APIs don't share tokens
- Same API with different credentials gets separate tokens
- Thread-safe concurrent access

---

## Error Handling

### Token Fetch Failure

If token endpoint fails, the adapter throws:
```
RuntimeException: Failed to fetch OAuth2 token from {tokenEndpoint}
```

This is caught by REST protocol handler and returned as:
```json
{
  "success": false,
  "status": "TECHNICAL_ERROR",
  "error": {
    "errorCode": "PROCESSING_ERROR",
    "errorMessage": "Handler failure",
    "category": "TECHNICAL"
  }
}
```

### Invalid Credentials

OAuth2 server returns 401/403 → mapped to HTTP error response with status code.

---

## Best Practices

1. **Secure Secrets:** Store `clientSecret` in environment variables or secret managers
2. **Scope Management:** Use minimal required scopes
3. **Monitor Token Refresh:** Check logs for token fetch patterns
4. **Test Token Expiry:** Verify 55-minute refresh works correctly

---

## Logging

Enable debug logging to monitor token lifecycle:

```yaml
logging:
  level:
    org.adcb.adapter.spi.auth: DEBUG
```

**Log Examples:**
```
Using cached token for clientId: payment-client-123
Fetching new token from https://auth.payment.com/token for clientId: payment-client-123
Token fetched successfully for clientId: payment-client-123, expires in 3600s (buffer: 300s)
```

---

## Comparison with Other Auth Types

| Auth Type | Use Case | Configuration |
|-----------|----------|---------------|
| `OAUTH2` | Third-party APIs with client credentials | tokenEndpoint, clientId, clientSecret |
| `API_KEY` | Simple header/query-based auth | keyName, tokenSource |
| `JWT_BEARER` | Pre-generated JWT tokens | tokenSource |
| `NONE` | Public APIs or SOAP with embedded auth | - |

---

## Migration from Old Implementation

**Before:**
```java
// Manual token management
String token = fetchToken();
headers.add("Authorization", "Bearer " + token);
```

**After:**
```yaml
# Just configure OAuth2
auth:
  type: "OAUTH2"
  tokenEndpoint: "https://auth.example.com/token"
  clientId: "client-123"
  clientSecret: "secret-abc"
```

Adapter handles everything automatically!
