package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.action.QueryAction;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Action to execute a method from java.sql.DatabaseMetaData.
 * The Logic implementation that handles this method should simply execute the method with the given arguments exactly with no additional processing.
 * Any case-fixing etc. logic should occur in less "raw" Actions.
 */
public class QueryJdbcMetaDataAction extends AbstractAction implements QueryAction {
    public String method;
    public List<Object> arguments;

    public QueryJdbcMetaDataAction(String method, Object... arguments) {
        this.method = method;
        this.arguments = Arrays.asList(CollectionUtil.createIfNull(arguments));
    }

    @Override
    public String describe() {
        return method + "(" + StringUtils.join(arguments, ", ", new StringUtils.DefaultFormatter()) + ")";
    }
}
