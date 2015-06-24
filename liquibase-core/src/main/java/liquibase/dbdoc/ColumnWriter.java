package liquibase.dbdoc;

import liquibase.change.Change;
import liquibase.database.Database;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class ColumnWriter extends HTMLWriter {


    public ColumnWriter(File rootOutputDir, String outputFileEncoding, Database database) {
        super(new File(rootOutputDir, "columns"), outputFileEncoding, database);
    }

    @Override
    protected String createTitle(Object object) {
        return "Changes affecting column \""+object.toString() + "\"";
    }

    @Override
    protected void writeCustomHTML(Writer fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}
