package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.type.*;

import java.sql.Types;
import java.text.ParseException;

public class OracleTypeConverter extends AbstractTypeConverter {

	public int getPriority() {
		return PRIORITY_DATABASE;
	}

	public boolean supports(Database database) {
		return database instanceof OracleDatabase;
	}

	/**
	 * Extension of super.getDataType(String columnTypeString, Boolean autoIncrement, String dataTypeName, String precision)<br>
	 * Contains definition of Oracle's data-types
	 * */
	@Override
	protected DataType getDataType(String columnTypeString, Boolean autoIncrement, String dataTypeName, String precision, String additionalInformation) {
		// Try to define data type by searching of common standard types
		DataType returnTypeName = super.getDataType(columnTypeString, autoIncrement, dataTypeName, precision, additionalInformation);
		// If we found CustomType (it means - nothing compatible) then search for oracle types
		if (returnTypeName instanceof CustomType) {
			if (columnTypeString.toUpperCase().startsWith("VARCHAR2")) {
				// Varchar2 type pattern: VARCHAR2(50 BYTE) | VARCHAR2(50 CHAR)
				returnTypeName = getVarcharType();
				if (precision != null) {
					String[] typeParams = precision.split(" ");
					returnTypeName.setFirstParameter(typeParams[0].trim());
					if (typeParams.length > 1) {
						returnTypeName.setUnit(typeParams[1]);
					}
				}
			} else if (columnTypeString.toUpperCase().startsWith("NVARCHAR2")) {
				// NVarchar2 type pattern: VARCHAR2(50 BYTE) | VARCHAR2(50 CHAR)
				returnTypeName = getNVarcharType();
				if (precision != null) {
					String[] typeParams = precision.split(" ");
					returnTypeName.setFirstParameter(typeParams[0].trim());
					if (typeParams.length > 1) {
						returnTypeName.setUnit(typeParams[1]);
					}
				}
			}
		}
		return returnTypeName;
	}

    @Override
    public String convertToDatabaseTypeString(Column referenceColumn, Database database) {
        String translatedTypeName = referenceColumn.getTypeName();
        if ("NVARCHAR2".equals(translatedTypeName)) {
            translatedTypeName = translatedTypeName+ "(" + referenceColumn.getColumnSize() + ")";
        } else if ("BINARY_FLOAT".equals(translatedTypeName) || "BINARY_DOUBLE".equals(translatedTypeName)) {
            // nothing to do
        } else {
            translatedTypeName = super.convertToDatabaseTypeString(referenceColumn, database);
        }
        return translatedTypeName;
    }

	@Override
	public Object convertDatabaseValueToObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
		if (defaultValue != null) {
			if (defaultValue instanceof String) {
				if (dataType == Types.DATE || dataType == Types.TIME || dataType == Types.TIMESTAMP) {
					if (((String) defaultValue).indexOf("YYYY-MM-DD HH") > 0) {
						defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('", "").replaceFirst("', 'YYYY-MM-DD HH24:MI:SS'\\)$", "");
					} else if (((String) defaultValue).indexOf("YYYY-MM-DD") > 0) {
						defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('", "").replaceFirst("', 'YYYY-MM-DD'\\)$", "");
					} else {
						defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('", "").replaceFirst("', 'HH24:MI:SS'\\)$", "");
					}
				} else if (
						dataType == Types.BIGINT ||
						dataType == Types.NUMERIC ||
						dataType == Types.BIT ||
						dataType == Types.SMALLINT ||
						dataType == Types.DECIMAL ||
						dataType == Types.INTEGER ||
						dataType == Types.TINYINT ||
						dataType == Types.FLOAT ||
						dataType == Types.REAL
						) {
					/*
					* if dataType is numeric-type then cut "(" , ")" symbols
					* Cause: Column's default value option may be set by both ways:
					* DEFAULT 0
					* DEFAULT (0)
					* */
					defaultValue = ((String) defaultValue).replaceFirst("\\(", "").replaceFirst("\\)", "");
				}
				defaultValue = ((String) defaultValue).replaceFirst("'\\s*$", "'"); //sometimes oracle adds an extra space after the trailing ' (see http://sourceforge.net/tracker/index.php?func=detail&aid=1824663&group_id=187970&atid=923443).
			}
		}
		return super.convertDatabaseValueToObject(defaultValue, dataType, columnSize, decimalDigits, database);
	}

	@Override
    protected Object convertToCorrectObjectType(String value, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
		Object returnValue = super.convertToCorrectObjectType(value, dataType, columnSize, decimalDigits, database);
		// I'll do it lately.
		// It needs to design and create Database Function Dictionary first.
		/*if (dataType == Types.BLOB || dataType == Types.TIMESTAMP) {
			if (database.containsDatabaseFunction(value)) {
				returnValue = value;
			}
		}*/
		return returnValue;
	}

	@Override
	public BooleanType getBooleanType() {
		return new BooleanType.NumericBooleanType("NUMBER(1)");
	}

	@Override
	public CurrencyType getCurrencyType() {
		return new CurrencyType("NUMBER(15, 2)");
	}

	@Override
	public UUIDType getUUIDType() {
		return new UUIDType("RAW(16)");
	}

	@Override
	public TimeType getTimeType() {
		return new TimeType("DATE");
	}

	@Override
	public DateTimeType getDateTimeType() {
		return new DateTimeType("TIMESTAMP");
	}

	@Override
	public BigIntType getBigIntType() {
		return new BigIntType("NUMBER(38,0)");
	}

	@Override
	public IntType getIntType() {
		return new IntType("INTEGER");
	}

	@Override
	public VarcharType getVarcharType() {
		return new VarcharType("VARCHAR2");
	}

    @Override
    public NVarcharType getNVarcharType() {
        return new NVarcharType("NVARCHAR2");
    }

    @Override
	public DoubleType getDoubleType() {
		return new DoubleType("FLOAT(24)");
	}

	@Override
	public TinyIntType getTinyIntType() {
		return new TinyIntType("NUMBER(3)");
	}
}
