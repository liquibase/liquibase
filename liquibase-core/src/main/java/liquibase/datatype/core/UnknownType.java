package liquibase.datatype.core;

import liquibase.datatype.LiquibaseDataType;

public class UnknownType extends LiquibaseDataType {

    public UnknownType() {
        super("UNKNOWN", 0, 0);
    }

    public UnknownType(String name) {
        super(name, 0, 0);
    }

    public UnknownType(String name, int minParameters, int maxParameters) {
        super(name, minParameters, maxParameters);
    }

}
