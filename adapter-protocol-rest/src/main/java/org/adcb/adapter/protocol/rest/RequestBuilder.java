package org.adcb.adapter.protocol.rest;


import java.util.Map;

public class RequestBuilder {

    /**
     * Builds/serializes request body if needed (currently handled outside)
     */
    public static String buildJsonRequest(Map<String, Object> data) {
        // Use Jackson or other utility for serialization as required
        // For now, assume the requestBody is already a JSON string
        return data.toString();
    }

    public static String buildXmlRequest(Map<String, Object> data) {
        // Build XML string or object if required
        // Placeholder for future extension
        return "<root>" + data.toString() + "</root>";
    }
}

