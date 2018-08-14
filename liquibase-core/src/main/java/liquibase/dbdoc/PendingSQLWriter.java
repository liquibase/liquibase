package liquibase.dbdoc;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.MigrationFailedException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class PendingSQLWriter extends HTMLWriter {

    private DatabaseChangeLog databaseChangeLog;

    public PendingSQLWriter(File rootOutputDir, Database database, DatabaseChangeLog databaseChangeLog) {
        super(new File(rootOutputDir, "pending"), database);
        this.databaseChangeLog = databaseChangeLog;
    }

    @Override
    protected String createTitle(Object object) {
        return "Pending SQL";
    }

    @Override
    protected void writeBody(Writer fileWriter, Object object, List<Change> ranChanges, List<Change> changesToRun) throws IOException, DatabaseHistoryException, DatabaseException {

        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), fileWriter, database);
        ExecutorService.getInstance().setExecutor(database, loggingExecutor);

        try {
            if (changesToRun.isEmpty()) {
                fileWriter.append("<b>NONE</b>");
            }

            fileWriter.append("<code><pre>");

            ChangeSet lastRunChangeSet = null;

            for (Change change : changesToRun) {
                ChangeSet thisChangeSet = change.getChangeSet();
                if (thisChangeSet.equals(lastRunChangeSet)) {
                    continue;
                }
                lastRunChangeSet = thisChangeSet;
                String anchor = thisChangeSet.toString(false).replaceAll("\\W","_");
                fileWriter.append("<a name='").append(anchor).append("'/>");
                try {
                    thisChangeSet.execute(databaseChangeLog, null, this.database);
                } catch (MigrationFailedException e) {
                    fileWriter.append("EXECUTION ERROR: ").append(Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(change).getDescription()).append(": ").append(e.getMessage()).append("\n\n");
                }
            }
            fileWriter.append("</pre></code>");
        } finally {
            ExecutorService.getInstance().setExecutor(database, oldTemplate);
        }
    }

    @Override
    protected void writeCustomHTML(Writer fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}
