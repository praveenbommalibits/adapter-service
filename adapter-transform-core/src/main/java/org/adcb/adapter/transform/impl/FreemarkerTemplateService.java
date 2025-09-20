package org.adcb.adapter.transform.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.cache.FileTemplateLoader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.transform.TemplateService;
import org.adcb.adapter.transform.exception.TemplateProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Freemarker-based implementation of TemplateService.
 *
 * <p>This implementation provides high-performance template processing using Freemarker
 * with intelligent caching and validation. It supports both file-based and inline templates.
 *
 * <p>Key features:
 * <ul>
 *   <li>Automatic template compilation and caching</li>
 *   <li>TTL-based cache expiration for development flexibility</li>
 *   <li>File-based template loading from configurable directory</li>
 *   <li>Comprehensive error handling and reporting</li>
 *   <li>Template syntax validation</li>
 * </ul>
 *
 * @since 1.0
 */
@Service
@Slf4j
public class FreemarkerTemplateService implements TemplateService {

    private final Configuration freemarkerConfig;
    private final ConcurrentHashMap<String, Template> templateCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    @Value("${adapter.templates.path:classpath:/adapter-templates/}")
    private String templateBasePath;

    @Value("${adapter.templates.cache.ttl-minutes:60}")
    private long cacheTtlMinutes;

    @Value("${adapter.templates.cache.enabled:true}")
    private boolean cacheEnabled;

    public FreemarkerTemplateService() {
        this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        this.freemarkerConfig.setDefaultEncoding("UTF-8");
        this.freemarkerConfig.setLogTemplateExceptions(false);
        this.freemarkerConfig.setWrapUncheckedExceptions(true);
    }

    @PostConstruct
    public void initialize() {
        try {
            setupTemplateLoader();
            if (cacheEnabled) {
                log.info("Template cache enabled with TTL: {} minutes", cacheTtlMinutes);
            }
            log.info("FreemarkerTemplateService initialized with base path: {}", templateBasePath);
        } catch (Exception e) {
            log.error("Failed to initialize FreemarkerTemplateService", e);
            throw new RuntimeException("Template service initialization failed", e);
        }
    }

    private void setupTemplateLoader() throws IOException {
        if (templateBasePath.startsWith("classpath:")) {
            // Use classpath-based loading
            freemarkerConfig.setClassForTemplateLoading(this.getClass(),
                    templateBasePath.substring("classpath:".length()));
        } else {
            // Use file-based loading
            Path templateDir = Paths.get(templateBasePath);
            if (!Files.exists(templateDir)) {
                Files.createDirectories(templateDir);
                log.info("Created template directory: {}", templateDir);
            }
            freemarkerConfig.setTemplateLoader(new FileTemplateLoader(templateDir.toFile()));
        }
    }

    @Override
    public String process(String templatePath, Map<String, Object> context) throws TemplateProcessingException {
        try {
            Template template = getTemplate(templatePath);
            StringWriter writer = new StringWriter();

            // Add system variables to context
            Map<String, Object> enrichedContext = enrichContext(context);

            template.process(enrichedContext, writer);

            String result = writer.toString();
            log.debug("Processed template '{}' with context keys: {}", templatePath, context.keySet());

            return result;

        } catch (TemplateException e) {
            String msg = String.format("Template processing failed for '%s': %s", templatePath, e.getMessage());
            log.error(msg, e);
            throw new TemplateProcessingException(msg, e);
        } catch (IOException e) {
            String msg = String.format("Template loading failed for '%s': %s", templatePath, e.getMessage());
            log.error(msg, e);
            throw new TemplateProcessingException(msg, e);
        }
    }

    @Override
    public String processInline(String templateContent, Map<String, Object> context, String templateName)
            throws TemplateProcessingException {
        try {
            Template template = getInlineTemplate(templateContent, templateName);
            StringWriter writer = new StringWriter();

            Map<String, Object> enrichedContext = enrichContext(context);
            template.process(enrichedContext, writer);

            String result = writer.toString();
            log.debug("Processed inline template '{}' with context keys: {}", templateName, context.keySet());

            return result;

        } catch (TemplateException e) {
            String msg = String.format("Inline template processing failed for '%s': %s", templateName, e.getMessage());
            log.error(msg, e);
            throw new TemplateProcessingException(msg, e);
        } catch (IOException e) {
            String msg = String.format("Inline template compilation failed for '%s': %s", templateName, e.getMessage());
            log.error(msg, e);
            throw new TemplateProcessingException(msg, e);
        }
    }

    @Override
    public boolean validateTemplate(String templateContent) {
        try {
            // Use the Template constructor for validation
            new Template("validationTemplate", new StringReader(templateContent), freemarkerConfig);
            return true;
        } catch (Exception e) {
            log.debug("Template validation failed: {}", e.getMessage());
            return false;
        }
    }


    @Override
    public void preloadTemplates() throws TemplateProcessingException {
        try {
            if (templateBasePath.startsWith("classpath:")) {
                // For classpath resources, we can't easily list all files
                log.info("Classpath-based templates will be loaded on-demand");
                return;
            }

            Path templateDir = Paths.get(templateBasePath);
            if (!Files.exists(templateDir)) {
                log.warn("Template directory does not exist: {}", templateDir);
                return;
            }

            Files.walk(templateDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json") || path.toString().endsWith(".xml"))
                    .forEach(path -> {
                        try {
                            String relativePath = templateDir.relativize(path).toString();
                            getTemplate(relativePath); // This will cache the template
                            log.debug("Preloaded template: {}", relativePath);
                        } catch (Exception e) {
                            log.error("Failed to preload template: {}", path, e);
                        }
                    });

            log.info("Preloaded {} templates from {}", templateCache.size(), templateDir);

        } catch (IOException e) {
            throw new TemplateProcessingException("Failed to preload templates", e);
        }
    }

    @Override
    public void clearCache() {
        templateCache.clear();
        cacheTimestamps.clear();
        log.info("Template cache cleared");
    }

    private Template getTemplate(String templatePath) throws IOException {
        if (!cacheEnabled) {
            return freemarkerConfig.getTemplate(templatePath);
        }

        return templateCache.compute(templatePath, (path, cached) -> {
            try {
                if (cached == null || isExpired(path)) {
                    cacheTimestamps.put(path, System.currentTimeMillis());
                    return freemarkerConfig.getTemplate(path);
                }
                return cached;
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to load template: " + path, e);
            }
        });
    }

    private Template getInlineTemplate(String templateContent, String templateName) throws IOException {
        String cacheKey = "inline:" + templateName;

        if (!cacheEnabled) {
            return new Template(templateName, new StringReader(templateContent), freemarkerConfig);
        }

        return templateCache.compute(cacheKey, (key, cached) -> {
            try {
                if (cached == null || isExpired(key)) {
                    cacheTimestamps.put(key, System.currentTimeMillis());
                    return new Template(templateName, new StringReader(templateContent), freemarkerConfig);
                }
                return cached;
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to compile inline template: " + templateName, e);
            }
        });
    }

    private boolean isExpired(String cacheKey) {
        Long timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp == null) return true;

        long ttlMillis = TimeUnit.MINUTES.toMillis(cacheTtlMinutes);
        return (System.currentTimeMillis() - timestamp) > ttlMillis;
    }

    private Map<String, Object> enrichContext(Map<String, Object> originalContext) {
        Map<String, Object> enriched = new ConcurrentHashMap<>(originalContext);

        // Add system variables
        enriched.put("currentTimestamp", System.currentTimeMillis());
        enriched.put("currentTimeISO", java.time.LocalDateTime.now().toString());
        enriched.put("systemName", "ADCB_ADAPTER");
        enriched.put("version", "1.0");

        // Generate correlation ID if not present
        if (!enriched.containsKey("correlationId")) {
            enriched.put("correlationId", java.util.UUID.randomUUID().toString());
        }

        return enriched;
    }
}
