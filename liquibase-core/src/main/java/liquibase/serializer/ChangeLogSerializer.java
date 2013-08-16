package liquibase.serializer;

import liquibase.changelog.ChangeSet;

import java.io.File;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

public interface ChangeLogSerializer {

    String[] getValidFileExtensions();

    String serialize(LiquibaseSerializable object, boolean pretty);

	void write(List<ChangeSet> changeSets, OutputStream out) throws IOException;

    void append(ChangeSet changeSet, File changeLogFile) throws IOException;
}
