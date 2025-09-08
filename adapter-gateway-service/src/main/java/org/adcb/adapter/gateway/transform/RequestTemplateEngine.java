package org.adcb.adapter.gateway.transform;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Template processing logic using simple ${key} substitution.
 */


@Component
public class RequestTemplateEngine {

    /**
     * Simple placeholder replacement implementation.
     * Replace ${key} in template with corresponding values from data
     * TODO
     */
    public String processTemplate(String template, Map<String, Object> data) {
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue().toString());
        }
        return result;
    }
}

