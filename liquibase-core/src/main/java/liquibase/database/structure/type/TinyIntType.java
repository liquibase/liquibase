package liquibase.database.structure.type;

public class TinyIntType  extends DataType {
    public TinyIntType() {
        super("TINYINT",0,1);
    }

    public TinyIntType(String dataTypeName) {
        super(dataTypeName,0,1);
    }

}
