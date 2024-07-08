package liquibase.parser.core.sql;

import liquibase.Scope;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changeset.ChangeSetService;
import liquibase.changeset.ChangeSetServiceFactory;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.io.IOException;

@SuppressWarnings("java:S2583")
public class SqlChangeLogParser implements ChangeLogParser {

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        return changeLogFile.toLowerCase().endsWith(".sql");
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {

        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        changeLog.setPhysicalFilePath(physicalChangeLogLocation);

        RawSQLChange change = new RawSQLChange();

        try {
            Resource sqlResource = resourceAccessor.getExisting(physicalChangeLogLocation);
            String sql = StreamUtil.readStreamAsString(sqlResource.openInputStream());
            //
            // Handle empty files with a WARNING message
            //
            if (StringUtil.isEmpty(sql)) {
                String message = String.format("Unable to parse empty file '%s'", physicalChangeLogLocation);
                Scope.getCurrentScope().getLog(getClass()).warning(message);
                throw new ChangeLogParseException(message);
            }
            change.setSql(sql);
        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        }
        change.setSplitStatements(false);
        change.setStripComments(false);

        ChangeSetServiceFactory factory = ChangeSetServiceFactory.getInstance();
        ChangeSetService service = factory.createChangeSetService();
        ChangeSet changeSet =
           service.createChangeSet("raw", "includeAll",
                false, false, physicalChangeLogLocation, null,
                  null, null, null, true,
                         ObjectQuotingStrategy.LEGACY, changeLog);
        changeSet.addChange(change);

        changeLog.addChangeSet(changeSet);

        return changeLog;
    }
}
