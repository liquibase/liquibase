package liquibase.integration.ant;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.logging.LogFactory;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenerateChangeLogTask extends BaseLiquibaseTask {

	private String diffTypes;
    private String dataDir;
    private boolean includeCatalog;
    private boolean includeSchema;
    private boolean includeTablespace;

    public String getDiffTypes() {
		return diffTypes;
	}

	public void setDiffTypes(String diffTypes) {
		this.diffTypes = diffTypes;
	}

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }


    public boolean getIncludeCatalog() {
        return includeCatalog;
    }

    public void setIncludeCatalog(boolean includeCatalog) {
        this.includeCatalog = includeCatalog;
    }

    public boolean getIncludeSchema() {
        return includeSchema;
    }

    public void setIncludeSchema(boolean includeSchema) {
        this.includeSchema = includeSchema;
    }

    public boolean getIncludeTablespace() {
        return includeTablespace;
    }

    public void setIncludeTablespace(boolean includeTablespace) {
        this.includeTablespace = includeTablespace;
    }

    @Override
	public void executeWithLiquibaseClassloader() throws BuildException {
		Liquibase liquibase = null;
		try {
			PrintStream writer = createPrintStream();
			if (writer == null) {
				throw new BuildException("generateChangeLog requires outputFile to be set");
			}

			liquibase = createLiquibase();

            Database database = liquibase.getDatabase();
            SnapshotControl snapshotControl = new SnapshotControl(database, getDiffTypes());

            DatabaseSnapshot referenceSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName()), database, snapshotControl);

            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(referenceSnapshot, null, new CompareControl(new CompareControl.SchemaComparison[] {new CompareControl.SchemaComparison(new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName()), new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName()))}, getDiffTypes() ));
//			diff.addStatusListener(new OutDiffStatusListener());

            DiffOutputControl diffOutputConfig = new DiffOutputControl(getIncludeCatalog(), getIncludeSchema(), getIncludeTablespace());
            diffOutputConfig.setDataDir(getDataDir());
			if (getChangeLogFile() == null) {
				new DiffToChangeLog(diffResult, diffOutputConfig).print(writer);
			} else {
                new DiffToChangeLog(diffResult, diffOutputConfig).print(getChangeLogFile());
			}

			writer.flush();
			writer.close();
		} catch (Exception e) {
			throw new BuildException(e);
		} finally {
			closeDatabase(liquibase);
		}
	}

}
