package liquibase.database.structure.type;

public class IntType  extends DataType {
    public IntType() {
        super("INT",0,1);
    }

    public IntType(String dataTypeName) {
        super(dataTypeName,0,1);
    }

}
