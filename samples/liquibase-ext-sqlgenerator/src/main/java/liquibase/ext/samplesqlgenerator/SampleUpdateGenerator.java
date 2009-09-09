package liquibase.ext.samplesqlgenerator;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.UpdateStatement;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class SampleUpdateGenerator implements SqlGenerator<UpdateStatement> {
    public int getPriority() {
        return 15;
    }

    public boolean supports(UpdateStatement statement, Database database) {
        return false; //normally would want true, but we don't want to have this called in all our tests
    }

    public ValidationErrors validate(UpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(UpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> list = new ArrayList<Sql>();
        list.add(new UnparsedSql("select "+database.getCurrentDateTimeFunction()));
        list.addAll(Arrays.asList(sqlGeneratorChain.generateSql(statement, database)));

        return list.toArray(new Sql[list.size()]);

    }
}
