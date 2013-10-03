package liquibase.dbdoc;

import java.io.File;

public class TableListWriter extends HTMLListWriter {

    public TableListWriter(File outputDir) {
        super("Current Tables", "currenttables.html", "tables", outputDir);
    }


}
