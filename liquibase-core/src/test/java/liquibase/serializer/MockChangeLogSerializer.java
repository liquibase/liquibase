package liquibase.serializer;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.sql.visitor.SqlVisitor;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.io.OutputStream;
import java.io.IOException;


public class MockChangeLogSerializer implements ChangeLogSerializer {

    private String[] validExtensions;

    public MockChangeLogSerializer(String... validExtensions) {
        this.validExtensions = validExtensions;
    }

    public String[] getValidFileExtensions() {
        return validExtensions;
    }

    public String serialize(DatabaseChangeLog databaseChangeLog) {
        return null;
    }

    public String serialize(ChangeSet changeSet) {
        return null;
    }

    public String serialize(Change change) {
        return null;
    }

    public String serialize(ColumnConfig columnConfig) {
        return null;
    }

    public String serialize(SqlVisitor visitor) {
        return null;  
    }

	public void write(List<ChangeSet> changeSets, OutputStream out)
			throws IOException {
		;
	}

    public void append(ChangeSet changeSet, File changeLogFile) throws IOException {

    }
}
