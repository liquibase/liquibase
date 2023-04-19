package liquibase.sqlgenerator.core;

import liquibase.change.AddColumnConfig;
import liquibase.database.core.MockDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.structure.core.Column;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateIndexGeneratorFirebirdTest {

    // #1490
    @Test
    public void insertsSpaceForIndexOnComputedField() {
        CreateIndexGeneratorFirebird createIndexGeneratorFirebird = new CreateIndexGeneratorFirebird();

        Sql[] sqls = createIndexGeneratorFirebird.generateSql(
            new CreateIndexStatement(
                "idx_bar",
                null,
                null,
                "foo",
                null,
                null,
                new AddColumnConfig(new Column("baz + 1"))
            ),
            new MockDatabase(),
            new MockSqlGeneratorChain()
        );

        assertThat(sqls[0].toSql())
            .doesNotContain("ON fooCOMPUTED BY");

    }

}
