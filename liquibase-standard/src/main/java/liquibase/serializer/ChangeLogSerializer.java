package liquibase.serializer;

import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.ChangeSet;
import liquibase.servicelocator.PrioritizedService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ChangeLogSerializer extends LiquibaseSerializer, PrioritizedService {
    <T extends ChangeLogChild> void write(List<T> children, OutputStream out) throws IOException;

    void append(ChangeSet changeSet, File changeLogFile) throws IOException;
}
