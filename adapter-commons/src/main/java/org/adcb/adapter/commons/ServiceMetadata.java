package org.adcb.adapter.commons;

import lombok.Data;
import java.util.Map;

@Data
public class ServiceMetadata {
    // Existing fields
    private String serviceName;
    private String protocol;
    private String requestTemplate;
    private String responseTemplate;
    private String endpointUrl;
    private String httpMethod;
    private Map<String, String> headers;

    // New fields for SOAP error extraction
    private String errorCodeXPath;
    private String errorDescriptionXPath;

    // Runtime status fields (to be set by handlers/gateway)
    private Integer lastHttpStatus;
    private String lastErrorDescription;
}
