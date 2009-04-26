package liquibase.database.statement.generator;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.statement.TagDatabaseStatement;
import liquibase.database.statement.UpdateStatement;
import liquibase.exception.JDBCException;

public class TagDatabaseGenerator implements SqlGenerator<TagDatabaseStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(TagDatabaseStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(TagDatabaseStatement tagDatabaseStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(TagDatabaseStatement statement, Database database) throws JDBCException {
        UpdateStatement updateStatement = new UpdateStatement(database.getDefaultSchemaName(), database.getDatabaseChangeLogTableName());
        updateStatement.addNewColumnValue("TAG", statement.getTag());
        if (database instanceof MySQLDatabase) {
            try {
                long version = Long.parseLong(database.getDatabaseProductVersion().substring(0, 1));

                if (version < 5) {
                    return new Sql[]{
                            new UnparsedSql("UPDATE DATABASECHANGELOG C LEFT JOIN (SELECT MAX(DATEEXECUTED) as MAXDATE FROM (SELECT DATEEXECUTED FROM `DATABASECHANGELOG`) AS X) D ON C.DATEEXECUTED = D.MAXDATE SET C.TAG = '" + statement.getTag() + "' WHERE D.MAXDATE IS NOT NULL")
                    };
                }

            } catch (Throwable e) {
                ; //assume it is version 5
            }
            updateStatement.setWhereClause("DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM (SELECT DATEEXECUTED FROM " + database.escapeTableName(database.getDefaultSchemaName(), database.getDatabaseChangeLogTableName()) + ") AS X)");
        } else {
            updateStatement.setWhereClause("DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM " + database.escapeTableName(database.getDefaultSchemaName(), database.getDatabaseChangeLogTableName()) + ")");
        }

        return SqlGeneratorFactory.getInstance().getBestGenerator(updateStatement, database).generateSql(updateStatement, database);

    }
}
