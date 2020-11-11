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
        Database referenceDatabase = createDatabaseFromType(referenceDatabaseType, getResourceAccessor());

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
            throw new BuildException("Unable to create a DatabaseSnapshot: " + e.getMessage(), e);
        }

        CompareControl compareControl = new CompareControl(schemaComparisons, referenceSnapshot.getSnapshotControl().getTypesToInclude());

        try {
            return liquibase.diff(referenceDatabase, targetDatabase, compareControl);
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to diff databases: " + e.getMessage(), e);
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
}
