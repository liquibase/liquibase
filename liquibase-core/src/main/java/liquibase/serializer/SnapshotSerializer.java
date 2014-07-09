package liquibase.serializer;

import liquibase.snapshot.DatabaseSnapshot;

import java.io.IOException;
import java.io.OutputStream;

public interface SnapshotSerializer {

    String[] getValidFileExtensions();

    String serialize(LiquibaseSerializable object, boolean pretty);

    void write(DatabaseSnapshot snapshot, OutputStream out) throws IOException;
}
