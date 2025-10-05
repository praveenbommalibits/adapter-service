# Adapter Service Library—Comprehensive Usage Guide

---

## Session Agenda

1. **Introduction**
    - What is the Adapter Service?
    - Why do we need it in microservices architecture?
2. **Example Architecture & Integration Flow**
    - Microservices -> Adapter Service Library -> Downstream systems
    - Problem statements addressed by the Adapter Service 
3. **Adapter Service Walkthrough**
    - Demo: End-to-end call flow
    - Code & module walkthrough
4. **How to Integrate: Step-by-Step**
    - Gradle setup
    - Service & protocol configuration
    - Invoking the adapter (sync + async + error handling)
5. **Core Features**
    - Protocol abstraction (REST/SOAP/gRPC/DB/Kafka/etc.)
    - Centralized error/exception handling, request templating, resilience, and versioning
6. **Resilience & Enhancements**
    - Circuit breaker/retry/timeout
    - Opt-in/opt-out for services
    - Extensibility & module selection
7. **Customization Examples & Best Practices**
    - Using only REST or SOAP modules
    - Minimal configuration for new integrations
8. **Q&A + Next Steps**

---

## 1. Adapter Service: Concept & Value

An *Adapter Service* is a pluggable middleware library that **binds with your microservices** through dependency inclusion. Each business microservice adds the adapter client-starter dependency and leverages its unified API to communicate with various downstream systems (REST/SOAP/gRPC/DB/Kafka/etc.).

**Why use it?**
- **Eliminates Technical Debt**: No more scattered HTTP clients, SOAP clients, error handling, and configuration management across 50+ microservices
- **Reduces Development Time**: New integrations require only YAML configuration instead of weeks of client code development and testing
- **Seamless Technology Evolution**: Migrate downstream services from SOAP to REST or gRPC with zero code changes in microservices only configuration updates
- **Centralized Best Practices**: Security, logging, monitoring, and resilience patterns applied consistently across all integrations
- **Developer Experience**: Single, familiar API regardless of downstream protocol complexity
- **Operational Excellence**: Centralized monitoring, error tracking, and performance metrics for all external communications

**Example High-Level Flow:**
```
[Domain Microservice]   [Adapter Library]    [Downstream Systems]
      |                      |                     |
      | invoke("rest-svc")   | REST Handler    --> | REST API
      | invoke("soap-svc")   | SOAP Handler    --> | SOAP Service  
      | invoke("grpc-svc")   | gRPC Handler    --> | gRPC Service
      | invoke("kafka-svc")  | Kafka Handler   --> | Kafka Topic
```
![Adapter Service Architecture](./AdapterService%20Flow%20Diagram.png)
---

## 2. Problem Statements Addressed

- **Code Duplication**: Eliminates repetitive HTTP/SOAP client setup, connection pooling, timeout configuration, and error handling across microservices
- **Protocol Coupling**: Decouples business logic from integration protocols switch from SOAP to REST without touching microservice code
- **Integration Agility**: Add new downstream services in minutes through configuration rather than development sprints
- **Protocol Agnostic**: Abstract away differences between REST, SOAP, gRPC, database, and messaging protocols
- **Server Agnostic**: Abstract away non-reactive(tomcat) and reactive(netty, undertow) servers. 
- **Inconsistent Resilience**: Standardizes circuit breaker, retry, and timeout patterns with per-service customization and optional enablement
- **Error Handling **: Centralizes error/exception handling with a unified response model irrespective of downstream protocol
- **Observability Gaps**: Provides built-in performance metrics, error rates, and tracing and timeouts for downstream interactions—optional enablement
- **Maintenance Overhead**: Centralize endpoint management, credential rotation, and protocol version upgrades

---

## 3. How Microservices Integrate With Adapter

### Step 1: Add Dependency
```kotlin
dependencies {
    implementation("org.adcb.adapter:adapter-client-starter:1.0.0")
}
```

### Step 2: Configure Services in `application.yml`
```yaml
adapter:
  services:
    user-service:
      protocol: REST_JSON
      endpointUrl: https://userapi.company.com/v1/getuser
      httpMethod: POST
      requestTemplate: user-request.json
      responseTemplate: user-response.json
      headers:
        Authorization: Bearer ${auth.token}
        Content-Type: application/json
      resilience:
        enabled: true
        circuitBreaker:
          enabled: true
          failureRateThreshold: 50.0      # Open circuit at 50% failure rate
          waitDurationInOpenStateMs: 30000 # Wait 30s before half-open
          slidingWindowSize: 10            # Evaluate last 10 calls
          minimumNumberOfCalls: 5          # Need 5 calls before evaluation
        retry:
          enabled: true
          maxAttempts: 3                   # Retry up to 3 times
          strategy: "EXPONENTIAL_BACKOFF"  # 1s, 2s, 4s delays
          initialInterval: 1000
          multiplier: 2.0
          maxInterval: 10000
    
    legacy-payment:
      protocol: SOAP
      endpointUrl: https://legacy.payments.com/PaymentService
      httpMethod: POST
      requestTemplate: payment-request.xml
      responseTemplate: payment-response.json
      headers:
        Content-Type: text/xml; charset=utf-8
        SOAPAction: ""
      resilience:
        enabled: false  # High-performance path for legacy system
```

### Step 3: FreeMarker—Request & Response Templates
The adapter service uses **Freemarker** templating engine for dynamic request/response transformation.


#### Core Templating Capabilities
- **Variables & Expressions** – Strong support for variables, expressions, built-ins.
- **Conditions & Loops** – `<#if>`, `<#elseif>`, `<#list>` for dynamic rendering.
- **Macros (Reusable Blocks)** – Define UI fragments or snippets once and reuse across templates.
- **Includes & Imports** – Modularize templates for better maintainability.

#### High Performance & Scalability
- **Template Caching** – Precompiled & cached templates for low latency.
- **Thread-Safe Processing** – Safe to use in high-concurrency environments.
- **Lightweight Footprint** – Faster than HTML-centric engines like Thymeleaf.

#### Separation of Concerns
- **Logic vs View Separation** – Business logic stays in Java, templates purely handle presentation.
- **Strict Syntax Mode** – Helps avoid silent failures and enforces cleaner templates.

#### Multi-Format Output
- **HTML / XHTML** – Render dynamic web pages.
- **Email Templates** – Popular for transactional and marketing emails.
- **PDF, Word, Excel Generation** – Integrate with libraries like iText or Apache POI.
- **XML / JSON / Text** – Generate structured data files easily.

#### Internationalization (i18n) & Localization
- **Resource Bundle Integration** – Native support for message resource bundles.
- **Dynamic Language Switching** – Locale-specific templates/messages.

#### Extensibility
- **Custom Directives & Built-ins** – Extend FreeMarker with your own tags.
- **Template Loaders** – Load templates from DB, file system, or classpath.
- **Shared Variables / Models** – Inject global variables (like company info, logos).

#### Security & Safety
- **Auto-Escaping** – Reduce XSS by auto-escaping HTML/XML if configured.
- **Strict Variable Handling** – Catch undefined variables early.

#### Spring Boot Integration
- **Out-of-the-box Starter** – `spring-boot-starter-freemarker` simplifies setup.
- **View Resolver Auto-Config** – Works seamlessly with Spring MVC.
- **Property-based Configuration** – Customize template location, suffix, cache via `application.properties`.

#### Developer Productivity
- **Clear Error Messages** – Better debugging of template errors.
- **Template Inheritance** – Common layouts with blocks for dynamic content.
- **Hot Reload in Dev Mode** – Rapid iteration without redeploy.

#### Long-Term Stability
- **Mature & Enterprise-Proven** – Stable releases and backward compatibility.
- **Large Community** – Many examples, integrations, and third-party tools.

---

##### ✅ When to Choose FreeMarker
- Complex **email or document generation** beyond HTML pages.
- High-performance, **multi-format templating** needs.
- Enterprise apps needing **modular, reusable templates**.
- Projects that require **strict separation of logic & presentation**.

**Request Template Example** (`payment-request.xml`):
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <pay:ProcessPayment xmlns:pay="http://payments.company.com">
      <pay:Amount>${request.amount?c}</pay:Amount>
      <pay:Currency>${request.currency!"USD"}</pay:Currency>
      <pay:AccountId>${request.accountId}</pay:AccountId>
      <pay:TransactionId>${correlationId}</pay:TransactionId>
      <pay:Timestamp>${.now?string["yyyy-MM-dd'T'HH:mm:ss"]}</pay:Timestamp>
    </pay:ProcessPayment>
  </soap:Body>
</soap:Envelope>
```

**Response Template Example** (`payment-response.json`):
```json
{
  "transactionId": "${response.transactionId}",
  "status": "${response.status}",
  "amount": ${response.amount},
  "processedAt": "${response.timestamp}",
  "fees": ${response.processingFee!0}
}
```

### Step 4: Service Invocation Patterns

#### Constructor-based Injection (Recommended)
```java
@Service
public class UserService {
    private final AdapterGatewayClient adapterClient;
    
    public UserService(AdapterGatewayClient adapterClient) {
        this.adapterClient = adapterClient;
    }
}
```

#### Field-based Injection (Alternative)
```java
@Autowired 
private AdapterGatewayClient adapterClient;
```

#### Synchronous Invocation
```java
StandardResponse<?> response = adapterClient.invoke("user-service", 
    Map.of("userId", "12345", "includeProfile", true));
```
**Approach**: Blocking call that waits for downstream response  
**When to use**: Standard request-response scenarios, data retrieval, transactional operations  
**How it works**: Thread blocks until response received or timeout occurs, suitable for traditional MVC controllers

#### Asynchronous Invocation
```java
CompletableFuture<StandardResponse<?>> future = adapterClient.invokeAsync("user-service", 
    Map.of("userId", "12345"));

future.thenAccept(response -> {
    // Process response asynchronously
    log.info("User loaded: {}", response.getPayload());
});
```
**Approach**: Non-blocking call that returns immediately with CompletableFuture  
**When to use**: Event-driven architectures, bulk processing, parallel service calls  
**How it works**: Leverages reactive programming for better resource utilization and scalability

#### Asynchronous with Timeout
```java
CompletableFuture<StandardResponse<?>> future = adapterClient.invokeAsyncWithTimeout(
    "legacy-payment", paymentRequest, Duration.ofSeconds(30));

future.whenComplete((response, throwable) -> {
    if (throwable != null) {
        log.error("Payment failed: {}", throwable.getMessage());
    } else {
        log.info("Payment processed: {}", response.getPayload());
    }
});
```
**Approach**: Asynchronous with explicit timeout control  
**When to use**: Long-running operations, external vendor APIs, unreliable networks  
**How it works**: Automatically cancels operation if timeout exceeded, prevents thread pool exhaustion

---

## 4. Major Modules & Dependency Flow

### adapter-client-starter
**Purpose**: Primary entry point and auto-configuration for consuming microservices  
**Contents**: AdapterGatewayClient API, Spring Boot auto-configuration classes, dependency orchestration  
**How it works**: When microservices include this dependency, Spring Boot automatically configures all necessary beans, protocol handlers, and templates. No manual configuration requiredâ€”just add dependency and start invoking services.  
**Transitive Dependencies**: Automatically includes gateway-service, protocol handlers, commons, SPI, and transform-core

### adapter-gateway-service
**Purpose**: Central orchestration engine that routes requests to appropriate protocol handlers  
**Contents**: EnhancedProtocolAdapterService (main orchestrator), resilience management, error handling, request enrichment  
**How it works**: Receives service invocation requests, loads service metadata, selects protocol handler, applies resilience patterns (if enabled), executes downstream call, normalizes response format  
**Key Features**: Correlation ID tracking, performance metrics, circuit breaker state management, request/response logging

### adapter-protocol-rest
**Purpose**: Handles REST/JSON API communications using Spring WebClient  
**Contents**: RestJsonProtocolHandler, HTTP client configuration, JSON serialization/deserialization  
**How it works**: Constructs HTTP requests with dynamic URL path variables, applies headers and authentication, sends requests via WebClient (reactive), processes JSON responses through templates  
**Features**: Connection pooling, timeout management, error status code mapping, multipart/form-data support

### adapter-protocol-soap
**Purpose**: Manages SOAP/XML web service communications  
**Contents**: SoapProtocolHandler, JAXB marshalling/unmarshalling, SOAP fault handling  
**How it works**: Generates SOAP envelopes from Freemarker templates, sends via Spring WebServiceTemplate, handles SOAP faults and converts to standard errors, extracts response data via XPath or templates  
**Features**: WS-Security support, SOAP action headers, namespace handling, fault tolerance

### adapter-commons
**Purpose**: Shared data models, utilities, and constants across all modules  
**Contents**: ServiceMetadata, StandardResponse, ErrorDetails, PerformanceMetrics, resilience configuration POJOs  
**How it works**: Provides type-safe configuration binding, standardized response format, error categorization enums, and utility classes for correlation ID generation and date formatting  
**Key Models**: All protocol handlers use ServiceMetadata for configuration, all responses normalized to StandardResponse format

### adapter-spi (Service Provider Interface)
**Purpose**: Defines extension contracts for adding new protocol handlers  
**Contents**: ProtocolHandler interface, protocol registration annotations, handler discovery mechanism  
**How it works**: New protocols implement ProtocolHandler interface, register as Spring beans with protocol identifier, automatically discovered by gateway service during startup  
**Extensibility**: Add gRPC, GraphQL, database, or messaging protocols by implementing SPI without changing existing code

### adapter-transform-core
**Purpose**: Template processing engine for request/response transformation  
**Contents**: Freemarker configuration, template caching, data binding utilities  
**How it works**: Loads templates from classpath or external files, compiles them for performance, applies request data to generate dynamic content, caches compiled templates to avoid recompilation overhead  
**Performance**: Template compilation cached for 1 hour by default, supports hot reloading in development mode

### Dependency Management
- Microservices include only `adapter-client-starter`
- All transitive module dependencies automatically resolved through Maven/Gradle
- No version conflictsâ€”BOM (Bill of Materials) ensures consistent versions across all adapter modules
- Minimal classpath footprintâ€”unused protocol handlers are present but don't impact performance

### Versioning Strategy
- Semantic versioning: `MAJOR.MINOR.PATCH` (e.g., `1.2.3`)
- Breaking changes increment MAJOR version
- New features increment MINOR version
- Bug fixes increment PATCH version
- All modules versioned together for consistency

### Template Engine (Freemarker) Benefits
**Why Freemarker over alternatives?**
- **Performance**: 3-5x faster than Velocity, especially with caching enabled
- **Feature Rich**: Built-in date formatting, number formatting, conditional logic, loops
- **Spring Integration**: Native Spring Boot support, easy configuration management
- **Security**: Sandboxed execution prevents code injection attacks
- **IDE Support**: IntelliJ and VS Code plugins for syntax highlighting and debugging

---

## 5. Resilience Features Deep Dive

### Circuit Breaker Configuration
```yaml
resilience:
  circuitBreaker:
    enabled: true
    failureRateThreshold: 50.0      # Percentage of failures to open circuit
    waitDurationInOpenStateMs: 30000 # Time to wait before attempting half-open
    slidingWindowSize: 10            # Number of calls to evaluate
    minimumNumberOfCalls: 5          # Minimum calls before circuit evaluation
```

**Configuration Explanation:**
- `failureRateThreshold`: Circuit opens when failure rate exceeds this percentage (e.g., 50% = 5 out of 10 calls fail)
- `waitDurationInOpenStateMs`: How long circuit stays open before trying half-open state (30 seconds)
- `slidingWindowSize`: Rolling window of recent calls to monitor (last 10 calls)
- `minimumNumberOfCalls`: Required calls before circuit breaker activates (avoids premature opening)

### Retry Configuration
```yaml
resilience:
  retry:
    enabled: true
    maxAttempts: 3                   # Total attempts (initial + retries)
    strategy: "EXPONENTIAL_BACKOFF"  # FIXED_DELAY or EXPONENTIAL_BACKOFF
    initialInterval: 1000            # First retry after 1 second
    multiplier: 2.0                  # Each retry doubles the delay
    maxInterval: 10000               # Cap retry delay at 10 seconds
    retryableExceptions:
      - "org.springframework.web.reactive.function.client.WebClientException"
      - "java.net.ConnectException"
```

**Configuration Explanation:**
- `maxAttempts`: Total attempts including initial call (3 = 1 initial + 2 retries)
- `strategy`: EXPONENTIAL_BACKOFF increases delay (1s, 2s, 4s), FIXED_DELAY uses constant interval
- `retryableExceptions`: Only retry on specific exceptionsâ€”avoid retrying business logic errors
- `maxInterval`: Prevents exponential backoff from creating excessively long delays

### Per-Service Resilience Control
```yaml
adapter:
  resilience:
    global-enabled: true  # Master switch for all resilience patterns
  services:
    critical-payment:
      resilience:
        enabled: true     # Full resilience for critical services
    audit-logging:
      resilience:
        enabled: false    # Fast path for non-critical services
```

---

## 6. Advanced Usage: Protocol-Specific Configurations

### REST-Only Integration
For microservices that only integrate with REST APIs, you can optimize by using minimal configuration:

```yaml
adapter:
  services:
    # Only REST services configured
    orders-api:
      protocol: REST_JSON
      endpointUrl: https://orders.company.com/api/orders
      httpMethod: POST
      headers:
        Authorization: Bearer ${orders.token}
    
    inventory-api:
      protocol: REST_JSON  
      endpointUrl: https://inventory.company.com/api/stock/{productId}
      httpMethod: GET

# No SOAP or other protocol configurations needed
```

**Benefits for REST-only usage:**
- Only `adapter-protocol-rest` handler activates at runtime
- SOAP, gRPC, and other protocol libraries present but unused (no performance impact)
- Smaller configuration files and easier maintenance
- Faster application startup due to fewer beans to initialize

**Gradle Dependencies Automatically Included:**
- `adapter-protocol-rest` â†’ Spring WebClient, Jackson JSON processing
- `adapter-commons` â†’ Standard response models, error handling
- `adapter-transform-core` â†’ Freemarker templates (if using requestTemplate/responseTemplate)
- `adapter-gateway-service` â†’ Main orchestration logic

### SOAP-Only Integration
For microservices integrating only with legacy SOAP services:

```yaml
adapter:
  services:
    legacy-erp:
      protocol: SOAP
      endpointUrl: https://erp.company.com/services/EmployeeService
      httpMethod: POST
      requestTemplate: employee-request.xml
      responseTemplate: employee-response.json
      headers:
        Content-Type: text/xml; charset=utf-8
        SOAPAction: ""
      
    legacy-billing:
      protocol: SOAP
      endpointUrl: https://billing.company.com/BillingService
      httpMethod: POST
      requestTemplate: billing-request.xml
      
# No REST configurations needed
```

**Benefits for SOAP-only usage:**
- Only `adapter-protocol-soap` handler processes requests
- XML templating optimized for SOAP envelope generation
- SOAP fault handling and WS-Security support available
- REST protocol libraries included but dormant

**SOAP-Specific Features Available:**
- SOAP fault to StandardResponse error mapping
- WS-Security authentication support
- Namespace and SOAPAction header management
- XML Schema validation (optional)

### Mixed Protocol Integration
For microservices that need multiple protocols:

```yaml
adapter:
  services:
    # Modern REST API
    notifications:
      protocol: REST_JSON
      endpointUrl: https://notifications.company.com/send
      httpMethod: POST
      
    # Legacy SOAP service  
    mainframe-data:
      protocol: SOAP
      endpointUrl: https://mainframe.company.com/DataService
      httpMethod: POST
      requestTemplate: mainframe-request.xml
      
    # Direct proxy for external APIs
    third-party-weather:
      protocol: PROXY_PASS
      endpointUrl: https://api.weather.com/current
      httpMethod: GET
```

**All protocol handlers available simultaneouslyâ€”adapter automatically routes to correct handler based on protocol configuration.**

### Minimal Configuration for New Integrations
Adding a new downstream service requires only configuration changes:

```yaml
adapter:
  services:
    new-service:  # Service name for invoking
      protocol: REST_JSON  # or SOAP, PROXY_PASS, etc.
      endpointUrl: https://newapi.company.com/endpoint
      httpMethod: POST  # GET, PUT, DELETE, etc.
      headers:  # Optional
        Authorization: Bearer ${new-service.token}
      resilience:  # Optional
        enabled: false  # Start without resilience, enable later if needed
```

**No code changes required in microservicesâ€”immediately available via:**
```java
StandardResponse<?> result = adapterClient.invoke("new-service", requestData);
```

---

## 7. Pending Enhancements

### Protocol Extensions
- **gRPC Support**: Add `adapter-protocol-grpc` module for high-performance binary communication
- **Database Integration**: Direct database calls via `adapter-protocol-jdbc` for read-only queries
- **Messaging Support**: Kafka/RabbitMQ integration via `adapter-protocol-messaging`
- **GraphQL Support**: GraphQL query execution via `adapter-protocol-graphql`

### Operational Enhancements
- **Dynamic Configuration**: Hot-reload service configurations without application restart
- **Advanced Monitoring**: Prometheus metrics, distributed tracing, health checks per service
- **Security Enhancements**: OAuth2, mutual TLS, API key rotation automation
- **Performance Optimization**: Connection pooling per service, request batching, caching layer

### Developer Experience
- **Configuration Validation**: Startup-time validation of service configurations
- **Testing Utilities**: Mock adapter client for unit testing, integration test helpers
- **Documentation Generation**: Auto-generate API docs from service configurations
- **IDE Plugins**: IntelliJ/VS Code plugins for configuration assistance and testing

---

## 8. Best Practices & Guidelines

### Configuration Management
- Store sensitive data (tokens, passwords) in external configuration (Vault, ConfigMaps)
- Use environment-specific profiles for endpoint URLs
- Enable resilience for external/unreliable services, disable for internal/high-performance paths
- Start with minimal configuration and add features incrementally

### Performance Optimization
- Use asynchronous invocation for non-blocking operations
- Disable resilience for high-throughput, internal service calls
- Cache frequently-used templates and configurations
- Monitor adapter performance metrics and optimize bottlenecks

### Error Handling
- Define retry strategies based on downstream service characteristics
- Use circuit breakers for external vendor APIs and unreliable networks
- Configure appropriate timeout values based on SLA requirements
- Log correlation IDs for easier troubleshooting

---

## 9. Session Wrap-up

The Adapter Service provides:
- **Simplified Integration**: Single dependency and unified API for all protocols
- **Operational Excellence**: Centralized resilience, monitoring, and error handling
- **Developer Productivity**: Configuration-driven integration with minimal code
- **Future-Proof Architecture**: Easy protocol migration and new service integration
- **Enterprise Readiness**: Security, auditing, and performance optimization built-in

**Key Takeaways:**
1. Add `adapter-client-starter` dependency to your microservice
2. Configure downstream services in `application.yml`
3. Use `AdapterGatewayClient.invoke()` for all external communications
4. Enable resilience patterns only where needed for optimal performance
5. Leverage templates for complex request/response transformations

**Next Steps:**
- Integrate adapter in your microservice
- Configure your first downstream service
- Test with different invocation patterns
- Enable monitoring and resilience as needed

**Questions & Discussion**

---

_Last updated: 2025-10-03_