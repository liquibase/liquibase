package liquibase.dbdoc;

import liquibase.ChangeSet;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.MigrationFailedException;
import liquibase.Liquibase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PendingSQLWriter extends HTMLWriter {

    public PendingSQLWriter(File rootOutputDir, Database database) {
        super(new File(rootOutputDir, "pending"), database);
    }

    protected String createTitle(Object object) {
        return "Pending SQL";
    }

    protected void writeBody(FileWriter fileWriter, Object object, List<Change> ranChanges, List<Change> changesToRun, Liquibase liquibase) throws IOException{
        if (changesToRun.size() == 0) {
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
                thisChangeSet.execute(liquibase.getDatabase());
            } catch (MigrationFailedException e) {
                fileWriter.append("EXECUTION ERROR: ").append(change.getChangeMetaData().getDescription()).append(": ").append(e.getMessage()).append("\n\n");
            }
        }
        fileWriter.append("</pre></code>");
    }

    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}
