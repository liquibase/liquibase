package liquibase.migrator.dbdoc;

import java.io.File;

public class ChangeLogListWriter extends HTMLListWriter {

    public ChangeLogListWriter(File outputDir) {
        super("All Change Logs", "changelogs.html", "changelogs", outputDir);
    }

    public String getTargetExtension() {
        return ".xml";
    }

}
