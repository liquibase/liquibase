package liquibase.resource.list;

import java.io.IOException;
import java.util.Set;

public interface ListHandler {

	/**
	 * As the list methods returns a set of resources which is always loaded by a classloader impl so;
	 * 'The name of a resource is a "/"-separated sequence of identifiers.' reference;
	 * https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html#classloader
	 */
	static String RESOURCE_PATH_SEPERATOR = "/";

	Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException;
}
