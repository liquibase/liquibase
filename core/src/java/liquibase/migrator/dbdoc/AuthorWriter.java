package liquibase.migrator.dbdoc;

import liquibase.migrator.change.Change;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class AuthorWriter extends HTMLWriter {

    public AuthorWriter(File rootOutputDir) {
        super(new File(rootOutputDir, "authors"));
    }

    protected String createTitle(Object object) {
        return object.toString() + " (Author)";
    }

    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes) throws IOException {
    }
}
