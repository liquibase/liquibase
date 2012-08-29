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
 * Standard superclass for Changes to implement. This is a <i>skeletal implementation</i>,
 * as defined in Effective Java#16.
 *
 * @see Change
 */
public abstract class AbstractChange implements Change {

    @ChangeProperty(includeInSerialization = false, includeInMetaData = false)
    private ChangeMetaData changeMetaData;

    @ChangeProperty(includeInSerialization = false, includeInMetaData = false)
    private ResourceAccessor resourceAccessor;

    @ChangeProperty(includeInSerialization = false, includeInMetaData = false)
    private ChangeSet changeSet;

    @ChangeProperty(includeInSerialization = false)
    private ChangeLogParameters changeLogParameters;

//    /**
//     * Constructor with tag name and name
//     *
//     * @param changeName        the tag name for this change
//     * @param changeDescription the name for this change
//     */
//    protected AbstractChange(String changeName, String changeDescription, int priority) {
//        this.changeMetaData = new ChangeMetaData(changeName, changeDescription, priority);
//    }

    public AbstractChange() {
        this.changeMetaData = createChangeMetaData();
    }

    /**
     * Generate the ChangeMetaData for this class. By default reads from the @DatabaseChange annotation, but can return anything
     * @return
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
                    ChangeProperty annotation = readMethod.getAnnotation(ChangeProperty.class);
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

    private ChangeParameterMetaData createChangeParameterMetadata(String propertyName) throws Exception {

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
        ChangeProperty changePropertyAnnotation = property.getReadMethod().getAnnotation(ChangeProperty.class);

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

    @ChangeProperty(includeInMetaData = false)
    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public boolean requiresUpdatedDatabaseMetadata(Database database) {
        for (SqlStatement statement : generateStatements(database)) {
            if (SqlGeneratorFactory.getInstance().requiresCurrentDatabaseMetadata(statement, database)) {
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

    /*
    * Skipped by this skeletal implementation
    *
    * @see liquibase.change.Change#generateStatements(liquibase.database.Database)
    */

    /**
     * @see liquibase.change.Change#generateRollbackStatements(liquibase.database.Database)
     */
    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        return generateRollbackStatementsFromInverse(database);
    }

    /**
     * @see Change#supportsRollback(liquibase.database.Database)
     * @param database
     */
    public boolean supportsRollback(Database database) {
        return createInverses() != null;
    }

    /**
     * @see liquibase.change.Change#generateCheckSum()
     */
    public CheckSum generateCheckSum() {
        return CheckSum.compute(new StringChangeLogSerializer().serialize(this));
    }

    //~ ------------------------------------------------------------------------------- private methods
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
    @ChangeProperty(includeInMetaData = false)
    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    /**
     * Most Changes don't need to do any setup.
     * This implements a no-op
     */
    public void init() throws SetupException {

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
