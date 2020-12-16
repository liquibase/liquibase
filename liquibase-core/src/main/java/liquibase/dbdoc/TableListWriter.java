package liquibase.dbdoc;

import java.nio.file.Path;

public class TableListWriter extends HTMLListWriter {

    public TableListWriter(Path outputDir) {
        super("Current Tables", "currenttables.html", "tables", outputDir);
    }


}
