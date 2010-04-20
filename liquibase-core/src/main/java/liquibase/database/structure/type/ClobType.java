package liquibase.database.structure.type;

import liquibase.database.Database;

public class ClobType extends TextType {

    public ClobType() {
        super("CLOB",0,0);
    }

    public ClobType(String dataTypeName) {
        super(dataTypeName,0,0);
    }
}
