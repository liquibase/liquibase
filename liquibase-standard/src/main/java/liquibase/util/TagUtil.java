package liquibase.util;

import liquibase.change.Change;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.column.LiquibaseColumn;
import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateStatement;

public class TagUtil {
    public static Sql[] buildClearDuplicatedTagSql(Database database, String tag) {
        SqlStatement runStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .addNewColumnValue("TAG", null)
                .setWhereClause(database.escapeObjectName("TAG", LiquibaseColumn.class) + " = ?")
                .addWhereParameters(tag);
        return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
    }

    public static String getTagFromChangeset(ChangeSet changeSet) {
        if (changeSet != null) {
            for (Change change : changeSet.getChanges()) {
                if (change instanceof TagDatabaseChange) {
                    TagDatabaseChange tagChange = (TagDatabaseChange) change;
                    return tagChange.getTag();
                }
            }
        }
        return null;
    }
}
