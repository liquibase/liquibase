package liquibase.parser.core.xml;

import liquibase.logging.LogFactory;
import liquibase.logging.LogTarget;
import liquibase.logging.Logger;

import java.io.InputStream;

public class ClassLoaderXsdStreamResolver extends XsdStreamResolver {

    private static final Logger LOGGER = new LogFactory().getLog("ContextClassLoaderXsdStreamResolver");

    @Override
    public InputStream getResourceAsStream(String xsdFile) {
        LOGGER.debug(LogTarget.LOG, "Trying to load resource from class classloader");

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(xsdFile);
        if(resourceAsStream == null){
            LOGGER.debug(LogTarget.LOG, "Failed to load resource from class classloader");
            return getSuccessorValue(xsdFile);
        }
        return resourceAsStream;
    }
}
