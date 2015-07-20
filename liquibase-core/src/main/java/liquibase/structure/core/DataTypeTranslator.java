package liquibase.structure.core;

import liquibase.Scope;
import liquibase.servicelocator.Service;

public class DataTypeTranslator implements Service {

    public int getPriority(Scope scope) {
        return PRIORITY_DEFAULT;
    }


    /**
     * Translate the given dataType into a string for inclusion in SQL.
     */
    public String toSql(DataType dataType, Scope scope) {
        dataType = (DataType) dataType.clone();
        return dataType.toString();
    }

    /**
     * Translate the given dataType into a new DataType instance that is more database-independent
     */
    public DataType standardizeType(DataType dataType, Scope scope) {
        return (DataType) dataType.clone();
    }

}
