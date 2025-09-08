package org.adcb.adapter.commons;

import lombok.Data;

import java.util.Map;

@Data
public class ServiceMetadata {
    private String endpointUrl;
    private String protocol;
    private String httpMethod;
    private Map<String, String> headers;
    private String requestTemplate;  // Template file name
    private String responseTemplate; // Template file name
}
