package liquibase.serializer;

import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.ChangeSet;
import liquibase.servicelocator.PrioritizedService;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

public interface ChangeLogSerializer extends LiquibaseSerializer, PrioritizedService {
    <T extends ChangeLogChild> void write(List<T> children, OutputStream out) throws IOException;

    void append(ChangeSet changeSet, File changeLogFile) throws IOException;

    default <T extends ChangeLogChild> void validateFilePath(T child) {
        Object filePathObject = child.getSerializableFieldValue("filePath");
        String filePath = StringUtils.trimToNull(Optional.ofNullable(filePathObject).map(Object::toString).orElse(null));
        if (StringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("The changeSet `filePath` attribute cannot be empty or null");

        }
    }
}
