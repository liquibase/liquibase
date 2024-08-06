package liquibase.change;

import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import lombok.Getter;
import lombok.Setter;

@Setter
@DatabaseChange(name = "exampleAbstractChange", description = "Used for the AbstractChangeTest unit test", priority = 1)
class ExampleAbstractChange extends AbstractChange {

    private String paramOne;
    private Integer paramTwo;
    @Getter
    private String paramNoMetadata;
    private String paramNotIncluded;

    @Override
    public String getConfirmationMessage() {
        return "Test Confirmation Message";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return null;
    }

    @DatabaseChangeProperty
    public String getParamOne() {
        return paramOne;
    }

    @DatabaseChangeProperty(requiredForDatabase = {"mysql", "mssql"}, mustEqualExisting = "table", serializationType = SerializationType.NESTED_OBJECT)
    public Integer getParamTwo() {
        return paramTwo;
    }

    @DatabaseChangeProperty(isChangeProperty = false)
    public String getParamNotIncluded() {
        return paramNotIncluded;
    }

    public String getNotWriteMethod() {
        return null;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
