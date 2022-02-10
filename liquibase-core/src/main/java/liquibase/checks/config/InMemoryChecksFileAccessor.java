package liquibase.checks.config;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link FileAccessor} which stores all files in memory, not on disk.
 */
public class InMemoryChecksFileAccessor implements FileAccessor {
    private final Map<String, String> files = new ConcurrentHashMap<>();

    @Override
    public FileAccessorDTO loadFileContents(String filename) throws FileNotFoundException {
        if (files.containsKey(filename)) {
            FileAccessorDTO dto = new FileAccessorDTO();
            dto.versioned = false;
            dto.contents = files.get(filename);
            return dto;
        } else {
            throw new FileNotFoundException();
        }
    }

    @Override
    public void writeFileContents(String filename, String contents) {
        files.put(filename, contents);
    }
}
