package liquibase.dbdoc;

import java.nio.file.Path;

public class ChangeLogListWriter extends HTMLListWriter {

    public ChangeLogListWriter(Path outputDir) {
        super("All Change Logs", "changelogs.html", "changelogs", outputDir);
    }

    @Override
    public String getTargetExtension() {
        return ".html";
    }

}
