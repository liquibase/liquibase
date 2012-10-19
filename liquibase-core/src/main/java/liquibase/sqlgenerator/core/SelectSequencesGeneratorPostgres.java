package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.structure.Schema;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SelectSequencesStatement;

public class SelectSequencesGeneratorPostgres extends AbstractSqlGenerator<SelectSequencesStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SelectSequencesStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }

    public ValidationErrors validate(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
            Schema schema = database.correctSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));
            
            return new Sql[] {
                    new UnparsedSql("SELECT relname AS SEQUENCE_NAME FROM pg_class, pg_namespace " +
                "WHERE relkind='S' " +
                "AND pg_class.relnamespace = pg_namespace.oid " +
                "AND nspname = '" + schema.getName() + "' " +
                "AND 'nextval(''" + schema + "." + "'||relname||'''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null) " +
                "AND 'nextval(''" + schema + "." + "\"'||relname||'\"''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null) " +
                "AND 'nextval('''||relname||'''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null)" +
                "AND 'nextval(''\"'||relname||'\"''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null)")
            };
    }
}