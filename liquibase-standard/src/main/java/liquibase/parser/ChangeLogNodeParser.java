package liquibase.parser;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.util.FileUtil;

public interface ChangeLogNodeParser extends ChangeLogParser {

    default DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters,
                                    ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        try {
            Resource resource = resourceAccessor.get(physicalChangeLogLocation);
            if (!resource.exists()) {
                if (physicalChangeLogLocation.startsWith("WEB-INF/classes/")) {
                   // Correct physicalChangeLogLocation and try again.
                    return parse(physicalChangeLogLocation.replaceFirst("WEB-INF/classes/", ""),
                                 changeLogParameters, resourceAccessor);
                } else {
                    throw new ChangeLogParseException(FileUtil.getFileNotFoundMessage(physicalChangeLogLocation));
                }
            }
            ParsedNode parsedNode = parseToNode(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
            if (parsedNode == null) {
                return null;
            }

            DatabaseChangeLog changeLog = new DatabaseChangeLog(DatabaseChangeLog.normalizePath(physicalChangeLogLocation));
            changeLog.setChangeLogParameters(changeLogParameters);
            changeLog.load(parsedNode, resourceAccessor);
            return changeLog;
        } catch (ChangeLogParseException e) {
            throw e;
        } catch (Exception e) {
           throw new ChangeLogParseException(e);
        }
    }

    ParsedNode parseToNode(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters,
                           ResourceAccessor resourceAccessor) throws ChangeLogParseException;
}
