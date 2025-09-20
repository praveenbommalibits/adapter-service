package org.adcb.adapter.transform.engine;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import java.util.Map;

/**
 * Transforms SOAP/XML responses using XPath and converts to JSON.
 * After receiving the downstream response as a raw string:
 * 	•	For JSON, use `JsonResponseTransformer.toMap()` → yield a `Map<String,Object>`.
 * 	•	For XML or SOAP, use `XmlJsonUtil.convertXmlToMap()` or `XmlResponseTransformer.toJsonMap()`.
 * 	•	Pass the map into your template engine if you need to apply a response template, or directly wrap it in `StandardResponse`.
 */
@Slf4j
public class XmlResponseTransformer {

    public String extractField(String xmlString, String xpathExpr) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(xmlString.getBytes()));
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(xpathExpr);
        Object value = expr.evaluate(doc, XPathConstants.STRING);
        return value != null ? value.toString() : null;
    }

    // Optionally: implement an xml-to-json converter for payload
    public Map<String, Object> toJsonMap(String xmlString) throws Exception {
        // For brevity, use a simple recursive DOM traversal to convert XML nodes to Map
        // Can use libraries like org.json or Jackson XML module for production
        return XmlJsonUtil.convertXmlToMap(xmlString); // Implement this utility as needed
    }
}
