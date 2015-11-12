package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateIndexesAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateIndexesLogic extends AbstractActionLogic<CreateIndexesAction> {

    public static enum Clauses {
        indexName,
        columns,
        tableName,
        tablespace,
    }

    @Override
    protected Class<CreateIndexesAction> getSupportedAction() {
        return CreateIndexesAction.class;
    }

    @Override
    public ValidationErrors validate(CreateIndexesAction action, Scope scope) {
        Database database = scope.getDatabase();

        ValidationErrors validationErrors = new ValidationErrors();
        for (Index index : action.indexes) {
            validationErrors.checkForRequiredField("name", index);
            validationErrors.checkForRequiredField("columns", index);

            if (!database.supportsClustered(Index.class)) {
                if (ObjectUtil.defaultIfEmpty(index.clustered, false)) {
                    validationErrors.addWarning("Creating clustered index not supported with " + database);
                }
            }
        }

        return validationErrors;
    }

    @Override
    public ActionResult execute(CreateIndexesAction action, Scope scope) throws ActionPerformException {

        List<Action> actions = new ArrayList<>();

        for (Index index : action.indexes) {
            actions.addAll(Arrays.asList(execute(index, action, scope)));
        }

        return new DelegateResult(actions.toArray(new Action[actions.size()]));
    }

    protected Action execute(Index index, CreateIndexesAction action, Scope scope) {
        return new ExecuteSqlAction(generateSql(index, action, scope).toString());
    }

    protected StringClauses generateSql(Index index, CreateIndexesAction action, Scope scope) {
        final Database database = scope.getDatabase();
        ObjectReference indexName = index.toReference();
        ObjectReference tableName = index.columns.get(0).container;
        String tablespace = index.tablespace;


        StringClauses clauses = new StringClauses().append("CREATE");

        if (ObjectUtil.defaultIfEmpty(index.unique, false)) {
            clauses.append("UNIQUE ");
        }

        clauses.append("INDEX ");

        if (indexName != null) {
            clauses.append(Clauses.indexName, database.escapeObjectName(indexName));
        }

        clauses.append("ON");

        clauses.append(Clauses.tableName, database.escapeObjectName(tableName));

        clauses.append(Clauses.columns, "(" + StringUtils.join(index.columns, ", ", new StringUtils.StringUtilsFormatter<Index.IndexedColumn>() {
            @Override
            public String toString(Index.IndexedColumn column) {
                Boolean computed = column.computed;
                String name;
                if (computed == null) {
                    name = database.escapeObjectName(column.name, Column.class);
                } else if (computed) {
                    name = column.name;
                } else {
                    name = database.escapeObjectName(column.name, Column.class);
                }

                if (ObjectUtil.defaultIfEmpty(column.descending, false)) {
                    name += " DESC";
                }
                return name;
            }
        }) + ")");


        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append("TABLESPACE " + tablespace);
        }

        return clauses;
    }
}
