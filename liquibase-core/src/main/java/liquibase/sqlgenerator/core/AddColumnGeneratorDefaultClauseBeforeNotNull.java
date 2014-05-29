package liquibase.sqlgenerator.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.core.AddColumnStatement;

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
            validationErrors.addError("Cannot add an identity column to derby");
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ADD " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnType() + (statement.isAutoIncrement() ? "{autoIncrement:true}" : ""), database).toDatabaseDataType(database);

        alterTable += getDefaultClause(statement, database);

        if (primaryKeyBeforeNotNull(database)) {
            if (statement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }

        if (statement.isAutoIncrement()) {
            AutoIncrementConstraint autoIncrementConstraint = statement.getAutoIncrementConstraint();
            alterTable += " " + database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy());
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

        List<Sql> returnSql = new ArrayList<Sql>();
        returnSql.add(new UnparsedSql(alterTable, getAffectedColumn(statement)));

        addUniqueConstrantStatements(statement, database, returnSql);
        addForeignKeyStatements(statement, database, returnSql);

        return returnSql.toArray(new Sql[returnSql.size()]);
    }


    private String getDefaultClause(AddColumnStatement statement, Database database) {
        String clause = "";
        Object defaultValue = statement.getDefaultValue();
        if (defaultValue != null) {
            clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
        }
        return clause;
    }

    private boolean primaryKeyBeforeNotNull(Database database) {
        return !(database instanceof HsqlDatabase || database instanceof H2Database);
    }


}
