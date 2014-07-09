package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.statement.Statement;

/**
 * Convenience base class for Statements that work on a sequence.
 */
abstract class AbstractSequenceStatement extends AbstractStatement {

    public static final String CATALOG_NAME = "catalogName";
    public static final String SCHEMA_NAME = "schemaName";
    public static final String SEQUENCE_NAME = "sequenceName";

    protected AbstractSequenceStatement() {
    }

    public AbstractSequenceStatement(String catalogName, String schemaName, String sequenceName) {
        setCatalogName(catalogName);
        setSchemaName(schemaName);
        setSequenceName(sequenceName);
    }

    public String getCatalogName() {
        return getAttribute(CATALOG_NAME, String.class);
    }

    public Statement setCatalogName(String catalogName) {
        return (Statement) setAttribute(CATALOG_NAME, catalogName);
    }

    public String getSchemaName() {
        return getAttribute(SCHEMA_NAME, String.class);
    }

    public Statement setSchemaName(String schemaName) {
        return (Statement) setAttribute(SCHEMA_NAME, schemaName);
    }

    public String getSequenceName() {
        return getAttribute(SEQUENCE_NAME, String.class);
    }

    public Statement setSequenceName(String sequenceName) {
        return (Statement) setAttribute(SEQUENCE_NAME, sequenceName);
    }
}
