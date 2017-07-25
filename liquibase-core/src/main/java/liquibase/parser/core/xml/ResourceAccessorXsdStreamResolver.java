package liquibase.parser.core.xml;

import liquibase.logging.LogFactory;
import liquibase.logging.LogTarget;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;

public class ResourceAccessorXsdStreamResolver extends XsdStreamResolver {

	private static final Logger LOGGER = new LogFactory().getLog("ResourceAccessorXsdStreamResolver");

	private ResourceAccessor resourceAccessor;

	public ResourceAccessorXsdStreamResolver(ResourceAccessor resourceAccessor) {
		this.resourceAccessor = resourceAccessor;
	}

	@Override
	public InputStream getResourceAsStream(String xsdFile) {
		try {

			InputStream resourceAsStream = StreamUtil.singleInputStream(xsdFile, resourceAccessor);
			if(resourceAsStream == null){
				LOGGER.debug(LogTarget.LOG, "Could not load "+xsdFile+" with the standard resource accessor.");
				return getSuccessorValue(xsdFile);
			}
			return resourceAsStream;

		}catch (IOException e){
			LOGGER.debug(LogTarget.LOG, "Could not load "+xsdFile+" with the standard resource accessor.");
			return getSuccessorValue(xsdFile);
		}
	}
}
