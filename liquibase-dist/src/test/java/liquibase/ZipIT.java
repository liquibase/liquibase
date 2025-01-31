import liquibase.util.FileUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ArchiveUtils;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ZipIT {
    @Test
    @DisplayName("Generated zip should match expected contents")
    void checkZipContents() throws Exception {
        String zipPath = ArchiveUtils.getGeneratedArchivePath("target", "liquibase", ".zip");
        assertNotNull(zipPath, "Unable to find generated zip!");
        StringBuilder zipContents = new StringBuilder();
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            zipFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .map(ZipEntry::getName)
                    .forEach(fileName -> {
                        zipContents.append(fileName);
                        zipContents.append("\n");
                    });
        }
        String expected = FileUtil.getContents(new File("expected-distribution-contents.txt"));
        assertNotNull(expected);
        assertEquals(ArchiveUtils.getSortedLines(expected), ArchiveUtils.getSortedLines(zipContents.toString()), "Generated zip does not match expected zip contents. Did you add a new dependency?");
    }
}
