package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

/**
 * Tags the current state in the liquibase change log history for future rollback.
 */
public class TagDatabaseStatement extends AbstractStatement {

    public static final String TAG = "tag";

    public TagDatabaseStatement(String tag) {
        setTag(tag);
    }

    public String getTag() {
        return getAttribute(TAG, String.class);
    }

    public TagDatabaseStatement setTag(String tag) {
        return (TagDatabaseStatement) setAttribute(TAG, tag);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
