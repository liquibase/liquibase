package liquibase.parser.sql;

import liquibase.DatabaseChangeLog;
import liquibase.FileOpener;
import liquibase.ChangeSet;
import liquibase.parser.ChangeLogParser;
import liquibase.util.StreamUtil;
import liquibase.change.SQLFileChange;
import liquibase.change.RawSQLChange;
import liquibase.exception.ChangeLogParseException;

import java.util.Map;
import java.io.IOException;
import java.io.InputStream;

public class SqlChangeLogParser implements ChangeLogParser {
    public String[] getValidFileExtensions() {
        return new String [] {
                "sql"
        };
    }

    public DatabaseChangeLog parse(String physicalChangeLogLocation, Map<String, Object> changeLogParameters, FileOpener fileOpener) throws ChangeLogParseException {

        RawSQLChange change = new RawSQLChange();

        try {
            InputStream sqlStream = fileOpener.getResourceAsStream(physicalChangeLogLocation);
            String sql = StreamUtil.getStreamContents(sqlStream, null);
            change.setSql(sql);
        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        }
        change.setFileOpener(fileOpener);
        change.setSplitStatements(false);
        change.setStripComments(false);

        ChangeSet changeSet = new ChangeSet("raw", "includeAll", false, false, physicalChangeLogLocation, physicalChangeLogLocation, null, null, false);
        changeSet.addChange(change);

        DatabaseChangeLog changeLog = new DatabaseChangeLog(physicalChangeLogLocation);
        changeLog.addChangeSet(changeSet);

        return changeLog;
    }
}
