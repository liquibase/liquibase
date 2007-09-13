package liquibase.dbdoc;

import liquibase.change.Change;
import liquibase.migrator.Migrator;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.JDBCException;
import liquibase.database.Database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PendingChangesWriter extends HTMLWriter {

    public PendingChangesWriter(File rootOutputDir) {
        super(new File(rootOutputDir, "pending"));
    }

    protected String createTitle(Object object) {
        return "Pending Changes";
    }

    protected void writeBody(FileWriter fileWriter, Object object, List<Change> ranChanges, List<Change> changesToRun, Migrator migrator) throws IOException, DatabaseHistoryException, JDBCException {
        writeCustomHTML(fileWriter, object, ranChanges, migrator.getDatabase());
        writeChanges("Pending Changes", fileWriter, object, changesToRun, migrator);
    }

    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}