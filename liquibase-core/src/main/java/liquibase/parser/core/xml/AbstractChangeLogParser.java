package liquibase.parser.core.xml;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;

import java.text.ParseException;

public abstract class AbstractChangeLogParser implements ChangeLogParser {

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        ParsedNode parsedNode = parseToNode(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
        if (parsedNode == null) {
            return null;
        }

        DatabaseChangeLog changeLog = new DatabaseChangeLog(physicalChangeLogLocation);

        try {
            for (ParsedNode node : parsedNode.getChildren(null, "changeSet")) {
                changeLog.addChangeSet(createChangeSet(node, changeLog));
            }
        } catch (ParseException e) {
            throw new ChangeLogParseException(e);
        }

        return changeLog;
    }

    protected ChangeSet createChangeSet(ParsedNode node, DatabaseChangeLog changeLog) throws ParseException {
        ChangeSet changeSet = new ChangeSet(changeLog);
        changeSet.load(node);
        return changeSet;
    }

    protected abstract ParsedNode parseToNode(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException;
}
