# ADCB Adapter Platform

A flexible, protocol-agnostic integration platform for connecting to external services with support for REST, SOAP, and HTTP Proxy protocols.

## Features

- 🔌 **Multi-Protocol Support**: REST/JSON, SOAP/XML, HTTP Proxy
- 🔐 **Multiple Authentication**: API Key, Basic, Bearer, OAuth2
- 🔄 **Request/Response Transformation**: FreeMarker templates
- 🛡️ **Resilience Patterns**: Circuit breaker, retry, timeouts
- 📦 **Spring Boot Auto-Configuration**: Zero-config integration
- 🎯 **Type-Safe Configuration**: Strongly-typed service metadata

---

## Quick Start

### For Application Developers

**1. Add dependency:**
```gradle
repositories {
    mavenLocal()
}

dependencies {
    implementation 'org.adcb.adapter:adapter-client-starter:5.0.0-SNAPSHOT'
}
```

**2. Configure service:**
```properties
adapter.services.my-api.serviceName=my-api
adapter.services.my-api.protocol=REST_JSON
adapter.services.my-api.endpointUrl=http://api.example.com/users
adapter.services.my-api.httpMethod=GET
```

**3. Use in code:**
```java
@Service
public class MyService {
    private final AdapterGatewayClient adapter;
    
    public Mono<StandardResponse> getUser(String id) {
        return adapter.invokeService("my-api", Map.of("userId", id));
    }
}
```

📖 **See [QUICK_START.md](QUICK_START.md) for 5-minute setup**  
📚 **See [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) for complete documentation**

---

## Project Structure

```
adapter-service/
├── adapter-client-starter/      # 👈 Use this in your applications
├── adapter-gateway-service/     # Core orchestration engine
├── adapter-commons/             # Shared models and DTOs
├── adapter-spi/                 # Protocol handler interface
├── adapter-protocol-rest/       # REST/JSON handler
├── adapter-protocol-soap/       # SOAP/XML handler
├── adapter-protocol-proxy/      # HTTP Proxy handler
└── adapter-transform-core/      # Template transformation engine
```

---

## Module Overview

### adapter-client-starter
**Purpose:** Spring Boot starter for consuming applications  
**Use When:** Integrating adapter into your application  
**Provides:** Auto-configuration, client wrapper, all dependencies

### adapter-gateway-service
**Purpose:** Core orchestration and service invocation  
**Use When:** Internal library (included via client-starter)  
**Provides:** Protocol routing, resilience, error handling

### adapter-commons
**Purpose:** Shared models and utilities  
**Provides:** StandardResponse, ServiceMetadata, error codes

### adapter-spi
**Purpose:** Protocol handler interface  
**Provides:** ProtocolHandler, AuthStrategy contracts

### adapter-protocol-rest
**Purpose:** REST/JSON protocol implementation  
**Supports:** GET, POST, PUT, DELETE, PATCH

### adapter-protocol-soap
**Purpose:** SOAP/XML protocol implementation  
**Supports:** SOAP 1.1/1.2, WSDL-based services

### adapter-protocol-proxy
**Purpose:** HTTP proxy forwarding  
**Supports:** Transparent request forwarding

### adapter-transform-core
**Purpose:** Request/response transformation  
**Provides:** FreeMarker template engine

---

## Supported Protocols

| Protocol | Description | Use Case |
|----------|-------------|----------|
| REST_JSON | RESTful JSON APIs | Modern REST services |
| SOAP | SOAP/XML Web Services | Legacy enterprise systems |
| PROXY | HTTP Proxy | Direct forwarding |

---

## Supported Authentication

| Type | Description | Configuration |
|------|-------------|---------------|
| NONE | No authentication | `auth.type=NONE` |
| API_KEY | API Key (Header/Query) | `auth.type=API_KEY` |
| BASIC | Basic Authentication | `auth.type=BASIC` |
| BEARER | Bearer Token | `auth.type=BEARER` |
| OAUTH2 | OAuth2 Client Credentials | `auth.type=OAUTH2` |

---

## Building the Project

### Prerequisites
- Java 21
- Gradle 8.5+

### Build All Modules
```bash
./gradlew clean build
```

### Publish to Maven Local
```bash
./gradlew publishToMavenLocal
```

### Run Tests
```bash
./gradlew test
```

---

## Configuration Example

```properties
# Template Configuration
adapter.templates.path=classpath:/adapter-templates/
adapter.templates.cache.enabled=true

# REST API Service
adapter.services.user-api.serviceName=user-api
adapter.services.user-api.protocol=REST_JSON
adapter.services.user-api.endpointUrl=http://localhost:8090/api/users/{userId}
adapter.services.user-api.httpMethod=GET
adapter.services.user-api.requestTemplate=user-request.json
adapter.services.user-api.responseTemplate=user-response.json
adapter.services.user-api.auth.type=API_KEY
adapter.services.user-api.auth.strategy=HEADER
adapter.services.user-api.auth.keyName=X-API-Key
adapter.services.user-api.auth.tokenSource=your-api-key

# Resilience Configuration
adapter.services.user-api.resilience.timeouts.connectionTimeout=5000
adapter.services.user-api.resilience.timeouts.readTimeout=10000
adapter.services.user-api.resilience.circuitBreaker.enabled=true
adapter.services.user-api.resilience.retry.enabled=true
adapter.services.user-api.resilience.retry.maxAttempts=3
```

---

## Documentation

- [Quick Start Guide](QUICK_START.md) - 5-minute integration
- [Integration Guide](INTEGRATION_GUIDE.md) - Complete documentation
- [Architecture Overview](docs/ARCHITECTURE.md) - System design (if exists)

---

## Requirements

- **Java:** 21+
- **Spring Boot:** 3.5.5+
- **Gradle:** 8.5+ or Maven 3.8+

---

## Version

**Current Version:** 5.0.0-SNAPSHOT  
**Java Version:** 21  
**Spring Boot Version:** 3.5.5

---

## License

Apache License 2.0

---

## Support

For questions or issues, contact the ADCB Adapter Team.

**Last Updated:** November 2025
