package liquibase.command.core;

import liquibase.command.CommandResult;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.util.StringUtils;
import liquibase.logging.LogService;
import liquibase.logging.LogType;

import java.io.PrintStream;

public class GenerateChangeLogCommand extends DiffToChangeLogCommand {
    private static final String INFO_MESSAGE = 
        "When generating formatted SQL changelogs, it is important to decide if batched statements\n" +
        "should be split (splitStatements:true is the default behavior) or not (splitStatements:false).\n" +
        "See http://liquibase.org for additional documentation.";

    private String author;
    private String context;

    @Override
    public String getName() {
        return "generateChangeLog";
    }

    public String getAuthor() {
        return author;
    }

    public GenerateChangeLogCommand setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getContext() {
        return context;
    }

    public GenerateChangeLogCommand setContext(String context) {
        this.context = context;
        return this;
    }

    @Override
    protected CommandResult run() throws Exception {
        outputBestPracticeMessage();

        String changeLogFile = StringUtils.trimToNull(getChangeLogFile());
        if (changeLogFile.toLowerCase().endsWith(".sql")) {
          System.out.println("\n" + INFO_MESSAGE + "\n");
          LogService.getLog(getClass()).info(LogType.LOG, "\n" + INFO_MESSAGE + "\n");
        }
 
        SnapshotCommand.logUnsupportedDatabase(this.getReferenceDatabase(), this.getClass());

        DiffResult diffResult = createDiffResult();

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, getDiffOutputControl());

        changeLogWriter.setChangeSetAuthor(author);
        changeLogWriter.setChangeSetContext(context);
        changeLogWriter.setChangeSetPath(getChangeLogFile());

        ObjectQuotingStrategy originalStrategy = getReferenceDatabase().getObjectQuotingStrategy();
        try {
            getReferenceDatabase().setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            if (StringUtils.trimToNull(getChangeLogFile()) != null) {
                changeLogWriter.print(getChangeLogFile());
            } else {
                PrintStream outputStream = getOutputStream();
                if (outputStream == null) {
                    outputStream = System.out;
                }
                changeLogWriter.print(outputStream);
            }
        }
        finally {
            getReferenceDatabase().setObjectQuotingStrategy(originalStrategy);
        }

        return new CommandResult("OK");

    }

    @Override
    protected DatabaseSnapshot createTargetSnapshot() throws DatabaseException, InvalidExampleException {
        SnapshotControl snapshotControl = new SnapshotControl(getReferenceDatabase(), getSnapshotTypes());
        return SnapshotGeneratorFactory.getInstance().createSnapshot(getCompareControl().getSchemas(CompareControl.DatabaseRole.REFERENCE), null, snapshotControl);
    }
}
