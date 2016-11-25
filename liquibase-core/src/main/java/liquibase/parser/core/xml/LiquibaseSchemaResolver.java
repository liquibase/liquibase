package liquibase.parser.core.xml;

import java.io.InputStream;

import org.xml.sax.InputSource;

import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.LiquibaseParser;
import liquibase.parser.NamespaceDetails;
import liquibase.parser.NamespaceDetailsFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializer;

class LiquibaseSchemaResolver {

	private static final Logger LOGGER = new LogFactory().getLog("LiquibaseSchemaResolver");
	private String systemId;
	private String publicId;
	private ResourceAccessorXsdStreamResolver resourceAccessorXsdStreamResolver;
	private ContextClassLoaderXsdStreamResolver contextClassLoaderXsdStreamResolver;
	private ClassLoaderXsdStreamResolver classLoaderXsdStreamResolver;

	public LiquibaseSchemaResolver(String systemId, String publicId, ResourceAccessor resourceAccessor) {
		this.systemId = systemId;
		this.publicId = publicId;

		resourceAccessorXsdStreamResolver = new ResourceAccessorXsdStreamResolver(resourceAccessor);
		contextClassLoaderXsdStreamResolver = new ContextClassLoaderXsdStreamResolver();
		classLoaderXsdStreamResolver = new ClassLoaderXsdStreamResolver();

		resourceAccessorXsdStreamResolver.setSuccessor(contextClassLoaderXsdStreamResolver);
		contextClassLoaderXsdStreamResolver.setSuccessor(classLoaderXsdStreamResolver);
	}

	public InputSource resolve(LiquibaseSerializer serializer){
		if(serializer == null){
			throw new RuntimeException("Serializer can not be null");
		}
		NamespaceDetails namespaceDetails = NamespaceDetailsFactory.getInstance().getNamespaceDetails(serializer, systemId);
		return getInputSourceFromXsd(namespaceDetails);
	}

	public InputSource resolve(LiquibaseParser parser){
		if(parser == null){
			throw new RuntimeException("Parser can not be null");
		}
		NamespaceDetails namespaceDetails = NamespaceDetailsFactory.getInstance().getNamespaceDetails(parser, systemId);
		return getInputSourceFromXsd(namespaceDetails);
	}

	private InputSource getInputSourceFromXsd(NamespaceDetails namespaceDetails){
		if (systemId == null) {
			return null;
		}

		LOGGER.debug("Found namespace details class "+namespaceDetails.getClass().getName()+" for "+ systemId);
		String xsdFile = namespaceDetails.getLocalPath(systemId);
		LOGGER.debug("Local path for "+ systemId +" is "+xsdFile);

		if (xsdFile == null) {
			return null;
		}

		try {
			InputStream resourceAsStream = resourceAccessorXsdStreamResolver.getResourceAsStream(xsdFile);

			if (resourceAsStream == null) {
				LOGGER.debug("Could not find "+xsdFile+" locally");
				return null;
			}

			LOGGER.debug("Successfully loaded XSD from "+xsdFile);
			org.xml.sax.InputSource source = new org.xml.sax.InputSource(resourceAsStream);
			source.setPublicId(publicId);
			source.setSystemId(systemId);
			return source;
		} catch (Exception ex) {
			LOGGER.debug("Error loading XSD", ex);
			return null; // We don't have the schema, try the network
		}
	}
}
