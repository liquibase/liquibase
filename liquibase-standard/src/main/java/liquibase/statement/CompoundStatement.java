package liquibase.statement;

/**
 * Marker interface to indicate that a {@link SqlStatement} can generate different {@link liquibase.sql.Sql}
 * which should be specifically executed
 */
public interface CompoundStatement extends SqlStatement {
}
