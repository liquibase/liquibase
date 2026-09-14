package liquibase.sqlgenerator.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.TreeSet;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SetColumnRemarksStatement;

public class SetColumnRemarksGeneratorSnowflakeTest {

    private final SetColumnRemarksGeneratorSnowflake generator = new SetColumnRemarksGeneratorSnowflake();

    @Test
    public void showViewsIsScopedToTheSchemaBeingChanged() {
        SetColumnRemarksStatement statement =
            new SetColumnRemarksStatement("mycatalog", "myschema", "mytable", "mycolumn", "a remark");

        assertThat(generator.buildShowViewsStatement(statement, new SnowflakeDatabase()).getSql())
            .isEqualTo("SHOW VIEWS LIKE 'mytable' IN SCHEMA mycatalog.myschema");
    }

    @Test
    public void showViewsOmitsTheCatalogWhenOnlyTheSchemaIsKnown() {
        SetColumnRemarksStatement statement =
            new SetColumnRemarksStatement(null, "myschema", "mytable", "mycolumn", "a remark");

        assertThat(generator.buildShowViewsStatement(statement, new SnowflakeDatabase()).getSql())
            .isEqualTo("SHOW VIEWS LIKE 'mytable' IN SCHEMA myschema");
    }

    @Test
    public void showViewsIsUnscopedWhenNoSchemaIsKnown() {
        SetColumnRemarksStatement statement =
            new SetColumnRemarksStatement(null, null, "mytable", "mycolumn", "a remark");

        assertThat(generator.buildShowViewsStatement(statement, new SnowflakeDatabase()).getSql())
            .isEqualTo("SHOW VIEWS LIKE 'mytable'");
    }

    @Test
    public void showViewsFallsBackToTheCatalogWhenNoSchemaIsKnown() {
        // an unscoped SHOW VIEWS would still reach views in other databases, so narrow it to the
        // catalog when that is all we have
        SetColumnRemarksStatement statement =
            new SetColumnRemarksStatement("mycatalog", null, "mytable", "mycolumn", "a remark");

        assertThat(generator.buildShowViewsStatement(statement, new SnowflakeDatabase()).getSql())
            .isEqualTo("SHOW VIEWS LIKE 'mytable' IN DATABASE mycatalog");
    }

    @Test
    public void validateReturnsEarlyWithoutATableName() {
        // the view lookup builds a LIKE pattern from the table name, so it cannot run before the
        // statement itself is valid
        SetColumnRemarksStatement statement =
            new SetColumnRemarksStatement("mycatalog", "myschema", null, "mycolumn", "a remark");

        assertThat(generator.validate(statement, new SnowflakeDatabase(), new SqlGeneratorChain<>(new TreeSet<>()))
            .hasErrors()).isTrue();
    }

    @Test
    public void likeWildcardsInTheTableNameAreEscaped() {
        // snowflake treats _ and % in a SHOW ... LIKE pattern as wildcards, so an unescaped
        // my_table would also match a view named myXtable
        SetColumnRemarksStatement statement =
            new SetColumnRemarksStatement(null, "myschema", "my_table%", "mycolumn", "a remark");

        assertThat(generator.buildShowViewsStatement(statement, new SnowflakeDatabase()).getSql())
            .isEqualTo("SHOW VIEWS LIKE 'my\\_table\\%' IN SCHEMA myschema");
    }
}
