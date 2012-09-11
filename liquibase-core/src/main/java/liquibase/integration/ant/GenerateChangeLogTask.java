package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.structure.core.Schema;
import liquibase.diff.DiffControl;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputConfig;
import liquibase.diff.output.DiffToChangeLog;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import org.apache.tools.ant.BuildException;

import java.io.PrintStream;

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
	public void execute() throws BuildException {
        super.execute();
        
		Liquibase liquibase = null;
		try {
			PrintStream writer = createPrintStream();
			if (writer == null) {
				throw new BuildException("generateChangeLog requires outputFile to be set");
			}

			liquibase = createLiquibase();

			Database database = liquibase.getDatabase();
            DiffControl diffControl = new DiffControl(new Schema(getDefaultCatalogName(), getDefaultSchemaName()), getDiffTypes());
            diffControl.setDataDir(getDataDir());

            DatabaseSnapshot referenceSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, diffControl, DiffControl.DatabaseRole.REFERENCE);

            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(referenceSnapshot, new DatabaseSnapshot(database, diffControl.getSchemas(DiffControl.DatabaseRole.REFERENCE)), diffControl);
//			diff.addStatusListener(new OutDiffStatusListener());

            DiffOutputConfig diffOutputConfig = new DiffOutputConfig(getIncludeCatalog(), getIncludeSchema(), getIncludeTablespace());
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
