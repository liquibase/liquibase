package liquibase.sql.visitor;

import liquibase.changelog.ChangeLogParameters;
import liquibase.database.Database;
import lombok.Getter;

/** Visitor making parameter expansion. It maintains a separate set of parameters for better performance
 *  and to avoid applying all changelog parameters, that might not be intended. Although that could be an option.
 *  It's a singleton created on the first {@code get} call
 */
public class InjectRuntimeVariablesVisitor extends AbstractSqlVisitor {
    public final ChangeLogParameters params = new ChangeLogParameters();
	 private InjectRuntimeVariablesVisitor(){}
    @Getter
    protected static InjectRuntimeVariablesVisitor instance;

    public static InjectRuntimeVariablesVisitor get() {
        if(null == instance) {
             instance = new InjectRuntimeVariablesVisitor();
        }
        return instance;
    }

    public String expandExpressions(String s) {
        return params.expandExpressions(s, null);
    }

    @Override
    public String modifySql(String sql, Database database) {
        return expandExpressions(sql);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
