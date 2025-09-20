package org.adcb.adapter.transform.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Transforms JSON responses using JsonPath.
 * After receiving the downstream response as a raw string:
 * 	•	For JSON, use `JsonResponseTransformer.toMap()` → yield a `Map<String,Object>`.
 * 	•	For XML or SOAP, use `XmlJsonUtil.convertXmlToMap()` or `XmlResponseTransformer.toJsonMap()`.
 * 	•	Pass the map into your template engine if you need to apply a response template, or directly wrap it in `StandardResponse`.
 */
@Slf4j
public class JsonResponseTransformer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object extractField(String jsonString, String jsonPath) {
        return JsonPath.parse(jsonString).read(jsonPath); // e.g. "$.profile.contact.email"
    }

    public Map<String, Object> toMap(String jsonString) throws Exception {
        return objectMapper.readValue(jsonString, Map.class);
    }
}
