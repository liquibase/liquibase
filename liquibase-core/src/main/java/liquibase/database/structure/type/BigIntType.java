package liquibase.database.structure.type;

public class BigIntType extends DataType {

    public BigIntType() {
        super("BIGINT",0,1);
    }

    public BigIntType(String dataTypeName) {
        super(dataTypeName,0,1);
    }

}
