package org.adcb.adapter.protocol.soap;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.Map;

/**
 * Utility methods for SOAP XML request templating and message manipulation.
 */
public class SoapTemplateUtils {

    /**
     * Render the SOAP XML request, substituting any placeholders in the template with data from requestBody.
     */
    public static String renderRequest(String templateName, Object requestBody) {
        // For simplicity, just return the templateName as XML, or use requestBody directly if already XML.
        // In production, use a template engine (like Thymeleaf or Freemarker) to fill in fields.
        return requestBody instanceof String ? (String) requestBody : requestBody.toString();
    }

    /**
     * Write the string XML as a SOAP payload to the outbound message.
     */
    public static void setSoapPayload(WebServiceMessage message, String xmlPayload) throws Exception {
        TransformerFactory.newInstance().newTransformer()
                .transform(new StreamSource(new StringReader(xmlPayload)), message.getPayloadResult());
    }

    /**
     * Dynamically add SOAP headers if requested.
     */
    public static void addSoapHeaders(WebServiceMessage message, Map<String, String> headers) {
        if (headers == null || !(message instanceof SoapMessage)) return;
        SoapMessage soapMessage = (SoapMessage) message;
        headers.forEach((k,v) -> soapMessage.getSoapHeader().addHeaderElement(new javax.xml.namespace.QName(k)).setText(v));
    }

    /**
     * Read and parse the SOAP response.
     * Here, simply return the raw XML string.
     */
    public static Object readSoapResponse(WebServiceMessage message) throws Exception {
        // In real-world, use JAXB to unmarshal the XML or an XML parser to extract fields
        java.io.StringWriter writer = new java.io.StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(message.getPayloadSource(), new javax.xml.transform.stream.StreamResult(writer));
        return writer.toString();
    }
}
