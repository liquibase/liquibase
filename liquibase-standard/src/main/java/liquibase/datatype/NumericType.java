package liquibase.datatype;

import liquibase.change.core.LoadDataChange;
import lombok.Setter;

/** Base class for all Numeric types */
public abstract class NumericType extends LiquibaseDataType {
	@Setter
	private boolean autoIncrement;

    @Override
    public boolean isAutoIncrement() {
        return autoIncrement;
    }

	@Override
	public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;}
}
