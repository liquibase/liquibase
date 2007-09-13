package liquibase.dbdoc;

import liquibase.change.Change;
import liquibase.database.Database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ColumnWriter extends HTMLWriter {


    public ColumnWriter(File rootOutputDir) {
        super(new File(rootOutputDir, "columns"));
    }

    protected String createTitle(Object object) {
        return object.toString() + " (Column)";
    }

    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}
