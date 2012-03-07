package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.DiffControl;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffToChangeLog;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import org.apache.tools.ant.BuildException;

import java.io.PrintStream;

public class GenerateChangeLogTask extends BaseLiquibaseTask {

	private String diffTypes;
    private String dataDir;

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
            DiffControl diffControl = new DiffControl(getDefaultCatalogName(), getDefaultSchemaName(), null, null, getDiffTypes());
            diffControl.setDataDir(getDataDir());

            DatabaseSnapshot referenceSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, diffControl, DiffControl.DatabaseRole.REFERENCE);

            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(referenceSnapshot, new DatabaseSnapshot(database), diffControl);
//			diff.addStatusListener(new OutDiffStatusListener());

			if (getChangeLogFile() == null) {
				new DiffToChangeLog(diffResult).print(writer);
			} else {
                new DiffToChangeLog(diffResult).print(getChangeLogFile());
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
