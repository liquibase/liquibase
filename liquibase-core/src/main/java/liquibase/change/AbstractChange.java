package liquibase.change;

import java.util.*;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.structure.DatabaseObject;
import liquibase.exception.*;
import liquibase.logging.LogFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Standard superclass to simplify {@link Change } implementations. You can implement Change directly, this class is purely for convenience.
 * <p></p>
 * By default, this base class relies on annotations such as {@link DatabaseChange} and {@link DatabaseChangeProperty}
 * and delegating logic to the {@link liquibase.sqlgenerator.SqlGenerator} objects created to do the actual change work.
 */
public abstract class AbstractChange implements Change {

    @DatabaseChangeProperty(includeInSerialization = false, includeInMetaData = false)
    private ChangeMetaData changeMetaData;

    @DatabaseChangeProperty(includeInSerialization = false, includeInMetaData = false)
    private ResourceAccessor resourceAccessor;

    @DatabaseChangeProperty(includeInSerialization = false, includeInMetaData = false)
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

            Set<ChangeParameterMetaData> params = new HashSet<ChangeParameterMetaData>();
            for (PropertyDescriptor property : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
                Method readMethod = property.getReadMethod();
                Method writeMethod = property.getWriteMethod();
                if (readMethod != null && writeMethod != null) {
                    DatabaseChangeProperty annotation = readMethod.getAnnotation(DatabaseChangeProperty.class);
                    if (annotation == null || annotation.includeInMetaData()) {
                        ChangeParameterMetaData param = createChangeParameterMetadata(property.getDisplayName());
                        params.add(param);
                    }
                }

            }

            return new ChangeMetaData(databaseChange.name(), databaseChange.description(), databaseChange.priority(), databaseChange.appliesTo(), params);
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
    protected ChangeParameterMetaData createChangeParameterMetadata(String parameterName) throws Exception {

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
            throw new RuntimeException("Could not find property " + parameterName);
        }

        String type = property.getPropertyType().getSimpleName();
        DatabaseChangeProperty changePropertyAnnotation = property.getReadMethod().getAnnotation(DatabaseChangeProperty.class);

        String[] requiredForDatabase;
        String mustApplyTo = null;
        if (changePropertyAnnotation == null) {
            requiredForDatabase = new String[]{"none"};
        } else {
            requiredForDatabase = changePropertyAnnotation.requiredForDatabase();
            mustApplyTo = changePropertyAnnotation.mustApplyTo();
        }

        return new ChangeParameterMetaData(parameterName, displayName, type, requiredForDatabase, mustApplyTo);
    }

    /**
     * {@inheritDoc}
     */
    public ChangeMetaData getChangeMetaData() {
        return changeMetaData;
    }

    /**
     * {@inheritDoc}
     */
    @DatabaseChangeProperty(includeInMetaData = false)
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
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#generateStatementsQueriesDatabase(Database) } method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     */
    public boolean generateStatementsQueriesDatabase(Database database) throws UnsupportedChangeException {
        for (SqlStatement statement : generateStatements(database)) {
            if (SqlGeneratorFactory.getInstance().generateStatementsQueriesDatabase(statement, database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#generateRollbackStatementsQueriesDatabase(Database) } method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     */
    public boolean generateRollbackStatementsQueriesDatabase(Database database) throws UnsupportedChangeException {
        for (SqlStatement statement : generateStatements(database)) {
            if (SqlGeneratorFactory.getInstance().generateRollbackStatementsQueriesDatabase(statement, database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#supports(liquibase.statement.SqlStatement, liquibase.database.Database)} method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     */
    public boolean supports(Database database) throws UnsupportedChangeException {
        for (SqlStatement statement : generateStatements(database)) {
            if (!SqlGeneratorFactory.getInstance().supports(statement, database)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#warn(liquibase.statement.SqlStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)} method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     */
    public Warnings warn(Database database) throws UnsupportedChangeException {
        Warnings warnings = new Warnings();
        for (SqlStatement statement : generateStatements(database)) {
            if (SqlGeneratorFactory.getInstance().supports(statement, database)) {
                warnings.addAll(SqlGeneratorFactory.getInstance().warn(statement, database));
            }
        }

        return warnings;
    }

    /**
     * Implementation checks the ChangeParameterMetaData for declared required fields
     * and also delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#validate(liquibase.statement.SqlStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)}  method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     */
    public ValidationErrors validate(Database database) throws UnsupportedChangeException {
        ValidationErrors changeValidationErrors = new ValidationErrors();

        for (ChangeParameterMetaData param : getChangeMetaData().getParameters()) {
            if (param.isRequiredFor(database) && param.getCurrentValue(this) == null) {
                changeValidationErrors.addError(param.getParameterName() + " is required for " + getChangeMetaData().getName() + " on " + database.getShortName());
            }
        }
        if (changeValidationErrors.hasErrors()) {
            return changeValidationErrors;
        }

        for (SqlStatement statement : generateStatements(database)) {
            boolean supported = SqlGeneratorFactory.getInstance().supports(statement, database);
            if (!supported) {
                if (statement.skipOnUnsupported()) {
                    LogFactory.getLogger().info(getChangeMetaData().getName() + " is not supported on " + database.getShortName() + " but will continue");
                } else {
                    changeValidationErrors.addError(getChangeMetaData().getName() + " is not supported on " + database.getShortName());
                }
            } else {
                changeValidationErrors.addAll(SqlGeneratorFactory.getInstance().validate(statement, database));
            }
        }

        return changeValidationErrors;
    }

    /**
     * Implementation relies on value returned from {@link #createInverses()}.
     */
    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
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
        return CheckSum.compute(new StringChangeLogSerializer().serialize(this));
    }

    /*
     * Generates rollback statements from the inverse changes returned by createInverses()
     *
     */
    private SqlStatement[] generateRollbackStatementsFromInverse(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        Change[] inverses = createInverses();
        if (inverses == null) {
            throw new RollbackImpossibleException("No inverse to " + getClass().getName() + " created");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        for (Change inverse : inverses) {
            statements.addAll(Arrays.asList(inverse.generateStatements(database)));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    /**
     * Create inverse changes that can roll back this change. This method is intended
     * to be overriden by Change implementations that have a logical inverse operation.
     * <p/>
     * If {@link #generateRollbackStatements(liquibase.database.Database)} is overridden, this method may not be called.
     *
     * @return Return null if there is no corresponding inverse and therefore automatic rollback is not possible.
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
    @DatabaseChangeProperty(includeInMetaData = false)
    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGeneratorFactory#getAffectedDatabaseObjects(liquibase.statement.SqlStatement, liquibase.database.Database)}  method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     */
    public Set<DatabaseObject> getAffectedDatabaseObjects(Database database) throws UnsupportedChangeException {
        Set<DatabaseObject> affectedObjects = new HashSet<DatabaseObject>();
        for (SqlStatement statement : generateStatements(database)) {
            affectedObjects.addAll(SqlGeneratorFactory.getInstance().getAffectedDatabaseObjects(statement, database));
        }

        return affectedObjects;
    }
}
