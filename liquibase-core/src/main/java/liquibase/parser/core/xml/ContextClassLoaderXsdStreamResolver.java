package liquibase.parser.core.xml;

import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;

import java.io.InputStream;

public class ContextClassLoaderXsdStreamResolver extends XsdStreamResolver {

    private static final Logger LOGGER = LogService.getLog(ContextClassLoaderXsdStreamResolver.class);

    @Override
    public InputStream getResourceAsStream(String xsdFile) {
        LOGGER.debug(LogType.LOG, "Trying to load resource from context classloader");

        if (Thread.currentThread().getContextClassLoader() == null) {
            LOGGER.debug(LogType.LOG, "Failed to load resource from context classloader");
            return getSuccessorValue(xsdFile);
        }

        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xsdFile);
        if(resourceAsStream == null){
            LOGGER.debug(LogType.LOG, "Failed to load resource from context classloader");
            return getSuccessorValue(xsdFile);
        }
        return resourceAsStream;
    }
}
