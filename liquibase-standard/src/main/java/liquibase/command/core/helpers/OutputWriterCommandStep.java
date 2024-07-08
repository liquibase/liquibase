package liquibase.command.core.helpers;

import liquibase.database.Database;

import java.io.Writer;

/**
 * This helper class can be run prior to any command (but usually the *-sql commands, like update-sql) to redirect
 * the SQL to the console, rather than running it against an actual database. This has a dependency on {@link Database}.
 * If you need a writer and are operating against the reference database, use {@link ReferenceDatabaseOutputWriterCommandStep}
 * instead.
 */
public class OutputWriterCommandStep extends AbstractOutputWriterCommandStep {
    @Override
    public Class<?> getProvidedWriterDependency() {
        return Writer.class;
    }

    @Override
    public Class<?> getDatabaseDependency() {
        return Database.class;
    }
}
