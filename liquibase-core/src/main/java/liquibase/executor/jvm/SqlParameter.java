package liquibase.executor.jvm;

/**
 * Object to represent a SQL parameter definition.
 * <p/>
 * <p>Parameters may be anonymous, in which case "name" is <code>null</code>.
 * However, all parameters must define a SQL type according to {@link java.sql.Types}.
 *
 * @author Spring Framework
 * @see java.sql.Types
 */
class SqlParameter {

    /**
     * The name of the parameter, if any
     */
    private String name;

    /**
     * SQL type constant from <code>java.sql.Types</code>
     */
    private final int sqlType;

    /**
     * Used for types that are user-named like: STRUCT, DISTINCT, JAVA_OBJECT, named array types
     */
    private String typeName;


    /**
     * The scale to apply in case of a NUMERIC or DECIMAL type, if any
     */
    private Integer scale;


    /**
     * Create a new anonymous SqlParameter, supplying the SQL type.
     *
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     */
    public SqlParameter(int sqlType) {
        this.sqlType = sqlType;
    }

    /**
     * Create a new anonymous SqlParameter, supplying the SQL type.
     *
     * @param sqlType  SQL type of the parameter according to <code>java.sql.Types</code>
     * @param typeName the type name of the parameter (optional)
     */
    public SqlParameter(int sqlType, String typeName) {
        this.sqlType = sqlType;
        this.typeName = typeName;
    }

    /**
     * Create a new anonymous SqlParameter, supplying the SQL type.
     *
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     * @param scale   the number of digits after the decimal point
     *                (for DECIMAL and NUMERIC types)
     */
    public SqlParameter(int sqlType, int scale) {
        this.sqlType = sqlType;
        this.scale = scale;
    }

    /**
     * Create a new SqlParameter, supplying name and SQL type.
     *
     * @param name    name of the parameter, as used in input and output maps
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     */
    public SqlParameter(String name, int sqlType) {
        this.name = name;
        this.sqlType = sqlType;
    }

    /**
     * Create a new SqlParameter, supplying name and SQL type.
     *
     * @param name     name of the parameter, as used in input and output maps
     * @param sqlType  SQL type of the parameter according to <code>java.sql.Types</code>
     * @param typeName the type name of the parameter (optional)
     */
    public SqlParameter(String name, int sqlType, String typeName) {
        this.name = name;
        this.sqlType = sqlType;
        this.typeName = typeName;
    }

    /**
     * Create a new SqlParameter, supplying name and SQL type.
     *
     * @param name    name of the parameter, as used in input and output maps
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     * @param scale   the number of digits after the decimal point
     *                (for DECIMAL and NUMERIC types)
     */
    public SqlParameter(String name, int sqlType, int scale) {
        this.name = name;
        this.sqlType = sqlType;
        this.scale = scale;
    }

    /**
     * Copy constructor.
     *
     * @param otherParam the SqlParameter object to copy from
     */
    public SqlParameter(SqlParameter otherParam) {
        this.name = otherParam.name;
        this.sqlType = otherParam.sqlType;
        this.typeName = otherParam.typeName;
        this.scale = otherParam.scale;
    }


    /**
     * Return the name of the parameter.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return the SQL type of the parameter.
     */
    public int getSqlType() {
        return this.sqlType;
    }

    /**
     * Return the type name of the parameter, if any.
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * Return the scale of the parameter, if any.
     */
    public Integer getScale() {
        return this.scale;
    }


    /**
     * Return whether this parameter holds input values that should be set
     * before execution even if they are <code>null</code>.
     * <p>This implementation always returns <code>true</code>.
     */
    public boolean isInputValueProvided() {
        return true;
    }

    /**
     * Return whether this parameter is an implicit return parameter used during the
     * reults preocessing of the CallableStatement.getMoreResults/getUpdateCount.
     * <p>This implementation always returns <code>false</code>.
     */
    public boolean isResultsParameter() {
        return false;
    }
}
