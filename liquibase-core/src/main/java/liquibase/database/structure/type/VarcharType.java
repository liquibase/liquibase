package liquibase.database.structure.type;

public class VarcharType extends CharType {

    @Override
    public String getDataTypeName() {
        return "VARCHAR";
    }
}
