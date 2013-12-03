package liquibase.serializer;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.sql.visitor.SqlVisitor;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

public interface ChangeLogSerializer extends LiquibaseSerializer {

	void write(List<ChangeSet> changeSets, OutputStream out) throws IOException;

    void append(ChangeSet changeSet, File changeLogFile) throws IOException;
}
