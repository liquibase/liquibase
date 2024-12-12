package liquibase.include;

import java.util.Map;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.SetupException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.plugin.Plugin;
import liquibase.resource.ResourceAccessor;

public interface IncludeService extends Plugin {

 int getPriority();

 ChangeLogInclude createChangelogInclude(ParsedNode node, ResourceAccessor resourceAccessor,
                                         DatabaseChangeLog databaseChangeLog, Map<String, Object> attrs)
		 throws SetupException, ParsedNodeException;

 ChangeLogIncludeAll createChangelogIncludeAll(ParsedNode node, ResourceAccessor resourceAccessor,
																				 DatabaseChangeLog databaseChangeLog, Map<String, Object> attrs)
		 throws SetupException, ParsedNodeException;
}