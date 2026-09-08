package liquibase.sqlgenerator.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import liquibase.database.core.SnowflakeDatabase;
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
    public void likeWildcardsInTheTableNameAreEscaped() {
        // snowflake treats _ and % in a SHOW ... LIKE pattern as wildcards, so an unescaped
        // my_table would also match a view named myXtable
        SetColumnRemarksStatement statement =
            new SetColumnRemarksStatement(null, "myschema", "my_table%", "mycolumn", "a remark");

        assertThat(generator.buildShowViewsStatement(statement, new SnowflakeDatabase()).getSql())
            .isEqualTo("SHOW VIEWS LIKE 'my\\_table\\%' IN SCHEMA myschema");
    }
}
