package liquibase.change;

import liquibase.change.core.LoadDataColumnConfig;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.DatabaseList;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;
import liquibase.util.beans.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;

/**
 * Static metadata about a {@link Change} parameter.
 * Instances of this class are tracked within {@link ChangeMetaData} and are immutable.
 */
public class ChangeParameterMetaData {

    public static final String COMPUTE = "COMPUTE";

    private Change change;
    private String parameterName;
    private String description;
    private Map<String, Object> exampleValues;
    private String displayName;
    private String dataType;
    private Class dataTypeClass;
    private Type[] dataTypeClassParameters = new Type[0];
    private String since;
    private Set<String> requiredForDatabase;
    private Set<String> supportedDatabases;
    private String mustEqualExisting;
    private LiquibaseSerializable.SerializationType serializationType;

    public ChangeParameterMetaData(Change change, String parameterName, String displayName, String description,
                                   Map<String, Object> exampleValues, String since, Type dataType,
                                   String[] requiredForDatabase, String[] supportedDatabases, String mustEqualExisting,
                                   LiquibaseSerializable.SerializationType serializationType) {
        if (parameterName == null) {
            throw new UnexpectedLiquibaseException("Unexpected null parameterName");
        }
        if (parameterName.contains(" ")) {
            throw new UnexpectedLiquibaseException("Unexpected space in parameterName");
        }
        if (displayName == null) {
            throw new UnexpectedLiquibaseException("Unexpected null displayName");
        }
        if (dataType == null) {
            throw new UnexpectedLiquibaseException("Unexpected null dataType");
        }

        this.change = change;
        this.parameterName = parameterName;
        this.displayName = displayName;
        this.description = description;
        this.exampleValues = exampleValues;
        if (dataType instanceof Class) {
            this.dataType = StringUtils.lowerCaseFirst(((Class) dataType).getSimpleName());
            this.dataTypeClass = (Class) dataType;
        } else if (dataType instanceof ParameterizedType) {
            this.dataType = StringUtils.lowerCaseFirst(
                ((Class) ((ParameterizedType) dataType).getRawType()).getSimpleName() +
                    " of " +
                    StringUtils.lowerCaseFirst(
                        ((Class) ((ParameterizedType) dataType).getActualTypeArguments()[0]).getSimpleName()
                    )
            );
            this.dataTypeClass = (Class) ((ParameterizedType) dataType).getRawType();
            this.dataTypeClassParameters = ((ParameterizedType) dataType).getActualTypeArguments();
        }

        this.mustEqualExisting = mustEqualExisting;
        this.serializationType = serializationType;
        this.since = since;

        this.supportedDatabases = Collections.unmodifiableSet(analyzeSupportedDatabases(supportedDatabases));
        this.requiredForDatabase = Collections.unmodifiableSet(analyzeRequiredDatabases(requiredForDatabase));
    }

    protected Set<String> analyzeSupportedDatabases(String[] supportedDatabases) {
        if (supportedDatabases == null) {
            supportedDatabases = new String[]{ChangeParameterMetaData.COMPUTE};
        }

        Set<String> computedDatabases = new HashSet<>();

        if ((supportedDatabases.length == 1) && StringUtils.join(supportedDatabases, ",").equals
            (ChangeParameterMetaData.COMPUTE)) {
            int validDatabases = 0;
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                if ((database.getShortName() == null) || "unsupported".equals(database.getShortName())) {
                    continue;
                }
                if (!change.supports(database)) {
                    continue;
                }
                try {
                    if (!change.generateStatementsVolatile(database)) {
                        Change testChange = change.getClass().newInstance();
                        ValidationErrors originalErrors = getStatementErrors(testChange, database);
                        this.setValue(testChange, this.getExampleValue(database));
                        ValidationErrors finalErrors = getStatementErrors(testChange, database);
                        if (finalErrors.getUnsupportedErrorMessages().isEmpty() || (finalErrors
                            .getUnsupportedErrorMessages().size() == originalErrors.getUnsupportedErrorMessages()
                            .size())) {
                            computedDatabases.add(database.getShortName());
                        }
                        validDatabases++;
                    }
                } catch (Exception ignore) {
                    // Do nothing
                }
            }

            if (validDatabases == 0) {
                return new HashSet<>(Arrays.asList("all"));
            } else if (computedDatabases.size() == validDatabases) {
                computedDatabases = new HashSet<>(Arrays.asList("all"));
            }

            computedDatabases.remove("none");

            return computedDatabases;
        } else {
            return new HashSet<>(Arrays.asList(supportedDatabases));
        }
    }


    protected Set<String> analyzeRequiredDatabases(String[] requiredDatabases) {
        if (requiredDatabases == null) {
            requiredDatabases = new String[]{ChangeParameterMetaData.COMPUTE};
        }

        Set<String> computedDatabases = new HashSet<>();

        if ((requiredDatabases.length == 1) && StringUtils.join(requiredDatabases, ",").equals
            (ChangeParameterMetaData.COMPUTE)) {
            int validDatabases = 0;
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                try {
                    if (!change.generateStatementsVolatile(database)) {
                        Change testChange = change.getClass().newInstance();
                        ValidationErrors originalErrors = getStatementErrors(testChange, database);
                        this.setValue(testChange, this.getExampleValue(database));
                        ValidationErrors finalErrors = getStatementErrors(testChange, database);
                        if (!originalErrors.getRequiredErrorMessages().isEmpty() && (finalErrors
                            .getRequiredErrorMessages().size() < originalErrors.getRequiredErrorMessages().size())
                         ) {
                            computedDatabases.add(database.getShortName());
                        }
                        validDatabases++;
                    }
                } catch (Exception ignore) {
                    // Do nothing
                }
            }

            if (validDatabases == 0) {
                return new HashSet<>();
            } else if (computedDatabases.size() == validDatabases) {
                computedDatabases = new HashSet<>(Arrays.asList("all"));
            }

            computedDatabases.remove("none");
        } else {
            computedDatabases = new HashSet<>(Arrays.asList(requiredDatabases));
        }
        computedDatabases.remove("none");
        return computedDatabases;
    }

    private static ValidationErrors getStatementErrors(Change testChange, Database database) {
        ValidationErrors errors = new ValidationErrors();
        SqlStatement[] statements = testChange.generateStatements(database);
        for (SqlStatement statement : statements) {
            errors.addAll(SqlGeneratorFactory.getInstance().validate(statement, database));
        }
        return errors;
    }

    /**
     * Programmatic Name of the parameter. Will not contain spaces so it can be used for XMl tag names etc.
     * By convention, Change names should start be camel case starting with a lower case letter.
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * A more friendly name of the parameter.
     */
    public String getDisplayName() {
        return displayName;
    }

    public String getSince() {
        return since;
    }

    /**
     * Return the data type of value stored in this parameter. Used for documentation and integration purposes as well
     * as validation.
     */
    public String getDataType() {
        return dataType;
    }

    public Class getDataTypeClass() {
        return dataTypeClass;
    }

    public Type[] getDataTypeClassParameters() {
        return dataTypeClassParameters;
    }

    /**
     * Return the database types for which this parameter is required. The strings returned correspond to the values
     * returned by {@link liquibase.database.Database#getShortName()}.
     * If the parameter is required for all datatabases, this will return the string "all" as an element.
     * If the parameter is required for no databases, this will return an empty set. Passing the string "none" to the
     * constructor also results in an empty set.
     * This method will never return a null value
     */
    public Set<String> getRequiredForDatabase() {
        return requiredForDatabase;
    }

    public Set<String> getSupportedDatabases() {
        return supportedDatabases;
    }

    /**
     * A convenience method for testing the value returned by {@link #getRequiredForDatabase()} against a given database.
     * Returns true if the {@link Database#getShortName()} method is contained in the required databases or the
     * required database list contains the string "all"
     */
    public boolean isRequiredFor(Database database) {
        return requiredForDatabase.contains("all") || requiredForDatabase.contains(database.getShortName());
    }

    public boolean supports(Database database) {
        return supportedDatabases.contains("all") || supportedDatabases.contains(database.getShortName());
    }


    /**
     * Returns the current value of this parameter for the given Change.
     */
    public Object getCurrentValue(Change change) {
        try {
            for (PropertyDescriptor descriptor : PropertyUtils.getInstance().getDescriptors(change.getClass())) {
                if (descriptor.getDisplayName().equals(this.parameterName)) {
                    Method readMethod = descriptor.getReadMethod();
                    if (readMethod == null) {
                        readMethod = change.getClass().getMethod(
                            "is" + StringUtils.upperCaseFirst(descriptor.getName())
                        );
                    }
                    return readMethod.invoke(change);
                }
            }
            throw new RuntimeException("Could not find readMethod for " + this.parameterName);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Sets the value of this parameter on the given change.
     */
    public void setValue(Change change, Object value) {
        if ((value instanceof String) && (!"string".equals(dataType))) {
            try {
                switch (dataType) {
                    case "bigInteger":
                        value = new BigInteger((String) value);
                        break;
                    case "databaseFunction":
                        value = new DatabaseFunction((String) value);
                        break;
                    default:
                        throw new UnexpectedLiquibaseException("Unknown data type: " + dataType);
                }
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException("Cannot convert string value '" + value + "' to " +
                    dataType + ": " + e.getMessage());
            }
        }

        try {
            for (PropertyDescriptor descriptor : PropertyUtils.getInstance().getDescriptors(change.getClass())) {
                if (descriptor.getDisplayName().equals(this.parameterName)) {
                    Method writeMethod = descriptor.getWriteMethod();
                    if (writeMethod == null) {
                        throw new UnexpectedLiquibaseException("Could not find writeMethod for " + this.parameterName);
                    }
                    Class<?> expectedWriteType = writeMethod.getParameterTypes()[0];
                    if ((value != null) && !expectedWriteType.isAssignableFrom(value.getClass())) {
                        if (expectedWriteType.equals(String.class)) {
                            value = value.toString();
                        } else {
                            throw new UnexpectedLiquibaseException(
                                "Could not convert " + value.getClass().getName() +
                                " to " +
                                expectedWriteType.getName()
                            );
                        }
                    }
                    writeMethod.invoke(change, value);
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException("Error setting " + this.parameterName + " to " + value, e);
        }
    }

    /**
     * Returns a dot-delimited chain of {@link liquibase.structure.DatabaseObject} fields describing what existing
     * value this parameter would need to be set if applying the Change to a particular DatabaseObject.
     * <p></p>
     * For example, in an addColumn Change, the "name" parameter would return "column.name" because if you know of an
     * existing Column object, the "name" parameter needs to be set to the column's name.
     * In the addColumn's "tableName" parameter, this method would return "column.table.name".
     * <p></p>
     * The values of the chain correspond to the {@link liquibase.structure.DatabaseObject#getObjectTypeName()} and
     * {@link liquibase.structure.DatabaseObject#getAttributes()}
     * <p></p>
     * This method is used by integrations that want to generate Change instances or configurations pre-filled with
     * data required to apply to an existing database object.
     */
    public String getMustEqualExisting() {
        return mustEqualExisting;
    }

    /**
     * Return the {@link liquibase.serializer.LiquibaseSerializable.SerializationType}
     * to use when serializing this object.
     */
    public liquibase.serializer.LiquibaseSerializable.SerializationType getSerializationType() {
        return serializationType;
    }

    public Object getExampleValue(Database database) {
        if (exampleValues != null) {
            Object exampleValue = null;

            for (Map.Entry<String, Object> entry: exampleValues.entrySet()) {
                if ("all".equalsIgnoreCase(entry.getKey())) {
                    exampleValue = entry.getValue();
                } else if (DatabaseList.definitionMatches(entry.getKey(), database, false)) {
                    return entry.getValue();
                }
            }

            if (exampleValue != null) {
                return exampleValue;
            }
        }

        Map<String, String> standardExamples = new HashMap<>();
        standardExamples.put("tableName", "person");
        standardExamples.put("schemaName", "public");
        standardExamples.put("tableSchemaName", "public");
        standardExamples.put("catalogName", "cat");
        standardExamples.put("tableCatalogName", "cat");
        standardExamples.put("columnName", "id");
        standardExamples.put("columnNames", "id, name");
        standardExamples.put("indexName", "idx_address");
        standardExamples.put("columnDataType", "int");
        standardExamples.put("dataType", "int");
        standardExamples.put("sequenceName", "seq_id");
        standardExamples.put("viewName", "v_person");
        standardExamples.put("constraintName", "const_name");
        standardExamples.put("primaryKey", "pk_id");



        if (standardExamples.containsKey(parameterName)) {
            return standardExamples.get(parameterName);
        }

        for (String prefix : new String[] {"base", "referenced", "new", "old"}) {
            if (parameterName.startsWith(prefix)) {
                String mainName = StringUtils.lowerCaseFirst(parameterName.replaceFirst("^"+prefix, ""));
                if (standardExamples.containsKey(mainName)) {
                    return standardExamples.get(mainName);
                }
            }
        }
    
        switch (dataType) {
            case "string":
                return "A String";
            case "integer":
                return 3;
            case "boolean":
                return true;
            case "bigInteger":
                return new BigInteger("371717");
            case "list":
                return null; // TODO
        
            case "sequenceNextValueFunction":
                return new SequenceNextValueFunction("seq_name");
            case "databaseFunction":
                return new DatabaseFunction("now");
            case "list of columnConfig": {
                ArrayList<ColumnConfig> list = new ArrayList<>();
                list.add(new ColumnConfig().setName("id").setType("int"));
                return list;
            }
            case "list of addColumnConfig": {
                ArrayList<ColumnConfig> list = new ArrayList<>();
                list.add(new AddColumnConfig().setName("id").setType("int"));
                return list;
            }
            case "list of loadDataColumnConfig": {
                ArrayList<ColumnConfig> list = new ArrayList<>();
                list.add(new LoadDataColumnConfig().setName("id").setType("int"));
                return list;
            }
            default:
                throw new UnexpectedLiquibaseException("Unknown dataType " + dataType + " for " + getParameterName());
        }
    }

    public String getDescription() {
        if (description != null) {
            return description;
        }

        Map<String, String> standardDescriptions = new HashMap<>();
        standardDescriptions.put("tableName", "Name of the table");
        standardDescriptions.put("schemaName", "Name of the schema");
        standardDescriptions.put("catalogName", "Name of the catalog");
        standardDescriptions.put("columnName", "Name of the column");

        return StringUtils.trimToEmpty(standardDescriptions.get(parameterName));

    }

    @Override
    public String toString() {
        return (change != null ? (change.toString() + ".") : "") + getParameterName();
    }
}
