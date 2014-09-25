package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

/**
 * Queries database for the metadata about a defined object.
 */
public class SelectMetaDataStatement extends AbstractStatement {

    public static final String EXAMPLE = "example";

    public SelectMetaDataStatement() {
    }

    public SelectMetaDataStatement(DatabaseObject example) {
        setExample(example);
    }

    public DatabaseObject getExample() {
        return getAttribute(EXAMPLE, DatabaseObject.class);
    }

    public SelectMetaDataStatement setExample(DatabaseObject example) {
        return (SelectMetaDataStatement) setAttribute(EXAMPLE, example);
    }

    @Override
    public String toString() {
        DatabaseObject example = getExample();
        return "Select "+ example.getClass().getSimpleName()+"(s) like '"+example.toString()+"' in '"+example.getSchema()+"'";
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
