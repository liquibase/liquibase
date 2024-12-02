import liquibase.util.FileUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ArchiveUtils;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

public class ZipIT {
    @Test
    @DisplayName("Generated zip should match expected contents")
    void checkZipContents() throws Exception {
        String zipPath = ArchiveUtils.getGeneratedArchivePath("target", "liquibase", ".zip");
        assertNotNull(zipPath, "There is a zip generated");
        StringBuilder zipContents = new StringBuilder();
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            zipFile.stream()
                    .map(ZipEntry::getName)
                    .forEach(fileName -> {
                        zipContents.append(fileName);
                        zipContents.append("\n");
                    });
        }
        String expected = FileUtil.getContents(new File("expected-distribution-contents-zip.txt"));
        assertNotNull(expected);
        assertTrue(ArchiveUtils.linesEqual(expected, zipContents.toString()), "Generated zip matches expected zip contents");
    }
}
