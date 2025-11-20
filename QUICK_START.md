# Quick Start Guide - ADCB Adapter Platform

## 5-Minute Integration

### 1. Publish Adapter (One-time setup)
```bash
cd /path/to/adapter-service
./gradlew publishToMavenLocal
```

### 2. Add to Your Project

**build.gradle:**
```gradle
repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation 'org.adcb.adapter:adapter-client-starter:5.0.0-SNAPSHOT'
}

sourceCompatibility = '21'
```

### 3. Configure Service

**application.properties:**
```properties
adapter.templates.path=classpath:/adapter-templates/

adapter.services.my-api.serviceName=my-api
adapter.services.my-api.protocol=REST_JSON
adapter.services.my-api.endpointUrl=http://api.example.com/users/{userId}
adapter.services.my-api.httpMethod=GET
adapter.services.my-api.auth.type=API_KEY
adapter.services.my-api.auth.strategy=HEADER
adapter.services.my-api.auth.keyName=X-API-Key
adapter.services.my-api.auth.tokenSource=your-key-here
```

### 4. Use in Code

```java
@Service
public class MyService {
    
    private final AdapterGatewayClient adapter;
    
    public MyService(AdapterGatewayClient adapter) {
        this.adapter = adapter;
    }
    
    public Mono<StandardResponse> getUser(String userId) {
        return adapter.invokeService("my-api", Map.of("userId", userId));
    }
}
```

### 5. Run
```bash
./gradlew bootRun
```

**Done!** 🎉

---

## What You Get

✅ Auto-configured adapter client  
✅ REST, SOAP, and Proxy protocol support  
✅ Multiple authentication strategies  
✅ Circuit breaker and retry patterns  
✅ Request/response transformation  
✅ Centralized error handling  

---

See [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) for detailed documentation.
