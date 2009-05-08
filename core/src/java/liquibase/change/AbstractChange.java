package liquibase.change;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.parser.string.StringChangeLogSerializer;
import liquibase.resource.FileOpener;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;

import java.util.*;

/**
 * Standard superclass for Changes to implement. This is a <i>skeletal implementation</i>,
 * as defined in Effective Java#16.
 *
 * @see Change
 */
public abstract class AbstractChange implements Change {

    @ChangeMetaDataField
    private ChangeMetaData changeMetaData;

    @ChangeMetaDataField
    private FileOpener fileOpener;

    @ChangeMetaDataField
    private ChangeSet changeSet;

    /**
     * Constructor with tag name and name
     *
     * @param changeName        the tag name for this change
     * @param changeDescription the name for this change
     */
    protected AbstractChange(String changeName, String changeDescription, int priority) {
        this.changeMetaData = new ChangeMetaData(changeName, changeDescription, priority);
    }

    public ChangeMetaData getChangeMetaData() {
        return changeMetaData;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public boolean supports(Database database) {
        for (SqlStatement statement : generateStatements(database)) {
            if (!SqlGeneratorFactory.getInstance().supports(statement, database)) {
                return false;
            }
        }
        return true;
    }

    public ValidationErrors validate(Database database) {
        ValidationErrors changeValidationErrors = new ValidationErrors();
        for (SqlStatement statement : generateStatements(database)) {
            if (!SqlGeneratorFactory.getInstance().supports(statement, database)) {
                changeValidationErrors.addError(getChangeMetaData().getName()+" is not supported on "+database.getProductName());
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
        return new CheckSum(new StringChangeLogSerializer().serialize(this));
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
    public void setFileOpener(FileOpener fileOpener) {
        this.fileOpener = fileOpener;
    }

    /**
     * Returns the FileOpen as provided by the creating ChangeLog.
     *
     * @return The file opener
     */
    public FileOpener getFileOpener() {
        return fileOpener;
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

}
