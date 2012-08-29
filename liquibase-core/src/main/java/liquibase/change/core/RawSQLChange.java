package liquibase.change.core;

import liquibase.change.AbstractSQLChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;

/**
 * Allows execution of arbitrary SQL.  This change can be used when existing changes are either don't exist,
 * are not flexible enough, or buggy. 
 */
@DatabaseChange(name="sql", description = "Custom SQL", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class RawSQLChange extends AbstractSQLChange {

    private String comments;
    
    public RawSQLChange() {
    }

    public RawSQLChange(String sql) {
        setSql(sql);
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getConfirmationMessage() {
        return "Custom SQL executed";
    }
}
