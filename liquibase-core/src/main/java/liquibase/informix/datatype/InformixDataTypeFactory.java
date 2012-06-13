package liquibase.informix.datatype;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;

public class InformixDataTypeFactory extends DataTypeFactory {

    private static InformixDataTypeFactory instance;

    private InformixDataTypeFactory() {
        super();
    }

    @Deprecated
    public static synchronized InformixDataTypeFactory getInstance() {
        if (instance == null) {
            instance = new InformixDataTypeFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new InformixDataTypeFactory();
    }
    
    @Override
    public LiquibaseDataType fromObject(Object object, Database database) {
       	return super.fromObject(object, database);
    }

    public LiquibaseDataType fromDescription(String dataTypeDefinition) {
    	LiquibaseDataType dataType = super.fromDescription(dataTypeDefinition);

    	// Use the InformixDataType wrapper to correctly resolve informix-specific
    	// data type that is not compatible with the common implementation.
    	
		return new InformixDataType(dataType);
    }
}
