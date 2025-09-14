package org.adcb.adapter.transform.service;

import freemarker.template.TemplateException;
import org.adcb.adapter.transform.engine.RequestTemplateEngine;
import org.adcb.adapter.transform.engine.JsonResponseTransformer;
import org.adcb.adapter.transform.engine.XmlResponseTransformer;
import org.adcb.adapter.transform.model.StandardResponse;

import java.time.Instant;
import java.util.Map;

/**
 * Orchestrates request rendering, response parsing and standardization.
 */
public class AdapterTransformService {

    private final RequestTemplateEngine templateEngine = new RequestTemplateEngine();
    private final JsonResponseTransformer jsonTransformer = new JsonResponseTransformer();
    private final XmlResponseTransformer xmlTransformer = new XmlResponseTransformer();

    /**
     * Builds the protocol request payload as JSON or XML from template and map.
     */
    public String buildRequestPayload(String templateString, Map<String, Object> requestMap)
            throws Exception {
        return templateEngine.render(templateString, requestMap);
    }

    /**
     * Standardizes REST JSON response.
     */
    public StandardResponse<Map<String, Object>> parseRestResponse(
            String jsonResponse,
            int httpStatus,
            String errorDescription,
            String correlationId,
            String serviceName) throws Exception {
        boolean success = httpStatus >= 200 && httpStatus < 300;
        Map<String, Object> payload = jsonTransformer.toMap(jsonResponse);
        return createResponse(success, String.valueOf(httpStatus), errorDescription, payload, correlationId, serviceName);
    }

    /**
     * Standardizes SOAP/XML response.
     */
    public StandardResponse<Map<String, Object>> parseSoapResponse(
            String xmlResponse,
            String errorCodeXPath,
            String errorDescriptionXPath,
            String correlationId,
            String serviceName) throws Exception {
        String errorCode = xmlTransformer.extractField(xmlResponse, errorCodeXPath);
        String errorDesc = xmlTransformer.extractField(xmlResponse, errorDescriptionXPath);
        boolean success = (errorCode == null || "0".equals(errorCode)); // Define logic
        Map<String, Object> payload = xmlTransformer.toJsonMap(xmlResponse);
        return createResponse(success, errorCode, errorDesc, payload, correlationId, serviceName);
    }

    private StandardResponse<Map<String, Object>> createResponse(
            boolean success,
            String errorCode,
            String errorDescription,
            Map<String, Object> payload,
            String correlationId,
            String serviceName) {
        StandardResponse<Map<String, Object>> resp = new StandardResponse<>();
        resp.setSuccess(success);
        resp.setErrorCode(errorCode);
        resp.setErrorDescription(errorDescription);
        resp.setPayload(payload);
        resp.setCorrelationId(correlationId);
        resp.setTimestamp(Instant.now());
        // Service name could be added if desired
        return resp;
    }
}
