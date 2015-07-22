package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateIndexAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

public class CreateIndexLogic extends AbstractSqlBuilderLogic<CreateIndexAction> {

    public static enum Clauses {
        indexName,
        columns,
        tableName,
        tablespace,
    }

    @Override
    protected Class<CreateIndexAction> getSupportedAction() {
        return CreateIndexAction.class;
    }

    @Override
    public ValidationErrors validate(CreateIndexAction action, Scope scope) {
        Database database = scope.getDatabase();

        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField("tableName", action);
        validationErrors.checkForRequiredField("columns", action);

        if (!database.supportsClustered(Index.class)) {
            if (ObjectUtil.defaultIfEmpty(action.clustered, false)) {
                validationErrors.addWarning("Creating clustered index not supported with "+database);
            }
        }

        return validationErrors;
    }

    @Override
    public ActionResult execute(CreateIndexAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new ExecuteSqlAction(generateSql(action, scope).toString()));
    }

    @Override
    protected StringClauses generateSql(CreateIndexAction action, Scope scope) {
        final Database database = scope.getDatabase();
        ObjectName indexName = action.indexName;
        ObjectName tableName = action.tableName;
        String tablespace = action.tablespace;


        StringClauses clauses = new StringClauses().append("CREATE");

        if (ObjectUtil.defaultIfEmpty(action.unique, false)) {
		    clauses.append("UNIQUE ");
	    }

        clauses.append("INDEX ");

	    if (indexName != null) {
            clauses.append(Clauses.indexName, database.escapeObjectName(indexName, Index.class));
	    }

        clauses.append("ON");

	    clauses.append(Clauses.tableName, database.escapeObjectName(tableName, Table.class));

        clauses.append(Clauses.columns, "("+ StringUtils.join(action.columns, ", ", new StringUtils.StringUtilsFormatter<Index.IndexedColumn>() {
            @Override
            public String toString(Index.IndexedColumn column) {
                Boolean computed = column.computed;
                String name;
                if (computed == null) {
                    name = database.escapeObjectName(column.getSimpleName(), Column.class);
                } else if (computed) {
                    name = column.getSimpleName();
                } else {
                    name = database.escapeObjectName(column.getSimpleName(), Column.class);
                }

                if (ObjectUtil.defaultIfEmpty(column.descending, false)) {
                    name += " DESC";
                }
                return name;
            }
            })+")");


        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append("TABLESPACE "+tablespace);
        }

        return clauses;
    }
}
