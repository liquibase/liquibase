package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.TagDatabaseStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class TagDatabaseGenerator extends AbstractSqlGenerator<TagDatabaseStatement> {

    @Override
    public ValidationErrors validate(TagDatabaseStatement tagDatabaseStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tag", tagDatabaseStatement.getTag());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(TagDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        UpdateStatement updateStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
        updateStatement.addNewColumnValue("TAG", statement.getTag());
        String tableNameEscaped = database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
        String orderColumnNameEscaped = database.escapeObjectName("ORDEREXECUTED", Column.class);
        String tagColumnNameEscaped = database.escapeObjectName("TAG", Column.class);
        String tagEscaped = DataTypeFactory.getInstance().fromObject(statement.getTag(), database).objectToSql(statement.getTag(), database);
        if (database instanceof MySQLDatabase) {
            try {
                if (database.getDatabaseMajorVersion() < 5) {
                    return new Sql[] {
                        new UnparsedSql(
                                "UPDATE " + tableNameEscaped + " AS C " +
                                "INNER JOIN (" +
                                    "SELECT MAX(" + orderColumnNameEscaped + ") AS " + orderColumnNameEscaped + " " +
                                    "FROM " + tableNameEscaped +
                                ") AS D " +
                                "ON C." + orderColumnNameEscaped + " = D." + orderColumnNameEscaped + " " +
                                "SET C." + tagColumnNameEscaped + " = " + tagEscaped + " " +
                                "WHERE C." + tagColumnNameEscaped + " IS NULL")
                    };
                }
            } catch (DatabaseException e) {
                //assume it is version 5 or greater
            }
            updateStatement.setWhereClause(
                    orderColumnNameEscaped + " = (" +
                        "SELECT MAX(" + orderColumnNameEscaped + ") " +
                        "FROM " + tableNameEscaped +
                    ") " +
                    "AND " + tagColumnNameEscaped + " IS NULL");
        } else if (database instanceof InformixDatabase) {
            String tempTableNameEscaped = database.escapeObjectName("max_order_temp", Table.class);
			return new Sql[] {
                    new UnparsedSql(
                            "SELECT MAX(" + orderColumnNameEscaped + ") AS " + orderColumnNameEscaped + " " +
                            "FROM " + tableNameEscaped + " " +
                            "INTO TEMP " + tempTableNameEscaped + " WITH NO LOG"),
                    new UnparsedSql(
                            "UPDATE " + tableNameEscaped + " " +
                            "SET TAG = " + tagEscaped + " " +
                            "WHERE " + orderColumnNameEscaped + " = (" +
                                "SELECT " + orderColumnNameEscaped + " " +
                                "FROM " + tempTableNameEscaped +
                            ") " +
                            "AND " + tagColumnNameEscaped + " IS NULL;"),
                    new UnparsedSql(
                            "DROP TABLE " + tempTableNameEscaped + ";")
            };
        } else {
            updateStatement.setWhereClause(
                    orderColumnNameEscaped + " = (" +
                        "SELECT MAX(" + orderColumnNameEscaped + ") " +
                        "FROM " + tableNameEscaped +
                    ") " +
                    "AND " + tagColumnNameEscaped + " IS NULL");
        }

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

    }
}
