package liquibase.dbdoc;

import java.io.File;

public class AuthorListWriter extends HTMLListWriter {

    public AuthorListWriter(File outputDir) {
        super("All Authors", "authors.html", "authors", outputDir);
    }

}
