package liquibase.dbdoc;

import java.io.File;

public class ChangeLogListWriter extends HTMLListWriter {

    public ChangeLogListWriter(File outputDir) {
        super("All Change Logs", "changelogs.html", "changelogs", outputDir);
    }

    @Override
    public String getTargetExtension() {
        return ".html";
    }

}
