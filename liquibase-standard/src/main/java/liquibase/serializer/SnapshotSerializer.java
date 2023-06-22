package liquibase.serializer;

import liquibase.snapshot.DatabaseSnapshot;

import java.io.IOException;
import java.io.OutputStream;

public interface SnapshotSerializer extends LiquibaseSerializer {

    void write(DatabaseSnapshot snapshot, OutputStream out) throws IOException;
}
