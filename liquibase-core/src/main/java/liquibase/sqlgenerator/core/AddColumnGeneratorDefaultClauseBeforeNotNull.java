package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.database.core.*;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddColumnStatement;

import java.util.ArrayList;
import java.util.List;

public class AddColumnGeneratorDefaultClauseBeforeNotNull extends AddColumnGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddColumnStatement statement, Database database) {
        return database instanceof OracleDatabase
                || database instanceof HsqlDatabase
                || database instanceof H2Database
                || database instanceof DerbyDatabase
                || database instanceof DB2Database
                || database instanceof FirebirdDatabase
                || database instanceof SybaseDatabase
                || database instanceof SybaseASADatabase
                || database instanceof InformixDatabase;
    }

    @Override
    public ValidationErrors validate(AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        if (database instanceof DerbyDatabase && statement.isAutoIncrement()) {
            validationErrors.addError("Cannot add an identity column to a database");
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + TypeConverterFactory.getInstance().findTypeConverter(database).getDataType(statement.getColumnType(), statement.isAutoIncrement());

        alterTable += getDefaultClause(statement, database);

        if (primaryKeyBeforeNotNull(database)) {
            if (statement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }

        if (statement.isAutoIncrement()) {
            alterTable += " " + database.getAutoIncrementClause();
        }

        if (!statement.isNullable()) {
            alterTable += " NOT NULL";
        } else if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
            alterTable += " NULL";
        }

        if (!primaryKeyBeforeNotNull(database)) {
            if (statement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }
        if (statement.isUnique()) {
            alterTable += " UNIQUE ";
        }

        List<Sql> returnSql = new ArrayList<Sql>();
        returnSql.add(new UnparsedSql(alterTable, new Column()
                        .setTable(new Table(statement.getTableName()).setSchema(statement.getSchemaName()))
                        .setName(statement.getColumnName())));

        addForeignKeyStatements(statement, database, returnSql);

        return returnSql.toArray(new Sql[returnSql.size()]);
    }


    private String getDefaultClause(AddColumnStatement statement, Database database) {
        String clause = "";
        Object defaultValue = statement.getDefaultValue();
        if (defaultValue != null) {
            clause += " DEFAULT " + TypeConverterFactory.getInstance().findTypeConverter(database).getDataType(defaultValue).convertObjectToString(defaultValue, database);
        }
        return clause;
    }

    private boolean primaryKeyBeforeNotNull(Database database) {
        return !(database instanceof HsqlDatabase || database instanceof H2Database);
    }


}
