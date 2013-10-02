package liquibase.executor.jvm;

/**
 * Object to represent a SQL parameter value, including parameter metadata
 * such as the SQL type and the scale for numeric values.
 * <p/>
 * <p>Designed for use with {@link liquibase.executor.Executor}'s operations that take an array of
 * argument values: Each such argument value may be a <code>SqlParameterValue</code>,
 * indicating the SQL type (and optionally the scale) instead of letting the
 * template guess a default type. Note that this only applies to the operations with
 * a 'plain' argument array, not to the overloaded variants with an explicit type array.
 *
 * @author Spring Framework
 * @see java.sql.Types
 */
class SqlParameterValue extends SqlParameter {

    private final Object value;


    /**
     * Create a new SqlParameterValue, supplying the SQL type.
     *
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     * @param value   the value object
     */
    public SqlParameterValue(int sqlType, Object value) {
        super(sqlType);
        this.value = value;
    }

    /**
     * Create a new SqlParameterValue, supplying the SQL type.
     *
     * @param sqlType  SQL type of the parameter according to <code>java.sql.Types</code>
     * @param typeName the type name of the parameter (optional)
     * @param value    the value object
     */
    public SqlParameterValue(int sqlType, String typeName, Object value) {
        super(sqlType, typeName);
        this.value = value;
    }

    /**
     * Create a new SqlParameterValue, supplying the SQL type.
     *
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     * @param scale   the number of digits after the decimal point
     *                (for DECIMAL and NUMERIC types)
     * @param value   the value object
     */
    public SqlParameterValue(int sqlType, int scale, Object value) {
        super(sqlType, scale);
        this.value = value;
    }

    /**
     * Create a new SqlParameterValue based on the given SqlParameter declaration.
     *
     * @param declaredParam the declared SqlParameter to define a value for
     * @param value         the value object
     */
    public SqlParameterValue(SqlParameter declaredParam, Object value) {
        super(declaredParam);
        this.value = value;
    }


    /**
     * Return the value object that this parameter value holds.
	 */
	public Object getValue() {
		return this.value;
	}

}
