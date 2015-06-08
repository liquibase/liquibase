package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.InsertDataAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.InsertStatement;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;

public class InsertDataLogic extends AbstractSqlBuilderLogic<InsertDataAction> {

    @Override
    protected Class<InsertDataAction> getSupportedAction() {
        return InsertDataAction.class;
    }

    @Override
    public ValidationErrors validate(InsertDataAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope)
                .checkForRequiredField("tableName", action)
                .checkForRequiredField("columnNames", action)
                .checkForRequiredField("columnValues", action);

        if (CollectionUtil.createIfNull(action.columnNames).size() != CollectionUtil.createIfNull(action.columnValues).size()) {
            errors.addError("InsertData columnNames and columnValues must contain the same number of values");
        }

        return errors;
    }

    @Override
    protected StringClauses generateSql(InsertDataAction action, Scope scope) {
        final Database database = scope.getDatabase();
        return new StringClauses()
                .append("INSERT INTO")
                .append(database.escapeObjectName(action.tableName, Table.class))
                .append("("+ StringUtils.join(CollectionUtil.createIfNull(action.columnNames), ", ", new StringUtils.ObjectNameFormatter(Column.class, database))+")")
                .append("VALUES")
        .append("("+StringUtils.join(CollectionUtil.createIfNull(action.columnNames), ", ", new StringUtils.StringUtilsFormatter() {
            @Override
            public String toString(Object obj) {
                if (obj == null || obj.toString().equalsIgnoreCase("NULL")) {
                    return "NULL";
                } else if (obj instanceof String && !database.looksLikeFunctionCall(((String) obj))) {
                    return DataTypeFactory.getInstance().fromObject(obj, database).objectToSql(obj, database);
                } else if (obj instanceof Date) {
                    return database.getDateLiteral(((Date) obj));
                } else if (obj instanceof Boolean) {
                    if (((Boolean) obj)) {
                        return DataTypeFactory.getInstance().getTrueBooleanValue(database);
                    } else {
                        return DataTypeFactory.getInstance().getFalseBooleanValue(database);
                    }
                } else if (obj instanceof DatabaseFunction) {
                    return database.generateDatabaseFunctionValue((DatabaseFunction) obj);
                } else {
                    return obj.toString();
                }
            }
        })+")");

    }
}
