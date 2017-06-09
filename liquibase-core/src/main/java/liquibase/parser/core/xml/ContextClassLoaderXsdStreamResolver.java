package liquibase.parser.core.xml;

import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

import java.io.InputStream;

public class ContextClassLoaderXsdStreamResolver extends XsdStreamResolver {

	private static final Logger LOGGER = new LogFactory().getLog("ContextClassLoaderXsdStreamResolver");

	@Override
	public InputStream getResourceAsStream(String xsdFile) {
		LOGGER.debug("Trying to load resource from context classloader");

		if (Thread.currentThread().getContextClassLoader() == null) {
			LOGGER.debug("Failed to load resource from context classloader");
			return getSuccessorValue(xsdFile);
		}

		InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xsdFile);
		if(resourceAsStream == null){
			LOGGER.debug("Failed to load resource from context classloader");
			return getSuccessorValue(xsdFile);
		}
		return resourceAsStream;
	}
}
