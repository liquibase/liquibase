package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SelectSequencesStatement;

public class SelectSequencesGeneratorDerby
extends AbstractSqlGenerator<SelectSequencesStatement>
{
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SelectSequencesStatement statement,
    		Database database)
    {
        return database instanceof DerbyDatabase;
    }

    public ValidationErrors validate(SelectSequencesStatement statement,
    		Database database, SqlGeneratorChain sqlGeneratorChain)
    {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SelectSequencesStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        	String schemaName = database.correctSchemaName(statement.getSchemaName());
            return new Sql[] {
                    new UnparsedSql(
                    		"SELECT " +
                    		"  seq.SEQUENCENAME AS SEQUENCE_NAME " +
                    		"FROM " +
                    		"  SYS.SYSSEQUENCES seq, " +
                    		"  SYS.SYSSCHEMAS sch " +
                    		"WHERE " +
                    		"  sch.SCHEMANAME = '" + schemaName + "' AND " +
                    		"  sch.SCHEMAID = seq.SCHEMAID")
            };
    }
}
