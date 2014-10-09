package liquibase.integration.ant;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.LiquibaseException;
import liquibase.integration.ant.type.DatabaseType;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Reference;

public abstract class AbstractDatabaseDiffTask extends BaseLiquibaseTask {
    private DatabaseType referenceDatabaseType;
    private String diffTypes;

    protected DiffResult getDiffResult() {
        Liquibase liquibase = getLiquibase();
        Database targetDatabase = liquibase.getDatabase();
        Database referenceDatabase = createDatabaseFromType(referenceDatabaseType);

        CatalogAndSchema targetCatalogAndSchema = buildCatalogAndSchema(targetDatabase);
        CatalogAndSchema referenceCatalogAndSchema = buildCatalogAndSchema(referenceDatabase);
        CompareControl.SchemaComparison[] schemaComparisons = {
                new CompareControl.SchemaComparison(referenceCatalogAndSchema, targetCatalogAndSchema)
        };

        SnapshotGeneratorFactory snapshotGeneratorFactory = SnapshotGeneratorFactory.getInstance();
        DatabaseSnapshot referenceSnapshot;
        try {
            referenceSnapshot = snapshotGeneratorFactory.createSnapshot(referenceDatabase.getDefaultSchema(),
                    referenceDatabase, new SnapshotControl(referenceDatabase, diffTypes));
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to create a DatabaseSnapshot.", e);
        }

        CompareControl compareControl = new CompareControl(schemaComparisons, referenceSnapshot.getSnapshotControl().getTypesToInclude());

        try {
            return liquibase.diff(referenceDatabase, targetDatabase, compareControl);
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to diff databases.", e);
        }
    }

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(referenceDatabaseType == null) {
            throw new BuildException("Reference database element or reference required.");
        }
    }

    private CatalogAndSchema buildCatalogAndSchema(Database database) {
        return new CatalogAndSchema(database.getDefaultCatalogName(), database.getDefaultSchemaName());
    }

    public void addReferenceDatabase(DatabaseType referenceDatabase) {
        if(this.referenceDatabaseType != null) {
            throw new BuildException("Only one <referenceDatabase> element is allowed.");
        }
        this.referenceDatabaseType = referenceDatabase;
    }

    public void setReferenceDatabaseRef(Reference referenceDatabaseRef) {
        referenceDatabaseType = new DatabaseType(getProject());
        referenceDatabaseType.setRefid(referenceDatabaseRef);
    }

    public String getDiffTypes() {
        return diffTypes;
    }

    public void setDiffTypes(String diffTypes) {
        this.diffTypes = diffTypes;
    }

    /*************************
     * Deprecated attributes *
     *************************/

    /**
     * Helper method for deprecated ant attributes. This method will be removed when the deprecated methods are removed.
     * Do not rely on this method.
     */
    private DatabaseType getReferenceDatabaseType() {
        if(referenceDatabaseType == null) {
            referenceDatabaseType = new DatabaseType(getProject());
        }
        return referenceDatabaseType;
    }

    @Deprecated
    public String getReferenceDriver() {
        return getReferenceDatabaseType().getDriver();
    }

    @Deprecated
    public void setReferenceDriver(String referenceDriver) {
        log("The referenceDriver attribute is deprecated. Use a nested <referenceDatabase> element or set the referenceDatabaseRef attribute instead.", Project.MSG_WARN);
        getReferenceDatabaseType().setDriver(referenceDriver);
    }

    @Deprecated
    public String getReferenceUrl() {
        return getReferenceDatabaseType().getUrl();
    }

    @Deprecated
    public void setReferenceUrl(String referenceUrl) {
        log("The referenceUrl attribute is deprecated. Use a nested <referenceDatabase> element or set the referenceDatabaseRef attribute instead.", Project.MSG_WARN);
        getReferenceDatabaseType().setUrl(referenceUrl);
    }

    @Deprecated
    public String getReferenceUsername() {
        return getReferenceDatabaseType().getUser();
    }

    @Deprecated
    public void setReferenceUsername(String referenceUsername) {
        log("The referenceUsername attribute is deprecated. Use a nested <referenceDatabase> element or set the referenceDatabaseRef attribute instead.", Project.MSG_WARN);
        getReferenceDatabaseType().setUser(referenceUsername);
    }

    @Deprecated
    public String getReferencePassword() {
        return getReferenceDatabaseType().getPassword();
    }

    @Deprecated
    public void setReferencePassword(String referencePassword) {
        log("The referencePassword attribute is deprecated. Use a nested <referenceDatabase> element or set the referenceDatabaseRef attribute instead.", Project.MSG_WARN);
        getReferenceDatabaseType().setPassword(referencePassword);
    }

    @Deprecated
    public String getReferenceDefaultCatalogName() {
        return getReferenceDatabaseType().getDefaultCatalogName();
    }

    @Deprecated
    public void setReferenceDefaultCatalogName(String referenceDefaultCatalogName) {
        log("The referenceDefaultCatalogName attribute is deprecated. Use a nested <referenceDatabase> element or set the referenceDatabaseRef attribute instead.", Project.MSG_WARN);
        getReferenceDatabaseType().setDefaultCatalogName(referenceDefaultCatalogName);
    }

    @Deprecated
    public String getReferenceDefaultSchemaName() {
        return getReferenceDatabaseType().getDefaultSchemaName();
    }

    @Deprecated
    public void setReferenceDefaultSchemaName(String referenceDefaultSchemaName) {
        log("The referenceDefaultSchemaName attribute is deprecated. Use a nested <referenceDatabase> element or set the referenceDatabaseRef attribute instead.", Project.MSG_WARN);
        getReferenceDatabaseType().setDefaultSchemaName(referenceDefaultSchemaName);
    }

    @Deprecated
    public String getDataDir() {
        return null;
    }

    @Deprecated
    public void setDataDir(String dataDir) {
        log("The dataDir attribute is deprecated. It is no longer needed and will be removed in the future.", Project.MSG_WARN);
    }
}
