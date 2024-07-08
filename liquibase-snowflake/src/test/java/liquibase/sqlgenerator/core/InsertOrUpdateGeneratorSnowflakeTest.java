package liquibase.sqlgenerator.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.UpdateStatement;

public class InsertOrUpdateGeneratorSnowflakeTest {

    @Test
    public void testUpdateGeneratorWithSequenceMustSucceed() {
        SnowflakeDatabase database = new SnowflakeDatabase();

        UpdateGenerator generator = new UpdateGenerator();
        UpdateStatement statement = new UpdateStatement("mycatalog", "myschema", "mytable");

        SequenceNextValueFunction sequenceNext = new SequenceNextValueFunction("myschema", "mysequence");
        statement.addNewColumnValue("mycolumn", sequenceNext);

        Sql[] sql = generator.generateSql(statement, database, null);

        assertThat(sql).isNotNull();
        assertThat(sql).hasSize(1);
        assertThat(sql[0].toSql())
            .isEqualTo("UPDATE mycatalog.myschema.mytable SET mycolumn = myschema.mysequence.nextval");
    }
}
