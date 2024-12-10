package liquibase.include;

import java.util.Map;
import liquibase.changelog.DatabaseChangeLog;
import static liquibase.changelog.DatabaseChangeLog.MODIFY_CHANGE_SETS;
import liquibase.changelog.ModifyChangeSets;
import liquibase.exception.SetupException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

public class StandardIncludeService implements IncludeService {

 @Override
 public int getPriority() {
	return PRIORITY_DEFAULT;
 }

 @Override
 public ChangeLogInclude createChangelogInclude(ParsedNode node, ResourceAccessor resourceAccessor,
																								DatabaseChangeLog databaseChangeLog,
																								Map<String, Object> attrs) throws SetupException, ParsedNodeException {
	return new ChangeLogInclude(node, resourceAccessor, databaseChangeLog,
			(ModifyChangeSets) attrs.get(MODIFY_CHANGE_SETS));
 }

 @Override
 public ChangeLogIncludeAll createChangelogIncludeAll(ParsedNode node,
																											ResourceAccessor resourceAccessor,
																											DatabaseChangeLog databaseChangeLog,
																											Map<String, Object> attrs) throws SetupException, ParsedNodeException {
	return new ChangeLogIncludeAll(node, resourceAccessor, databaseChangeLog,
			(ModifyChangeSets) attrs.get(MODIFY_CHANGE_SETS));
 }
}
