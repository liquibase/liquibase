package liquibase.parser.core.xml;

import liquibase.Scope;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.io.InputStream;

public class ResourceAccessorXsdStreamResolver extends XsdStreamResolver {

	private static final Logger LOGGER = Scope.getCurrentScope().getLog(ResourceAccessorXsdStreamResolver.class);

	private ResourceAccessor resourceAccessor;

	public ResourceAccessorXsdStreamResolver(ResourceAccessor resourceAccessor) {
		this.resourceAccessor = resourceAccessor;
	}

	@Override
	public InputStream getResourceAsStream(String xsdFile) {
		try {

			InputStream resourceAsStream = resourceAccessor.openStream(null, xsdFile);
			if(resourceAsStream == null){
				LOGGER.debug("Could not load "+xsdFile+" with the standard resource accessor.");
				return getSuccessorValue(xsdFile);
			}
			return resourceAsStream;

		}catch (IOException e){
			LOGGER.debug("Could not load "+xsdFile+" with the standard resource accessor.");
			return getSuccessorValue(xsdFile);
		}
	}
}
