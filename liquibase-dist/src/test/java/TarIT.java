import liquibase.util.FileUtil;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ArchiveUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TarIT {
    @Test
    @DisplayName("Generated tar should match expected contents")
    void checkTarContents() throws Exception {
        String tarPath = ArchiveUtils.getGeneratedArchivePath("target", "liquibase", ".tar.gz");
        assertNotNull(tarPath, "Unable to find generated tar!");
        TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(Paths.get(tarPath))));
        TarArchiveEntry currentEntry = tarInput.getNextEntry();
        StringBuilder tarContents = new StringBuilder();
        while (currentEntry != null) {
            if (!currentEntry.isDirectory()) {
                tarContents.append(currentEntry.getName());
                tarContents.append("\n");
            }
            currentEntry = tarInput.getNextEntry();
        }
        String expected = FileUtil.getContents(new File("expected-distribution-contents.txt"));
        assertNotNull(expected);
        assertEquals(ArchiveUtils.getSortedLines(expected), ArchiveUtils.getSortedLines(tarContents.toString()), "Generated tar does not match expected tar contents. Did you add a new dependency?");
    }
}
