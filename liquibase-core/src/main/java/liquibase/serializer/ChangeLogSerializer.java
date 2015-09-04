package liquibase.serializer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import liquibase.changelog.ChangeSet;

public interface ChangeLogSerializer {

    String[] getValidFileExtensions();

    String serialize(LiquibaseSerializable object, boolean pretty);

	void write(List<ChangeSet> changeSets, OutputStream out) throws IOException;

    void append(ChangeSet changeSet, File changeLogFile) throws IOException;
}
