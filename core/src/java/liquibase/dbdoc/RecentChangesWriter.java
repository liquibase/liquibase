package liquibase.dbdoc;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.JDBCException;
import liquibase.migrator.Migrator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class RecentChangesWriter extends HTMLWriter {

    public RecentChangesWriter(File rootOutputDir) {
        super(new File(rootOutputDir, "recent"));
    }

    protected String createTitle(Object object) {
        return "Recent Changes";
    }

    protected void writeBody(FileWriter fileWriter, Object object, List<Change> ranChanges, List<Change> changesToRun, Migrator migrator) throws IOException, DatabaseHistoryException, JDBCException {
        writeCustomHTML(fileWriter, object, ranChanges, migrator.getDatabase());
        writeChanges("Most Recent Changes", fileWriter, object, ranChanges, migrator);
    }

    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}
