package liquibase.ext.changewithnestedtags;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeClass;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.database.Database;

@ChangeClass(name="changeWithNestedTags", description = "Sample Extension: Change With Nested Tags", priority = 15)
public class ChangeWithNestedTags extends AbstractChange {
    private String name;
    private SampleChild child;
    private SampleChild child2;

    public String getConfirmationMessage() {
        return "changeWithNestedTags executed";
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new CreateTableStatement(null, "cwnestedtags").addColumn("id", TypeConverterFactory.getInstance().findTypeConverter(database).getDataType("int", false))
                    .addColumn("name", TypeConverterFactory.getInstance().findTypeConverter(database).getDataType("varchar(5)", false))
        };
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SampleChild getChild() {
        return child;
    }

    public SampleChild getChild2() {
        return child2;
    }

    public SampleChild createSampleSubValue() {
        child = new SampleChild();
        return child;
    }

    public SampleChild createOtherSampleValue() {
        child2 = new SampleChild();
        return child2;
    }
}
