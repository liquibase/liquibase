package liquibase.integration.commandline;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TreeStyleLayout extends LayoutBase<ILoggingEvent> {

    private static final String BLANK_TIME = "        ";
    private static final String BLANK_CHANGELOG = "|  ";
    private static final String BLANK_CHANGESET = "|     ";
    protected LocalTime lastLoggingTs;
    protected String lastChangeLogPath;
    protected String lastChangeSetName;

    @Override
    public String doLayout(ILoggingEvent event) {
        StringBuilder formattedMessage = new StringBuilder();

        // Print the time if the second has changed compared to the previous call
        printTimeIfChanged(formattedMessage);

        // Print the DatabaseChangeLog name if it has changed compared to the previous call
        String currentChangeLogPath = event.getMDCPropertyMap().getOrDefault("databaseChangeLog", null);
        String currentChangeSetName = event.getMDCPropertyMap().getOrDefault("changeSet", null);

        // Print the name of the database changelog, if it has changed since we were last called
        printDatabaseChangeLogIfChanged(formattedMessage, currentChangeLogPath);
        lastChangeLogPath = currentChangeLogPath;

        // Print the ChangeSet name if it has changed compared to the previous call
        printDatabaseChangeSetIfChanged(formattedMessage, currentChangeSetName);
        lastChangeSetName = currentChangeSetName;

        // The message string might be split into multiple lines. Indent each line.
        String message = event.getFormattedMessage();
        Level logLevel = event.getLevel();

        final String REGEXP_LINE_ENDINGS = "[\\r?\\n?]";
        String[] messages = message.split(REGEXP_LINE_ENDINGS);
        boolean firstLine = true;
        for (String theMsg : messages) {
            if (!firstLine) {
                formattedMessage.append(BLANK_TIME + " "
                    + (currentChangeLogPath != null ? BLANK_CHANGELOG : "")
                    + (currentChangeSetName != null ? (" " + BLANK_CHANGESET) : "")
                );
            }

            // If the message is anything but expected (=INFO), print the log level.
            if (logLevel != Level.INFO) {
                formattedMessage.append(String.format("[%s] ", logLevel.toString()));
            }

            formattedMessage.append(theMsg);
            firstLine = false;
        }
        formattedMessage.append(CoreConstants.LINE_SEPARATOR);
        return formattedMessage.toString();
    }

    /**
     * Print the name of the database changeset, if it has changed since we were last called
     *
     * @param formattedMessage     the message generated so far
     * @param currentChangeSetName name of the current database changeset
     */
    private void printDatabaseChangeSetIfChanged(StringBuilder formattedMessage, String currentChangeSetName) {
        if (currentChangeSetName != null) {
            formattedMessage.append(" ");
            if ((lastChangeSetName == null) || (lastChangeSetName.compareTo(currentChangeSetName) != 0)) {
                formattedMessage.append(String.format("|- %s%n%s", currentChangeSetName, BLANK_TIME + " "
                    + BLANK_CHANGELOG + " " + BLANK_CHANGESET));
            } else {
                formattedMessage.append(BLANK_CHANGESET);
            }
        }
    }

    /**
     * Print the name of the database changelog, if it has changed since we were last called
     *
     * @param formattedMessage     the message generated so far
     * @param currentChangeLogPath name of the current database changelog
     */
    private void printDatabaseChangeLogIfChanged(StringBuilder formattedMessage, String currentChangeLogPath) {
        if (currentChangeLogPath != null) {
            if ((lastChangeLogPath == null) || (lastChangeLogPath.compareTo(currentChangeLogPath) != 0)) {
                formattedMessage.append(
                    String.format("|- %s%n%s", currentChangeLogPath, BLANK_TIME + " " + BLANK_CHANGELOG));
            } else {
                formattedMessage.append(BLANK_CHANGELOG);
            }
        }
    }

    /**
     * Print the time if the second has changed compared to the previous call
     *
     * @param formattedMessage the message generated so far
     */
    private void printTimeIfChanged(StringBuilder formattedMessage) {
        LocalTime now = LocalTime.now();
        now = now.truncatedTo(ChronoUnit.SECONDS);
        String timeString = now.format(DateTimeFormatter.ISO_LOCAL_TIME);
        if ((lastLoggingTs == null) || (lastLoggingTs.compareTo(now) != 0)) {
            formattedMessage.append(timeString);
        } else {
            formattedMessage.append(BLANK_TIME);
        }
        lastLoggingTs = now;
        formattedMessage.append(" ");
    }
}
