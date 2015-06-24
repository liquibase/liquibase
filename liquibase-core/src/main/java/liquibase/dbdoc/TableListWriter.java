package liquibase.dbdoc;

import java.io.File;

public class TableListWriter extends HTMLListWriter {

    public TableListWriter(File outputDir, String outputFileEncoding) {
        super("Current Tables", "currenttables.html", "tables", outputDir, outputFileEncoding);
    }


}
