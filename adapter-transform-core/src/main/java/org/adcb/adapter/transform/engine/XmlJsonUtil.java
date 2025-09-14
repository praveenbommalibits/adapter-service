package org.adcb.adapter.transform.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.Map;

/**
 * Utility to convert XML to Map<String, Object> (JSON-like) structure.
 */
public class XmlJsonUtil {

    private static final XmlMapper xmlMapper = new XmlMapper();
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static Map<String, Object> convertXmlToMap(String xml) throws Exception {
        // Parse XML into a tree and convert to Map
        Object obj = xmlMapper.readValue(xml, Object.class);
        // Convert to Map without changing object structure
        return jsonMapper.convertValue(obj, Map.class);
    }
}
