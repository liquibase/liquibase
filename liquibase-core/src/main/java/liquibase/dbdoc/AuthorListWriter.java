package liquibase.dbdoc;

import java.nio.file.Path;

public class AuthorListWriter extends HTMLListWriter {

    public AuthorListWriter(Path outputDir) {
        super("All Authors", "authors.html", "authors", outputDir);
    }

}
