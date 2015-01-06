package liquibase.action;

/**
 * Describes a SQL-based action that updates data.
 */
public class UpdateSqlAction extends AbstractSqlAction implements UpdateAction {

    public UpdateSqlAction(String sql) {
        super(sql);
    }
}
