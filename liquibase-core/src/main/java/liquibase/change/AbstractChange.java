package liquibase.change;

import java.beans.IntrospectionException;
import java.lang.reflect.Type;
import java.util.*;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.logging.LogFactory;
import liquibase.structure.DatabaseObject;
import liquibase.exception.*;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Standard superclass to simplify {@link Change } implementations. You can implement Change directly, this class is purely for convenience.
 * <p></p>
 * By default, this base class relies on annotations such as {@link DatabaseChange} and {@link DatabaseChangeProperty}
 * and delegating logic to the {@link liquibase.sqlgenerator.SqlGenerator} objects created to do the actual change work.
 * Place the @DatabaseChangeProperty annotations on the read get methods.
 */
public abstract class AbstractChange implements Change {

    private String dbms;

    private ChangeMetaData changeMetaData;

    private ResourceAccessor resourceAccessor;

    private ChangeSet changeSet;

    public AbstractChange() {
        this.changeMetaData = createChangeMetaData();
    }

    /**
     * Default implementation is a no-op
     */
    public void finishInitialization() throws SetupException {

    }

    /**
     * Generate the ChangeMetaData for this class. Default implementation reads from the @{@link DatabaseChange } annotation.
     * Override to add more or different information to the ChangeMetaData returned by {@link #getChangeMetaData()}.
     */
    protected ChangeMetaData createChangeMetaData() {
        try {
            DatabaseChange databaseChange = this.getClass().getAnnotation(DatabaseChange.class);

            if (databaseChange == null) {
                throw new UnexpectedLiquibaseException("No @DatabaseChange annotation for " + getClass().getName());
            }

            Map<String, ChangeParameterMetaData> params = new HashMap<String, ChangeParameterMetaData>();
            for (PropertyDescriptor property : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
                Method readMethod = property.getReadMethod();
                Method writeMethod = property.getWriteMethod();
                if (readMethod == null) {
                    try {
                        readMethod = this.getClass().getMethod("is"+ StringUtils.upperCaseFirst(property.getName()));
                    } catch (Exception ignore) {
                        //it was worth a try
                    }
                }
                if (readMethod != null && writeMethod != null) {
                    DatabaseChangeProperty annotation = readMethod.getAnnotation(DatabaseChangeProperty.class);
                    if (annotation == null || annotation.isChangeProperty()) {
                        ChangeParameterMetaData param = createChangeParameterMetadata(property.getDisplayName());
                        params.put(param.getParameterName(), param);
                    }
                }

            }

            Map<String, String> notes = new HashMap<String, String>();
            for (DatabaseChangeNote note : databaseChange.databaseNotes()) {
                notes.put(note.database(), note.notes());
            }

            return new ChangeMetaData(databaseChange.name(), databaseChange.description(), databaseChange.priority(), databaseChange.appliesTo(), notes, params);
        } catch (Throwable e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Called by {@link #createChangeMetaData()} to create metadata for a given parameter.
     * The default implementation reads from a @{@link DatabaseChangeProperty} annotation on the field.
     *
     * @param parameterName
     * @return
     * @throws Exception
     */
    protected ChangeParameterMetaData createChangeParameterMetadata(String parameterName) throws IntrospectionException, NoSuchMethodException {

        String displayName = parameterName.replaceAll("([A-Z])", " $1");
        displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);

        PropertyDescriptor property = null;
        for (PropertyDescriptor prop : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
            if (prop.getDisplayName().equals(parameterName)) {
                property = prop;
                break;
            }
        }
        if (property == null) {
            throw new UnexpectedLiquibaseException("Could not find property " + parameterName);
        }

        Method readMethod = property.getReadMethod();
        if (readMethod == null) {
            readMethod = getClass().getMethod("is"+StringUtils.upperCaseFirst(property.getName()));
        }
        Type type = readMethod.getGenericReturnType();

        DatabaseChangeProperty changePropertyAnnotation = readMethod.getAnnotation(DatabaseChangeProperty.class);

        String[] requiredForDatabase;
        String mustEqualExisting = null;
        String description = null;
        String example = null;
        String since = null;
        SerializationType serializationType = SerializationType.NAMED_FIELD;
        if (changePropertyAnnotation == null) {
            requiredForDatabase = new String[]{"none"};
        } else {
            requiredForDatabase = changePropertyAnnotation.requiredForDatabase();
            mustEqualExisting = changePropertyAnnotation.mustEqualExisting();
            serializationType = changePropertyAnnotation.serializationType();
            description = StringUtils.trimToNull(changePropertyAnnotation.description());
            example = StringUtils.trimToNull(changePropertyAnnotation.exampleValue());
            since = StringUtils.trimToNull(changePropertyAnnotation.since());
        }

        return new ChangeParameterMetaData(parameterName, displayName, description, example, since, type, requiredForDatabase, mustEqualExisting, serializationType);
    }

    /**
     * {@inheritDoc}
     */
    @DatabaseChangeProperty(isChangeProperty = false)
    public ChangeMetaData getChangeMetaData() {
        return changeMetaData;
    }

    /**
     * {@inheritDoc}
     */
    @DatabaseChangeProperty(isChangeProperty = false)
    public ChangeSet getChangeSet() {
        return changeSet;
    }

    /**
     * {@inheritDoc}
     */
    public void setChangeSet(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#generateStatementsIsVolatile(Database) } method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If zero or null SqlStatements are returned by generateStatements then this method returns false.
     * Returns false if generateStatements throws UnsupportedChangeException.
     */
    public boolean generateStatementsVolatile(Database database) {
        SqlStatement[] statements;
        try {
            statements = generateStatements(database);
        } catch (UnsupportedChangeException e) {
            return false;
        }
        if (statements == null) {
            return false;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().generateStatementsVolatile(statement, database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#generateRollbackStatementsIsVolatile(Database) } method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     * If no or null SqlStatements are returned by generateRollbackStatements then this method returns false.
     * Returns false if generateStatements throws UnsupportedChangeException.
     */
    public boolean generateRollbackStatementsVolatile(Database database) {
        SqlStatement[] statements;
        try {
            statements = generateStatements(database);
        } catch (UnsupportedChangeException e) {
            return false;
        }
        if (statements == null) {
             return false;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().generateRollbackStatementsVolatile(statement, database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#supports(liquibase.statement.SqlStatement, liquibase.database.Database)} method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If no or null SqlStatements are returned by generateStatements then this method returns true.
     * If {@link #generateStatementsVolatile(liquibase.database.Database)} returns true, we cannot call generateStatements and so assume true.
     */
    public boolean supports(Database database) {
        if (generateStatementsVolatile(database)) {
            return true;
        }
        try {
            SqlStatement[] statements = generateStatements(database);
            if (statements == null) {
                return true;
            }
            for (SqlStatement statement : statements) {
                if (!SqlGeneratorFactory.getInstance().supports(statement, database)) {
                    return false;
                }
            }
        } catch (UnsupportedChangeException e) {
            return false;
        }
        return true;
    }

    public boolean includes(final Database database) {
        if (dbms == null || dbms.trim().isEmpty()) {
            return true;
        }
        List<String> dbmsList = StringUtils.splitAndTrim(dbms, ",");
        return dbmsList.contains(database.getShortName());
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#warn(liquibase.statement.SqlStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)} method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If a generated statement is not supported for the given database, no warning will be added since that is a validation error.
     * If no or null SqlStatements are returned by generateStatements then this method returns no warnings.
     */
    public Warnings warn(Database database) {
        Warnings warnings = new Warnings();
        SqlStatement[] statements;
        try {
            statements = generateStatements(database);
        } catch (UnsupportedChangeException e) {
            return warnings;
        }
        if (statements == null) {
            return warnings;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().supports(statement, database)) {
                warnings.addAll(SqlGeneratorFactory.getInstance().warn(statement, database));
            } else if (statement.skipOnUnsupported()) {
                warnings.addWarning(statement.getClass().getName()+" is not supported on " + database.getShortName() + ", but "+getChangeMetaData().getName() + " will still execute");
            }
        }

        return warnings;
    }

    /**
     * Implementation checks the ChangeParameterMetaData for declared required fields
     * and also delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#validate(liquibase.statement.SqlStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)}  method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If no or null SqlStatements are returned by generateStatements then this method returns no errors.
     * If there are no parameters than this method returns no errors
     */
    public ValidationErrors validate(Database database) {
        ValidationErrors changeValidationErrors = new ValidationErrors();

        for (ChangeParameterMetaData param : getChangeMetaData().getParameters().values()) {
            if (param.isRequiredFor(database) && param.getCurrentValue(this) == null) {
                changeValidationErrors.addError(param.getParameterName() + " is required for " + getChangeMetaData().getName() + " on " + database.getShortName());
            }
        }
        if (changeValidationErrors.hasErrors()) {
            return changeValidationErrors;
        }

        String unsupportedWarning = getChangeMetaData().getName() + " is not supported on " + database.getShortName();
        boolean sawUnsupportedError = false;
        try {
            SqlStatement[] statements;
            statements = generateStatements(database);
            if (statements != null) {
                for (SqlStatement statement : statements) {
                    boolean supported = SqlGeneratorFactory.getInstance().supports(statement, database);
                    if (!supported && !sawUnsupportedError) {
                        if (!statement.skipOnUnsupported()) {
                            changeValidationErrors.addError(unsupportedWarning);
                            sawUnsupportedError = true;
                        }
                    } else {
                        changeValidationErrors.addAll(SqlGeneratorFactory.getInstance().validate(statement, database));
                    }
                }
            }
        } catch (UnsupportedChangeException e) {
            changeValidationErrors.addError(unsupportedWarning);
        }

        return changeValidationErrors;
    }

    /**
     * Implementation relies on value returned from {@link #createInverses()}.
     */
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        return generateRollbackStatementsFromInverse(database);
    }

    /**
     * Implementation returns true if {@link #createInverses()} returns a non-null value.
     */
    public boolean supportsRollback(Database database) {
        return createInverses() != null;
    }

    /**
     * Implementation generates checksum by serializing the change with {@link StringChangeLogSerializer}
     */
    public CheckSum generateCheckSum() {
        return CheckSum.compute(new StringChangeLogSerializer().serialize(this, false));
    }

    /*
     * Generates rollback statements from the inverse changes returned by createInverses().
     * Throws RollbackImpossibleException if the changes created by createInverses() is not supported for the passed database.
     *
     */
    private SqlStatement[] generateRollbackStatementsFromInverse(Database database) throws RollbackImpossibleException {
        Change[] inverses = createInverses();
        if (inverses == null) {
            throw new RollbackImpossibleException("No inverse to " + getClass().getName() + " created");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        try {
            for (Change inverse : inverses) {
                if (!inverse.supports(database)) {
                    throw new RollbackImpossibleException(inverse.getChangeMetaData().getName()+" is not supported on "+database.getShortName());
                }
                statements.addAll(Arrays.asList(inverse.generateStatements(database)));
            }
        } catch (UnsupportedChangeException e) {
            throw new RollbackImpossibleException(e);
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    /**
     * Create inverse changes that can roll back this change. This method is intended
     * to be overriden by Change implementations that have a logical inverse operation. Default implementation returns null.
     * <p/>
     * If {@link #generateRollbackStatements(liquibase.database.Database)} is overridden, this method may not be called.
     *
     * @return Return null if there is no corresponding inverse and therefore automatic rollback is not possible. Return an empty array to have a no-op rollback.
     * @also #generateRollbackStatements #supportsRollback
     */
    protected Change[] createInverses() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    /**
     * @{inheritDoc}
     */
    @DatabaseChangeProperty(isChangeProperty = false)
    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGeneratorFactory#getAffectedDatabaseObjects(liquibase.statement.SqlStatement, liquibase.database.Database)}  method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     * Returns empty set if change is not supported for the passed database
     */
    public Set<DatabaseObject> getAffectedDatabaseObjects(Database database) {
        Set<DatabaseObject> affectedObjects = new HashSet<DatabaseObject>();
        SqlStatement[] statements;
        try {
            statements = generateStatements(database);
        } catch (UnsupportedChangeException e) {
            return affectedObjects;
        }
        if (statements != null) {
            for (SqlStatement statement : statements) {
                affectedObjects.addAll(SqlGeneratorFactory.getInstance().getAffectedDatabaseObjects(statement, database));
            }
        }

        return affectedObjects;
    }

    /**
     * @return A comma separated list of dbms' that this change will be run for. Will run for all dbms' if empty or null.
     */
    @DatabaseChangeProperty(since = "3.0", exampleValue = "h2, oracle")
    public String getDbms() {
        return dbms;
    }

    public void setDbms(final String dbms) {
        this.dbms = dbms;
    }

    public Set<String> getSerializableFields() {
        return getChangeMetaData().getParameters().keySet();
    }

    public Object getSerializableFieldValue(String field) {
        return getChangeMetaData().getParameters().get(field).getCurrentValue(this);
    }

    public String getSerializedObjectName() {
        return getChangeMetaData().getName();
    }

    public SerializationType getSerializableFieldType(String field) {
        return getChangeMetaData().getParameters().get(field).getSerializationType();
    }
}
