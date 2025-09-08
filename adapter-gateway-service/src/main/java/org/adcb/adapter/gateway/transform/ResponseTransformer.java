package org.adcb.adapter.gateway.transform;

/**
 * Response transformation logic placeholder (no-op).
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseTransformer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Simple JSONPath-based transformation to extract fields as per responseTemplate.
     * Assumes template is JSON defining output keys mapped to JSONPaths.
     */
    public Map<String, Object> transform(Object rawResponse, String responseTemplate) {

        Map<String, String> templateMap;
        try {
            templateMap = objectMapper.readValue(responseTemplate, Map.class);
        } catch (IOException e) {
            // If template is not valid JSON, return raw response
            return Map.of("response", rawResponse);
        }

        try {
            JsonNode responseNode = objectMapper.readTree(rawResponse.toString());
            Map<String, Object> transformed = new HashMap<>();

            for (Map.Entry<String, String> entry : templateMap.entrySet()) {
                // Naive JSON Path extraction: supports $.field paths only
                String outputKey = entry.getKey();
                String jsonPath = entry.getValue();

                if (jsonPath.startsWith("$.")) {
                    String field = jsonPath.substring(2);
                    JsonNode valueNode = responseNode.path(field);
                    if (valueNode.isValueNode()) {
                        transformed.put(outputKey, valueNode.asText());
                    } else {
                        transformed.put(outputKey, valueNode.toString());
                    }
                }
            }
            return transformed;

        } catch (IOException e) {
            return Map.of("response", rawResponse);
        }
    }
}
