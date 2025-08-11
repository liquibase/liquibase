package liquibase.logging.mdc.customobjects;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.mdc.CustomMdcObject;
import liquibase.util.LiquibaseUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.PrintWriter;
import java.io.StringWriter;

@Getter
@Setter
public class ExceptionDetails implements CustomMdcObject {
    private String primaryException;
    private String primaryExceptionReason;
    private String primaryExceptionSource;
    private String exception;

    public ExceptionDetails() {
    }

    public ExceptionDetails(Throwable exception, String source) {
        //
        // Drill down to get the lowest level exception
        //
        Throwable primaryException = exception;
        while (primaryException != null && primaryException.getCause() != null) {
            primaryException = primaryException.getCause();
        }
        if (primaryException != null) {
            if (primaryException instanceof LiquibaseException || source == null) {
                source = LiquibaseUtil.getBuildVersionInfo();
            }
            this.primaryException = primaryException.getClass().getSimpleName();
            this.primaryExceptionReason = primaryException.getMessage();
            this.primaryExceptionSource = source;
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            exception.printStackTrace(printWriter);
            this.exception = stringWriter.toString();
        }
    }

    public String getFormattedPrimaryException() {
        return getPrimaryException() != null
                ? String.format("ERROR: Exception Primary Class:  %s", getPrimaryException())
                : "";
    }

    public String getFormattedPrimaryExceptionReason() {
        return getPrimaryExceptionReason() != null
                ? String.format("ERROR: Exception Primary Reason:  %s", getPrimaryExceptionReason())
                : "";
    }

    public String getFormattedPrimaryExceptionSource() {
        return getPrimaryExceptionSource() != null
                ? String.format("ERROR: Exception Primary Source:  %s", getPrimaryExceptionSource())
                : "";
    }

    public static String findSource(Database database) {
        try {
            String source;
            try {
                source = String.format("%s %s", database.getDatabaseProductName(), database.getDatabaseProductVersion());
            } catch (DatabaseException dbe) {
                source = database.getDatabaseProductName();
            }
            return source;
        } catch (RuntimeException ignored) {
            // For some reason we decided to have AbstractJdbcDatabase#getDatabaseProductName throw a runtime exception.
            // In this case since we always want to fall back to some sort of identifier for the database
            // we can just ignore and return the display name.
            return database.getDisplayName();
        }
    }
}
