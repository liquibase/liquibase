package liquibase.dbdoc;

import liquibase.resource.Resource;

public class TableListWriter extends HTMLListWriter {

    public TableListWriter(Resource outputDir) {
        super("Current Tables", "currenttables.html", "tables", outputDir);
    }


}
