# ADCB Adapter Platform - Integration Guide

## Overview
This guide explains how to integrate the ADCB Adapter Platform into any Spring Boot application.

## Prerequisites
- Java 21
- Spring Boot 3.5.5+
- Gradle or Maven

---

## Integration Steps

### Step 1: Publish Adapter to Maven Local

From the adapter-service project root:

```bash
./gradlew publishToMavenLocal
```

This publishes all adapter modules to `~/.m2/repository/org/adcb/adapter/`

---

### Step 2: Add Repository to Your Project

**Gradle (build.gradle):**
```gradle
repositories {
    mavenCentral()
    mavenLocal()  // Add this line
}
```

**Maven (pom.xml):**
```xml
<repositories>
    <repository>
        <id>local-maven</id>
        <url>file://${user.home}/.m2/repository</url>
    </repository>
</repositories>
```

---

### Step 3: Add Adapter Dependency

**Gradle (build.gradle):**
```gradle
dependencies {
    implementation 'org.adcb.adapter:adapter-client-starter:5.0.0-SNAPSHOT'
}
```

**Maven (pom.xml):**
```xml
<dependency>
    <groupId>org.adcb.adapter</groupId>
    <artifactId>adapter-client-starter</artifactId>
    <version>5.0.0-SNAPSHOT</version>
</dependency>
```

---

### Step 4: Set Java Version

**Gradle:**
```gradle
sourceCompatibility = '21'
targetCompatibility = '21'
```

**Maven:**
```xml
<properties>
    <java.version>21</java.version>
</properties>
```

---

### Step 5: Main Application Class

**No special configuration needed!** Auto-configuration handles everything:

```java
package com.example.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

---

### Step 6: Add Configuration Files

#### 6.1 Create Template Directory

```
src/main/resources/
└── adapter-templates/
    ├── user-request.json
    ├── user-response.json
    ├── user-soap-request.xml
    └── user-soap-response.json
```

#### 6.2 Configure Services

**application.properties:**
```properties
# Adapter Template Configuration
adapter.templates.path=classpath:/adapter-templates/
adapter.templates.cache.enabled=true
adapter.templates.cache.ttl-minutes=60

# REST API Service Example
adapter.services.user-api.serviceName=user-api
adapter.services.user-api.protocol=REST_JSON
adapter.services.user-api.endpointUrl=http://localhost:8090/api/users/{userId}
adapter.services.user-api.httpMethod=GET
adapter.services.user-api.requestTemplate=user-request.json
adapter.services.user-api.responseTemplate=user-response.json
adapter.services.user-api.headers.Accept=application/json
adapter.services.user-api.headers.Content-Type=application/json
adapter.services.user-api.auth.type=API_KEY
adapter.services.user-api.auth.strategy=HEADER
adapter.services.user-api.auth.keyName=X-API-Key
adapter.services.user-api.auth.tokenSource=your-api-key
adapter.services.user-api.resilience.timeouts.connectionTimeout=5000
adapter.services.user-api.resilience.timeouts.readTimeout=10000
adapter.services.user-api.resilience.timeouts.totalTimeout=15000

# SOAP Service Example
adapter.services.user-soap-api.serviceName=user-soap-api
adapter.services.user-soap-api.protocol=SOAP
adapter.services.user-soap-api.endpointUrl=http://localhost:8090/soap
adapter.services.user-soap-api.httpMethod=POST
adapter.services.user-soap-api.requestTemplate=user-soap-request.xml
adapter.services.user-soap-api.responseTemplate=user-soap-response.json
adapter.services.user-soap-api.headers.Content-Type=text/xml; charset=utf-8
adapter.services.user-soap-api.headers.SOAPAction=
adapter.services.user-soap-api.auth.type=NONE
adapter.services.user-soap-api.resilience.timeouts.connectionTimeout=5000
adapter.services.user-soap-api.resilience.timeouts.readTimeout=10000
adapter.services.user-soap-api.resilience.timeouts.totalTimeout=15000

# OAuth2 Service Example
adapter.services.payment-api.serviceName=payment-api
adapter.services.payment-api.protocol=REST_JSON
adapter.services.payment-api.endpointUrl=https://api.payment.com/v1/transactions
adapter.services.payment-api.httpMethod=POST
adapter.services.payment-api.requestTemplate=payment-request.json
adapter.services.payment-api.responseTemplate=payment-response.json
adapter.services.payment-api.auth.type=OAUTH2
adapter.services.payment-api.auth.tokenEndpoint=https://auth.payment.com/oauth/token
adapter.services.payment-api.auth.clientId=your-client-id
adapter.services.payment-api.auth.clientSecret=your-client-secret
adapter.services.payment-api.auth.scope=payment.write payment.read
```

**application.yml (alternative):**
```yaml
adapter:
  templates:
    path: classpath:/adapter-templates/
    cache:
      enabled: true
      ttl-minutes: 60
  
  services:
    user-api:
      serviceName: user-api
      protocol: REST_JSON
      endpointUrl: http://localhost:8090/api/users/{userId}
      httpMethod: GET
      requestTemplate: user-request.json
      responseTemplate: user-response.json
      headers:
        Accept: application/json
        Content-Type: application/json
      auth:
        type: API_KEY
        strategy: HEADER
        keyName: X-API-Key
        tokenSource: your-api-key
      resilience:
        timeouts:
          connectionTimeout: 5000
          readTimeout: 10000
          totalTimeout: 15000
```

---

### Step 7: Use the Adapter in Your Code

#### Option 1: Using AdapterGatewayClient (Recommended)

```java
package com.example.myapp.service;

import org.adcb.adapter.client.AdapterGatewayClient;
import org.adcb.adapter.commons.StandardResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class MyService {
    
    private final AdapterGatewayClient adapterClient;
    
    public MyService(AdapterGatewayClient adapterClient) {
        this.adapterClient = adapterClient;
    }
    
    public Mono<StandardResponse> getUserDetails(String userId) {
        Map<String, Object> requestData = Map.of("userId", userId);
        return adapterClient.invokeService("user-api", requestData);
    }
}
```

#### Option 2: Using EnhancedProtocolAdapterService Directly

```java
package com.example.myapp.service;

import org.adcb.adapter.commons.StandardResponse;
import org.adcb.adapter.gateway.service.EnhancedProtocolAdapterService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class MyService {
    
    private final EnhancedProtocolAdapterService adapterService;
    
    public MyService(EnhancedProtocolAdapterService adapterService) {
        this.adapterService = adapterService;
    }
    
    public Mono<StandardResponse> getUserDetails(String userId) {
        Map<String, Object> requestData = Map.of("userId", userId);
        return adapterService.invokeService("user-api", requestData);
    }
}
```

#### Option 3: REST Controller Example

```java
package com.example.myapp.controller;

import org.adcb.adapter.client.AdapterGatewayClient;
import org.adcb.adapter.commons.StandardResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
    
    private final AdapterGatewayClient adapterClient;
    
    public UserController(AdapterGatewayClient adapterClient) {
        this.adapterClient = adapterClient;
    }
    
    @GetMapping("/users/{userId}")
    public Mono<StandardResponse> getUser(@PathVariable String userId) {
        Map<String, Object> request = Map.of("userId", userId);
        return adapterClient.invokeService("user-api", request);
    }
    
    @PostMapping("/users")
    public Mono<StandardResponse> createUser(@RequestBody Map<String, Object> userData) {
        return adapterClient.invokeService("user-soap-api", userData);
    }
}
```

---

### Step 8: Build and Run

```bash
# Build
./gradlew clean build

# Run
java -jar build/libs/your-app.jar
```

---

## Template Examples

### REST Request Template (user-request.json)
```json
{
  "userId": "${userId}",
  "includeDetails": true
}
```

### REST Response Template (user-response.json)
```json
{
  "id": "${id}",
  "name": "${name}",
  "email": "${email}",
  "status": "${status}"
}
```

### SOAP Request Template (user-soap-request.xml)
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:usr="http://example.com/downstream/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <usr:getUserDetailsRequest>
         <usr:userId>${userId}</usr:userId>
      </usr:getUserDetailsRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

### SOAP Response Template (user-soap-response.json)
```json
{
  "userId": "${getUserDetailsResponse.userId}",
  "name": "${getUserDetailsResponse.name}",
  "email": "${getUserDetailsResponse.email}",
  "status": "SUCCESS"
}
```

---

## Supported Protocols

- **REST_JSON** - RESTful JSON APIs
- **SOAP** - SOAP/XML Web Services
- **PROXY** - HTTP Proxy forwarding

---

## Supported Authentication Types

- **NONE** - No authentication
- **API_KEY** - API Key (Header or Query Parameter)
- **BASIC** - Basic Authentication
- **BEARER** - Bearer Token
- **OAUTH2** - OAuth2 Client Credentials

---

## Configuration Properties Reference

| Property | Description | Default |
|----------|-------------|---------|
| `adapter.templates.path` | Template directory path | `classpath:/adapter-templates/` |
| `adapter.templates.cache.enabled` | Enable template caching | `true` |
| `adapter.templates.cache.ttl-minutes` | Cache TTL in minutes | `60` |
| `adcb.adapter.client.enabled` | Enable adapter client | `true` |

---

## Troubleshooting

### Issue: Port Already in Use
```
Web server failed to start. Port 8090 was already in use.
```
**Solution:** Change port in `application.properties`:
```properties
server.port=8091
```

### Issue: Template Not Found
```
Template not found: user-request.json
```
**Solution:** Ensure templates are in `src/main/resources/adapter-templates/`

### Issue: Service Configuration Not Found
```
Service configuration not found for: user-api
```
**Solution:** Check `adapter.services.user-api.*` properties are configured

---

## Advanced Configuration

### Circuit Breaker
```properties
adapter.services.user-api.resilience.circuitBreaker.enabled=true
adapter.services.user-api.resilience.circuitBreaker.failureRateThreshold=50.0
adapter.services.user-api.resilience.circuitBreaker.waitDurationInOpenStateMs=30000
adapter.services.user-api.resilience.circuitBreaker.slidingWindowSize=10
```

### Retry Policy
```properties
adapter.services.user-api.resilience.retry.enabled=true
adapter.services.user-api.resilience.retry.maxAttempts=3
adapter.services.user-api.resilience.retry.strategy=EXPONENTIAL_BACKOFF
adapter.services.user-api.resilience.retry.initialInterval=1000
adapter.services.user-api.resilience.retry.multiplier=2.0
```

---

## Support

For issues or questions, contact the ADCB Adapter Team.

**Version:** 5.0.0-SNAPSHOT  
**Last Updated:** November 2025
