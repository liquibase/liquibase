package liquibase.command.core.helpers;

import liquibase.command.providers.ReferenceDatabase;

/**
 * This helper class can be run prior to any command (but usually the *-sql commands, like update-sql) to redirect
 * the SQL to the console, rather than running it against an actual database. This has a dependency on
 * {@link ReferenceDatabaseOutputWriterCommandStep}. If you need a writer and are operating against the database, use
 * {@link OutputWriterCommandStep}
 * instead.
 */
public class ReferenceDatabaseOutputWriterCommandStep extends AbstractOutputWriterCommandStep {
    @Override
    public Class<?> getProvidedWriterDependency() {
        return ReferenceDatabaseWriter.class;
    }

    @Override
    public Class<?> getDatabaseDependency() {
        return ReferenceDatabase.class;
    }

    /**
     * The provided dependency. This has to be a different class than {@link java.io.Writer} because {@link java.io.Writer}
     * is used in the {@link OutputWriterCommandStep} and it is not permitted to provide the same dependency
     * class in two different places.
     */
    public static class ReferenceDatabaseWriter {

    }
}
