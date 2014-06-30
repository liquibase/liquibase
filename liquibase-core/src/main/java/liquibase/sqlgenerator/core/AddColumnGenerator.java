package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import liquibase.statement.core.AddUniqueConstraintStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddColumnGenerator extends AbstractSqlGenerator<AddColumnStatement> {

    @Override
    public ValidationErrors validate(AddColumnStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        Database database = env.getTargetDatabase();

        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("columnType", statement.getColumnType());
        validationErrors.checkRequiredField("tableName", statement.getTableName());

        if (statement.isPrimaryKey() && (database instanceof H2Database
                || database instanceof DB2Database
                || database instanceof DerbyDatabase
                || database instanceof SQLiteDatabase)) {
            validationErrors.addError("Cannot add a primary key column");
        }

        // TODO HsqlDatabase autoincrement on non primary key? other databases?
        if (database instanceof MySQLDatabase && statement.isAutoIncrement() && !statement.isPrimaryKey()) {
            validationErrors.addError("Cannot add a non-primary key identity column");
        }
        
        // TODO is this feature valid for other databases?
        if ((statement.getAddAfterColumn() != null) && !(database instanceof MySQLDatabase)) {
        	validationErrors.addError("Cannot add column on specific position");
        }
        if ((statement.getAddBeforeColumn() != null) && !((database instanceof H2Database) || (database instanceof HsqlDatabase))) {
        	validationErrors.addError("Cannot add column on specific position");
        }
        if ((statement.getAddAtPosition() != null) && !(database instanceof FirebirdDatabase)) {
        	validationErrors.addError("Cannot add column on specific position");
        }
        
        return validationErrors;
    }

    @Override
    public Action[] generateActions(AddColumnStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {

        Database database = env.getTargetDatabase();
        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ADD " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnType() + (statement.isAutoIncrement() ? "{autoIncrement:true}" : ""), database).toDatabaseDataType(database);

        if (statement.isAutoIncrement() && database.supportsAutoIncrement()) {
            AutoIncrementConstraint autoIncrementConstraint = statement.getAutoIncrementConstraint();
            alterTable += " " + database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy());
        }

        if (!statement.isNullable()) {
            alterTable += " NOT NULL";
        } else {
            if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase || database instanceof MySQLDatabase) {
                alterTable += " NULL";
            }
        }

        if (statement.isPrimaryKey()) {
            alterTable += " PRIMARY KEY";
        }

        alterTable += getDefaultClause(statement, database);

        if( database instanceof MySQLDatabase && statement.getRemarks() != null ) {
            alterTable += " COMMENT '" + statement.getRemarks() + "' ";
        }

        List<Action> returnSql = new ArrayList<Action>();
        returnSql.add(new UnparsedSql(alterTable));

        addUniqueConstrantStatements(statement, env, returnSql);
        addForeignKeyStatements(statement, env, returnSql);

        return returnSql.toArray(new Action[returnSql.size()]);
    }

    protected void addUniqueConstrantStatements(AddColumnStatement statement, ExecutionEnvironment env, List<Action> returnSql) throws UnsupportedException {
        if (statement.isUnique()) {
            AddUniqueConstraintStatement addConstraintStmt = new AddUniqueConstraintStatement(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName(), statement.getUniqueConstraintName());
            returnSql.addAll(Arrays.asList(StatementLogicFactory.getInstance().generateActions(addConstraintStmt, env)));
        }
    }

    protected void addForeignKeyStatements(AddColumnStatement statement, ExecutionEnvironment env, List<Action> returnSql) throws UnsupportedException {
        for (ColumnConstraint constraint : statement.getConstraints()) {
            if (constraint instanceof ForeignKeyConstraint) {
                ForeignKeyConstraint fkConstraint = (ForeignKeyConstraint) constraint;
                String refSchemaName = null;
                String refTableName;
                String refColName;
                if (fkConstraint.getReferences() != null) {
                    Matcher referencesMatcher = Pattern.compile("([\\w\\._]+)\\(([\\w_]+)\\)").matcher(fkConstraint.getReferences());
                    if (!referencesMatcher.matches()) {
                        throw new UnexpectedLiquibaseException("Don't know how to find table and column names from " + fkConstraint.getReferences());
                    }
                    refTableName = referencesMatcher.group(1);
                    refColName = referencesMatcher.group(2);
                } else {
                    refTableName = ((ForeignKeyConstraint) constraint).getReferencedTableName();
                    refColName = ((ForeignKeyConstraint) constraint).getReferencedColumnNames();
                }

                if (refTableName.indexOf(".") > 0) {
                    refSchemaName = refTableName.split("\\.")[0];
                    refTableName = refTableName.split("\\.")[1];
                }


                AddForeignKeyConstraintStatement addForeignKeyConstraintStatement = new AddForeignKeyConstraintStatement(fkConstraint.getForeignKeyName(), statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName(), null, refSchemaName, refTableName, refColName);
                returnSql.addAll(Arrays.asList(StatementLogicFactory.getInstance().generateActions(addForeignKeyConstraintStatement, env)));
            }
        }
    }

    private String getDefaultClause(AddColumnStatement statement, Database database) {
        String clause = "";
        Object defaultValue = statement.getDefaultValue();
        if (defaultValue != null) {
            if (database instanceof MSSQLDatabase) {
                clause += " CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), statement.getColumnName());
            }
            clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
        }
        return clause;
    }

}
