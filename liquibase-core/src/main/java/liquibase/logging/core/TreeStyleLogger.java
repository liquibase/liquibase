package liquibase.logging.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogLevel;

import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * A new Logger for (hopefully) better readable output during changeset execution
 */
public class TreeStyleLogger extends DefaultLogger {

    private String name = "liquibase";
    private PrintStream stderr = System.err;
    private PrintStream stdout = System.out;

    LocalTime lastLoggingTs;
    String lastChangeLogPath;
    String currentChangeLogPath;
    String lastChangeSetName;
    String currentChangeSetName;

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public void setChangeLog(DatabaseChangeLog databaseChangeLog) {
        if (databaseChangeLog != null)
            currentChangeLogPath = databaseChangeLog.getFilePath();
    }

    @Override
    public void setChangeSet(ChangeSet changeSet) {
        if (changeSet != null) {
            currentChangeLogPath = changeSet.toString().split("::")[0];
            currentChangeSetName = changeSet.toString().replace(currentChangeLogPath + "::", "");
        }
    }

    @Override
    protected void print(LogLevel logLevel, String message) throws UnexpectedLiquibaseException {
        final String BLANK_TIME      = "        ";
        final String BLANK_CHANGELOG = "|  ";
        final String BLANK_CHANGESET = "|     ";

        PrintStream out = getOutStream(logLevel);

        // Print the time if the second has changed compared to the previous call
        LocalTime now = LocalTime.now();
        now = now.truncatedTo(ChronoUnit.SECONDS);
        String timeString = now.format(DateTimeFormatter.ISO_LOCAL_TIME);
        if (lastLoggingTs == null || (lastLoggingTs.compareTo(now) != 0))
            out.print(timeString);
        else
            out.print(BLANK_TIME);
        lastLoggingTs = now;
        out.print(" ");

        // Print the DatabaseChangeLog name if it has changed compared to the previous call
        if (currentChangeLogPath != null) {
            if (lastChangeLogPath == null || (lastChangeLogPath.compareTo(currentChangeLogPath) != 0)) {
                out.print(String.format("|- %s%n%s", currentChangeLogPath, BLANK_TIME + " " + BLANK_CHANGELOG));
            } else {
                out.print(BLANK_CHANGELOG);
            }
        }
        lastChangeLogPath = currentChangeLogPath;

        // Print the ChangeSet name if it has changed compared to the previous call
        if (currentChangeSetName != null) {
            out.print(" ");
            if (lastChangeSetName == null || (lastChangeSetName.compareTo(currentChangeSetName)  != 0)) {
                out.print(String.format("|- %s%n%s", currentChangeSetName, BLANK_TIME + " " + BLANK_CHANGELOG + " " + BLANK_CHANGESET));
            } else {
                out.print(BLANK_CHANGESET);
            }
        }
        lastChangeSetName = currentChangeSetName;

        // The message string might be split into multiple lines. Indent each line.
        String[] messages = message.split("[\\r?\\n?]");
        boolean firstLine = true;
        for (String theMsg: messages) {
            if (!firstLine) {
                out.print(BLANK_TIME + " "
                        + (currentChangeLogPath != null ? BLANK_CHANGELOG : "")
                        + (currentChangeSetName != null ? " " + BLANK_CHANGESET : "")
                );
            }
            if (logLevel != LogLevel.INFO)
                out.print(String.format("[%s] ",  logLevel.toString()));
            out.println(theMsg);
            firstLine = false;
        }
    }

    /**
     * Gets stdout or stderr, depending on the logLevel
     * @param logLevel the logLevel to examine
     * @return stdout if logLevel is DEBUG or INFO, or stderr if it is WARN or SEVERE
     */
    protected PrintStream getOutStream(LogLevel logLevel) {
        PrintStream out;
        switch (logLevel) {
            case DEBUG:
            case INFO:
                out = stdout;
                break;
            case SEVERE:
            case WARNING:
                out = stderr;
                break;
            default:
                throw new UnexpectedLiquibaseException("Encountered an unknown log level: " + logLevel.toString());
        }
        return out;
    }
}
