package org.adcb.adapter.protocol.soap;

import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.spi.ProtocolHandler;
import org.adcb.adapter.commons.ServiceMetadata;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;

/**
 * SOAP protocol handler implementation for invoking SOAP downstream endpoints.
 */
@Slf4j
@Component("SOAP")
public class SoapProtocolHandler implements ProtocolHandler {

    private final WebServiceTemplate webServiceTemplate;

    /**
     * No-arg constructor for production, initializes WebServiceTemplate with JAXB marshaller.
     */
    public SoapProtocolHandler() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("org.adcb.adapter.generated");
        this.webServiceTemplate = new WebServiceTemplate(marshaller);
    }

    /**
     * Constructor for injecting WebServiceTemplate, used in unit tests.
     *
     * @param webServiceTemplate the WebServiceTemplate to use
     */
    public SoapProtocolHandler(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    @Override
    public Object execute(ServiceMetadata config, Object requestBody) {
        try {
            log.info("SOAP: Invoking endpoint: {}", config.getEndpointUrl());

            String xmlPayload = SoapTemplateUtils.renderRequest(config.getRequestTemplate(), requestBody);

            Object response = webServiceTemplate.sendAndReceive(
                    config.getEndpointUrl(),
                    (WebServiceMessage message) -> {
                        try {
                            SoapTemplateUtils.setSoapPayload(message, xmlPayload);
                            SoapTemplateUtils.addSoapHeaders(message, config.getHeaders());
                        } catch (Exception e) {
                            throw new RuntimeException("Error setting SOAP request payload or headers", e);
                        }
                    },
                    (WebServiceMessageExtractor<Object>) message -> {
                        try {
                            return SoapTemplateUtils.readSoapResponse(message);
                        } catch (Exception e) {
                            throw new RuntimeException("Error reading SOAP response", e);
                        }
                    }
            );

            log.debug("Received SOAP response: {}", response);
            return response;

        } catch (SoapFaultClientException fault) {
            log.error("SOAP fault received: {}", fault.getFaultStringOrReason());
            throw new SoapProtocolException("SOAP fault: " + fault.getFaultStringOrReason(), fault);

        } catch (WebServiceIOException ioEx) {
            log.error("SOAP transport error: {}", ioEx.getMessage());
            throw new SoapProtocolException("SOAP transport failed: " + ioEx.getMessage(), ioEx);

        } catch (Exception ex) {
            log.error("Unexpected SOAP error", ex);
            throw new SoapProtocolException("Unexpected SOAP error: " + ex.getMessage(), ex);
        }
    }
}
