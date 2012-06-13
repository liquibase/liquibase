package liquibase.common.datatype;

import liquibase.database.Database;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

/**
 * A wrapper class for data types.
 * Wraps a LiquibaseDataType instance and replaces some of the
 * original data types with dbms-compatible ones.
 * 
 * @author islavov
 */
public class DataTypeWrapper  extends LiquibaseDataType {
	
	private static LiquibaseDataType stripWrappedDataType(LiquibaseDataType candidate) {
		
		if (candidate instanceof DataTypeWrapper) {
			
			// Strip off any wrapped data type
			
			return((DataTypeWrapper) candidate).getUnderlyingDataType();
		}
		
		return candidate; 
	}
	
	private LiquibaseDataType underlyingDataType;
	
	public LiquibaseDataType getUnderlyingDataType() {
		return underlyingDataType;
	}
	
	public DataTypeWrapper(LiquibaseDataType originalType) {
		super(stripWrappedDataType(originalType));
		this.underlyingDataType = stripWrappedDataType(originalType);
	}
	
	public String getName() {
        return underlyingDataType.getName();
    }

    public String[] getAliases() {
        return underlyingDataType.getAliases();
    }

    public int getPriority() {
        return underlyingDataType.getPriority();
    }
    
    public boolean supports(Database database) {
        return underlyingDataType.supports(database);
    }
    
    public int getMinParameters(Database database) {
        return underlyingDataType.getMinParameters(database);
    }

    public int getMaxParameters(Database database) {
    	return underlyingDataType.getMaxParameters(database);
    }

    public Object[] getParameters() {
    	return underlyingDataType.getParameters();
    }
    
    public void addParameter(Object value) {
        this.underlyingDataType.addParameter(value);
    }

    public boolean validate(Database database) {
        return underlyingDataType.validate(database);
    }

    public DatabaseDataType toDatabaseDataType(Database database) {
        return underlyingDataType.toDatabaseDataType(database);
    }

    public String objectToString(Object value, Database database) {
    	return underlyingDataType.objectToString(value, database);
    }
    
    public Object stringToObject(String value, Database database) {
        return value;
    }

    @Override
    public String toString() {
        return underlyingDataType.toString();
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof DataTypeWrapper && underlyingDataType.equals(((DataTypeWrapper)o).getUnderlyingDataType());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}