package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.CreateIndexAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Index;
import liquibase.util.StringUtils;

import java.util.List;

public class CreateIndexLogic extends AbstractActionLogic {

    public static enum Clauses {
        indexName,
        columns,
        tableName,
        tablespace,
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return CreateIndexAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField(CreateIndexAction.Attr.tableName, action);
        validationErrors.checkForRequiredField(CreateIndexAction.Attr.columnDefinitions, action);

        if (!database.supportsClustered(Index.class)) {
            if (action.get(CreateIndexAction.Attr.clustered, false)) {
                validationErrors.addWarning("Creating clustered index not supported with "+database);
            }
        }

        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new RewriteResult(new ExecuteSqlAction(getCreateIndexClauses(action, scope).toString()));
    }

    public StringClauses getCreateIndexClauses(Action action, Scope scope) {
        final Database database = scope.get(Scope.Attr.database, Database.class);
        final String tableCatalogName = action.get(CreateIndexAction.Attr.tableCatalogName, String.class);
        final String tableSchemaName = action.get(CreateIndexAction.Attr.tableSchemaName, String.class);
        final String tableName = action.get(CreateIndexAction.Attr.tableName, String.class);
        String indexName = action.get(CreateIndexAction.Attr.indexName, String.class);
        String tablespace = action.get(CreateIndexAction.Attr.tablespace, String.class);


        StringClauses clauses = new StringClauses().append("CREATE");

        if (action.get(CreateIndexAction.Attr.unique, false)) {
		    clauses.append("UNIQUE ");
	    }

        clauses.append("INDEX ");

	    if (indexName != null) {
            clauses.append(Clauses.indexName, database.escapeIndexName(tableCatalogName, tableSchemaName, indexName));
	    }

        clauses.append("ON");

	    clauses.append(Clauses.tableName, database.escapeTableName(tableCatalogName, tableSchemaName, tableName));

        clauses.append(Clauses.columns, "("+ StringUtils.join(action.get(CreateIndexAction.Attr.columnDefinitions, List.class), ", ", new StringUtils.StringUtilsFormatter<ColumnDefinition>() {
            @Override
            public String toString(ColumnDefinition column) {
                Boolean computed = column.get(ColumnDefinition.Attr.computed, Boolean.class);
                if (computed == null) {
                    return database.escapeColumnName(tableCatalogName, tableSchemaName, tableName, column.get(ColumnDefinition.Attr.columnName, String.class), true);
                } else if (computed) {
                    return column.get(ColumnDefinition.Attr.columnName, String.class);
                } else {
                    return database.escapeColumnName(tableCatalogName, tableSchemaName, tableName, column.get(ColumnDefinition.Attr.columnName, String.class), false);
                }
            }
            })+")");


        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append("TABLESPACE "+tablespace);
        }

        return clauses;
    }
}
