package liquibase.change;

import liquibase.database.Database;
import liquibase.statement.SqlStatement;

@DatabaseChange(name = "exampleAbstractChange", description = "Used for the AbstractChangeTest unit test", priority = 1)
class ExampleAbstractChange extends AbstractChange {

    private String paramOne;
    private Integer paramTwo;
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

    public void setParamOne(String paramOne) {
        this.paramOne = paramOne;
    }

    @DatabaseChangeProperty(requiredForDatabase = {"mysql", "mssql"}, mustEqualExisting = "table", serializationType = SerializationType.NESTED_OBJECT)
    public Integer getParamTwo() {
        return paramTwo;
    }

    public void setParamTwo(Integer paramTwo) {
        this.paramTwo = paramTwo;
    }

    public String getParamNoMetadata() {
        return paramNoMetadata;
    }

    public void setParamNoMetadata(String paramNoMetadata) {
        this.paramNoMetadata = paramNoMetadata;
    }

    @DatabaseChangeProperty(isChangeProperty = false)
    public String getParamNotIncluded() {
        return paramNotIncluded;
    }

    public void setParamNotIncluded(String paramNotIncluded) {
        this.paramNotIncluded = paramNotIncluded;
    }

    public String getNotWriteMethod() {
        return null;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
