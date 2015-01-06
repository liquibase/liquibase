package liquibase.action;

import liquibase.actionlogic.QueryResult;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.util.*;

/**
 * Describes an {@link liquibase.action.Action} that fetches data from a database or any other location.
 * The logic for the query goes in an implementation of {@link liquibase.actionlogic.ActionLogic}
 */
public interface QueryAction extends Action {

}
