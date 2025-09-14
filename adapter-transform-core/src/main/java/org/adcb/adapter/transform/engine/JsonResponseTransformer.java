package org.adcb.adapter.transform.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Transforms JSON responses using JsonPath.
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
