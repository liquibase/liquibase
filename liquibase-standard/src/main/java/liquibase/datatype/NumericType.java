package liquibase.datatype;

import liquibase.change.core.LoadDataChange;
import lombok.Getter;
import lombok.Setter;

/** Base class for all Numeric types */
public abstract class NumericType extends LiquibaseDataType {
	@Getter
	@Setter
	private boolean autoIncrement;

	@Override
	public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;}
}
