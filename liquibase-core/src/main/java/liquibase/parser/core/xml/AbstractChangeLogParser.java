package liquibase.parser.core.xml;

import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.EmptyResource;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.util.FileUtil;

public abstract class AbstractChangeLogParser implements ChangeLogParser {

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters,
                                   ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        ParsedNode parsedNode = parseToNode(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
        if (parsedNode == null) {
            return null;
        }

        DatabaseChangeLog changeLog = new DatabaseChangeLog(DatabaseChangeLog.normalizePath(physicalChangeLogLocation));
        changeLog.setChangeLogParameters(changeLogParameters);
        try {
            changeLog.load(parsedNode, resourceAccessor);
        } catch (Exception e) {
            throw new ChangeLogParseException(e);
        }

        return changeLog;
    }

    protected abstract ParsedNode parseToNode(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters,
                                              ResourceAccessor resourceAccessor) throws ChangeLogParseException;

    protected Resource returnEmptyResourceIfMissingFileResource(String physicalChangeLogLocation)
            throws ChangeLogParseException {
        if (ChangeLogParserConfiguration.WARN_OR_ERROR_ON_MISSING_CHANGELOGS.getCurrentValue()) {
            Scope.getCurrentScope().getLog(getClass()).warning(FileUtil.getFileNotFoundMessage(physicalChangeLogLocation));
            return new EmptyResource();
        } else {
            throw new ChangeLogParseException(FileUtil.getFileNotFoundMessage(physicalChangeLogLocation));
        }
    }
}
