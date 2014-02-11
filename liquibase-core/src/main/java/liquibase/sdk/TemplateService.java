package liquibase.sdk;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.*;
import java.util.Map;

public class TemplateService {

    private static TemplateService instance = new TemplateService();
    private final VelocityEngine engine;

    public static TemplateService getInstance() {
        return instance;
    }

    private TemplateService() {
        engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        engine.init();
    }

    public String output(String templatePath, Map<String, Object> contextParams) throws IOException {
        StringWriter writer = new StringWriter();
        try {
            write(templatePath, writer, contextParams);
        } finally {
            writer.flush();
            writer.close();
        }

        return writer.toString();
    }

    public void write(String templatePath, File outputFile, Map<String, Object> contextParams) throws IOException {
        outputFile.getAbsoluteFile().getParentFile().mkdirs();
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        try {
            write(templatePath, writer, contextParams);
        } finally {
            writer.flush();
            writer.close();
        }

    }
    public void write(String templatePath, Writer output, Map<String, Object> contextParams) throws IOException {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream(templatePath);


        try {
            if (input == null) {
                throw new IOException("Template file " + templatePath + " doesn't exist");
            }

            VelocityContext context = new VelocityContext();
            if (contextParams != null) {
                for (Map.Entry<String, Object> entry : contextParams.entrySet()) {
                    context.put(entry.getKey(), entry.getValue());
                }
            }

            Template template = engine.getTemplate(templatePath, "UTF-8");

            template.merge(context, output);
        } finally {
            if (input != null) {
                input.close();
            }
        }

    }
}
