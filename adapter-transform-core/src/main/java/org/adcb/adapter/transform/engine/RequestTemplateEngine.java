package org.adcb.adapter.transform.engine;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.cache.StringTemplateLoader;

import java.io.StringWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Renders request templates using Freemarker (supports both JSON and XML).
 */
public class RequestTemplateEngine {

    private final Configuration freemarkerConfig;

    public RequestTemplateEngine() {
        this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        freemarkerConfig.setTemplateLoader(new StringTemplateLoader());
        freemarkerConfig.setDefaultEncoding("UTF-8");
    }

    /**
     * Renders a template given as string, populated with values from dataModel.
     * Supports nested values for maps/lists.
     */
    public String render(String templateString, Map<String, Object> dataModel) throws IOException, TemplateException {
        StringTemplateLoader loader = (StringTemplateLoader) freemarkerConfig.getTemplateLoader();
        loader.putTemplate("dynamicTemplate", templateString);
        Template template = freemarkerConfig.getTemplate("dynamicTemplate");
        StringWriter writer = new StringWriter();
        template.process(dataModel, writer);
        return writer.toString();
    }
}
