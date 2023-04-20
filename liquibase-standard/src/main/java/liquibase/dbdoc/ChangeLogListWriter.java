package liquibase.dbdoc;

import liquibase.resource.Resource;

public class ChangeLogListWriter extends HTMLListWriter {

    public ChangeLogListWriter(Resource outputDir) {
        super("All Change Logs", "changelogs.html", "changelogs", outputDir);
    }

    @Override
    public String getTargetExtension() {
        return ".html";
    }

}
