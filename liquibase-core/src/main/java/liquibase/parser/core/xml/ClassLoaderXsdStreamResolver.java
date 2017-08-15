package liquibase.parser.core.xml;

import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;

import java.io.InputStream;

public class ClassLoaderXsdStreamResolver extends XsdStreamResolver {

    private static final Logger LOGGER = LogService.getLog(ClassLoaderXsdStreamResolver.class);

    @Override
    public InputStream getResourceAsStream(String xsdFile) {
        LOGGER.debug(LogType.LOG, "Trying to load resource from class classloader");

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(xsdFile);
        if(resourceAsStream == null){
            LOGGER.debug(LogType.LOG, "Failed to load resource from class classloader");
            return getSuccessorValue(xsdFile);
        }
        return resourceAsStream;
    }
}
