rootProject.name = "adapter-service"

include(
    "adapter-commons",
    "adapter-spi",
    "adapter-client-starter",
    "adapter-gateway-service",
    "adapter-config-service",
    "adapter-admin-ui",
    "adapter-protocol-rest",
    "adapter-protocol-grpc",
    "adapter-transform-core",
    "adapter-security"
)

include("adapter-protocol-soap")
include("adapter-protocol-proxy")