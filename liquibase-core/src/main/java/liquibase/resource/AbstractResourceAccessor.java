package liquibase.resource;

import liquibase.AbstractExtensibleObject;
import liquibase.exception.UnexpectedLiquibaseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Convenience base class for {@link ResourceAccessor} implementations.
 */
public abstract class AbstractResourceAccessor extends AbstractExtensibleObject implements ResourceAccessor {

}
