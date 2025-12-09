package liquibase.sql.visitor;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import lombok.Getter;

import java.util.List;

/** Visitor making parameter expansion. It maintains a separate set of parameters for better performance
 *  and to avoid applying all changelog parameters, that might not be intended.
 * Although that could be an option.
 * It's a singleton gets created on the first {@code get} call
 *
 * Alternative solution:
 *  ChangeLogParameters could store these runtime variables also + use existsing ReplaceSqlVisitor
 */
public class InjectRuntimeVariablesVisitor extends AbstractSqlVisitor {
    ChangeLogParameters params = new ChangeLogParameters();
    public ChangeLogParameters params() {return params;}

    protected DatabaseChangeLog changeLog;
	 private InjectRuntimeVariablesVisitor(){}
    @Getter
    protected static InjectRuntimeVariablesVisitor instance;

    public static InjectRuntimeVariablesVisitor get() {
        if(null == instance) {
             instance = new InjectRuntimeVariablesVisitor();
        }
        return instance;
    }

    public String expandExpressions(String s, DatabaseChangeLog changeLog) {
        return params.expandExpressions(s, changeLog);
    }

    @Override
    public String modifySql(String sql, Database database) {
        return expandExpressions(sql, changeLog);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /** If there are runtime properties, add the instance to {@code sqlVisitors} */
    public static List<SqlVisitor> addTo(List<SqlVisitor> sqlVisitors, DatabaseChangeLog changeLog) {
        InjectRuntimeVariablesVisitor v = getInstance();
        if (null != v && null != sqlVisitors && !sqlVisitors.contains(v)) {
            sqlVisitors.add(v);
            v.changeLog = changeLog;
        }
        return sqlVisitors;
    }

    /** If there are runtime properties, expandExpressions */
    public static String expand(String s, DatabaseChangeLog changeLog) {
        InjectRuntimeVariablesVisitor v = getInstance();
        if (null != v) {
            return v.expandExpressions(s, changeLog);
        }
        return s;
    }
}
