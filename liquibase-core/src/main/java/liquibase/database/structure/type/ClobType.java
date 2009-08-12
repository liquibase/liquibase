package liquibase.database.structure.type;

public class ClobType extends DataType {
    @Override
    public String getDataTypeName() {
        return "CLOB";
    }

}
