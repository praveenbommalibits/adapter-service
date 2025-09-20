package org.adcb.adapter.transform;

import org.adcb.adapter.transform.exception.TemplateProcessingException;

import java.util.Map;

/**
 * Unified interface for template processing across all protocols.
 *
 * <p>This service provides template rendering capabilities for both request and response
 * transformations. It supports variable substitution, conditional logic, and nested
 * object access using Freemarker syntax.
 *
 * <p>Templates are cached automatically to improve performance in high-throughput scenarios.
 * The service validates template syntax and provides detailed error reporting.
 *
 * @since 1.0
 * @author ADCB Adapter Team
 */
public interface TemplateService {

    /**
     * Processes a template with the given context data.
     *
     * @param templatePath relative path to template file (e.g., "customer_request.json")
     * @param context data model for variable substitution
     * @return processed template as string
     * @throws TemplateProcessingException if template processing fails
     */
    String process(String templatePath, Map<String, Object> context) throws TemplateProcessingException;

    /**
     * Processes a template string directly without file loading.
     *
     * @param templateContent template content as string
     * @param context data model for variable substitution
     * @param templateName unique name for caching purposes
     * @return processed template as string
     * @throws TemplateProcessingException if template processing fails
     */
    String processInline(String templateContent, Map<String, Object> context, String templateName)
            throws TemplateProcessingException;

    /**
     * Validates template syntax without processing.
     *
     * @param templateContent template content to validate
     * @return true if template is syntactically valid
     */
    boolean validateTemplate(String templateContent);

    /**
     * Preloads and validates all templates from configured directory.
     * Called during application startup.
     *
     * @throws TemplateProcessingException if any template fails validation
     */
    void preloadTemplates() throws TemplateProcessingException;

    /**
     * Clears template cache. Useful for development/testing.
     */
    void clearCache();
}
