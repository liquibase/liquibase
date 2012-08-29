package liquibase.change;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
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
 * Standard superclass for Changes to implement.
 *
 * @see Change
 */
public abstract class AbstractChange implements Change {

    @DatabaseChangeProperty(includeInSerialization = false, includeInMetaData = false)
    private ChangeMetaData changeMetaData;

    @DatabaseChangeProperty(includeInSerialization = false, includeInMetaData = false)
    private ResourceAccessor resourceAccessor;

    @DatabaseChangeProperty(includeInSerialization = false, includeInMetaData = false)
    private ChangeSet changeSet;

    @DatabaseChangeProperty(includeInSerialization = false)
    private ChangeLogParameters changeLogParameters;

    public AbstractChange() {
        this.changeMetaData = createChangeMetaData();
    }

    /**
     * Most Changes don't need to do any setup.
     * This implements a no-op
     */
    public void init() throws SetupException {

    }

    /**
     * Generate the ChangeMetaData for this class. By default reads from the @DatabaseChange annotation, but can return anything
     */
    protected ChangeMetaData createChangeMetaData() {
        try {
            DatabaseChange databaseChange = this.getClass().getAnnotation(DatabaseChange.class);

            if (databaseChange == null) {
                throw new UnexpectedLiquibaseException("No @DatabaseChange annotation for "+getClass().getName());
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

    protected ChangeParameterMetaData createChangeParameterMetadata(String propertyName) throws Exception {

        String displayName = propertyName.replaceAll("([A-Z])", " $1");
        displayName = displayName.substring(0,1).toUpperCase()+displayName.substring(1);

        PropertyDescriptor property = null;
        for (PropertyDescriptor prop : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
            if (prop.getDisplayName().equals(propertyName)) {
                property = prop;
                break;
            }
        }
        if (property == null) {
            throw new RuntimeException("Could not find property "+propertyName);
        }

        String type = property.getPropertyType().getSimpleName();
        DatabaseChangeProperty changePropertyAnnotation = property.getReadMethod().getAnnotation(DatabaseChangeProperty.class);

        String[] requiredForDatabase;
        String mustApplyTo = null;
        if (changePropertyAnnotation == null) {
            requiredForDatabase = new String[] {"none"};
        } else {
            requiredForDatabase = changePropertyAnnotation.requiredForDatabase();
            mustApplyTo = changePropertyAnnotation.mustApplyTo();
        }

        return new ChangeParameterMetaData(propertyName, displayName, type, requiredForDatabase, mustApplyTo);
    }

    public ChangeMetaData getChangeMetaData() {
        return changeMetaData;
    }

    @DatabaseChangeProperty(includeInMetaData = false)
    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    /**
     * Return true if the Change class queries the database in any way to determine Statements to execute.
     * If the change queries the database, it cannot be used in updateSql type operations
     */
    public boolean queriesDatabase(Database database) {
        for (SqlStatement statement : generateStatements(database)) {
            if (SqlGeneratorFactory.getInstance().queriesDatabase(statement, database)) {
                return true;
            }
        }
        return false;
    }

    public boolean supports(Database database) {
        for (SqlStatement statement : generateStatements(database)) {
            if (!SqlGeneratorFactory.getInstance().supports(statement, database)) {
                return false;
            }
        }
        return true;
    }

    public Warnings warn(Database database) {
        Warnings warnings = new Warnings();
        for (SqlStatement statement : generateStatements(database)) {
            if (SqlGeneratorFactory.getInstance().supports(statement, database)) {
                warnings.addAll(SqlGeneratorFactory.getInstance().warn(statement, database));
            }
        }

        return warnings;
    }

    public ValidationErrors validate(Database database) {
        ValidationErrors changeValidationErrors = new ValidationErrors();

        for (ChangeParameterMetaData param : getChangeMetaData().getParameters()) {
            if (param.isRequiredFor(database) && param.getCurrentValue(this) == null) {
                changeValidationErrors.addError(param.getParameterName()+" is required for "+getChangeMetaData().getName()+" on "+database.getTypeName());
            }
        }
        if (changeValidationErrors.hasErrors()) {
            return changeValidationErrors;
        }

        for (SqlStatement statement : generateStatements(database)) {
            boolean supported = SqlGeneratorFactory.getInstance().supports(statement, database);
            if (!supported) {
                if (statement.skipOnUnsupported()) {
                    LogFactory.getLogger().info(getChangeMetaData().getName()+" is not supported on "+database.getTypeName()+" but will continue");
                } else {
                    changeValidationErrors.addError(getChangeMetaData().getName()+" is not supported on "+database.getTypeName());
                }
            } else {
                changeValidationErrors.addAll(SqlGeneratorFactory.getInstance().validate(statement, database));
            }
        }

        return changeValidationErrors;
    }

    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        return generateRollbackStatementsFromInverse(database);
    }

    public boolean supportsRollback(Database database) {
        return createInverses() != null;
    }

    public CheckSum generateCheckSum() {
        return CheckSum.compute(new StringChangeLogSerializer().serialize(this));
    }

    /*
     * Generates rollback statements from the inverse changes returned by createInverses()
     *
     * @param database the target {@link Database} associated to this change's rollback statements
     * @return an array of {@link String}s containing the rollback statements from the inverse changes
     * @throws UnsupportedChangeException if this change is not supported by the {@link Database} passed as argument
     * @throws RollbackImpossibleException if rollback is not supported for this change
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

    /*
     * Create inverse changes that can roll back this change. This method is intended
     * to be overriden by the subclasses that can create inverses.
     *
     * @return an array of {@link Change}s containing the inverse
     *         changes that can roll back this change
     */
    protected Change[] createInverses() {
        return null;
    }

    /**
     * Default implementation that stores the file opener provided when the
     * Change was created.
     */
    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    /**
     * Returns the FileOpen as provided by the creating ChangeLog.
     *
     * @return The file opener
     */
    @DatabaseChangeProperty(includeInMetaData = false)
    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects(Database database) {
        Set<DatabaseObject> affectedObjects = new HashSet<DatabaseObject>();
        for (SqlStatement statement : generateStatements(database)) {
            affectedObjects.addAll(SqlGeneratorFactory.getInstance().getAffectedDatabaseObjects(statement, database));
        }

        return affectedObjects;
    }

    protected ChangeLogParameters getChangeLogParameters() {
        return changeLogParameters;
    }

    public void setChangeLogParameters(ChangeLogParameters changeLogParameters) {
        this.changeLogParameters = changeLogParameters;
    }

}
