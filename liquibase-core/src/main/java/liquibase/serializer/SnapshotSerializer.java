package liquibase.serializer;

import liquibase.changelog.ChangeSet;
import liquibase.snapshot.DatabaseSnapshot;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface SnapshotSerializer {

    String[] getValidFileExtensions();

    String serialize(LiquibaseSerializable object, boolean pretty);

    void write(DatabaseSnapshot snapshot, OutputStream out) throws IOException;
}
