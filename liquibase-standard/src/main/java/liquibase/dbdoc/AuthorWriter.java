package liquibase.dbdoc;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.resource.Resource;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class AuthorWriter extends HTMLWriter {

    public AuthorWriter(Resource rootOutputDir, Database database) {
        super(rootOutputDir.resolve("authors"), database);
    }

    @Override
    protected String createTitle(Object object) {
        return "Changes created by author "+object.toString();
    }

    @Override
    protected void writeCustomHTML(Writer fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}
