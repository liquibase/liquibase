package liquibase.parser.core.formattedsql;

import liquibase.change.Change;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.sql.SqlChangeLogParser;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedSqlChangeLogParser implements ChangeLogParser {

    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        BufferedReader reader = null;
        try {
            if (changeLogFile.endsWith(".sql")) {
                reader = new BufferedReader(new InputStreamReader(openChangeLogFile(changeLogFile, resourceAccessor)));

                return reader.readLine().startsWith("--liquibase formatted");
            } else {
                return false;
            }
        } catch (IOException e) {
            LogFactory.getLogger().debug("Exception reading " + changeLogFile, e);
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LogFactory.getLogger().debug("Exception closing " + changeLogFile, e);
                }
            }
        }
    }

    public int getPriority() {
        return PRIORITY_DEFAULT + 5;
    }

    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {

        DatabaseChangeLog changeLog = new DatabaseChangeLog(physicalChangeLogLocation);

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(openChangeLogFile(physicalChangeLogLocation, resourceAccessor)));
            StringBuffer currentSql = new StringBuffer();

            ChangeSet changeSet = null;
            Pattern pattern = Pattern.compile("--changeset (\\w+):(\\w+)", Pattern.CASE_INSENSITIVE);
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String finalCurrentSql = StringUtils.trimToNull(currentSql.toString());
                    if (changeSet != null) {
                        changeSet.addChange(createChange(finalCurrentSql, changeSet, resourceAccessor));
                        changeLog.addChangeSet(changeSet);
                    }

                    changeSet = new ChangeSet(matcher.group(2), matcher.group(1), false, false, physicalChangeLogLocation, physicalChangeLogLocation, null, null, true);
                    currentSql = new StringBuffer();
                } else {
                    if (changeSet != null) {
                        currentSql.append(line).append("\n");
                    }
                }
            }

            if (changeSet != null) {
                changeSet.addChange(createChange(StringUtils.trimToNull(currentSql.toString()), changeSet, resourceAccessor));
                changeLog.addChangeSet(changeSet);
            }

        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        }

        return changeLog;
    }

    private Change createChange(String finalCurrentSql, ChangeSet changeSet, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        if (finalCurrentSql == null) {
            throw new ChangeLogParseException("No SQL for changeset " + changeSet.toString(false));
        }
        RawSQLChange change = new RawSQLChange();
        change.setSql(finalCurrentSql);
        change.setFileOpener(resourceAccessor);
        change.setSplitStatements(true);
        change.setStripComments(true);

        return change;
    }

    protected InputStream openChangeLogFile(String physicalChangeLogLocation, ResourceAccessor resourceAccessor) throws IOException {
        return resourceAccessor.getResourceAsStream(physicalChangeLogLocation);
    }
}