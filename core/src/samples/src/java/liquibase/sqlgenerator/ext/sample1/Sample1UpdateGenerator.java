package liquibase.sqlgenerator.ext.sample1;

import liquibase.statement.UpdateStatement;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

public class Sample1UpdateGenerator implements SqlGenerator<UpdateStatement> {
    public int getPriority() {
        return 15;
    }

    public boolean supports(UpdateStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(UpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(UpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("Sql from Sample1UpdateGenerator")
        };
    }
}
