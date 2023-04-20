package liquibase.dbdoc;

import liquibase.resource.Resource;

public class AuthorListWriter extends HTMLListWriter {

    public AuthorListWriter(Resource outputDir) {
        super("All Authors", "authors.html", "authors", outputDir);
    }

}
