package liquibase.parser.core.xml;

import java.io.InputStream;

import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

public class ClassLoaderXsdStreamResolver extends XsdStreamResolver {

	private static final Logger LOGGER = new LogFactory().getLog("ContextClassLoaderXsdStreamResolver");

	@Override
	public InputStream getResourceAsStream(String xsdFile) {
		LOGGER.debug("Trying to load resource from class classloader");

		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(xsdFile);
		if(resourceAsStream == null){
			LOGGER.debug("Failed to load resource from class classloader");
			return getSuccessorValue(xsdFile);
		}
		return resourceAsStream;
	}
}
