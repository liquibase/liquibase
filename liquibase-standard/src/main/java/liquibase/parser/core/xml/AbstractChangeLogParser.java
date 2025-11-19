package liquibase.parser.core.xml;

import liquibase.ContextExpression;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;

public abstract class AbstractChangeLogParser implements ChangeLogParser {

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters,
                                   ResourceAccessor resourceAccessor, ContextExpression includeContextFilter) throws ChangeLogParseException {
        ParsedNode parsedNode = parseToNode(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
        if (parsedNode == null) {
            return null;
        }

        DatabaseChangeLog changeLog = new DatabaseChangeLog(DatabaseChangeLog.normalizePath(physicalChangeLogLocation));
        if (changeLogParameters != null) {
            changeLogParameters.setLocal(ChangeLogParameters.PARENT_INCLUDE_CONTEXT_FILTER, includeContextFilter, changeLog);
        }
        changeLog.setChangeLogParameters(changeLogParameters);
        try {
            changeLog.load(parsedNode, resourceAccessor);
        } catch (Exception e) {
            throw new ChangeLogParseException(e);
        }

        return changeLog;
    }

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        return parse(physicalChangeLogLocation, changeLogParameters, resourceAccessor, null);
    }

    protected abstract ParsedNode parseToNode(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters,
                                              ResourceAccessor resourceAccessor) throws ChangeLogParseException;
}
