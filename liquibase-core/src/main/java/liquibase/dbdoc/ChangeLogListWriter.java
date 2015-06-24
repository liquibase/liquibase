package liquibase.dbdoc;

import java.io.File;

public class ChangeLogListWriter extends HTMLListWriter {

    public ChangeLogListWriter(File outputDir, String outputFileEncoding) {
        super("All Change Logs", "changelogs.html", "changelogs", outputDir, outputFileEncoding);
    }

    @Override
    public String getTargetExtension() {
        return ".html";
    }

}
