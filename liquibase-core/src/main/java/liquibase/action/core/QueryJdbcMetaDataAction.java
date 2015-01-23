package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.action.QueryAction;
import liquibase.util.StringUtils;

/**
 * Action to execute a method from java.sql.DatabaseMetaData.
 * The Logic implementation that handles this method should simply execute the method with the given arguments exactly with no additional processing.
 * Any case-fixing etc. logic should occur in less "raw" Actions.
 */
public class QueryJdbcMetaDataAction extends AbstractAction implements QueryAction {
    public static enum Attr {
        method,
        arguments
    }

    public QueryJdbcMetaDataAction(String method, Object... arguments) {
        set(Attr.method, method);
        set(Attr.arguments, arguments);
    }

    @Override
    public String describe() {
        return get(Attr.method, String.class)+"("+ StringUtils.join(get(Attr.arguments, Object[].class), ", ", new StringUtils.DefaultFormatter())+")";
    }
}
