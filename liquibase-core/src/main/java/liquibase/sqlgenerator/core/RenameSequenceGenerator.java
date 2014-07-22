package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RenameSequenceStatement;
import liquibase.structure.core.Sequence;

public class RenameSequenceGenerator extends AbstractSqlGenerator<RenameSequenceStatement> {

    @Override
    public boolean supports(RenameSequenceStatement statement, Database database) {
        return database.supportsSequences() 
            // TODO: following are not implemented/tested currently
            && !(database instanceof DB2Database)
            && !(database instanceof FirebirdDatabase)
            && !(database instanceof H2Database)
            && !(database instanceof HsqlDatabase)
            && !(database instanceof InformixDatabase);
    }

    @Override
    public ValidationErrors validate(RenameSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("newSequenceName", statement.getNewSequenceName());
        validationErrors.checkRequiredField("oldSequenceName", statement.getOldSequenceName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RenameSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;

        if (database instanceof PostgresDatabase) {
            sql = "ALTER SEQUENCE " + database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldSequenceName()) + " RENAME TO " + database.escapeObjectName(statement.getNewSequenceName(), Sequence.class);
        } else if (database instanceof OracleDatabase) {
            sql = "RENAME " + database.escapeObjectName(statement.getOldSequenceName(), Sequence.class) + " TO " + database.escapeObjectName(statement.getNewSequenceName(), Sequence.class);
        } else if( database instanceof MSSQLDatabase){
            sql = "SP_RENAME " + database.escapeObjectName(statement.getOldSequenceName(), Sequence.class) + " ," + database.escapeObjectName(statement.getNewSequenceName(),Sequence.class);
        } else {
            sql = "ALTER SEQUENCE " + database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldSequenceName()) + " RENAME TO " + database.escapeObjectName(statement.getNewSequenceName(), Sequence.class);
        }

        return new Sql[]{
                new UnparsedSql(sql,
                        getAffectedOldSequence(statement),
                        getAffectedNewSequence(statement)
                )
        };
    }

    protected Sequence getAffectedNewSequence(RenameSequenceStatement statement) {
        return new Sequence().setName(statement.getNewSequenceName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }

    protected Sequence getAffectedOldSequence(RenameSequenceStatement statement) {
        return new Sequence().setName(statement.getOldSequenceName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
