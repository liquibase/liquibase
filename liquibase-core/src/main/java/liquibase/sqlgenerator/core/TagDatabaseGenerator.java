package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
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
        String tableNameEscaped = database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
        String orderColumnNameEscaped = database.escapeObjectName("ORDEREXECUTED", Column.class);
        String dateColumnNameEscaped = database.escapeObjectName("DATEEXECUTED", Column.class);
        String tagColumnNameEscaped = database.escapeObjectName("TAG", Column.class);
        String tagEscaped = DataTypeFactory.getInstance().fromObject(statement.getTag(), database).objectToSql(statement.getTag(), database);
        if (database instanceof MySQLDatabase) {
            return new Sql[]{
                    new UnparsedSql(
                            "UPDATE " + tableNameEscaped + " AS C " +
                                    "INNER JOIN (" +
                                    "SELECT " + orderColumnNameEscaped + ", " + dateColumnNameEscaped + " " +
                                    "FROM " + tableNameEscaped +
                                    " order by " + dateColumnNameEscaped + " desc, " + orderColumnNameEscaped + " desc limit 1) AS D " +
                                    "ON C." + orderColumnNameEscaped + " = D." + orderColumnNameEscaped + " " +
                                    "SET C." + tagColumnNameEscaped + " = " + tagEscaped)
            };
        } else if (database instanceof PostgresDatabase) {
            return new Sql[]{
                    new UnparsedSql(
                            "UPDATE " + tableNameEscaped + " t SET TAG=" + tagEscaped +
                                    " FROM (SELECT " + dateColumnNameEscaped + ", " + orderColumnNameEscaped + " FROM " + tableNameEscaped + " ORDER BY " + dateColumnNameEscaped + " DESC, " + orderColumnNameEscaped + " DESC LIMIT 1) sub " +
                                    "WHERE t." + dateColumnNameEscaped + "=sub." + dateColumnNameEscaped + " AND t." + orderColumnNameEscaped + "=sub." + orderColumnNameEscaped)
            };
        } else if (database instanceof InformixDatabase) {
            String tempTableNameEscaped = database.escapeObjectName("max_order_temp", Table.class);
            return new Sql[]{
                    new UnparsedSql(
                            "SELECT MAX(" + dateColumnNameEscaped + ") AS " + dateColumnNameEscaped +
                                    ", MAX(" + orderColumnNameEscaped + ") AS " + orderColumnNameEscaped + " " +
                                    "FROM " + tableNameEscaped + " " +
                                    "INTO TEMP " + tempTableNameEscaped + " WITH NO LOG"),
                    new UnparsedSql(
                            "UPDATE " + tableNameEscaped + " " +
                                    "SET TAG = " + tagEscaped + " " +
                                    "WHERE " + dateColumnNameEscaped + " = (" +
                                    "SELECT " + dateColumnNameEscaped + " " +
                                    "FROM " + tempTableNameEscaped +
                                    ") AND " +
                                    orderColumnNameEscaped + " = (" +
                                    "SELECT " + orderColumnNameEscaped + " " +
                                    "FROM " + tempTableNameEscaped +
                                    ");"),
                    new UnparsedSql(
                            "DROP TABLE " + tempTableNameEscaped + ";")
            };
        } else if (database instanceof MSSQLDatabase) {
            String changelogAliasEscaped = database.escapeObjectName("changelog", Table.class);
            String latestAliasEscaped = database.escapeObjectName("latest", Table.class);
            String idColumnEscaped = database.escapeObjectName("ID", Column.class);
            String authorColumnEscaped = database.escapeObjectName("AUTHOR", Column.class);
            String filenameColumnEscaped = database.escapeObjectName("FILENAME", Column.class);

            String topClause = "TOP (1)";

            return new Sql[] {
                    new UnparsedSql(
                            "UPDATE " + changelogAliasEscaped + " " +
                            "SET " + tagColumnNameEscaped + " = " + tagEscaped + " " +
                            "FROM " + tableNameEscaped + " AS " + changelogAliasEscaped + " "  +
                            "INNER JOIN (" +
                                "SELECT " + topClause + " " + idColumnEscaped + ", " + authorColumnEscaped + ", " + filenameColumnEscaped + " " +
                                "FROM " + tableNameEscaped + " " +
                                "ORDER BY " + dateColumnNameEscaped + " DESC, " + orderColumnNameEscaped + " DESC" +
                            ") AS " + latestAliasEscaped + " " +
                            "ON " + latestAliasEscaped + "." + idColumnEscaped + " = " + changelogAliasEscaped + "." + idColumnEscaped + " " +
                            "AND " + latestAliasEscaped + "." + authorColumnEscaped + " = " + changelogAliasEscaped + "." + authorColumnEscaped + " " +
                            "AND " + latestAliasEscaped + "." + filenameColumnEscaped + " = " + changelogAliasEscaped + "." + filenameColumnEscaped)
                };
        } else if ((database instanceof OracleDatabase) || (database instanceof DB2Database)) {
            String selectClause = "SELECT";
            String endClause = ")";
            String delimiter = "";
            if (database instanceof OracleDatabase) {
                selectClause = "SELECT * FROM (SELECT";
                endClause = ") where rownum=1)";
            } else if (database instanceof AbstractDb2Database) {
                endClause = " FETCH FIRST 1 ROWS ONLY)";
            }

            return new Sql[]{
                    new UnparsedSql("MERGE INTO " + tableNameEscaped + " a " +
                            "USING (" + selectClause + " " + orderColumnNameEscaped + ", " + dateColumnNameEscaped + " from " + tableNameEscaped + " order by " + dateColumnNameEscaped + " desc, " + orderColumnNameEscaped + " desc" + endClause + " b " +
                            "ON ( a." + dateColumnNameEscaped + " = b." + dateColumnNameEscaped + " and a." + orderColumnNameEscaped + "=b." + orderColumnNameEscaped + " ) " +
                            "WHEN MATCHED THEN " +
                            "UPDATE SET  a.tag=" + tagEscaped + delimiter)
            };
        } else {

            //Only uses dateexecuted as a default. Depending on the timestamp resolution, multiple rows may be tagged which normally works fine but can cause confusion and some issues.
            //We cannot use orderexecuted alone because it is only guaranteed to be incrementing per update call.
            //TODO: Better handle other databases to use dateexecuted desc, orderexecuted desc.
            UpdateStatement updateStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                    .addNewColumnValue("TAG", statement.getTag())
                    .setWhereClause(
                            dateColumnNameEscaped + " = (" +
                                    "SELECT MAX(" + dateColumnNameEscaped + ") " +
                                    "FROM " + tableNameEscaped +
                                    ")");

            return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);
        }

    }
}
